package com.elevate.consultingplatform.controller.client;

import com.elevate.consultingplatform.dto.catalog.ModuleTreeResponse;
import com.elevate.consultingplatform.dto.training.StageCompleteRequest;
import com.elevate.consultingplatform.dto.training.StageStartResponse;
import com.elevate.consultingplatform.service.training.ProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client/training")
@RequiredArgsConstructor
@Tag(name = "Client - Training", description = "Client training assignments and progress")
public class ClientTrainingController {

    private final ProgressService progressService;

    @GetMapping("/assigned")
    @Operation(summary = "Get assigned training tree for current user")
    public ResponseEntity<List<ModuleTreeResponse>> getAssigned() {
        return ResponseEntity.ok(progressService.getAssignedTreeForCurrentUser());
    }

    @PostMapping("/stage/{stageId}/start")
    @Operation(summary = "Start a stage and return optional LMS launch URL")
    public ResponseEntity<StageStartResponse> startStage(@PathVariable Long stageId) {
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
}
