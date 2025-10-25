package com.elevate.consultingplatform.repository.assessment;

import com.elevate.consultingplatform.entity.assessment.AssessmentAttempt;
import com.elevate.consultingplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, Long> {
    List<AssessmentAttempt> findByUser(User user);
    long countByCompletedAtIsNull();
    long countByCompletedAtIsNotNull();
}
