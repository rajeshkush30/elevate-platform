package com.elevate.consultingplatform.controller.admin;

import com.elevate.consultingplatform.dto.assessment.CreateQuestionRequest;
import com.elevate.consultingplatform.dto.assessment.CreateQuestionnaireRequest;
import com.elevate.consultingplatform.entity.assessment.Question;
import com.elevate.consultingplatform.entity.assessment.Questionnaire;
import com.elevate.consultingplatform.repository.assessment.QuestionRepository;
import com.elevate.consultingplatform.repository.assessment.QuestionnaireRepository;
import com.elevate.consultingplatform.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/questionnaire")
@RequiredArgsConstructor
@Tag(name = "Admin - Questionnaire", description = "Manage questionnaires and questions")
public class QuestionnaireAdminController {

    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionRepository questionRepository;

    // List all questionnaires
    @GetMapping
    @Operation(summary = "List questionnaires")
    public ResponseEntity<List<Questionnaire>> listQuestionnaires() {
        return ResponseEntity.ok(questionnaireRepository.findAll());
    }

    // Get single questionnaire
    @GetMapping("/{id}")
    @Operation(summary = "Get questionnaire by id")
    public ResponseEntity<Questionnaire> getQuestionnaire(@PathVariable Long id) {
        var q = questionnaireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Questionnaire not found"));
        return ResponseEntity.ok(q);
    }

    // List questions for a questionnaire
    @GetMapping("/{id}/questions")
    @Operation(summary = "List questions for questionnaire")
    public ResponseEntity<List<Question>> listQuestions(@PathVariable Long id) {
        var q = questionnaireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Questionnaire not found"));
        return ResponseEntity.ok(questionRepository.findByQuestionnaireOrderByIdAsc(q));
    }

    // Create questionnaire
    @PostMapping
    @Operation(summary = "Create questionnaire")
    public ResponseEntity<Long> createQuestionnaire(@Valid @RequestBody CreateQuestionnaireRequest req) {
        Questionnaire q = Questionnaire.builder()
                .name(req.getName())
                .version(req.getVersion())
                .build();
        Long id = questionnaireRepository.save(q).getId();
        return ResponseEntity.created(URI.create("/api/v1/admin/questionnaire/" + id)).body(id);
    }

    // Update questionnaire
    @PutMapping("/{id}")
    @Operation(summary = "Update questionnaire")
    public ResponseEntity<Void> updateQuestionnaire(@PathVariable Long id,
                                                    @Valid @RequestBody CreateQuestionnaireRequest req) {
        Questionnaire q = questionnaireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Questionnaire not found"));
        if (req.getName() != null) q.setName(req.getName());
        if (req.getVersion() != null) q.setVersion(req.getVersion());
        questionnaireRepository.save(q);
        return ResponseEntity.noContent().build();
    }

    // Delete questionnaire
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete questionnaire")
    public ResponseEntity<Void> deleteQuestionnaire(@PathVariable Long id) {
        questionnaireRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Create question
    @PostMapping("/questions")
    @Operation(summary = "Create question")
    public ResponseEntity<Long> createQuestion(@Valid @RequestBody CreateQuestionRequest req) {
        Questionnaire q = questionnaireRepository.findById(req.getQuestionnaireId())
                .orElseThrow(() -> new ResourceNotFoundException("Questionnaire not found"));
        Question question = Question.builder()
                .questionnaire(q)
                .text(req.getText())
                .weight(req.getWeight())
                .type(req.getType())
                .optionsJson(req.getOptionsJson())
                .build();
        if (req.getSegmentId() != null) {
            var seg = com.elevate.consultingplatform.entity.catalog.Segment.builder()
                    .id(req.getSegmentId())
                    .build();
            question.setSegment(seg);
        }
        Long id = questionRepository.save(question).getId();
        return ResponseEntity.created(URI.create("/api/v1/admin/questionnaire/questions/" + id)).body(id);
    }

    // Update question
    @PutMapping("/questions/{id}")
    @Operation(summary = "Update question")
    public ResponseEntity<Void> updateQuestion(@PathVariable Long id,
                                               @Valid @RequestBody CreateQuestionRequest req) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        if (req.getQuestionnaireId() != null) {
            Questionnaire q = questionnaireRepository.findById(req.getQuestionnaireId())
                    .orElseThrow(() -> new ResourceNotFoundException("Questionnaire not found"));
            question.setQuestionnaire(q);
        }
        if (req.getSegmentId() != null) {
            var seg = com.elevate.consultingplatform.entity.catalog.Segment.builder()
                    .id(req.getSegmentId())
                    .build();
            question.setSegment(seg);
        }
        if (req.getText() != null) question.setText(req.getText());
        if (req.getWeight() != null) question.setWeight(req.getWeight());
        if (req.getType() != null) question.setType(req.getType());
        if (req.getOptionsJson() != null) question.setOptionsJson(req.getOptionsJson());
        questionRepository.save(question);
        return ResponseEntity.noContent().build();
    }

    // Delete question
    @DeleteMapping("/questions/{id}")
    @Operation(summary = "Delete question")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
