package com.elevate.consultingplatform.service.assessment;

import com.elevate.consultingplatform.entity.assessment.AssessmentAttempt;

public interface ScoringService {
    double computeTotalScore(AssessmentAttempt attempt);
}
