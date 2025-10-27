package com.elevate.consultingplatform.integration;

import java.util.List;
import java.util.Map;

public interface AIClient {
    record PreScoreRequest(String leadId, Map<String, Object> profile, List<String> intents, String notes) {}
    record PreScoreResponse(double preScore, String stageHint, String rationale, List<String> labels) {}

    record StageRequest(Long clientAssessmentId, Long questionnaireId, List<AnswerItem> answers, Map<String, Object> ruleContext) {}
    record AnswerItem(Long questionId, String answerText, List<Long> optionIds) {}
    record StageResponse(String stage, double score, String rationale, double confidence) {}

    record SummaryRequest(String stage, double score, Map<String, Object> answers) {}
    record SummaryResponse(String summary, List<String> recommendations) {}

    record FinalConsultationRequest(Long clientId, String stage, List<Map<String, Object>> strategyResponses, Map<String, Object> history) {}
    record FinalConsultationResponse(String draft, List<Map<String, Object>> sections) {}

    PreScoreResponse preScore(PreScoreRequest req);
    StageResponse determineStage(StageRequest req);
    SummaryResponse generateSummary(SummaryRequest req);
    FinalConsultationResponse finalConsultation(FinalConsultationRequest req);
}
