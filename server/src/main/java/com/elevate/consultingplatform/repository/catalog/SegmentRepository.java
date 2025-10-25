package com.elevate.consultingplatform.repository.catalog;

import com.elevate.consultingplatform.entity.catalog.Segment;
import com.elevate.consultingplatform.entity.catalog.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SegmentRepository extends JpaRepository<Segment, Long> {
    List<Segment> findByModuleOrderByOrderIndexAsc(Module module);
}
