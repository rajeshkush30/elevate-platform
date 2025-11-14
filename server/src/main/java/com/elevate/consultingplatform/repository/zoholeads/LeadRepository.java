package com.elevate.consultingplatform.repository.zoholeads;

import com.elevate.consultingplatform.entity.zoholeads.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LeadRepository extends JpaRepository<Lead, Long> {
    Optional<Lead> findByZohoLeadId(String zohoLeadId);
}
