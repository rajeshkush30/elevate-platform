package com.elevate.consultingplatform.controller.client;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/client")
@PreAuthorize("hasRole('CLIENT')")
public class ClientStrategyController {

    record OptionDto(long id, String label) {}
    record QuestionDto(long id, String text, String type, List<OptionDto> options) {}
    record FormDto(String version, List<QuestionDto> questions) {}

    @GetMapping("/strategy/form")
    public ResponseEntity<FormDto> getForm() {
        // Mock minimal schema
        List<QuestionDto> questions = List.of(
                new QuestionDto(301, "Top 3 goals", "TEXT", List.of()),
                new QuestionDto(302, "Capital focus areas", "MCQ_MULTI", List.of(
                        new OptionDto(1, "Runway"), new OptionDto(2, "Debt"), new OptionDto(3, "Equity")
                ))
        );
        return ResponseEntity.ok(new FormDto("v1", questions));
    }

    record AnswerItem(long questionId, String answerText, List<Long> optionIds) {}
    record SubmitPayload(List<AnswerItem> answers) {}

    @PostMapping("/strategy/submit")
    public ResponseEntity<Map<String, Object>> submit(@RequestBody SubmitPayload payload) {
        // TODO: persist payload to DB
        return ResponseEntity.ok(Map.of("status", "OK"));
    }

    @GetMapping("/consultation/final")
    public ResponseEntity<Map<String, Object>> finalConsultation() {
        // Mock readiness
        return ResponseEntity.ok(Map.of(
                "status", "PENDING",
                "approved", false
        ));
    }
}
