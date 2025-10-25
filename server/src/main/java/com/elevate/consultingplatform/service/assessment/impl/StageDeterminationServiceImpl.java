package com.elevate.consultingplatform.service.assessment.impl;

import com.elevate.consultingplatform.entity.assessment.Questionnaire;
import com.elevate.consultingplatform.entity.assessment.StageRule;
import com.elevate.consultingplatform.entity.catalog.Stage;
import com.elevate.consultingplatform.repository.assessment.StageRuleRepository;
import com.elevate.consultingplatform.service.assessment.StageDeterminationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StageDeterminationServiceImpl implements StageDeterminationService {

    private final StageRuleRepository stageRuleRepository;

    @Override
    public Stage determineStage(Questionnaire questionnaire, double totalScore) {
        // Prefer questionnaire-specific rules; fallback to global
        List<StageRule> rules = questionnaire != null
                ? stageRuleRepository.findByQuestionnaireOrderByPriorityAsc(questionnaire)
                : List.of();
        if (rules == null || rules.isEmpty()) {
            rules = stageRuleRepository.findByQuestionnaireIsNullOrderByPriorityAsc();
        }
        return rules.stream()
                .sorted(Comparator.comparing(r -> r.getPriority() == null ? Integer.MAX_VALUE : r.getPriority()))
                .filter(r -> totalScore >= r.getMinScore() && totalScore <= r.getMaxScore())
                .map(StageRule::getTargetStage)
                .findFirst()
                .orElse(null);
    }
}
