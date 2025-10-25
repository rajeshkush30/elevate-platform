package com.elevate.consultingplatform.controller.admin;

import com.elevate.consultingplatform.dto.assessment.CreateStageRuleRequest;
import com.elevate.consultingplatform.dto.assessment.UpdateStageRuleRequest;
import com.elevate.consultingplatform.entity.assessment.Questionnaire;
import com.elevate.consultingplatform.entity.assessment.StageRule;
import com.elevate.consultingplatform.entity.catalog.Stage;
import com.elevate.consultingplatform.exception.ResourceNotFoundException;
import com.elevate.consultingplatform.repository.assessment.QuestionnaireRepository;
import com.elevate.consultingplatform.repository.assessment.StageRuleRepository;
import com.elevate.consultingplatform.repository.catalog.StageRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/stage-rules")
@RequiredArgsConstructor
@Tag(name = "Admin - Stage Rules", description = "Manage score range to stage mapping rules")
public class StageRuleAdminController {

    private final StageRuleRepository stageRuleRepository;
    private final QuestionnaireRepository questionnaireRepository;
    private final StageRepository stageRepository;

    @GetMapping
    @Operation(summary = "List stage rules for a questionnaire or global")
    public ResponseEntity<List<StageRule>> list(@RequestParam(name = "questionnaireId", required = false) Long questionnaireId) {
        Questionnaire q = null;
        if (questionnaireId != null) {
            q = questionnaireRepository.findById(questionnaireId)
                    .orElseThrow(() -> new ResourceNotFoundException("Questionnaire not found"));
            return ResponseEntity.ok(stageRuleRepository.findByQuestionnaireOrderByPriorityAsc(q));
        } else {
            return ResponseEntity.ok(stageRuleRepository.findByQuestionnaireIsNullOrderByPriorityAsc());
        }
    }

    @PostMapping
    @Operation(summary = "Create stage rule")
    public ResponseEntity<Long> create(@RequestBody CreateStageRuleRequest req) {
        Questionnaire q = null;
        if (req.getQuestionnaireId() != null) {
            q = questionnaireRepository.findById(req.getQuestionnaireId())
                    .orElseThrow(() -> new ResourceNotFoundException("Questionnaire not found"));
        }
        Stage target = stageRepository.findById(req.getTargetStageId())
                .orElseThrow(() -> new ResourceNotFoundException("Target stage not found"));
        StageRule rule = StageRule.builder()
                .questionnaire(q)
                .minScore(req.getMinScore())
                .maxScore(req.getMaxScore())
                .targetStage(target)
                .priority(req.getPriority())
                .build();
        Long id = stageRuleRepository.save(rule).getId();
        return ResponseEntity.created(URI.create("/api/v1/admin/stage-rules/" + id)).body(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update stage rule")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody UpdateStageRuleRequest req) {
        StageRule rule = stageRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stage rule not found"));
        Questionnaire q = null;
        if (req.getQuestionnaireId() != null) {
            q = questionnaireRepository.findById(req.getQuestionnaireId())
                    .orElseThrow(() -> new IllegalArgumentException("Questionnaire not found"));
        }
        Stage target = stageRepository.findById(req.getTargetStageId())
                .orElseThrow(() -> new IllegalArgumentException("Target stage not found"));
        rule.setQuestionnaire(q);
        rule.setMinScore(req.getMinScore());
        rule.setMaxScore(req.getMaxScore());
        rule.setTargetStage(target);
        rule.setPriority(req.getPriority());
        stageRuleRepository.save(rule);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete stage rule")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stageRuleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
