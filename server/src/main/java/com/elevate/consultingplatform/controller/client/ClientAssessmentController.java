package com.elevate.consultingplatform.controller.client;

import com.elevate.consultingplatform.dto.assessment.SaveAnswersRequest;
import com.elevate.consultingplatform.security.UserDetailsImpl;
import com.elevate.consultingplatform.entity.assessment.ClientAssessment;
import com.elevate.consultingplatform.service.assessment.AssessmentClientService;
import com.elevate.consultingplatform.dto.assessment.ClientAssessmentDetailsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/client/assessments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ClientAssessmentController {

    private final AssessmentClientService assessmentClientService;

    private Long currentUserId(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetailsImpl u) {
            return u.getId();
        }
        // Fallback not expected; reject if missing
        throw new IllegalStateException("Authenticated user not resolved");
    }

    @GetMapping
    public ResponseEntity<List<ClientAssessment>> myAssessments(Authentication auth) {
        Long userId = currentUserId(auth);
        return ResponseEntity.ok(assessmentClientService.listMyAssignments(userId));
    }

    @GetMapping("/{clientAssessmentId}")
    public ResponseEntity<ClientAssessment> getForFill(@PathVariable Long clientAssessmentId, Authentication auth) {
        Long userId = currentUserId(auth);
        return ResponseEntity.ok(assessmentClientService.getForFill(clientAssessmentId, userId));
    }

    @GetMapping("/{clientAssessmentId}/details")
    public ResponseEntity<ClientAssessmentDetailsResponse> getDetails(@PathVariable Long clientAssessmentId, Authentication auth) {
        Long userId = currentUserId(auth);
        return ResponseEntity.ok(assessmentClientService.getDetails(clientAssessmentId, userId));
    }

    @PostMapping("/{clientAssessmentId}/answers")
    public ResponseEntity<Void> saveAnswers(@PathVariable Long clientAssessmentId,
                                            @RequestBody SaveAnswersRequest request,
                                            Authentication auth) {
        Long userId = currentUserId(auth);
        List<AssessmentClientService.AnswerItem> items = request.getAnswers() == null ? List.of() :
                request.getAnswers().stream().map(a -> {
                    AssessmentClientService.AnswerItem i = new AssessmentClientService.AnswerItem();
                    i.questionId = a.getQuestionId();
                    i.answerText = a.getAnswerText();
                    i.optionIds = a.getOptionIds();
                    return i;
                }).collect(Collectors.toList());
        assessmentClientService.saveAnswers(clientAssessmentId, userId, items, request.isSubmit());
        return ResponseEntity.ok().build();
    }
}
