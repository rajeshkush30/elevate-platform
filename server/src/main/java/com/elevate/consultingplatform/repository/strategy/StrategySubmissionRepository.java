package com.elevate.consultingplatform.repository.strategy;

import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.entity.strategy.StrategySubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StrategySubmissionRepository extends JpaRepository<StrategySubmission, Long> {
    Optional<StrategySubmission> findFirstByUserOrderByIdDesc(User user);
}
