package com.elevate.consultingplatform.controller;

    import com.elevate.consultingplatform.dto.questionnaire.QuestionDto;
    import com.elevate.consultingplatform.dto.questionnaire.SegmentDto;
    import com.elevate.consultingplatform.dto.questionnaire.SubmissionRequest;
    import com.elevate.consultingplatform.dto.questionnaire.SubmissionResponse;
    import com.elevate.consultingplatform.service.QuestionnaireService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    import java.util.List;
    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.tags.Tag;

    @RestController
    @RequestMapping("/api/v1/questionnaire")
    @RequiredArgsConstructor
    @Tag(name = "Questionnaire", description = "Client questionnaire endpoints")
    public class QuestionnaireController {

        private final QuestionnaireService questionnaireService;

        @GetMapping("/questions")
        @Operation(summary = "Get all questionnaire questions")
        public ResponseEntity<List<QuestionDto>> getQuestions() {
            return ResponseEntity.ok(questionnaireService.getAllQuestions());
        }

        @PostMapping("/submit")
        @Operation(summary = "Submit questionnaire answers")
        public ResponseEntity<SubmissionResponse> submit(@RequestBody SubmissionRequest request) {
            return ResponseEntity.ok(questionnaireService.submitAnswers(request));
        }

        @GetMapping("/segments")
        @Operation(summary = "Get questionnaire segments")
        public ResponseEntity<List<SegmentDto>> getSegments() {
            return ResponseEntity.ok(questionnaireService.getSegments());
        }
    }
