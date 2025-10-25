package com.elevate.consultingplatform.repository.assessment;

import com.elevate.consultingplatform.entity.assessment.Answer;
import com.elevate.consultingplatform.entity.assessment.AssessmentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByAttempt(AssessmentAttempt attempt);
}
