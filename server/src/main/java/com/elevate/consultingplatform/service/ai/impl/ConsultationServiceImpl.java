package com.elevate.consultingplatform.service.ai.impl;

import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.entity.assessment.ClientAssessment;
import com.elevate.consultingplatform.entity.strategy.ConsultationDraft;
import com.elevate.consultingplatform.entity.strategy.StrategySubmission;
import com.elevate.consultingplatform.integration.AIClient;
import com.elevate.consultingplatform.repository.UserRepository;
import com.elevate.consultingplatform.repository.assessment.ClientAssessmentRepository;
import com.elevate.consultingplatform.repository.strategy.ConsultationDraftRepository;
import com.elevate.consultingplatform.repository.strategy.StrategySubmissionRepository;
import com.elevate.consultingplatform.service.ai.ConsultationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConsultationServiceImpl implements ConsultationService {

    private final StrategySubmissionRepository strategySubmissionRepository;
    private final ConsultationDraftRepository consultationDraftRepository;
    private final ClientAssessmentRepository clientAssessmentRepository;
    private final UserRepository userRepository;
    private final AIClient aiClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    @Transactional
    public ConsultationDraft generateIfAbsent(User user) {
        // resolve persisted user
        User persisted = user;
        if (user.getId() == null && user.getEmail() != null) {
            persisted = userRepository.findByEmail(user.getEmail()).orElseThrow();
        }
        // If there is an approved or existing draft, return it
        ConsultationDraft existing = consultationDraftRepository.findFirstByUserOrderByIdDesc(persisted).orElse(null);
        if (existing != null) return existing;

        StrategySubmission sub = strategySubmissionRepository.findFirstByUserOrderByIdDesc(persisted).orElse(null);
        if (sub == null) return null; // strategy not submitted yet

        // Determine latest submitted assessment for stage context
        ClientAssessment latest = clientAssessmentRepository.findByClientOrderByIdDesc(persisted).stream().findFirst().orElse(null);
        String stage = latest != null ? latest.getStage() : null;

        Map<String, Object> history = new HashMap<>();
        if (latest != null) {
            history.put("assessmentScore", latest.getScore() != null ? latest.getScore().doubleValue() : null);
            history.put("stageSummary", latest.getStageSummary());
        }

        // Build strategy responses array from stored JSON
        List<Map<String, Object>> strategyResponses;
        try {
            strategyResponses = mapper.readValue(sub.getPayload(), mapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        } catch (Exception e) {
            throw new RuntimeException("Invalid strategy payload", e);
        }

        var resp = aiClient.finalConsultation(new AIClient.FinalConsultationRequest(
                persisted.getId(),
                stage != null ? stage : "StartUp",
                strategyResponses,
                history
        ));

        ConsultationDraft draft = ConsultationDraft.builder()
                .user(persisted)
                .draft(resp.draft())
                .sectionsJson(toJson(resp.sections()))
                .approved(false)
                .build();
        return consultationDraftRepository.save(draft);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> status(User user) {
        User persisted = user;
        if (user.getId() == null && user.getEmail() != null) {
            persisted = userRepository.findByEmail(user.getEmail()).orElseThrow();
        }
        ConsultationDraft existing = consultationDraftRepository.findFirstByUserOrderByIdDesc(persisted).orElse(null);
        Map<String, Object> out = new HashMap<>();
        if (existing == null) {
            out.put("status", "PENDING");
            return out;
        }
        out.put("status", existing.isApproved() ? "READY" : "PENDING");
        out.put("approved", existing.isApproved());
        out.put("draft", existing.getDraft());
        return out;
    }

    @Override
    @Transactional
    public ConsultationDraft approve(Long draftId) {
        ConsultationDraft draft = consultationDraftRepository.findById(draftId).orElseThrow();
        draft.setApproved(true);
        return consultationDraftRepository.save(draft);
    }

    private String toJson(Object o) {
        try { return mapper.writeValueAsString(o); } catch (Exception e) { return "[]"; }
    }
}
