package com.elevate.consultingplatform.controller.admin;

import com.elevate.consultingplatform.entity.catalog.Stage;
import com.elevate.consultingplatform.entity.lms.*;
import com.elevate.consultingplatform.repository.catalog.StageRepository;
import com.elevate.consultingplatform.repository.lms.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/admin/lms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - LMS", description = "Manage LMS-lite courses, lessons, quizzes")
public class AdminLmsController {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizOptionRepository quizOptionRepository;
    private final StageRepository stageRepository;

    // -------- Courses --------
    @Data
    public static class UpsertCourse {
        private Long stageId;
        private String title;
        private String description;
    }

    @PostMapping("/courses")
    @Operation(summary = "Create a course for a stage")
    public ResponseEntity<Map<String, Object>> createCourse(@RequestBody UpsertCourse body) {
        Stage stage = stageRepository.findById(body.getStageId()).orElseThrow();
        Course c = Course.builder().stage(stage).title(body.getTitle()).description(body.getDescription()).build();
        c = courseRepository.save(c);
        return ResponseEntity.ok(Map.of("courseId", c.getId()));
    }

    @PutMapping("/courses/{courseId}")
    @Operation(summary = "Update a course")
    public ResponseEntity<Void> updateCourse(@PathVariable Long courseId, @RequestBody UpsertCourse body) {
        Course c = courseRepository.findById(courseId).orElseThrow();
        if (body.getTitle() != null) c.setTitle(body.getTitle());
        if (body.getDescription() != null) c.setDescription(body.getDescription());
        if (body.getStageId() != null) c.setStage(stageRepository.findById(body.getStageId()).orElseThrow());
        courseRepository.save(c);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/courses")
    @Operation(summary = "List courses, optionally filter by stageId")
    public ResponseEntity<List<Map<String, Object>>> listCourses(@RequestParam(required = false) Long stageId) {
        List<Course> list;
        if (stageId != null) {
            Stage stage = stageRepository.findById(stageId).orElseThrow();
            list = courseRepository.findByStage(stage).map(List::of).orElse(List.of());
        } else {
            list = courseRepository.findAll();
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Course c : list) {
            out.add(Map.of(
                    "courseId", c.getId(),
                    "stageId", c.getStage().getId(),
                    "title", c.getTitle(),
                    "description", c.getDescription()
            ));
        }
        return ResponseEntity.ok(out);
    }

    @DeleteMapping("/courses/{courseId}")
    @Operation(summary = "Delete a course (and cascade lessons/quiz)")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        courseRepository.deleteById(courseId);
        return ResponseEntity.noContent().build();
    }

    // -------- Lessons --------
    @Data
    public static class UpsertLesson {
        private String title;
        private LessonContentType contentType;
        private String contentUrl;
        private Integer orderIndex;
        private Integer durationSeconds;
    }

    @PostMapping("/courses/{courseId}/lessons")
    @Operation(summary = "Create lesson for a course")
    public ResponseEntity<Map<String, Object>> createLesson(@PathVariable Long courseId, @RequestBody UpsertLesson body) {
        Course c = courseRepository.findById(courseId).orElseThrow();
        Lesson l = Lesson.builder()
                .course(c)
                .title(body.getTitle())
                .contentType(body.getContentType())
                .contentUrl(body.getContentUrl())
                .orderIndex(body.getOrderIndex() == null ? 0 : body.getOrderIndex())
                .durationSeconds(body.getDurationSeconds())
                .build();
        l = lessonRepository.save(l);
        return ResponseEntity.ok(Map.of("lessonId", l.getId()));
    }

    @PutMapping("/lessons/{lessonId}")
    @Operation(summary = "Update a lesson")
    public ResponseEntity<Void> updateLesson(@PathVariable Long lessonId, @RequestBody UpsertLesson body) {
        Lesson l = lessonRepository.findById(lessonId).orElseThrow();
        if (body.getTitle() != null) l.setTitle(body.getTitle());
        if (body.getContentType() != null) l.setContentType(body.getContentType());
        if (body.getContentUrl() != null) l.setContentUrl(body.getContentUrl());
        if (body.getOrderIndex() != null) l.setOrderIndex(body.getOrderIndex());
        if (body.getDurationSeconds() != null) l.setDurationSeconds(body.getDurationSeconds());
        lessonRepository.save(l);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/lessons/{lessonId}")
    @Operation(summary = "Delete a lesson")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long lessonId) {
        lessonRepository.deleteById(lessonId);
        return ResponseEntity.noContent().build();
    }

    // -------- Quiz / Questions / Options --------
    @Data
    public static class CreateQuiz { private Integer passScore; }

    @PostMapping("/lessons/{lessonId}/quiz")
    @Operation(summary = "Create or replace a quiz for a lesson")
    public ResponseEntity<Map<String, Object>> createQuiz(@PathVariable Long lessonId, @RequestBody CreateQuiz body) {
        Lesson l = lessonRepository.findById(lessonId).orElseThrow();
        // if exists, keep; otherwise create
        Quiz q = quizRepository.findByLesson(l).orElseGet(() -> quizRepository.save(Quiz.builder().lesson(l).passScore(body.getPassScore() == null ? 70 : body.getPassScore()).build()));
        if (body.getPassScore() != null) { q.setPassScore(body.getPassScore()); q = quizRepository.save(q); }
        return ResponseEntity.ok(Map.of("quizId", q.getId()));
    }

    @Data
    public static class CreateQuestion { private String text; private QuizQuestionType type; private Integer orderIndex; }

    @PostMapping("/quiz/{quizId}/questions")
    @Operation(summary = "Add question to quiz")
    public ResponseEntity<Map<String, Object>> addQuestion(@PathVariable Long quizId, @RequestBody CreateQuestion body) {
        Quiz q = quizRepository.findById(quizId).orElseThrow();
        QuizQuestion qq = QuizQuestion.builder()
                .quiz(q)
                .text(body.getText())
                .type(body.getType())
                .orderIndex(body.getOrderIndex() == null ? 0 : body.getOrderIndex())
                .build();
        qq = quizQuestionRepository.save(qq);
        return ResponseEntity.ok(Map.of("questionId", qq.getId()));
    }

    @Data
    public static class CreateOption { private String text; private Boolean correct; private Integer orderIndex; }

    @PostMapping("/questions/{questionId}/options")
    @Operation(summary = "Add option to question")
    public ResponseEntity<Map<String, Object>> addOption(@PathVariable Long questionId, @RequestBody CreateOption body) {
        QuizQuestion qq = quizQuestionRepository.findById(questionId).orElseThrow();
        QuizOption opt = QuizOption.builder()
                .question(qq)
                .text(body.getText())
                .correct(Boolean.TRUE.equals(body.getCorrect()))
                .orderIndex(body.getOrderIndex() == null ? 0 : body.getOrderIndex())
                .build();
        opt = quizOptionRepository.save(opt);
        return ResponseEntity.ok(Map.of("optionId", opt.getId()));
    }

    @DeleteMapping("/quiz/{quizId}")
    @Operation(summary = "Delete a quiz and its questions/options")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long quizId) {
        quizRepository.deleteById(quizId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/questions/{questionId}")
    @Operation(summary = "Delete a question and its options")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId) {
        quizQuestionRepository.deleteById(questionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/options/{optionId}")
    @Operation(summary = "Delete an option")
    public ResponseEntity<Void> deleteOption(@PathVariable Long optionId) {
        quizOptionRepository.deleteById(optionId);
        return ResponseEntity.noContent().build();
    }

    // -------- Retrieval and updates for Quiz tree --------
    @GetMapping("/lessons/{lessonId}/quiz")
    @Operation(summary = "Get quiz with questions and options for a lesson")
    public ResponseEntity<Map<String, Object>> getQuizForLesson(@PathVariable Long lessonId) {
        Lesson l = lessonRepository.findById(lessonId).orElseThrow();
        Optional<Quiz> quizOpt = quizRepository.findByLesson(l);
        if (quizOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("quizId", null, "passScore", null, "questions", List.of()));
        }
        Quiz q = quizOpt.get();
        List<Map<String, Object>> questions = new ArrayList<>();
        for (QuizQuestion qq : quizQuestionRepository.findByQuizOrderByOrderIndexAsc(q)) {
            List<Map<String, Object>> options = new ArrayList<>();
            for (QuizOption op : quizOptionRepository.findByQuestionOrderByOrderIndexAsc(qq)) {
                options.add(Map.of(
                        "id", op.getId(),
                        "text", op.getText(),
                        "correct", op.isCorrect(),
                        "orderIndex", op.getOrderIndex()
                ));
            }
            questions.add(Map.of(
                    "id", qq.getId(),
                    "text", qq.getText(),
                    "type", qq.getType().name(),
                    "orderIndex", qq.getOrderIndex(),
                    "options", options
            ));
        }
        return ResponseEntity.ok(Map.of(
                "quizId", q.getId(),
                "passScore", q.getPassScore(),
                "questions", questions
        ));
    }

    @Data
    public static class UpdateQuiz { private Integer passScore; }

    @PutMapping("/quiz/{quizId}")
    @Operation(summary = "Update quiz settings (e.g., pass score)")
    public ResponseEntity<Void> updateQuiz(@PathVariable Long quizId, @RequestBody UpdateQuiz body) {
        Quiz q = quizRepository.findById(quizId).orElseThrow();
        if (body.getPassScore() != null) q.setPassScore(body.getPassScore());
        quizRepository.save(q);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class UpdateQuestion { private String text; private QuizQuestionType type; private Integer orderIndex; }

    @PutMapping("/questions/{questionId}")
    @Operation(summary = "Update question text/type/order")
    public ResponseEntity<Void> updateQuestion(@PathVariable Long questionId, @RequestBody UpdateQuestion body) {
        QuizQuestion qq = quizQuestionRepository.findById(questionId).orElseThrow();
        if (body.getText() != null) qq.setText(body.getText());
        if (body.getType() != null) qq.setType(body.getType());
        if (body.getOrderIndex() != null) qq.setOrderIndex(body.getOrderIndex());
        quizQuestionRepository.save(qq);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class UpdateOption { private String text; private Boolean correct; private Integer orderIndex; }

    @PutMapping("/options/{optionId}")
    @Operation(summary = "Update option text/correct/order")
    public ResponseEntity<Void> updateOption(@PathVariable Long optionId, @RequestBody UpdateOption body) {
        QuizOption op = quizOptionRepository.findById(optionId).orElseThrow();
        if (body.getText() != null) op.setText(body.getText());
        if (body.getCorrect() != null) op.setCorrect(Boolean.TRUE.equals(body.getCorrect()));
        if (body.getOrderIndex() != null) op.setOrderIndex(body.getOrderIndex());
        quizOptionRepository.save(op);
        return ResponseEntity.ok().build();
    }
}
