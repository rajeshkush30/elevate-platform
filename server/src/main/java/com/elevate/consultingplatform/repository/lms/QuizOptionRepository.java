package com.elevate.consultingplatform.repository.lms;

import com.elevate.consultingplatform.entity.lms.QuizOption;
import com.elevate.consultingplatform.entity.lms.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizOptionRepository extends JpaRepository<QuizOption, Long> {
    List<QuizOption> findByQuestionOrderByOrderIndexAsc(QuizQuestion question);
}
