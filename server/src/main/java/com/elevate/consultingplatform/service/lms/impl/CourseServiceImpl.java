package com.elevate.consultingplatform.service.lms.impl;

import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.entity.catalog.Stage;
import com.elevate.consultingplatform.entity.lms.*;
import com.elevate.consultingplatform.entity.training.ProgressStatus;
import com.elevate.consultingplatform.entity.training.UserStageProgress;
import com.elevate.consultingplatform.repository.UserRepository;
import com.elevate.consultingplatform.repository.catalog.StageRepository;
import com.elevate.consultingplatform.repository.lms.*;
import com.elevate.consultingplatform.repository.training.UserStageProgressRepository;
import com.elevate.consultingplatform.service.lms.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizOptionRepository quizOptionRepository;
    private final UserLessonProgressRepository userLessonProgressRepository;
    private final UserRepository userRepository;
    private final StageRepository stageRepository;
    private final UserStageProgressRepository userStageProgressRepository;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getCourseForStage(Long stageId) {
        Stage stage = stageRepository.findById(stageId).orElseThrow();
        Course course = courseRepository.findByStage(stage)
                .orElseGet(() -> Course.builder().stage(stage).title(stage.getName()).description(null).build());
        List<Lesson> lessons = lessonRepository.findByCourseOrderByOrderIndexAsc(course);
        List<Map<String, Object>> lessonDtos = new ArrayList<>();
        for (Lesson l : lessons) {
            boolean hasQuiz = quizRepository.findByLesson(l).isPresent();
            Map<String, Object> dto = new HashMap<>();
            dto.put("lessonId", l.getId());
            dto.put("title", l.getTitle());
            dto.put("type", l.getContentType().name());
            dto.put("url", l.getContentUrl());
            dto.put("orderIndex", l.getOrderIndex());
            dto.put("hasQuiz", hasQuiz);
            lessonDtos.add(dto);
        }
        return Map.of(
                "courseId", course.getId(),
                "title", course.getTitle(),
                "description", course.getDescription(),
                "lessons", lessonDtos
        );
    }

    @Override
    @Transactional
    public void startLesson(Long lessonId, Integer lastPositionSeconds) {
        User u = currentUser();
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow();
        UserLessonProgress p = userLessonProgressRepository.findByUserAndLesson(u, lesson)
                .orElse(UserLessonProgress.builder().user(u).lesson(lesson).build());
        if (p.getStartedAt() == null) p.setStartedAt(LocalDateTime.now());
        if (lastPositionSeconds != null) p.setLastPositionSeconds(lastPositionSeconds);
        userLessonProgressRepository.save(p);
        ensureStageProgressStarted(u, lesson.getCourse().getStage());
    }

    @Override
    @Transactional
    public void completeLesson(Long lessonId, String evidenceUrl) {
        User u = currentUser();
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow();
        UserLessonProgress p = userLessonProgressRepository.findByUserAndLesson(u, lesson)
                .orElse(UserLessonProgress.builder().user(u).lesson(lesson).build());
        if (p.getStartedAt() == null) p.setStartedAt(LocalDateTime.now());
        p.setCompletedAt(LocalDateTime.now());
        p.setEvidenceUrl(evidenceUrl);
        userLessonProgressRepository.save(p);
        tryCompleteStageIfEligible(u, lesson.getCourse().getStage());
    }

    @Override
    @Transactional
    public Map<String, Object> submitQuiz(Long lessonId, List<AnswerItem> answers) {
        User u = currentUser();
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow();
        Quiz quiz = quizRepository.findByLesson(lesson).orElseThrow();
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizOrderByOrderIndexAsc(quiz);
        Map<Long, Set<Long>> answerMap = new HashMap<>();
        if (answers != null) {
            for (AnswerItem a : answers) {
                answerMap.put(a.questionId, a.optionIds == null ? Set.of() : new HashSet<>(a.optionIds));
            }
        }
        int total = questions.size();
        int correctCount = 0;
        for (QuizQuestion q : questions) {
            List<QuizOption> opts = quizOptionRepository.findByQuestionOrderByOrderIndexAsc(q);
            Set<Long> correct = new HashSet<>();
            for (QuizOption o : opts) if (o.isCorrect()) correct.add(o.getId());
            Set<Long> given = answerMap.getOrDefault(q.getId(), Set.of());
            if (given.equals(correct)) correctCount++;
        }
        int score = total == 0 ? 100 : (int)Math.round((correctCount * 100.0) / total);

        UserLessonProgress p = userLessonProgressRepository.findByUserAndLesson(u, lesson)
                .orElse(UserLessonProgress.builder().user(u).lesson(lesson).build());
        if (p.getStartedAt() == null) p.setStartedAt(LocalDateTime.now());
        p.setAttempts(p.getAttempts() == null ? 1 : p.getAttempts() + 1);
        p.setScore(score);
        if (score >= quiz.getPassScore()) {
            p.setCompletedAt(LocalDateTime.now());
        }
        userLessonProgressRepository.save(p);
        tryCompleteStageIfEligible(u, lesson.getCourse().getStage());
        return Map.of("score", score, "passed", score >= quiz.getPassScore());
    }

    private void ensureStageProgressStarted(User user, Stage stage) {
        UserStageProgress usp = userStageProgressRepository.findByUserAndStage(user, stage)
                .orElse(UserStageProgress.builder().user(user).stage(stage).build());
        if (usp.getStatus() == null || usp.getStatus() == ProgressStatus.NOT_STARTED) {
            usp.setStatus(ProgressStatus.IN_PROGRESS);
            if (usp.getStartedAt() == null) usp.setStartedAt(LocalDateTime.now());
            userStageProgressRepository.save(usp);
        }
    }

    private void tryCompleteStageIfEligible(User user, Stage stage) {
        Course course = courseRepository.findByStage(stage).orElse(null);
        if (course == null) return;
        List<Lesson> lessons = lessonRepository.findByCourseOrderByOrderIndexAsc(course);
        for (Lesson l : lessons) {
            UserLessonProgress p = userLessonProgressRepository.findByUserAndLesson(user, l).orElse(null);
            boolean ok = p != null && p.getCompletedAt() != null;
            if (!ok) return; // some lesson not completed
        }
        UserStageProgress usp = userStageProgressRepository.findByUserAndStage(user, stage)
                .orElse(UserStageProgress.builder().user(user).stage(stage).build());
        usp.setStatus(ProgressStatus.COMPLETED);
        usp.setCompletedAt(LocalDateTime.now());
        userStageProgressRepository.save(usp);
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email).orElseThrow();
    }
}
