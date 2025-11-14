package com.elevate.consultingplatform.service.zoho.impl;

import com.elevate.consultingplatform.config.ZohoConfig;
import com.elevate.consultingplatform.integration.live.ZohoOAuthService;
import com.elevate.consultingplatform.service.zoho.ZohoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZohoServiceImpl implements ZohoService {

    private final ZohoOAuthService zohoOAuthService;
    private final ZohoConfig zohoConfig;

    @Override
    public String createLead(Map<String, Object> leadData) {
        String url = zohoConfig.getApiUrl() + "/crm/v2/Leads";

        // ✅ Get token from ZohoOAuthService
        String accessToken = zohoOAuthService.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Zoho-oauthtoken " + accessToken);

        Map<String, Object> payload = Map.of("data", List.of(leadData));
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            log.info("Sending CRM request: {}", payload);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            log.info("CRM Response: {}", response.getBody());


            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.warn("Access token expired, refreshing...");
                zohoOAuthService.forceRefresh();
                return createLead(leadData);
            }

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseData = (Map<String, Object>) ((List<?>) response.getBody().get("data")).get(0);
                Map<String, Object> details = (Map<String, Object>) responseData.get("details");
                return String.valueOf(details.get("id"));
            }

            log.error("Lead creation failed: {}", response.getBody());
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("401 Unauthorized – refreshing and retrying");
            zohoOAuthService.forceRefresh();
            return createLead(leadData);
        } catch (Exception e) {
            log.error("Error creating lead: {}", e.getMessage(), e);
        }
        return null;
    }
}
