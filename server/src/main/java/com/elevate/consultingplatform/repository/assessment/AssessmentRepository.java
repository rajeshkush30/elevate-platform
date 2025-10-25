package com.elevate.consultingplatform.repository.assessment;

import com.elevate.consultingplatform.entity.assessment.Assessment;
import com.elevate.consultingplatform.entity.catalog.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    List<Assessment> findByStageOrderByIdAsc(Stage stage);
}
