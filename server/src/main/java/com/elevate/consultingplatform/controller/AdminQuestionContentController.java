package com.elevate.consultingplatform.controller;

import com.elevate.consultingplatform.dto.questionnaire.admin.OptionSummaryDto;
import com.elevate.consultingplatform.dto.questionnaire.admin.QuestionSummaryDto;
import com.elevate.consultingplatform.dto.questionnaire.admin.SegmentUpsertRequest;
import com.elevate.consultingplatform.dto.questionnaire.admin.ReorderItemDto;
import com.elevate.consultingplatform.service.AdminQuestionnaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/questionnaire")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminQuestionContentController {

    private final AdminQuestionnaireService adminService;

    // Questions
    @GetMapping("/segments/{segmentId}/questions")
    public ResponseEntity<List<QuestionSummaryDto>> listQuestions(@PathVariable Long segmentId) {
        return ResponseEntity.ok(adminService.listQuestions(segmentId));
    }

    @PostMapping("/segments/{segmentId}/questions")
    public ResponseEntity<QuestionSummaryDto> createQuestion(@PathVariable Long segmentId, @RequestBody QuestionSummaryDto req) {
        return ResponseEntity.ok(adminService.createQuestion(segmentId, req.getText(), req.getWeight(), req.getOrder()));
    }

    @PutMapping("/questions/{questionId}")
    public ResponseEntity<QuestionSummaryDto> updateQuestion(@PathVariable Long questionId, @RequestBody QuestionSummaryDto req) {
        return ResponseEntity.ok(adminService.updateQuestion(questionId, req.getText(), req.getWeight(), req.getOrder()));
    }

    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId) {
        adminService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/segments/{segmentId}/questions/reorder")
    public ResponseEntity<Void> reorderQuestions(@PathVariable Long segmentId, @RequestBody java.util.List<ReorderItemDto> items) {
        adminService.reorderQuestions(segmentId, items);
        return ResponseEntity.ok().build();
    }

    // Options
    @GetMapping("/questions/{questionId}/options")
    public ResponseEntity<List<OptionSummaryDto>> listOptions(@PathVariable Long questionId) {
        return ResponseEntity.ok(adminService.listOptions(questionId));
    }

    @PostMapping("/questions/{questionId}/options")
    public ResponseEntity<OptionSummaryDto> createOption(@PathVariable Long questionId, @RequestBody OptionSummaryDto req) {
        return ResponseEntity.ok(adminService.createOption(questionId, req.getLabel(), req.getValue(), req.getOrder()));
    }

    @PutMapping("/options/{optionId}")
    public ResponseEntity<OptionSummaryDto> updateOption(@PathVariable Long optionId, @RequestBody OptionSummaryDto req) {
        return ResponseEntity.ok(adminService.updateOption(optionId, req.getLabel(), req.getValue(), req.getOrder()));
    }

    @DeleteMapping("/options/{optionId}")
    public ResponseEntity<Void> deleteOption(@PathVariable Long optionId) {
        adminService.deleteOption(optionId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/questions/{questionId}/options/reorder")
    public ResponseEntity<Void> reorderOptions(@PathVariable Long questionId, @RequestBody java.util.List<ReorderItemDto> items) {
        adminService.reorderOptions(questionId, items);
        return ResponseEntity.ok().build();
    }
}
