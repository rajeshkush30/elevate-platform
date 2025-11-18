package com.elevate.consultingplatform.integration.live;

import com.elevate.consultingplatform.integration.AIClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Profile("live")
@Slf4j
public class LiveAIClient implements AIClient {

    private final WebClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${openai.model:gpt-5.1}")
    private String model;

    public LiveAIClient(@Value("${openai.apiKey:${openai.api.key:}}") String apiKey,
                        WebClient.Builder builder) {
        System.setProperty("io.netty.resolver.dns.useJdkResolver", "true");

        HttpClient http = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(60));

        String proxyEnv = System.getenv("HTTPS_PROXY");
        if (proxyEnv == null || proxyEnv.isBlank()) {
            proxyEnv = System.getenv("HTTP_PROXY");
        }
        if (proxyEnv != null && !proxyEnv.isBlank()) {
            try {
                URI p = URI.create(proxyEnv.trim());
                int port = p.getPort() == -1 ? ("https".equalsIgnoreCase(p.getScheme()) ? 443 : 80) : p.getPort();
                HttpClient finalHttp = http;
                http = http.proxy(spec -> {
                    ProxyProvider.Builder b = spec.type(ProxyProvider.Proxy.HTTP)
                            .host(p.getHost())
                            .port(port);
                    String ui = p.getUserInfo();
                    if (ui != null && !ui.isBlank()) {
                        String[] parts = ui.split(":", 2);
                        String u = parts[0];
                        String pw = parts.length > 1 ? parts[1] : "";
                        b.username(u).password(s -> pw);
                    }
                });
            } catch (Exception ignored) { }
        }

        this.client = builder
                .clientConnector(new ReactorClientHttpConnector(http))
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + (apiKey == null ? "" : apiKey.trim()))
                .build();
    }

    private JsonNode chatJson(List<? extends Map<String, ?>> messages) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        // Encourage JSON output
        body.put("response_format", Map.of("type", "json_object"));

        String resp = client.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(err -> Mono.error(new RuntimeException("OpenAI request failed: " + err.getMessage(), err)))
                .block();

        try {
            JsonNode root = mapper.readTree(resp);
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            if (content == null || content.isBlank()) {
                throw new RuntimeException("OpenAI returned empty content: " + resp);
            }
            return mapper.readTree(content);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenAI response: " + e.getMessage(), e);
        }
    }

    @Override
    public PreScoreResponse preScore(PreScoreRequest req) {
        String system = "You are an assistant that classifies inbound business inquiries for a consulting platform. " +
                "Output strict JSON with keys: preScore (0-100), stageHint (one of: StartUp, Grow, Scale, Endurance, Evolution), " +
                "rationale (short), labels (array of tags).";
        Map<String, Object> payload = new HashMap<>();
        payload.put("profile", req.profile());
        payload.put("intents", req.intents());
        payload.put("notes", req.notes());

        var messages = List.of(
                Map.of("role", "system", "content", system),
                Map.of("role", "user", "content", safeJson(payload))
        );

        JsonNode json = chatJson(messages);
        double pre = json.path("preScore").asDouble(0.0);
        String stage = json.path("stageHint").asText("StartUp");
        String rationale = json.path("rationale").asText("");
        List<String> labels = json.path("labels").isArray() ?
                json.findValuesAsText("labels") : List.of();
        return new PreScoreResponse(pre, stage, rationale, labels);
    }

    @Override
    public StageResponse determineStage(StageRequest req) {
        String system = "You are a scoring engine. Given questionnaire answers and boundary rules, " +
                "return JSON: { stage: 'StartUp|Grow|Scale|Endurance|Evolution', score: number, rationale: string, confidence: 0-1 }.";
        Map<String, Object> payload = new HashMap<>();
        payload.put("questionnaireId", req.questionnaireId());
        payload.put("answers", req.answers());
        payload.put("ruleContext", req.ruleContext());

        var messages = List.of(
                Map.of("role", "system", "content", system),
                Map.of("role", "user", "content", safeJson(payload))
        );
        try {
            JsonNode json = chatJson(messages);
            String stage = json.path("stage").asText("StartUp");
            double score = json.path("score").asDouble(0.0);
            String rationale = json.path("rationale").asText("");
            double confidence = json.path("confidence").asDouble(0.7);
            return new StageResponse(stage, score, rationale, confidence);
        } catch (Exception e) {
            return new StageResponse("Grow", 0.0, "AI unavailable - fallback applied", 0.0);
        }
    }

    @Override
    public SummaryResponse generateSummary(SummaryRequest req) {
        String system = "You are an expert consultant. Create a concise stage summary and 3-7 actionable recommendations. " +
                "Return JSON: { summary: string, recommendations: string[] }.";
        Map<String, Object> payload = new HashMap<>();
        payload.put("stage", req.stage());
        payload.put("score", req.score());
        payload.put("answers", req.answers());

        var messages = List.of(
                Map.of("role", "system", "content", system),
                Map.of("role", "user", "content", safeJson(payload))
        );
        try {
            JsonNode json = chatJson(messages);
            String summary = json.path("summary").asText("");
            List<String> recs = json.path("recommendations").isArray() ? json.findValuesAsText("recommendations") : List.of();
            return new SummaryResponse(summary, recs);
        } catch (Exception e) {
            return new SummaryResponse("AI temporarily unavailable. We will generate your consultation summary shortly.", List.of());
        }
    }

    @Override
    public FinalConsultationResponse finalConsultation(FinalConsultationRequest req) {
        String system = "You are a strategy consultant. Produce a structured final consultation draft based on stage, " +
                "strategy responses, and history. Return JSON: { draft: string, sections: [{title:string, content:string}] }.";
        Map<String, Object> payload = new HashMap<>();
        payload.put("clientId", req.clientId());
        payload.put("stage", req.stage());
        payload.put("strategyResponses", req.strategyResponses());
        payload.put("history", req.history());

        var messages = List.of(
                Map.of("role", "system", "content", system),
                Map.of("role", "user", "content", safeJson(payload))
        );

        JsonNode json = chatJson(messages);
        String draft = json.path("draft").asText("");
        // pass-through sections as map list
        List<Map<String, Object>> sections;
        try {
            sections = mapper.convertValue(json.path("sections"), mapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        } catch (IllegalArgumentException e) {
            sections = List.of();
        }
        return new FinalConsultationResponse(draft, sections);
    }

    private String safeJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }
}
