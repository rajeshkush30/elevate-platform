package com.elevate.consultingplatform.repository.assessment;

import com.elevate.consultingplatform.entity.assessment.AssessmentAnswer;
import com.elevate.consultingplatform.entity.assessment.ClientAssessment;
import com.elevate.consultingplatform.entity.assessment.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentAnswerRepository extends JpaRepository<AssessmentAnswer, Long> {
    List<AssessmentAnswer> findByClientAssessment(ClientAssessment clientAssessment);
    Optional<AssessmentAnswer> findByClientAssessmentAndQuestion(ClientAssessment clientAssessment, Question question);
}
