package com.elevate.consultingplatform.integration.live;

    import com.elevate.consultingplatform.integration.CRMClient;
    import org.springframework.boot.web.client.RestTemplateBuilder;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.context.annotation.Profile;
    import org.springframework.http.HttpEntity;
    import org.springframework.http.HttpHeaders;
    import org.springframework.http.MediaType;
    import org.springframework.stereotype.Service;
    import org.springframework.web.client.RestClientException;
    import org.springframework.web.client.RestTemplate;
@Service
@Profile("live")
public class LiveCRMClient implements CRMClient {

    private final RestTemplate restTemplate;

    public LiveCRMClient(@Value("${crm.zoho.baseUrl}") String baseUrl,
                         @Value("${crm.zoho.accessToken}") String accessToken,
                         RestTemplateBuilder builder) {
        this.restTemplate = builder
                .rootUri(baseUrl)
                .additionalInterceptors((request, body, execution) -> {
                    request.getHeaders().add("Authorization", "Zoho-oauthtoken " + accessToken);
                    return execution.execute(request, body);
                })
                .build();
    }

    private <T> T post(String path, Object body, Class<T> cls) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);
            return restTemplate.postForObject(path, entity, cls);
        } catch (RestClientException ex) {
            throw new RuntimeException("CRM request failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public CreateLeadResponse createLead(CreateLeadRequest req) {
        return post("/crm/leads", req, CreateLeadResponse.class);
    }

    @Override
    public ConvertLeadResponse convertLead(ConvertLeadRequest req) {
        return post("/crm/convert", req, ConvertLeadResponse.class);
    }

    @Override
    public void addNote(AddNoteRequest req) {
        post("/crm/notes", req, Void.class);
    }
}
