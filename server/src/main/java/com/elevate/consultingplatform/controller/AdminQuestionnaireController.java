package com.elevate.consultingplatform.controller;

import com.elevate.consultingplatform.dto.questionnaire.admin.SegmentSummaryDto;
import com.elevate.consultingplatform.dto.questionnaire.admin.SegmentUpsertRequest;
import com.elevate.consultingplatform.dto.questionnaire.admin.ReorderItemDto;
import com.elevate.consultingplatform.service.AdminQuestionnaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/questionnaire/segments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminQuestionnaireController {

    private final AdminQuestionnaireService adminQuestionnaireService;

    @GetMapping
    public ResponseEntity<List<SegmentSummaryDto>> list() {
        return ResponseEntity.ok(adminQuestionnaireService.listSegments());
    }

    @PostMapping
    public ResponseEntity<SegmentSummaryDto> create(@RequestBody SegmentUpsertRequest req) {
        return ResponseEntity.ok(adminQuestionnaireService.createSegment(req.getName(), req.getOrder()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SegmentSummaryDto> update(@PathVariable Long id, @RequestBody SegmentUpsertRequest req) {
        return ResponseEntity.ok(adminQuestionnaireService.updateSegment(id, req.getName(), req.getOrder()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adminQuestionnaireService.deleteSegment(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reorder")
    public ResponseEntity<Void> reorder(@RequestBody java.util.List<ReorderItemDto> items) {
        adminQuestionnaireService.reorderSegments(items);
        return ResponseEntity.ok().build();
    }
}
