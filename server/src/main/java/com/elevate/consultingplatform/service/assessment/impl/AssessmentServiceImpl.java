package com.elevate.consultingplatform.service.assessment.impl;

import com.elevate.consultingplatform.dto.assessment.AssessmentResultResponse;
import com.elevate.consultingplatform.dto.assessment.CreateAttemptRequest;
import com.elevate.consultingplatform.dto.assessment.SubmitAnswersRequest;
import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.entity.assessment.*;
import com.elevate.consultingplatform.repository.UserRepository;
import com.elevate.consultingplatform.repository.assessment.*;
import com.elevate.consultingplatform.service.assessment.AssessmentService;
import com.elevate.consultingplatform.service.assessment.ScoringService;
import com.elevate.consultingplatform.service.assessment.StageDeterminationService;
import lombok.RequiredArgsConstructor;
import com.elevate.consultingplatform.exception.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {

    private final UserRepository userRepository;
    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionRepository questionRepository;
    private final AssessmentAttemptRepository attemptRepository;
    private final AnswerRepository answerRepository;
    private final ScoringService scoringService;
    private final StageDeterminationService stageDeterminationService;

    @Override
    @Transactional
    public Long createAttempt(CreateAttemptRequest req) {
        User u = currentUser();
        Questionnaire q = questionnaireRepository.findById(req.getQuestionnaireId())
                .orElseThrow(() -> new ResourceNotFoundException("Questionnaire not found"));
        AssessmentAttempt attempt = AssessmentAttempt.builder()
                .user(u)
                .questionnaire(q)
                .startedAt(LocalDateTime.now())
                .build();
        return attemptRepository.save(attempt).getId();
    }

    @Override
    @Transactional
    public void submitAnswers(Long attemptId, SubmitAnswersRequest req) {
        AssessmentAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));
        // Index existing answers by question for idempotent updates
        Map<Long, Answer> existing = new HashMap<>();
        for (Answer a : attempt.getAnswers()) {
            existing.put(a.getQuestion().getId(), a);
        }
        for (SubmitAnswersRequest.AnswerItem item : req.getAnswers()) {
            Question q = questionRepository.findById(item.getQuestionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + item.getQuestionId()));
            Answer a = existing.getOrDefault(q.getId(), Answer.builder().attempt(attempt).question(q).build());
            a.setValue(item.getValue());
            a.setScore(item.getScore());
            answerRepository.save(a);
            existing.put(q.getId(), a);
        }
    }

    @Override
    @Transactional
    public AssessmentResultResponse finalizeAttempt(Long attemptId) {
        AssessmentAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));
        double total = scoringService.computeTotalScore(attempt);
        attempt.setTotalScore(total);
        attempt.setCompletedAt(LocalDateTime.now());
        attemptRepository.save(attempt);
        var stage = stageDeterminationService.determineStage(attempt.getQuestionnaire(), total);
        return AssessmentResultResponse.builder()
                .attemptId(attempt.getId())
                .totalScore(total)
                .recommendedStageId(stage != null ? stage.getId() : null)
                .build();
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));
    }
}
