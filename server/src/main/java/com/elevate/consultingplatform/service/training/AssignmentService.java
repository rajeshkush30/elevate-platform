package com.elevate.consultingplatform.service.training;

import com.elevate.consultingplatform.dto.training.CreateAssignmentRequest;

public interface AssignmentService {
    Long createAssignment(CreateAssignmentRequest req);
}
