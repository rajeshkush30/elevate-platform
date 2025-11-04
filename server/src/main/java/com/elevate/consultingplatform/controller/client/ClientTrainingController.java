package com.elevate.consultingplatform.controller.client;

import com.elevate.consultingplatform.dto.catalog.ModuleTreeResponse;
import com.elevate.consultingplatform.dto.training.StageCompleteRequest;
import com.elevate.consultingplatform.dto.training.StageStartResponse;
import com.elevate.consultingplatform.service.training.ProgressService;
import com.elevate.consultingplatform.entity.lms.*;
import com.elevate.consultingplatform.repository.lms.*;
import com.elevate.consultingplatform.service.lms.CourseService;
import com.elevate.consultingplatform.service.billing.TrainingEntitlementService;
import com.elevate.consultingplatform.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/client/training", "/api/v1/client/training"})
@RequiredArgsConstructor
@Tag(name = "Client - Training", description = "Client training assignments and progress")
public class ClientTrainingController {

    private final ProgressService progressService;
    private final TrainingEntitlementService entitlementService;
    private final UserRepository userRepository;
    private final CourseService courseService;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizOptionRepository quizOptionRepository;

    @GetMapping("/assigned")
    @Operation(summary = "Get assigned training tree for current user")
    public ResponseEntity<List<ModuleTreeResponse>> getAssigned() {
        return ResponseEntity.ok(progressService.getAssignedTreeForCurrentUser());
    }

    @PostMapping("/stage/{stageId}/start")
    @Operation(summary = "Start a stage and return optional LMS launch URL")
    public ResponseEntity<StageStartResponse> startStage(@PathVariable Long stageId) {
        // Resolve a simple scope key from stageId (for MVP we use stageId as string)
        String scopeKey = String.valueOf(stageId);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        var user = userRepository.findByEmail(email).orElseThrow();

        var entitlement = entitlementService.findActive(user, scopeKey);
        if (entitlement.isEmpty()) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).build();
        }

        StageStartResponse res = progressService.startStage(stageId);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/stage/{stageId}/complete")
    @Operation(summary = "Complete a stage for current user")
    public ResponseEntity<Void> completeStage(@PathVariable Long stageId,
                                              @RequestBody StageCompleteRequest req) {
        progressService.completeStage(stageId, req);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/status")
    @Operation(summary = "Get compact training status for dashboard")
    public ResponseEntity<Map<String, Object>> getStatus() {
        // Minimal derivation from assigned tree for now (status not tracked per module here)
        List<ModuleTreeResponse> tree = progressService.getAssignedTreeForCurrentUser();
        List<Map<String, Object>> modules = new java.util.ArrayList<>();
        for (ModuleTreeResponse m : tree) {
            for (ModuleTreeResponse.SegmentNode seg : m.getSegments()) {
                for (ModuleTreeResponse.StageNode st : seg.getStages()) {
                    modules.add(Map.of(
                            "name", st.getName(),
                            "externalId", st.getLmsCourseId(),
                            "status", "ASSIGNED",
                            "completion", 0
                    ));
                }
            }
        }
        return ResponseEntity.ok(Map.of("modules", modules));
    }

    // ----- LMS-lite endpoints (course/lesson/quiz) -----

    @GetMapping("/course/{stageId}")
    @Operation(summary = "Get course definition for a stage (lessons list)")
    public ResponseEntity<Map<String, Object>> getCourse(@PathVariable Long stageId) {
        return ResponseEntity.ok(courseService.getCourseForStage(stageId));
    }

    public record StartLessonBody(Integer lastPositionSeconds) {}

    @PostMapping("/lesson/{lessonId}/start")
    @Operation(summary = "Mark lesson started and optionally set last position")
    public ResponseEntity<Void> startLesson(@PathVariable Long lessonId, @RequestBody(required = false) StartLessonBody body) {
        courseService.startLesson(lessonId, body == null ? null : body.lastPositionSeconds());
        return ResponseEntity.ok().build();
    }

    public record CompleteLessonBody(String evidenceUrl) {}

    @PostMapping("/lesson/{lessonId}/complete")
    @Operation(summary = "Mark lesson completed and attach evidence URL if any")
    public ResponseEntity<Void> completeLesson(@PathVariable Long lessonId, @RequestBody(required = false) CompleteLessonBody body) {
        courseService.completeLesson(lessonId, body == null ? null : body.evidenceUrl());
        return ResponseEntity.accepted().build();
    }

    public record QuizAnswer(Long questionId, java.util.List<Long> optionIds) {}
    public record SubmitQuizBody(java.util.List<QuizAnswer> answers) {}

    @PostMapping("/lesson/{lessonId}/quiz/submit")
    @Operation(summary = "Submit quiz answers for a lesson and get score/pass status")
    public ResponseEntity<Map<String, Object>> submitQuiz(@PathVariable Long lessonId, @RequestBody SubmitQuizBody body) {
        java.util.List<CourseService.AnswerItem> items = new java.util.ArrayList<>();
        if (body != null && body.answers() != null) {
            for (QuizAnswer qa : body.answers()) {
                CourseService.AnswerItem it = new CourseService.AnswerItem();
                it.questionId = qa.questionId();
                it.optionIds = qa.optionIds();
                items.add(it);
            }
        }
        return ResponseEntity.ok(courseService.submitQuiz(lessonId, items));
    }

    @GetMapping("/lesson/{lessonId}/quiz")
    @Operation(summary = "Get quiz with questions and options for a lesson (client view)")
    public ResponseEntity<Map<String, Object>> getQuiz(@PathVariable Long lessonId) {
        Lesson l = lessonRepository.findById(lessonId).orElseThrow();
        java.util.Optional<Quiz> quizOpt = quizRepository.findByLesson(l);
        if (quizOpt.isEmpty()) {
            return ResponseEntity.ok(java.util.Map.of("quizId", null, "passScore", null, "questions", java.util.List.of()));
        }
        Quiz q = quizOpt.get();
        java.util.List<java.util.Map<String, Object>> questions = new java.util.ArrayList<>();
        for (QuizQuestion qq : quizQuestionRepository.findByQuizOrderByOrderIndexAsc(q)) {
            java.util.List<java.util.Map<String, Object>> options = new java.util.ArrayList<>();
            for (QuizOption op : quizOptionRepository.findByQuestionOrderByOrderIndexAsc(qq)) {
                options.add(java.util.Map.of(
                        "id", op.getId(),
                        "text", op.getText(),
                        "orderIndex", op.getOrderIndex()
                ));
            }
            questions.add(java.util.Map.of(
                    "id", qq.getId(),
                    "text", qq.getText(),
                    "type", qq.getType().name(),
                    "orderIndex", qq.getOrderIndex(),
                    "options", options
            ));
        }
        return ResponseEntity.ok(java.util.Map.of(
                "quizId", q.getId(),
                "passScore", q.getPassScore(),
                "questions", questions
        ));
    }
}
