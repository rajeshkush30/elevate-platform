package com.elevate.consultingplatform.service;

import com.elevate.consultingplatform.dto.questionnaire.QuestionDto;
import com.elevate.consultingplatform.dto.questionnaire.SegmentDto;
import com.elevate.consultingplatform.dto.questionnaire.SubmissionRequest;
import com.elevate.consultingplatform.dto.questionnaire.SubmissionResponse;

import java.util.List;

public interface QuestionnaireService {
    List<QuestionDto> getAllQuestions();
    SubmissionResponse submitAnswers(SubmissionRequest request);
    List<SegmentDto> getSegments();
}
