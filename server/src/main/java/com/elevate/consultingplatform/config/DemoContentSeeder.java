package com.elevate.consultingplatform.config;

import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.entity.assessment.*;
import com.elevate.consultingplatform.entity.catalog.Module;
import com.elevate.consultingplatform.entity.catalog.Segment;
import com.elevate.consultingplatform.entity.catalog.Stage;
import com.elevate.consultingplatform.entity.catalog.StageType;
import com.elevate.consultingplatform.repository.UserRepository;
import com.elevate.consultingplatform.repository.assessment.*;
import com.elevate.consultingplatform.repository.catalog.ModuleRepository;
import com.elevate.consultingplatform.repository.catalog.SegmentRepository;
import com.elevate.consultingplatform.repository.catalog.StageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DemoContentSeeder {

    private static final Logger log = LoggerFactory.getLogger(DemoContentSeeder.class);

    private final ModuleRepository moduleRepository;
    private final SegmentRepository segmentRepository;
    private final StageRepository stageRepository;
    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final AssessmentRepository assessmentRepository;
    private final ClientAssessmentRepository clientAssessmentRepository;
    private final UserRepository userRepository;

    @Value("${app.demo.seed:false}")
    private boolean enableDemoSeed;

    @Value("${app.client.email:}")
    private String defaultClientEmail;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedDemo() {
        if (!enableDemoSeed) {
            return;
        }

        // Avoid duplicate demo setup
        if (moduleRepository.findAll().stream().anyMatch(m -> "Demo Module".equalsIgnoreCase(m.getName()))) {
            log.info("DemoContentSeeder: Demo Module already present, skipping.");
            return;
        }

        log.info("DemoContentSeeder: Seeding demo catalog, questionnaire, assessment and assignment...");

        // Module
        Module module = moduleRepository.save(Module.builder()
                .name("Demo Module")
                .description("Demo module with segments and stages")
                .orderIndex(100)
                .build());

        // Segments
        Segment segBiz = segmentRepository.save(Segment.builder()
                .module(module)
                .name("Business Basics")
                .orderIndex(1)
                .build());
        Segment segOps = segmentRepository.save(Segment.builder()
                .module(module)
                .name("Operations")
                .orderIndex(2)
                .build());

        // Stages
        Stage stageLearn = stageRepository.save(Stage.builder()
                .segment(segBiz)
                .name("Introduction Training")
                .type(StageType.TRAINING)
                .orderIndex(1)
                .build());
        Stage stageAssess = stageRepository.save(Stage.builder()
                .segment(segBiz)
                .name("Initial Assessment")
                .type(StageType.ASSESSMENT)
                .orderIndex(2)
                .build());
        Stage stageConsult = stageRepository.save(Stage.builder()
                .segment(segOps)
                .name("Consultation")
                .type(StageType.CONSULTATION)
                .orderIndex(1)
                .build());
        Stage stageSummary = stageRepository.save(Stage.builder()
                .segment(segOps)
                .name("Summary")
                .type(StageType.SUMMARY)
                .orderIndex(2)
                .build());

        // Questionnaire
        Questionnaire questionnaire = questionnaireRepository.save(Questionnaire.builder()
                .name("Demo Questionnaire")
                .version("v1")
                .build());

        // Questions of different types
        // MCQ (single select)
        Question q1 = questionRepository.save(Question.builder()
                .questionnaire(questionnaire)
                .segment(segBiz)
                .text("How clear is your value proposition?")
                .type("MCQ")
                .weight(1.0)
                .orderIndex(1)
                .build());
        questionOptionRepository.saveAll(List.of(
                QuestionOption.builder().question(q1).label("Not clear").value("0").weight(0.0).orderIndex(1).build(),
                QuestionOption.builder().question(q1).label("Somewhat clear").value("1").weight(1.0).orderIndex(2).build(),
                QuestionOption.builder().question(q1).label("Clear").value("2").weight(2.0).orderIndex(3).build()
        ));

        // SCALE (e.g., 1-5)
        Question q2 = questionRepository.save(Question.builder()
                .questionnaire(questionnaire)
                .segment(segBiz)
                .text("Rate your sales pipeline strength")
                .type("SCALE")
                .weight(1.0)
                .orderIndex(2)
                .build());
        questionOptionRepository.saveAll(List.of(
                QuestionOption.builder().question(q2).label("1").value("1").weight(1.0).orderIndex(1).build(),
                QuestionOption.builder().question(q2).label("2").value("2").weight(2.0).orderIndex(2).build(),
                QuestionOption.builder().question(q2).label("3").value("3").weight(3.0).orderIndex(3).build(),
                QuestionOption.builder().question(q2).label("4").value("4").weight(4.0).orderIndex(4).build(),
                QuestionOption.builder().question(q2).label("5").value("5").weight(5.0).orderIndex(5).build()
        ));

        // TEXT (free form)
        Question q3 = questionRepository.save(Question.builder()
                .questionnaire(questionnaire)
                .segment(segOps)
                .text("Describe your key operational challenges")
                .type("TEXT")
                .weight(0.0)
                .orderIndex(3)
                .build());

        // Assessment linked to stage and questionnaire
        Assessment assessment = assessmentRepository.save(Assessment.builder()
                .stage(stageAssess)
                .questionnaire(questionnaire)
                .name("Demo Assessment")
                .description("A demo assessment across multiple question types.")
                .build());

        // Assign to default client if configured
        if (defaultClientEmail != null && !defaultClientEmail.isBlank()) {
            userRepository.findByEmail(defaultClientEmail).ifPresent(client -> {
                // prevent duplicate assignment
                List<ClientAssessment> existing = clientAssessmentRepository.findByClientAndAssessment(client, assessment);
                if (existing.isEmpty()) {
                    ClientAssessment ca = ClientAssessment.builder()
                            .client(client)
                            .assessment(assessment)
                            .status(AssessmentStatus.ASSIGNED)
                            .dueDate(LocalDate.now().plusDays(7))
                            .build();
                    clientAssessmentRepository.save(ca);
                    log.info("Assigned Demo Assessment to {}", defaultClientEmail);
                } else {
                    log.info("Demo assignment already exists for {}", defaultClientEmail);
                }
            });
        } else {
            log.warn("app.client.email not set; demo assignment will not be created.");
        }

        log.info("DemoContentSeeder: Demo content created.");
    }
}
