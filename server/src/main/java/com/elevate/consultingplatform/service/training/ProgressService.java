package com.elevate.consultingplatform.service.training;

import com.elevate.consultingplatform.dto.catalog.ModuleTreeResponse;
import com.elevate.consultingplatform.dto.training.StageCompleteRequest;
import com.elevate.consultingplatform.dto.training.StageStartResponse;

import java.util.List;

public interface ProgressService {
    List<ModuleTreeResponse> getAssignedTreeForCurrentUser();
    StageStartResponse startStage(Long stageId);
    void completeStage(Long stageId, StageCompleteRequest req);
}
