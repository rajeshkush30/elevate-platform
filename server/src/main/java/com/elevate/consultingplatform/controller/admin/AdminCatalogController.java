package com.elevate.consultingplatform.controller.admin;

import com.elevate.consultingplatform.dto.catalog.CreateModuleRequest;
import com.elevate.consultingplatform.dto.catalog.CreateSegmentRequest;
import com.elevate.consultingplatform.dto.catalog.CreateStageRequest;
import com.elevate.consultingplatform.dto.catalog.ModuleTreeResponse;
import com.elevate.consultingplatform.dto.catalog.ReorderRequest;
import com.elevate.consultingplatform.service.catalog.CatalogService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Catalog", description = "Manage Modules, Segments, Stages and ordering")
public class AdminCatalogController {

    private final CatalogService catalogService;

    @GetMapping("/modules/tree")
    public ResponseEntity<List<ModuleTreeResponse>> getModuleTree() {
        return ResponseEntity.ok(catalogService.getModuleTree());
    }

    @PostMapping("/modules")
    public ResponseEntity<Void> createModule(@Valid @RequestBody CreateModuleRequest req) {
        Long id = catalogService.createModule(req);
        return ResponseEntity.created(URI.create("/api/v1/admin/modules/" + id)).build();
    }

    @PostMapping("/segments")
    public ResponseEntity<Void> createSegment(@Valid @RequestBody CreateSegmentRequest req) {
        Long id = catalogService.createSegment(req);
        return ResponseEntity.created(URI.create("/api/v1/admin/segments/" + id)).build();
    }

    @PostMapping("/stages")
    public ResponseEntity<Void> createStage(@Valid @RequestBody CreateStageRequest req) {
        Long id = catalogService.createStage(req);
        return ResponseEntity.created(URI.create("/api/v1/admin/stages/" + id)).build();
    }

    @PostMapping("/reorder")
    public ResponseEntity<Void> reorder(@RequestBody ReorderRequest req) {
        catalogService.reorder(req);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/modules/{id}")
    public ResponseEntity<Void> updateModule(@PathVariable Long id, @Valid @RequestBody CreateModuleRequest req) {
        catalogService.updateModule(id, req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/modules/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        catalogService.deleteModule(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/segments/{id}")
    public ResponseEntity<Void> updateSegment(@PathVariable Long id, @Valid @RequestBody CreateSegmentRequest req) {
        catalogService.updateSegment(id, req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/segments/{id}")
    public ResponseEntity<Void> deleteSegment(@PathVariable Long id) {
        catalogService.deleteSegment(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/stages/{id}")
    public ResponseEntity<Void> updateStage(@PathVariable Long id, @Valid @RequestBody CreateStageRequest req) {
        catalogService.updateStage(id, req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/stages/{id}")
    public ResponseEntity<Void> deleteStage(@PathVariable Long id) {
        catalogService.deleteStage(id);
        return ResponseEntity.noContent().build();
    }
}
