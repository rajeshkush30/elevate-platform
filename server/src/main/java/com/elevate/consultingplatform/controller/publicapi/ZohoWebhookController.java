package com.elevate.consultingplatform.controller.publicapi;

import com.elevate.consultingplatform.service.zoho.ZohoWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/public/zoho/webhook")
@RequiredArgsConstructor
public class ZohoWebhookController {

    private final ZohoWebhookService zohoWebhookService;

    /**
     * This endpoint is called by Zoho when a lead is updated.
     * Example payload:
     * {
     *   "id": "123456789",
     *   "Email": "john@example.com",
     *   "First_Name": "John",
     *   "Last_Name": "Doe",
     *   "Status": "Approved"
     * }
     */
    @PostMapping("/status-update")
    public ResponseEntity<?> handleLeadStatus(@RequestBody Map<String, Object> payload,
                                              @RequestHeader(value = "x-zoho-webhook-token", required = false) String token) {
        log.info("Received webhook from Zoho: {}", payload);

        try {
            //  (Optional) Verify webhook token for security
            if (!zohoWebhookService.verifyToken(token)) {
                log.warn("Invalid webhook token: {}", token);
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized webhook source"));
            }

            Map<String, Object> response = zohoWebhookService.processLeadStatusUpdate(payload);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Webhook processing error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
