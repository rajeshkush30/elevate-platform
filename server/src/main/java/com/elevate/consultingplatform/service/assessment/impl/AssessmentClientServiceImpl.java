package com.elevate.consultingplatform.service.assessment.impl;

import com.elevate.consultingplatform.entity.assessment.*;
import com.elevate.consultingplatform.integration.AIClient;
import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.repository.UserRepository;
import com.elevate.consultingplatform.repository.assessment.*;
import com.elevate.consultingplatform.service.assessment.AssessmentClientService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssessmentClientServiceImpl implements AssessmentClientService {

    private final ClientAssessmentRepository clientAssessmentRepository;
    private final AssessmentAnswerRepository assessmentAnswerRepository;
    private final AssessmentAnswerOptionRepository assessmentAnswerOptionRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final UserRepository userRepository;
    private final AIClient aiClient;

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional(readOnly = true)
    public List<ClientAssessment> listMyAssignments(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return clientAssessmentRepository.findByClientOrderByIdDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientAssessment getForFill(Long clientAssessmentId, Long userId) {
        ClientAssessment ca = clientAssessmentRepository.findById(clientAssessmentId)
                .orElseThrow(() -> new IllegalArgumentException("ClientAssessment not found: " + clientAssessmentId));
        if (!ca.getClient().getId().equals(userId)) {
            throw new IllegalArgumentException("Access denied for this assessment");
        }
        return ca;
    }

    @Override
    @Transactional
    public void saveAnswers(Long clientAssessmentId, Long userId, List<AnswerItem> answers, boolean submit) {
        ClientAssessment ca = clientAssessmentRepository.findById(clientAssessmentId)
                .orElseThrow(() -> new IllegalArgumentException("ClientAssessment not found: " + clientAssessmentId));
        if (!ca.getClient().getId().equals(userId)) {
            throw new IllegalArgumentException("Access denied for this assessment");
        }

        // Upsert each answer
        for (AnswerItem item : answers) {
            Question question = questionRepository.findById(item.questionId)
                    .orElseThrow(() -> new IllegalArgumentException("Question not found: " + item.questionId));

            AssessmentAnswer answer = assessmentAnswerRepository
                    .findByClientAssessmentAndQuestion(ca, question)
                    .orElseGet(() -> AssessmentAnswer.builder()
                            .clientAssessment(ca)
                            .question(question)
                            .build());
            answer.setAnswerText(item.answerText);
            AssessmentAnswer saved = assessmentAnswerRepository.save(answer);

            // Replace option selections
            assessmentAnswerOptionRepository.deleteByAnswer(saved);
            if (item.optionIds != null && !item.optionIds.isEmpty()) {
                List<AssessmentAnswerOption> links = new ArrayList<>();
                for (Long optId : item.optionIds) {
                    QuestionOption opt = questionOptionRepository.findById(optId)
                            .orElseThrow(() -> new IllegalArgumentException("Option not found: " + optId));
                    AssessmentAnswerOption link = new AssessmentAnswerOption();
                    AssessmentAnswerOption.Id id = new AssessmentAnswerOption.Id(saved.getId(), opt.getId());
                    link.setId(id);
                    link.setAnswer(saved);
                    link.setOption(opt);
                    links.add(link);
                }
                assessmentAnswerOptionRepository.saveAll(links);
            }
        }

        if (submit) {
            // Compute score: sum of selected options' weights
            BigDecimal total = BigDecimal.ZERO;
            List<AssessmentAnswer> all = assessmentAnswerRepository.findByClientAssessment(ca);
            for (AssessmentAnswer ans : all) {
                var selected = assessmentAnswerOptionRepository.findByAnswer(ans);
                for (AssessmentAnswerOption link : selected) {
                    Double w = link.getOption().getWeight();
                    if (w != null) {
                        total = total.add(BigDecimal.valueOf(w));
                    }
                }
            }
            ca.setStatus(AssessmentStatus.SUBMITTED);
            ca.setScore(total);

            // Determine stage and generate summary via AI
            Questionnaire questionnaire = ca.getAssessment().getQuestionnaire();
            if (questionnaire != null) {
                // Build AI answer payload
                List<Question> questions = questionRepository.findByQuestionnaireOrderByIdAsc(questionnaire);
                List<AIClient.AnswerItem> items = new java.util.ArrayList<>();
                for (Question q : questions) {
                    var ansOpt = assessmentAnswerRepository.findByClientAssessmentAndQuestion(ca, q);
                    String text = ansOpt.map(AssessmentAnswer::getAnswerText).orElse(null);
                    java.util.List<Long> optionIds = new java.util.ArrayList<>();
                    if (ansOpt.isPresent()) {
                        var links = assessmentAnswerOptionRepository.findByAnswer(ansOpt.get());
                        for (AssessmentAnswerOption link : links) {
                            optionIds.add(link.getOption().getId());
                        }
                    }
                    items.add(new AIClient.AnswerItem(q.getId(), text, optionIds.isEmpty() ? null : optionIds));
                }
                var stageResp = aiClient.determineStage(new AIClient.StageRequest(
                        ca.getId(), questionnaire.getId(), items, java.util.Map.of("version", "v1")
                ));
                var summaryResp = aiClient.generateSummary(new AIClient.SummaryRequest(
                        stageResp.stage(), total.doubleValue(), java.util.Map.of()
                ));
                ca.setStage(stageResp.stage());
                ca.setAiConfidence(stageResp.confidence());
                ca.setStageSummary(summaryResp.summary());
            }
        } else {
            if (ca.getStatus() == AssessmentStatus.ASSIGNED) {
                ca.setStatus(AssessmentStatus.IN_PROGRESS);
            }
        }
        clientAssessmentRepository.save(ca);
    }

    @Override
    @Transactional(readOnly = true)
    public com.elevate.consultingplatform.dto.assessment.ClientAssessmentDetailsResponse getDetails(Long clientAssessmentId, Long userId) {
        ClientAssessment ca = clientAssessmentRepository.findById(clientAssessmentId)
                .orElseThrow(() -> new IllegalArgumentException("ClientAssessment not found: " + clientAssessmentId));
        if (!ca.getClient().getId().equals(userId)) {
            throw new IllegalArgumentException("Access denied for this assessment");
        }

        Assessment assessment = ca.getAssessment();
        Questionnaire questionnaire = assessment.getQuestionnaire();
        if (questionnaire == null) {
            // No questionnaire linked; return empty list
            return com.elevate.consultingplatform.dto.assessment.ClientAssessmentDetailsResponse.builder()
                    .clientAssessmentId(ca.getId())
                    .assessmentName(assessment.getName())
                    .questions(List.of())
                    .build();
        }

        // Fetch all questions for questionnaire
        List<Question> questions = questionRepository.findByQuestionnaireOrderByIdAsc(questionnaire);
        List<com.elevate.consultingplatform.dto.assessment.ClientAssessmentDetailsResponse.QuestionDto> qDtos = new ArrayList<>();

        for (Question q : questions) {
            // Options
            var opts = questionOptionRepository.findByQuestionOrderByOrderIndexAsc(q);
            List<com.elevate.consultingplatform.dto.assessment.ClientAssessmentDetailsResponse.OptionDto> oDtos = new ArrayList<>();
            for (QuestionOption o : opts) {
                oDtos.add(com.elevate.consultingplatform.dto.assessment.ClientAssessmentDetailsResponse.OptionDto.builder()
                        .id(o.getId()).text(o.getLabel()).build());
            }

            // Existing answer (if any)
            var existingOpt = assessmentAnswerRepository.findByClientAssessmentAndQuestion(ca, q);
            com.elevate.consultingplatform.dto.assessment.ClientAssessmentDetailsResponse.ExistingAnswer ea = null;
            if (existingOpt.isPresent()) {
                AssessmentAnswer ans = existingOpt.get();
                var selected = assessmentAnswerOptionRepository.findByAnswer(ans);
                List<Long> optionIds = new ArrayList<>();
                for (AssessmentAnswerOption link : selected) {
                    optionIds.add(link.getOption().getId());
                }
                ea = com.elevate.consultingplatform.dto.assessment.ClientAssessmentDetailsResponse.ExistingAnswer.builder()
                        .answerText(ans.getAnswerText())
                        .optionIds(optionIds)
                        .build();
            }

            qDtos.add(com.elevate.consultingplatform.dto.assessment.ClientAssessmentDetailsResponse.QuestionDto.builder()
                    .id(q.getId())
                    .text(q.getText())
                    .type(q.getType())
                    .weight(q.getWeight())
                    .options(oDtos)
                    .existingAnswer(ea)
                    .build());
        }

        return com.elevate.consultingplatform.dto.assessment.ClientAssessmentDetailsResponse.builder()
                .clientAssessmentId(ca.getId())
                .assessmentName(assessment.getName())
                .questions(qDtos)
                .build();
    }
}
