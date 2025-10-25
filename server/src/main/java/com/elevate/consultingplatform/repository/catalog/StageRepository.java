package com.elevate.consultingplatform.repository.catalog;

import com.elevate.consultingplatform.entity.catalog.Segment;
import com.elevate.consultingplatform.entity.catalog.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StageRepository extends JpaRepository<Stage, Long> {
    List<Stage> findBySegmentOrderByOrderIndexAsc(Segment segment);
}
