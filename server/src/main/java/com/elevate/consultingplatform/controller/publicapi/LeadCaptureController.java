package com.elevate.consultingplatform.controller.publicapi;

import com.elevate.consultingplatform.integration.AIClient;
import com.elevate.consultingplatform.integration.CRMClient;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Lead Capture", description = "Capture leads from chat and free text")
public class LeadCaptureController {

    private final AIClient aiClient;
    private final CRMClient crmClient;
    @Value("${app.client.base-url:http://localhost:3000}")
    private String clientBaseUrl;

    public record LeadPayload(
            String name,
            String email,
            String company,
            Map<String, Object> profile,
            List<String> intents,
            String notes
    ) {}

    public record LeadResponse(
            String leadId,
            Double preScore,
            String stageHint,
            String rationale,
            List<String> labels
    ) {}

    @PostMapping("/chat/intents")
    @PermitAll
    @Operation(summary = "Capture lead with structured payload", description = "Pre-scores via AI and creates/updates lead in CRM")
    public ResponseEntity<LeadResponse> capture(@RequestBody LeadPayload payload) {
        // 1) AI pre-score
        var pre = aiClient.preScore(new AIClient.PreScoreRequest(
                null,
                payload.profile(),
                payload.intents(),
                payload.notes()
        ));

        // 2) Create or update lead in CRM
        var lead = crmClient.createLead(new CRMClient.CreateLeadRequest(
                payload.name(),
                payload.email(),
                payload.company(),
                "Chatbot",
                pre.preScore(),
                pre.stageHint()
        ));

        // Invite will be handled by Zoho (approval + invite email). No invite sent from backend.

        return ResponseEntity.ok(new LeadResponse(
                lead.leadId(),
                pre.preScore(),
                pre.stageHint(),
                pre.rationale(),
                pre.labels()
        ));
    }

    // Create lead from free-form text (no external extraction)
    public record LeadFromTextRequest(String text, String name, String email, String company) {}

    @PostMapping("/chat/lead-from-text")
    @PermitAll
    @SuppressWarnings("unused")
    @Operation(summary = "Create lead from free-form text", description = "Validates email and pre-scores inquiry; no external extraction")
    public ResponseEntity<LeadResponse> leadFromText(@RequestBody LeadFromTextRequest req) {
        try {
            String text = req.text();
            String name = req.name();
            String email = req.email();
            String company = req.company();

            if (!isValidEmail(email)) {
                return ResponseEntity.badRequest().build();
            }

            var pre = aiClient.preScore(new AIClient.PreScoreRequest(
                    null,
                    Map.of("source", "website-chatbot"),
                    null,
                    text
            ));

            var lead = crmClient.createLead(new CRMClient.CreateLeadRequest(
                    name != null && !name.isBlank() ? name : email,
                    email,
                    company,
                    "Chatbot",
                    pre.preScore(),
                    pre.stageHint()
            ));

            return ResponseEntity.ok(new LeadResponse(
                    lead.leadId(),
                    pre.preScore(),
                    pre.stageHint(),
                    pre.rationale(),
                    pre.labels()
            ));
        } catch (Exception e) {
            log.warn("lead-from-text failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.contains("@");
    }
}
