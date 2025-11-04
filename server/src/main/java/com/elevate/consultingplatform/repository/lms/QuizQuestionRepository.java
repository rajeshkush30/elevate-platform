package com.elevate.consultingplatform.repository.lms;

import com.elevate.consultingplatform.entity.lms.Quiz;
import com.elevate.consultingplatform.entity.lms.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByQuizOrderByOrderIndexAsc(Quiz quiz);
}
