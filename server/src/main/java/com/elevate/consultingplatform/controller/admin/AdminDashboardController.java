package com.elevate.consultingplatform.controller.admin;

import com.elevate.consultingplatform.dto.admin.DashboardSummaryResponse;
import com.elevate.consultingplatform.entity.Role;
import com.elevate.consultingplatform.repository.UserRepository;
import com.elevate.consultingplatform.repository.assessment.AssessmentAttemptRepository;
import com.elevate.consultingplatform.repository.assessment.QuestionnaireRepository;
import com.elevate.consultingplatform.repository.training.UserModuleAssignmentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@Tag(name = "Admin - Dashboard", description = "Admin dashboard summary and metrics")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final QuestionnaireRepository questionnaireRepository;
    private final UserModuleAssignmentRepository userModuleAssignmentRepository;
    private final AssessmentAttemptRepository assessmentAttemptRepository;

    @GetMapping("/summary")
    @Operation(summary = "Get admin dashboard summary (stub)")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        long totalClients = userRepository.countByRole(Role.CLIENT);
        long activeClients = userRepository.countByRoleAndIsActive(Role.CLIENT, true);
        long totalQuestionnaires = questionnaireRepository.count();
        long totalAssignments = userModuleAssignmentRepository.count();
        long pendingAssessments = assessmentAttemptRepository.countByCompletedAtIsNull();
        long completedAssessments = assessmentAttemptRepository.countByCompletedAtIsNotNull();

        DashboardSummaryResponse resp = DashboardSummaryResponse.builder()
                .totalClients(totalClients)
                .activeClients(activeClients)
                .totalQuestionnaires(totalQuestionnaires)
                .totalAssignments(totalAssignments)
                .pendingAssessments(pendingAssessments)
                .completedAssessments(completedAssessments)
                .build();
        return ResponseEntity.ok(resp);
    }
}
