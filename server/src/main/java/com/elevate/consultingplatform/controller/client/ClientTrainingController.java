package com.elevate.consultingplatform.controller.client;

import com.elevate.consultingplatform.dto.catalog.ModuleTreeResponse;
import com.elevate.consultingplatform.dto.training.StageCompleteRequest;
import com.elevate.consultingplatform.dto.training.StageStartResponse;
import com.elevate.consultingplatform.service.training.ProgressService;
import com.elevate.consultingplatform.service.billing.TrainingEntitlementService;
import com.elevate.consultingplatform.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/client/training", "/api/v1/client/training"})
@RequiredArgsConstructor
@Tag(name = "Client - Training", description = "Client training assignments and progress")
public class ClientTrainingController {

    private final ProgressService progressService;
    private final TrainingEntitlementService entitlementService;
    private final UserRepository userRepository;

    @GetMapping("/assigned")
    @Operation(summary = "Get assigned training tree for current user")
    public ResponseEntity<List<ModuleTreeResponse>> getAssigned() {
        return ResponseEntity.ok(progressService.getAssignedTreeForCurrentUser());
    }

    @PostMapping("/stage/{stageId}/start")
    @Operation(summary = "Start a stage and return optional LMS launch URL")
    public ResponseEntity<StageStartResponse> startStage(@PathVariable Long stageId) {
        // Resolve a simple scope key from stageId (for MVP we use stageId as string)
        String scopeKey = String.valueOf(stageId);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        var user = userRepository.findByEmail(email).orElseThrow();

        var entitlement = entitlementService.findActive(user, scopeKey);
        if (entitlement.isEmpty()) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).build();
        }

        StageStartResponse res = progressService.startStage(stageId);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/stage/{stageId}/complete")
    @Operation(summary = "Complete a stage for current user")
    public ResponseEntity<Void> completeStage(@PathVariable Long stageId,
                                              @RequestBody StageCompleteRequest req) {
        progressService.completeStage(stageId, req);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/status")
    @Operation(summary = "Get compact training status for dashboard")
    public ResponseEntity<Map<String, Object>> getStatus() {
        // Minimal derivation from assigned tree for now (status not tracked per module here)
        List<ModuleTreeResponse> tree = progressService.getAssignedTreeForCurrentUser();
        List<Map<String, Object>> modules = new java.util.ArrayList<>();
        for (ModuleTreeResponse m : tree) {
            for (ModuleTreeResponse.SegmentNode seg : m.getSegments()) {
                for (ModuleTreeResponse.StageNode st : seg.getStages()) {
                    modules.add(Map.of(
                            "name", st.getName(),
                            "externalId", st.getLmsCourseId(),
                            "status", "ASSIGNED",
                            "completion", 0
                    ));
                }
            }
        }
        return ResponseEntity.ok(Map.of("modules", modules));
    }
}
