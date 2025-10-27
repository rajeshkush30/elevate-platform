package com.elevate.consultingplatform.controller.publicapi;

import com.elevate.consultingplatform.service.activation.ActivationLinkService;
import jakarta.annotation.security.PermitAll;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/public/zoho")
@RequiredArgsConstructor
@Tag(name = "Zoho Activation", description = "Public endpoints to generate activation links for Zoho contacts")
public class ZohoActivationController {

    private final ActivationLinkService activationLinkService;

    @PostMapping("/activation-link")
    @PermitAll
    @Operation(summary = "Generate activation link", description = "Returns an activation URL for the provided email and contact ID")
    public ResponseEntity<ActivationLinkResponse> activationLink(@RequestBody ActivationLinkRequest req) {
        String url = activationLinkService.buildActivationLink(req.getEmail(), req.getContactId());
        return ResponseEntity.ok(new ActivationLinkResponse(url));
    }

    @Data
    public static class ActivationLinkRequest {
        private String email;
        private String contactId;
    }

    @Data
    public static class ActivationLinkResponse {
        private final String activationUrl;
    }
}
