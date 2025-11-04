package com.elevate.consultingplatform.repository.lms;

import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.entity.lms.Lesson;
import com.elevate.consultingplatform.entity.lms.UserLessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserLessonProgressRepository extends JpaRepository<UserLessonProgress, Long> {
    Optional<UserLessonProgress> findByUserAndLesson(User user, Lesson lesson);
    List<UserLessonProgress> findByUser(User user);
}
