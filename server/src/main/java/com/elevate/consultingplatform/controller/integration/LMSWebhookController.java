package com.elevate.consultingplatform.controller.integration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/integrations/lms")
public class LMSWebhookController {

    // TODO: inject secret from properties
    private static final String WEBHOOK_SECRET = "changeme";

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestHeader(value = "X-Signature", required = false) String signature,
                                        @RequestBody Map<String, Object> body) {
        // Minimal mock verification
        if (signature == null || signature.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // In real: compute HMAC(body, secret) and compare to signature
        // For now accept any non-empty signature in mock profile

        // TODO: map event to domain updates
        // event: MODULE_COMPLETED, clientExternalId, moduleExternalId
        // update training assignment progress

        return ResponseEntity.ok().build();
    }
}
