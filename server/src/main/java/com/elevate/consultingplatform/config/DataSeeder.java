package com.elevate.consultingplatform.config;

import com.elevate.consultingplatform.entity.assessment.Question;
import com.elevate.consultingplatform.entity.assessment.Questionnaire;
import com.elevate.consultingplatform.entity.catalog.Module;
import com.elevate.consultingplatform.entity.catalog.Segment;
import com.elevate.consultingplatform.repository.assessment.QuestionRepository;
import com.elevate.consultingplatform.repository.assessment.QuestionnaireRepository;
import com.elevate.consultingplatform.repository.catalog.ModuleRepository;
import com.elevate.consultingplatform.repository.catalog.SegmentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

    @Component
    @Profile("seed")
    @RequiredArgsConstructor
    public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final SegmentRepository segmentRepository;
    private final ModuleRepository moduleRepository;
    private final QuestionRepository questionRepository;
    private final QuestionnaireRepository questionnaireRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedIfEmpty() {
        long moduleCount = moduleRepository.count();
        if (moduleCount > 0) {
            log.info("DataSeeder: modules already present ({}), skipping seed.", moduleCount);
            return;
        }

        log.info("DataSeeder: seeding initial catalog and questionnaire content...");

        // Module
        Module module = moduleRepository.save(Module.builder()
                .name("Business Assessment")
                .description("Segments for initial business assessment")
                .orderIndex(1)
                .build());

        // Segments (require module and use orderIndex)
        Segment s1 = segmentRepository.save(Segment.builder()
                .module(module)
                .name("Market & Value Proposition")
                .orderIndex(1)
                .build());
        Segment s2 = segmentRepository.save(Segment.builder()
                .module(module)
                .name("Sales & Pipeline")
                .orderIndex(2)
                .build());
        Segment s3 = segmentRepository.save(Segment.builder()
                .module(module)
                .name("Operations & SOPs")
                .orderIndex(3)
                .build());

        // Questionnaire
        Questionnaire questionnaire = questionnaireRepository.save(Questionnaire.builder()
                .name("MVP Assessment")
                .version("v1")
                .build());

        // Questions
        questionRepository.save(Question.builder()
                .questionnaire(questionnaire)
                .segment(s1)
                .text("How clear is your value proposition to customers?")
                .weight(1.0)
                .type("MCQ")
                .optionsJson("[{\"label\":\"Not clear\",\"value\":\"0\"},{\"label\":\"Somewhat clear\",\"value\":\"1\"},{\"label\":\"Clear\",\"value\":\"2\"}]")
                .build());

        questionRepository.save(Question.builder()
                .questionnaire(questionnaire)
                .segment(s2)
                .text("How strong is your sales pipeline?")
                .weight(1.0)
                .type("MCQ")
                .optionsJson("[{\"label\":\"Weak\",\"value\":\"0\"},{\"label\":\"Moderate\",\"value\":\"1\"},{\"label\":\"Strong\",\"value\":\"2\"}]")
                .build());

        questionRepository.save(Question.builder()
                .questionnaire(questionnaire)
                .segment(s3)
                .text("Do you have documented processes and SOPs?")
                .weight(1.0)
                .type("MCQ")
                .optionsJson("[{\"label\":\"No\",\"value\":\"0\"},{\"label\":\"Partial\",\"value\":\"1\"},{\"label\":\"Yes\",\"value\":\"2\"}]")
                .build());

        log.info("DataSeeder: seed complete.");
    }
}

