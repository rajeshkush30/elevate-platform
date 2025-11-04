package com.elevate.consultingplatform.repository.lms;

import com.elevate.consultingplatform.entity.catalog.Stage;
import com.elevate.consultingplatform.entity.lms.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByStage(Stage stage);
}
