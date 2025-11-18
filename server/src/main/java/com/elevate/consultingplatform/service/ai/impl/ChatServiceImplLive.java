package com.elevate.consultingplatform.service.ai.impl;

import com.elevate.consultingplatform.service.ai.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@Profile("live")
@RequiredArgsConstructor
public class ChatServiceImplLive implements ChatService {

    private final WebClient.Builder builder;

    @Value("${openai.apiKey:${openai.api.key:}}")
    private String apiKey;

    @Value("${openai.model:gpt-5.1}")
    private String model;

    @Override
    public String reply(String message) {
        if (message == null || message.isBlank()) {
            return "Hello! I'm your assistant. Ask me anything.";
        }
        WebClient client = builder
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + (apiKey == null ? "" : apiKey.trim()))
                .build();

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful assistant for a consulting platform."),
                        Map.of("role", "user", "content", message)
                )
        );

        try {
            var resp = client.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(body))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (resp == null) return "Sorry, I couldn't get a response right now.";
            Object choices = resp.get("choices");
            if (choices instanceof List<?> list && !list.isEmpty()) {
                Object first = list.get(0);
                if (first instanceof Map<?,?> m) {
                    Object msg = ((Map<?,?>) m.get("message"));
                    if (msg instanceof Map<?,?> mm) {
                        Object content = mm.get("content");
                        if (content != null) return content.toString();
                    }
                }
            }
            return "Sorry, I couldn't get a response right now.";
        } catch (Exception e) {
            return "[AI Error] " + e.getMessage();
        }
    }
}
