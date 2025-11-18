package com.elevate.consultingplatform.repository.assessment;

import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.entity.assessment.Assessment;
import com.elevate.consultingplatform.entity.assessment.ClientAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientAssessmentRepository extends JpaRepository<ClientAssessment, Long>, JpaSpecificationExecutor<ClientAssessment> {
    List<ClientAssessment> findByClientOrderByIdDesc(User client);
    List<ClientAssessment> findByAssessment(Assessment assessment);
    List<ClientAssessment> findByClientAndAssessment(User client, Assessment assessment);
}
