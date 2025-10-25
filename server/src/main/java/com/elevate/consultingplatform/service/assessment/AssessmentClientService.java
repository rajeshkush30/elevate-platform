package com.elevate.consultingplatform.service.assessment;

import com.elevate.consultingplatform.entity.assessment.ClientAssessment;
import com.elevate.consultingplatform.entity.assessment.AssessmentAnswer;
import com.elevate.consultingplatform.dto.assessment.ClientAssessmentDetailsResponse;

import java.util.List;

public interface AssessmentClientService {
    List<ClientAssessment> listMyAssignments(Long userId);
    ClientAssessment getForFill(Long clientAssessmentId, Long userId);
    void saveAnswers(Long clientAssessmentId, Long userId, List<AnswerItem> answers, boolean submit);
    ClientAssessmentDetailsResponse getDetails(Long clientAssessmentId, Long userId);

    class AnswerItem {
        public Long questionId;
        public String answerText;
        public List<Long> optionIds;
    }
}
