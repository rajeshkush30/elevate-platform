package com.elevate.consultingplatform.repository.assessment;

import com.elevate.consultingplatform.entity.assessment.Question;
import com.elevate.consultingplatform.entity.assessment.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {
    List<QuestionOption> findByQuestionOrderByOrderIndexAsc(Question question);
}
