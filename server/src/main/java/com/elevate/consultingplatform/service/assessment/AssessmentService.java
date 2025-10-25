package com.elevate.consultingplatform.service.assessment;

import com.elevate.consultingplatform.dto.assessment.AssessmentResultResponse;
import com.elevate.consultingplatform.dto.assessment.CreateAttemptRequest;
import com.elevate.consultingplatform.dto.assessment.SubmitAnswersRequest;

public interface AssessmentService {
    Long createAttempt(CreateAttemptRequest req);
    void submitAnswers(Long attemptId, SubmitAnswersRequest req);
    AssessmentResultResponse finalizeAttempt(Long attemptId);
}
