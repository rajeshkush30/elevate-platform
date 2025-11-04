package com.elevate.consultingplatform.integration.live;

import com.elevate.consultingplatform.integration.CRMClient;
import com.elevate.consultingplatform.config.ZohoCrmProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Profile("live")
@RequiredArgsConstructor
public class LiveCRMClient implements CRMClient {

    private final ZohoCrmProperties props;
    private final ZohoOAuthService oauth;
    private final RestTemplateBuilder builder;
    private final ObjectMapper mapper = new ObjectMapper();

    private RestTemplate restTemplate() {
        return builder
                .rootUri(props.getBaseUrl())
                .setConnectTimeout(java.time.Duration.ofMillis(props.getHttpConnectTimeoutMs()))
                .setReadTimeout(java.time.Duration.ofMillis(props.getHttpReadTimeoutMs()))
                .build();
    }

    private JsonNode postJson(String path, Object body) {
        RestTemplate rt = restTemplate();
        String token = oauth.getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.AUTHORIZATION, "Zoho-oauthtoken " + token);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> resp = rt.postForEntity(path, entity, String.class);
            return mapper.readTree(resp.getBody() == null ? "{}" : resp.getBody());
        } catch (RestClientResponseException ex) {
            if (ex.getRawStatusCode() == 401) {
                // force refresh and retry once
                oauth.forceRefresh();
                headers.set(HttpHeaders.AUTHORIZATION, "Zoho-oauthtoken " + oauth.getAccessToken());
                ResponseEntity<String> resp = rt.postForEntity(path, new HttpEntity<>(body, headers), String.class);
                try {
                    return mapper.readTree(resp.getBody() == null ? "{}" : resp.getBody());
                } catch (Exception e) {
                    throw new RuntimeException("CRM parse failed: " + e.getMessage(), e);
                }
            }
            String msg = ex.getResponseBodyAsString(StandardCharsets.UTF_8);
            throw new RuntimeException("CRM request failed: " + ex.getRawStatusCode() + " " + msg, ex);
        } catch (Exception e) {
            throw new RuntimeException("CRM request failed: " + e.getMessage(), e);
        }
    }

    @Override
    public CreateLeadResponse createLead(CreateLeadRequest req) {
        // Zoho CRM v2 Leads: POST /crm/v2/Leads with { data: [ { ... } ] }
        Map<String, Object> lead = new HashMap<>();
        // Zoho requires Last_Name field
        lead.put("Last_Name", req.name());
        if (req.company() != null) lead.put("Company", req.company());
        if (req.email() != null) lead.put("Email", req.email());
        if (req.source() != null) lead.put("Lead_Source", req.source());
        String desc = "";
        if (req.preScore() != null) desc += "PreScore: " + req.preScore() + "\n";
        if (req.stageHint() != null) desc += "StageHint: " + req.stageHint();
        if (!desc.isBlank()) lead.put("Description", desc);
        Map<String, Object> body = Map.of("data", List.of(lead));
        JsonNode res = postJson("/crm/v2/Leads", body);
        JsonNode data = res.path("data").path(0).path("details").path("id");
        String id = data.isTextual() ? data.asText() : null;
        return new CreateLeadResponse(id);
    }

    @Override
    public ConvertLeadResponse convertLead(ConvertLeadRequest req) {
        // Zoho convert: POST /crm/v2/Leads/{id}/actions/convert
        JsonNode res = postJson("/crm/v2/Leads/" + req.leadId() + "/actions/convert", Map.of());
        // Response: data[0].contacts
        String contactId = res.path("data").path(0).path("contacts").asText(null);
        return new ConvertLeadResponse(contactId);
    }

    @Override
    public void addNote(AddNoteRequest req) {
        // Notes: POST /crm/v2/Notes with Parent_Id + se_module
        Map<String, Object> note = new HashMap<>();
        note.put("Note_Title", req.title());
        note.put("Note_Content", req.content());
        note.put("Parent_Id", req.contactId());
        note.put("se_module", "Contacts");
        Map<String, Object> body = Map.of("data", List.of(note));
        postJson("/crm/v2/Notes", body);
    }
}
