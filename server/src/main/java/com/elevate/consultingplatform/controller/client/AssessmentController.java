package com.elevate.consultingplatform.controller.client;

import com.elevate.consultingplatform.dto.assessment.AssessmentResultResponse;
import com.elevate.consultingplatform.dto.assessment.CreateAttemptRequest;
import com.elevate.consultingplatform.dto.assessment.SubmitAnswersRequest;
import com.elevate.consultingplatform.service.assessment.AssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client/assessment")
@RequiredArgsConstructor
@Tag(name = "Client - Assessment", description = "Client assessment attempts and scoring")
public class AssessmentController {

    private final AssessmentService assessmentService;

    @PostMapping("/attempts")
    @Operation(summary = "Create assessment attempt")
    public ResponseEntity<Long> createAttempt(@RequestBody CreateAttemptRequest req) {
        Long id = assessmentService.createAttempt(req);
        return ResponseEntity.ok(id);
    }

    @PostMapping("/attempts/{attemptId}/answers")
    @Operation(summary = "Submit or update answers for attempt")
    public ResponseEntity<Void> submitAnswers(@PathVariable Long attemptId,
                                              @RequestBody SubmitAnswersRequest req) {
        assessmentService.submitAnswers(attemptId, req);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/attempts/{attemptId}/finalize")
    @Operation(summary = "Finalize attempt and get result")
    public ResponseEntity<AssessmentResultResponse> finalizeAttempt(@PathVariable Long attemptId) {
        var result = assessmentService.finalizeAttempt(attemptId);
        return ResponseEntity.ok(result);
    }
}
