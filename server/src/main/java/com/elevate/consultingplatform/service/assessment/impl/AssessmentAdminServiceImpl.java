package com.elevate.consultingplatform.service.assessment.impl;

import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.entity.assessment.*;
import com.elevate.consultingplatform.entity.catalog.Stage;
import com.elevate.consultingplatform.repository.assessment.AssessmentRepository;
import com.elevate.consultingplatform.repository.assessment.ClientAssessmentRepository;
import com.elevate.consultingplatform.repository.catalog.StageRepository;
import com.elevate.consultingplatform.repository.UserRepository;
import com.elevate.consultingplatform.service.assessment.AssessmentAdminService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssessmentAdminServiceImpl implements AssessmentAdminService {

    private final AssessmentRepository assessmentRepository;
    private final StageRepository stageRepository;
    private final UserRepository userRepository;
    private final ClientAssessmentRepository clientAssessmentRepository;

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public Assessment createAssessment(Long stageId, String name, String description, Long questionnaireId) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new IllegalArgumentException("Stage not found: " + stageId));

        Assessment assessment = Assessment.builder()
                .stage(stage)
                .name(name)
                .description(description)
                .build();

        if (questionnaireId != null) {
            Questionnaire ref = em.getReference(Questionnaire.class, questionnaireId);
            assessment.setQuestionnaire(ref);
        }
        return assessmentRepository.save(assessment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assessment> listByStage(Long stageId) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new IllegalArgumentException("Stage not found: " + stageId));
        return assessmentRepository.findByStageOrderByIdAsc(stage);
    }

    @Override
    @Transactional
    public void assignToClients(Long assessmentId, List<Long> clientIds, LocalDate dueDate) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + assessmentId));

        List<ClientAssessment> toSave = new ArrayList<>();
        for (Long clientId : clientIds) {
            Optional<User> userOpt = userRepository.findById(clientId);
            if (userOpt.isEmpty()) continue; // skip invalid ids silently
            User client = userOpt.get();

            // Avoid duplicate assignment: check if already exists
            boolean exists = clientAssessmentRepository.findByClientOrderByIdDesc(client).stream()
                    .anyMatch(ca -> ca.getAssessment().getId().equals(assessmentId));
            if (exists) continue;

            ClientAssessment ca = ClientAssessment.builder()
                    .client(client)
                    .assessment(assessment)
                    .status(AssessmentStatus.ASSIGNED)
                    .dueDate(dueDate)
                    .build();
            toSave.add(ca);
        }
        if (!toSave.isEmpty()) {
            clientAssessmentRepository.saveAll(toSave);
        }
    }

    @Override
    @Transactional
    public Assessment updateAssessment(Long assessmentId, String name, String description, Long questionnaireId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + assessmentId));

        if (name != null && !name.isBlank()) assessment.setName(name);
        if (description != null) assessment.setDescription(description);
        if (questionnaireId != null) {
            Questionnaire ref = em.getReference(Questionnaire.class, questionnaireId);
            assessment.setQuestionnaire(ref);
        }
        return assessmentRepository.save(assessment);
    }

    @Override
    @Transactional
    public void deleteAssessment(Long assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + assessmentId));
        // Optionally: check for existing client assignments and handle cascade if needed
        assessmentRepository.delete(assessment);
    }
}
