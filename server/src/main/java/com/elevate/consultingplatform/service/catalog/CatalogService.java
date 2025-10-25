package com.elevate.consultingplatform.service.catalog;

import com.elevate.consultingplatform.dto.catalog.CreateModuleRequest;
import com.elevate.consultingplatform.dto.catalog.CreateSegmentRequest;
import com.elevate.consultingplatform.dto.catalog.CreateStageRequest;
import com.elevate.consultingplatform.dto.catalog.ModuleTreeResponse;
import com.elevate.consultingplatform.dto.catalog.ReorderRequest;

import java.util.List;

public interface CatalogService {
    Long createModule(CreateModuleRequest req);
    void updateModule(Long id, CreateModuleRequest req);
    void deleteModule(Long id);
    Long createSegment(CreateSegmentRequest req);
    void updateSegment(Long id, CreateSegmentRequest req);
    void deleteSegment(Long id);
    Long createStage(CreateStageRequest req);
    void updateStage(Long id, CreateStageRequest req);
    void deleteStage(Long id);

    List<ModuleTreeResponse> getModuleTree();

    void reorder(ReorderRequest req);
}
