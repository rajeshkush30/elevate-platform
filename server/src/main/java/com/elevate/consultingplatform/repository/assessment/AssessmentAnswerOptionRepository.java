package com.elevate.consultingplatform.repository.assessment;

import com.elevate.consultingplatform.entity.assessment.AssessmentAnswer;
import com.elevate.consultingplatform.entity.assessment.AssessmentAnswerOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentAnswerOptionRepository extends JpaRepository<AssessmentAnswerOption, AssessmentAnswerOption.Id> {
    void deleteByAnswer(AssessmentAnswer answer);
    List<AssessmentAnswerOption> findByAnswer(AssessmentAnswer answer);
}
