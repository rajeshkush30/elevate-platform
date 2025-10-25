package com.elevate.consultingplatform.service;

import com.elevate.consultingplatform.dto.questionnaire.admin.*;

import java.util.List;

public interface AdminQuestionnaireService {
    // Segments
    List<SegmentSummaryDto> listSegments();
    SegmentSummaryDto createSegment(String name, Integer order);
    SegmentSummaryDto updateSegment(Long id, String name, Integer order);
    void deleteSegment(Long id);
    void reorderSegments(java.util.List<com.elevate.consultingplatform.dto.questionnaire.admin.ReorderItemDto> items);

    // Questions
    List<QuestionSummaryDto> listQuestions(Long segmentId);
    QuestionSummaryDto createQuestion(Long segmentId, String text, Integer weight, Integer order);
    QuestionSummaryDto updateQuestion(Long questionId, String text, Integer weight, Integer order);
    void deleteQuestion(Long questionId);
    void reorderQuestions(Long segmentId, java.util.List<com.elevate.consultingplatform.dto.questionnaire.admin.ReorderItemDto> items);

    // Options
    List<OptionSummaryDto> listOptions(Long questionId);
    OptionSummaryDto createOption(Long questionId, String label, String value, Integer order);
    OptionSummaryDto updateOption(Long optionId, String label, String value, Integer order);
    void deleteOption(Long optionId);
    void reorderOptions(Long questionId, java.util.List<com.elevate.consultingplatform.dto.questionnaire.admin.ReorderItemDto> items);
}
