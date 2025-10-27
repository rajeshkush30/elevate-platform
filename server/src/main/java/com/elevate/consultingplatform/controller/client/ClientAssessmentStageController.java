package com.elevate.consultingplatform.controller.client;

import com.elevate.consultingplatform.entity.assessment.*;
import com.elevate.consultingplatform.integration.AIClient;
import com.elevate.consultingplatform.repository.assessment.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/client/assessments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ClientAssessmentStageController {

    private final ClientAssessmentRepository clientAssessmentRepository;
    private final AssessmentAnswerRepository assessmentAnswerRepository;
    private final AssessmentAnswerOptionRepository assessmentAnswerOptionRepository;
    private final QuestionRepository questionRepository;
    private final AIClient aiClient;

    record StageView(String stage, BigDecimal score, String summary) {}

    @GetMapping("/{clientAssessmentId}/stage")
    public ResponseEntity<StageView> getStage(@PathVariable Long clientAssessmentId) {
        ClientAssessment ca = clientAssessmentRepository.findById(clientAssessmentId)
                .orElseThrow(() -> new IllegalArgumentException("ClientAssessment not found: " + clientAssessmentId));
        Questionnaire questionnaire = ca.getAssessment().getQuestionnaire();
        // Prefer persisted values computed at submit time
        if (ca.getStage() != null || ca.getStageSummary() != null) {
            return ResponseEntity.ok(new StageView(ca.getStage(), ca.getScore(), ca.getStageSummary()));
        }
        if (questionnaire == null) {
            return ResponseEntity.ok(new StageView(null, ca.getScore(), null));
        }

        // Build answers from DB
        List<Question> questions = questionRepository.findByQuestionnaireOrderByIdAsc(questionnaire);
        List<AIClient.AnswerItem> items = new ArrayList<>();
        for (Question q : questions) {
            Optional<AssessmentAnswer> ansOpt = assessmentAnswerRepository.findByClientAssessmentAndQuestion(ca, q);
            String text = ansOpt.map(AssessmentAnswer::getAnswerText).orElse(null);
            List<Long> optionIds = new ArrayList<>();
            if (ansOpt.isPresent()) {
                var links = assessmentAnswerOptionRepository.findByAnswer(ansOpt.get());
                for (AssessmentAnswerOption link : links) {
                    optionIds.add(link.getOption().getId());
                }
            }
            items.add(new AIClient.AnswerItem(q.getId(), text, optionIds.isEmpty() ? null : optionIds));
        }

        var stageResp = aiClient.determineStage(new AIClient.StageRequest(
                ca.getId(), questionnaire.getId(), items, Map.of("version", "v1")
        ));
        var summaryResp = aiClient.generateSummary(new AIClient.SummaryRequest(
                stageResp.stage(), ca.getScore() != null ? ca.getScore().doubleValue() : 0.0, Map.of()
        ));

        return ResponseEntity.ok(new StageView(stageResp.stage(), ca.getScore(), summaryResp.summary()));
    }
}
