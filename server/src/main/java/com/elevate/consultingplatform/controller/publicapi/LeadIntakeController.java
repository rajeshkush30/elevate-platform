package com.elevate.consultingplatform.controller.publicapi;

import jakarta.annotation.security.PermitAll;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import com.elevate.consultingplatform.integration.AIClient;

@RestController
@RequestMapping("/api/public/chat")
@RequiredArgsConstructor
public class LeadIntakeController {

    private final AIClient aiClient;

    @PostMapping("/intents")
    @PermitAll
    public ResponseEntity<LeadIntakeResponse> leadIntake(@RequestBody LeadIntakeRequest req) {
        // Try live pre-scoring via AI; fallback to heuristic
        try {
            var pre = aiClient.preScore(new AIClient.PreScoreRequest(
                    UUID.randomUUID().toString(),
                    Optional.ofNullable(req.getProfile()).orElseGet(HashMap::new),
                    Optional.ofNullable(req.getIntents()).orElseGet(ArrayList::new),
                    req.getNotes()
            ));
            LeadIntakeResponse resp = new LeadIntakeResponse();
            resp.setLeadId(UUID.randomUUID().toString());
            resp.setPreScore(pre.preScore());
            resp.setStageHint(pre.stageHint());
            resp.setRationale(pre.rationale());
            resp.setLabels(pre.labels());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            // Heuristic fallback
            List<String> labels = new ArrayList<>();
            if (req.getIntents() != null) {
                for (String it : req.getIntents()) {
                    if (it == null) continue;
                    String t = it.trim().toLowerCase(Locale.ROOT);
                    if (t.contains("fund") || t.contains("capital")) labels.add("Capital");
                    if (t.contains("sale") || t.contains("pipeline")) labels.add("Sales");
                    if (t.contains("process") || t.contains("ops") || t.contains("operation")) labels.add("Operations");
                }
            }
            if (labels.isEmpty()) labels = List.of("General");
            String stage = "EARLY";
            if (labels.contains("Sales")) stage = "GROWTH";
            if (labels.contains("Operations")) stage = "MATURE";
            LeadIntakeResponse resp = new LeadIntakeResponse();
            resp.setLeadId(UUID.randomUUID().toString());
            resp.setPreScore(50.0);
            resp.setStageHint(stage);
            resp.setRationale("Heuristic classification based on provided intents and notes.");
            resp.setLabels(labels);
            return ResponseEntity.ok(resp);
        }
    }

    @Data
    public static class LeadIntakeRequest {
        private String name;
        private String email;
        private String company;
        private Map<String, Object> profile;
        private List<String> intents;
        private String notes;
    }

    @Data
    public static class LeadIntakeResponse {
        private String leadId;
        private Double preScore;
        private String stageHint;
        private String rationale;
        private List<String> labels;
    }
}
