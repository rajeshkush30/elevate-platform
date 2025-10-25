package com.elevate.consultingplatform.repository.assessment;

import com.elevate.consultingplatform.entity.assessment.Option;
import com.elevate.consultingplatform.entity.assessment.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {
    List<Option> findByQuestionOrderByOrderIndexAscIdAsc(Question question);
}
