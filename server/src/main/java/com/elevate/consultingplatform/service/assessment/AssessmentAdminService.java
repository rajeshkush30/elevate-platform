package com.elevate.consultingplatform.service.assessment;

import com.elevate.consultingplatform.entity.assessment.Assessment;

import java.time.LocalDate;
import java.util.List;

public interface AssessmentAdminService {
    Assessment createAssessment(Long stageId, String name, String description, Long questionnaireId);
    List<Assessment> listByStage(Long stageId);
    void assignToClients(Long assessmentId, List<Long> clientIds, LocalDate dueDate);
    Assessment updateAssessment(Long assessmentId, String name, String description, Long questionnaireId);
    void deleteAssessment(Long assessmentId);
}
