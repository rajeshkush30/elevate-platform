package com.elevate.consultingplatform.integration.live;

import com.elevate.consultingplatform.config.ZohoCrmProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZohoOAuthService {

    private final ZohoCrmProperties props;
    private final RestTemplateBuilder builder;
    private final ObjectMapper mapper = new ObjectMapper();

    private volatile String cachedToken;
    private volatile Instant expiresAt = Instant.EPOCH;
    private final ReentrantLock lock = new ReentrantLock();

    private RestTemplate restTemplate() {
        return builder
                .setConnectTimeout(Duration.ofMillis(props.getHttpConnectTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(props.getHttpReadTimeoutMs()))
                .build();
    }

    public String getAccessToken() {
        if (cachedToken != null && Instant.now().isBefore(expiresAt.minusSeconds(60))) {
            return cachedToken;
        }
        refreshToken(false);
        return cachedToken;
    }

    public void forceRefresh() {
        refreshToken(true);
    }

    private void refreshToken(boolean force) {
        if (!force && cachedToken != null && Instant.now().isBefore(expiresAt.minusSeconds(60))) return;
        lock.lock();
        try {
            if (!force && cachedToken != null && Instant.now().isBefore(expiresAt.minusSeconds(60))) return;
            String accounts = props.getAccountsBaseUrl();
            String url = accounts + "/oauth/v2/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "refresh_token");
            form.add("client_id", props.getClientId());
            form.add("client_secret", props.getClientSecret());
            form.add("refresh_token", props.getRefreshToken());

            ResponseEntity<String> resp = restTemplate().postForEntity(url, new HttpEntity<>(form, headers), String.class);
            JsonNode node = mapper.readTree(resp.getBody());
            String token = node.path("access_token").asText(null);
            int expiresIn = node.path("expires_in").asInt(3600);
            if (token == null) {
                throw new IllegalStateException("Zoho OAuth refresh failed: " + resp.getBody());
            }
            cachedToken = token;
            expiresAt = Instant.now().plusSeconds(expiresIn);
            log.info("Zoho access token refreshed, expires in {}s", expiresIn);
        } catch (RuntimeException e) {
            log.error("Zoho OAuth refresh error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Zoho OAuth parse error: {}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}
