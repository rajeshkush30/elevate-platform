package com.elevate.consultingplatform.controller.admin;

import com.elevate.consultingplatform.dto.assessment.AssignAssessmentRequest;
import com.elevate.consultingplatform.dto.assessment.CreateAssessmentRequest;
import com.elevate.consultingplatform.dto.assessment.UpdateAssessmentRequest;
import com.elevate.consultingplatform.entity.assessment.Assessment;
import com.elevate.consultingplatform.service.assessment.AssessmentAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/assessments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAssessmentController {

    private final AssessmentAdminService assessmentAdminService;

    @PostMapping
    public ResponseEntity<Assessment> create(@RequestBody CreateAssessmentRequest req,
                                             @RequestParam(value = "questionnaireId", required = false) Long questionnaireId) {
        Assessment created = assessmentAdminService.createAssessment(req.getStageId(), req.getName(), req.getDescription(), questionnaireId);
        return ResponseEntity.created(URI.create("/api/v1/admin/assessments/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Assessment>> listByStage(@RequestParam("stageId") Long stageId) {
        return ResponseEntity.ok(assessmentAdminService.listByStage(stageId));
    }

    @PostMapping("/{assessmentId}/assign")
    public ResponseEntity<Void> assign(@PathVariable Long assessmentId, @RequestBody AssignAssessmentRequest req) {
        assessmentAdminService.assignToClients(assessmentId, req.getClientIds(), req.getDueDate());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{assessmentId}")
    public ResponseEntity<Assessment> update(@PathVariable Long assessmentId, @RequestBody UpdateAssessmentRequest req,
                                             @RequestParam(value = "questionnaireId", required = false) Long questionnaireId) {
        Assessment updated = assessmentAdminService.updateAssessment(assessmentId, req.getName(), req.getDescription(), questionnaireId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{assessmentId}")
    public ResponseEntity<Void> delete(@PathVariable Long assessmentId) {
        assessmentAdminService.deleteAssessment(assessmentId);
        return ResponseEntity.noContent().build();
    }
}
