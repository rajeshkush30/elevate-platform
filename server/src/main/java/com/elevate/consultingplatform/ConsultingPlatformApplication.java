package com.elevate.consultingplatform;

import com.elevate.consultingplatform.entity.catalog.Module;
import com.elevate.consultingplatform.entity.catalog.Segment;
import com.elevate.consultingplatform.entity.catalog.Stage;
import com.elevate.consultingplatform.entity.catalog.StageType;
import com.elevate.consultingplatform.repository.catalog.ModuleRepository;
import com.elevate.consultingplatform.repository.catalog.SegmentRepository;
import com.elevate.consultingplatform.repository.catalog.StageRepository;
import com.elevate.consultingplatform.entity.assessment.StageRule;
import com.elevate.consultingplatform.repository.assessment.StageRuleRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
public class ConsultingPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsultingPlatformApplication.class, args);
    }

    @Bean
    CommandLineRunner seedCatalog(ModuleRepository moduleRepository,
                                  SegmentRepository segmentRepository,
                                  StageRepository stageRepository,
                                  StageRuleRepository stageRuleRepository) {
        return args -> {
            if (moduleRepository.count() > 0) return; // already seeded

            // Module
            Module m = Module.builder().build();
            m.setName("Business Foundations");
            m.setDescription("Core fundamentals for growing businesses");
            m.setOrderIndex(1);
            m.setIsActive(true);
            m = moduleRepository.save(m);

            // Segments
            Segment seg1 = Segment.builder().module(m).build();
            seg1.setName("Vision & Strategy");
            seg1.setDescription("Clarify goals, strategy, and positioning");
            seg1.setOrderIndex(1);
            seg1.setIsActive(true);
            seg1 = segmentRepository.save(seg1);

            Segment seg2 = Segment.builder().module(m).build();
            seg2.setName("Operations");
            seg2.setDescription("Processes, metrics, and execution");
            seg2.setOrderIndex(2);
            seg2.setIsActive(true);
            seg2 = segmentRepository.save(seg2);

            // Stages for Segment 1
            Stage s1 = Stage.builder().segment(seg1).type(StageType.ASSESSMENT).build();
            s1.setName("Stage 1: Strategy Assessment");
            s1.setDescription("Initial assessment to baseline strategy maturity");
            s1.setOrderIndex(1);
            s1.setIsActive(true);
            s1.setAssessmentConfig("{\"sections\":[{\"id\":\"vision\",\"weight\":0.4}]}\n");
            stageRepository.save(s1);

            Stage s2 = Stage.builder().segment(seg1).type(StageType.TRAINING).build();
            s2.setName("Stage 2: Strategy Training");
            s2.setDescription("LMS module for strategic planning");
            s2.setOrderIndex(2);
            s2.setIsActive(true);
            s2.setContentUrl("https://lms.example.com/course/STRAT101");
            s2.setLmsCourseId("STRAT101");
            stageRepository.save(s2);

            // Stages for Segment 2
            Stage s3 = Stage.builder().segment(seg2).type(StageType.CONSULTATION).build();
            s3.setName("Stage 3: Ops Consultation");
            s3.setDescription("Consultation focused on operational bottlenecks");
            s3.setOrderIndex(1);
            s3.setIsActive(true);
            s3.setAiPromptTemplate("You are an ops consultant. Analyze pain points and propose a weekly plan.");
            stageRepository.save(s3);

            Stage s4 = Stage.builder().segment(seg2).type(StageType.SUMMARY).build();
            s4.setName("Stage 4: Final Summary");
            s4.setDescription("AI-generated summary and next steps");
            s4.setOrderIndex(2);
            s4.setIsActive(true);
            s4 = stageRepository.save(s4);

            // Stage rules (global ranges)
            if (stageRuleRepository.count() == 0) {
                // 0-25 -> s1 (Assessment)
                stageRuleRepository.save(StageRule.builder()
                        .minScore(0.0).maxScore(25.0).targetStage(s1).priority(1).build());
                // 26-50 -> s2 (Training)
                stageRuleRepository.save(StageRule.builder()
                        .minScore(25.000001).maxScore(50.0).targetStage(s2).priority(2).build());
                // 51-75 -> s3 (Consultation)
                stageRuleRepository.save(StageRule.builder()
                        .minScore(50.000001).maxScore(75.0).targetStage(s3).priority(3).build());
                // 76-100 -> s4 (Summary)
                stageRuleRepository.save(StageRule.builder()
                        .minScore(75.000001).maxScore(100.0).targetStage(s4).priority(4).build());
            }
        };
    }
}
