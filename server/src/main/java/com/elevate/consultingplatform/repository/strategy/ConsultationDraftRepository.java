package com.elevate.consultingplatform.repository.strategy;

import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.entity.strategy.ConsultationDraft;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsultationDraftRepository extends JpaRepository<ConsultationDraft, Long> {
    Optional<ConsultationDraft> findFirstByUserOrderByIdDesc(User user);
}
