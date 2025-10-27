package com.elevate.consultingplatform.integration.live;

import com.elevate.consultingplatform.integration.AIClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Profile("live")
public class LiveAIClient implements AIClient {

    private final WebClient client;

    public LiveAIClient(@Value("${ai.baseUrl}") String baseUrl,
                        WebClient.Builder builder) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    private <T> T post(String path, Object body, Class<T> cls) {
        return client.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(cls)
                .onErrorResume(err -> Mono.error(new RuntimeException("AI request failed: " + err.getMessage(), err)))
                .block();
    }

    @Override
    public PreScoreResponse preScore(PreScoreRequest req) {
        return post("/ai/pre-score", req, PreScoreResponse.class);
    }

    @Override
    public StageResponse determineStage(StageRequest req) {
        return post("/ai/stage", req, StageResponse.class);
    }

    @Override
    public SummaryResponse generateSummary(SummaryRequest req) {
        return post("/ai/summary", req, SummaryResponse.class);
    }

    @Override
    public FinalConsultationResponse finalConsultation(FinalConsultationRequest req) {
        return post("/ai/final-consultation", req, FinalConsultationResponse.class);
    }
}
