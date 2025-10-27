package com.elevate.consultingplatform.repository;

import com.elevate.consultingplatform.entity.TrainingEntitlement;
import com.elevate.consultingplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainingEntitlementRepository extends JpaRepository<TrainingEntitlement, Long> {

    @Query("SELECT e FROM TrainingEntitlement e WHERE e.user = :user AND e.scopeKey = :scopeKey AND e.active = true")
    Optional<TrainingEntitlement> findActiveByUserAndScope(@Param("user") User user,
                                                           @Param("scopeKey") String scopeKey);
}
