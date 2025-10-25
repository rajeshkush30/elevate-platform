package com.elevate.consultingplatform.repository.assessment;

import com.elevate.consultingplatform.entity.assessment.StageRule;
import com.elevate.consultingplatform.entity.assessment.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StageRuleRepository extends JpaRepository<StageRule, Long> {
    List<StageRule> findByQuestionnaireOrderByPriorityAsc(Questionnaire questionnaire);
    List<StageRule> findByQuestionnaireIsNullOrderByPriorityAsc();
}
