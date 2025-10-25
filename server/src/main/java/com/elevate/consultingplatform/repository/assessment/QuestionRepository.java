package com.elevate.consultingplatform.repository.assessment;

import com.elevate.consultingplatform.entity.assessment.Question;
import com.elevate.consultingplatform.entity.assessment.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuestionnaireOrderByIdAsc(Questionnaire questionnaire);
}
