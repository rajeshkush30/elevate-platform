package com.elevate.consultingplatform.controller.publicapi;

import com.elevate.consultingplatform.dto.CreateLeadRequest;
import com.elevate.consultingplatform.service.zoho.ZohoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public/zoho")
@RequiredArgsConstructor
@Tag(name = "Lead creation", description = "Public endpoints for creating leads")
public class NewZohoController {

    private final ZohoService zohoService;

    @PostMapping("/lead")

    @PermitAll
    @Operation(summary = "Handle Zoho lead cration", description = "Processes Zoho lead  for  admin ")
    public ResponseEntity<?> createLead(@RequestBody CreateLeadRequest request) {
        try {
            Map<String, Object> leadData = new HashMap<>();
            leadData.put("First_Name", request.getFirstName());
            leadData.put("Last_Name", request.getLastName());
            leadData.put("Email", request.getEmail());
            leadData.put("Phone", request.getPhone());
            leadData.put("Company", request.getCompany());
            leadData.put("Lead_Source", request.getSource());
            leadData.put("Ainotse_Score", request.getAinotseScore());
            leadData.put("Ainotse_summary", request.getAinotseSummary());
            leadData.put("Lead_Status", request.getStatus() == null ? "Pending" : request.getStatus());

            String leadId = zohoService.createLead(leadData);

            if (leadId != null) {
                return ResponseEntity.ok(Map.of(
                        "message", "Lead created successfully",
                        "leadId", leadId
                ));
            } else {
                return ResponseEntity.status(400).body(Map.of(
                        "message", "Failed to create lead"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "message", "Error creating lead",
                    "error", e.getMessage()
            ));
        }
    }
}
