package com.elevate.consultingplatform.integration.gemini;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "gemini.api.url", matchIfMissing = false)
public class GeminiClient {

    @Value("${gemini.api.url:}")
    private String apiUrl;

    @Value("${gemini.api.key:}")
    private String apiKey;

    private WebClient buildClient() {
        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Mono<String> generate(String userMessage) {
        var request = new GenerateContentRequest(
                List.of(new Content("user", List.of(new Part(userMessage))))
        );

        String urlWithKey = apiUrl + (apiUrl.contains("?") ? "&" : "?") + "key=" + apiKey;
        return buildClient()
                .post()
                .uri(urlWithKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GenerateContentResponse.class)
                .map(resp -> {
                    try {
                        if (resp == null || resp.candidates == null || resp.candidates.isEmpty()) return "";
                        var first = resp.candidates.get(0);
                        if (first == null || first.content == null || first.content.parts == null || first.content.parts.isEmpty()) return "";
                        var part = first.content.parts.get(0);
                        return part != null && part.text != null ? part.text : "";
                    } catch (Exception e) {
                        log.warn("Failed to parse Gemini response", e);
                        return "";
                    }
                });
    }

    // DTOs aligned with Gemini generateContent API
    @Data
    static class GenerateContentRequest {
        private final List<Content> contents;
    }

    @Data
    static class Content {
        private final String role;
        private final List<Part> parts;
    }

    @Data
    static class Part {
        private final String text;
    }

    @Data
    static class GenerateContentResponse {
        private List<Candidate> candidates;
    }

    @Data
    static class Candidate {
        private Content content;
        private String finishReason;
    }
}
