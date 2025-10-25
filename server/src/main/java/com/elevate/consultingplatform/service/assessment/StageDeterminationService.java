package com.elevate.consultingplatform.service.assessment;

import com.elevate.consultingplatform.entity.assessment.Questionnaire;
import com.elevate.consultingplatform.entity.catalog.Stage;

public interface StageDeterminationService {
    Stage determineStage(Questionnaire questionnaire, double totalScore);
}
