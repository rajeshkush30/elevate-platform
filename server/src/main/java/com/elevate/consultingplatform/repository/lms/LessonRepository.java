package com.elevate.consultingplatform.repository.lms;

import com.elevate.consultingplatform.entity.lms.Course;
import com.elevate.consultingplatform.entity.lms.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourseOrderByOrderIndexAsc(Course course);
}
