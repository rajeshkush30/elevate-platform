package com.elevate.consultingplatform.controller.client;

import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.service.ai.ConsultationService;
import com.elevate.consultingplatform.service.strategy.StrategyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/client")
@PreAuthorize("hasRole('CLIENT')")
@RequiredArgsConstructor
@Tag(name = "Client - Strategy & Consultation")
public class ClientStrategyController {

    private final StrategyService strategyService;
    private final ConsultationService consultationService;
    private final ObjectMapper mapper = new ObjectMapper();

    @Data
    public static class OptionDto { private long id; private String label; }
    @Data
    public static class QuestionDto { private long id; private String text; private String type; private List<OptionDto> options; }
    @Data
    public static class FormDto { private String version; private List<QuestionDto> questions; }

    @GetMapping("/strategy/form")
    @Operation(summary = "Get strategy form schema (v1)")
    public ResponseEntity<FormDto> getForm() {
        // Minimal static schema for MVP; later can be DB-managed
        List<OptionDto> capOpts = new ArrayList<>();
        capOpts.add(opt(1, "Runway")); capOpts.add(opt(2, "Debt")); capOpts.add(opt(3, "Equity"));
        List<QuestionDto> qs = new ArrayList<>();
        qs.add(q(301, "Top 3 goals", "TEXT", List.of()));
        qs.add(q(302, "Capital focus areas", "MCQ_MULTI", capOpts));
        FormDto f = new FormDto(); f.setVersion("v1"); f.setQuestions(qs);
        return ResponseEntity.ok(f);
    }

    @Data
    public static class AnswerItem { private long questionId; private String answerText; private List<Long> optionIds; }
    @Data
    public static class SubmitPayload { private List<AnswerItem> answers; private Long clientAssessmentId; }

    @PostMapping("/strategy/submit")
    @Operation(summary = "Submit strategy answers (persist as JSON)")
    public ResponseEntity<Map<String, Object>> submit(@RequestBody SubmitPayload payload) {
        User user = currentUser();
        String json = toJson(payload.getAnswers() == null ? List.of() : payload.getAnswers());
        strategyService.submit(user, json, payload.getClientAssessmentId());
        // Optionally trigger generation now to reduce latency later
        consultationService.generateIfAbsent(user);
        return ResponseEntity.ok(Map.of("status", "OK"));
    }

    @GetMapping("/consultation/final")
    @Operation(summary = "Get final consultation status; triggers draft generation if absent")
    public ResponseEntity<Map<String, Object>> finalConsultation() {
        User user = currentUser();
        consultationService.generateIfAbsent(user);
        return ResponseEntity.ok(consultationService.status(user));
    }

    private User currentUser() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        User u = new User();
        u.setEmail(a.getName()); // lightweight identity; services do not rely on id here
        return u;
    }

    private String toJson(Object o) {
        try { return mapper.writeValueAsString(o); } catch (Exception e) { return "[]"; }
    }

    private static OptionDto opt(long id, String label) { OptionDto o = new OptionDto(); o.setId(id); o.setLabel(label); return o; }
    private static QuestionDto q(long id, String text, String type, List<OptionDto> opts) { QuestionDto q = new QuestionDto(); q.setId(id); q.setText(text); q.setType(type); q.setOptions(opts); return q; }
}
