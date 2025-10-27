package com.elevate.consultingplatform.integration.live;

import com.elevate.consultingplatform.integration.LMSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Profile("live")
public class LiveLMSClient implements LMSClient {

    private final WebClient client;

    public LiveLMSClient(@Value("${lms.baseUrl}") String baseUrl,
                         @Value("${lms.apiKey}") String apiKey,
                         WebClient.Builder builder) {
        this.client = builder.baseUrl(baseUrl)
                .defaultHeader("X-API-Key", apiKey)
                .build();
    }

    private <T> T post(String path, Object body, Class<T> cls) {
        return client.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(cls)
                .onErrorResume(err -> Mono.error(new RuntimeException("LMS request failed: " + err.getMessage(), err)))
                .block();
    }

    private <T> T get(String path, Class<T> cls) {
        return client.get()
                .uri(path)
                .retrieve()
                .bodyToMono(cls)
                .onErrorResume(err -> Mono.error(new RuntimeException("LMS request failed: " + err.getMessage(), err)))
                .block();
    }

    @Override
    public AssignResponse assignModules(AssignRequest req) {
        return post("/lms/assign", req, AssignResponse.class);
    }

    @Override
    public StatusResponse getStatus(Long clientId) {
        return get("/lms/status?clientId=" + clientId, StatusResponse.class);
    }
}
