package com.elevate.consultingplatform.service.zoho;

import java.util.Map;

public interface ZohoWebhookService {
    boolean verifyToken(String token);
    Map<String, Object> processLeadStatusUpdate(Map<String, Object> payload);
}
