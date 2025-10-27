package com.elevate.consultingplatform.controller.admin;

import com.elevate.consultingplatform.dto.training.CreateAssignmentRequest;
import com.elevate.consultingplatform.dto.user.UserResponse;
import com.elevate.consultingplatform.integration.CRMClient;
import com.elevate.consultingplatform.service.AdminClientService;
import com.elevate.consultingplatform.service.training.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/leads")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminLeadApprovalController {

    private final CRMClient crmClient;
    private final AdminClientService adminClientService;
    private final AssignmentService assignmentService;

    public record ApproveRequest(String email, String firstName, String lastName, Long initialModuleId) {}
    public record ApproveResponse(String contactId, Long userId, Long assignmentId) {}

    @PostMapping("/{leadId}/approve")
    public ResponseEntity<ApproveResponse> approve(@PathVariable String leadId, @RequestBody ApproveRequest req) {
        // 1) Convert Zoho lead → contact
        var converted = crmClient.convertLead(new CRMClient.ConvertLeadRequest(leadId));

        // 2) Create platform user (sends password reset email via AdminClientService)
        var userReq = new UserResponse();
        userReq.setEmail(req.email());
        userReq.setFirstName(req.firstName());
        userReq.setLastName(req.lastName());
        var created = adminClientService.createClient(userReq);
        Long userId = Long.valueOf(created.getUser().getId());

        // 3) Optionally assign initial module (training) if provided
        Long assignmentId = null;
        if (req.initialModuleId() != null) {
            CreateAssignmentRequest ar = new CreateAssignmentRequest();
            ar.setUserId(userId);
            ar.setModuleId(req.initialModuleId());
            ar.setDueAt(LocalDateTime.now().plusDays(14));
            assignmentId = assignmentService.createAssignment(ar);
        }

        return ResponseEntity.ok(new ApproveResponse(converted.contactId(), userId, assignmentId));
    }

    @PostMapping("/{leadId}/reject")
    public ResponseEntity<Map<String, Object>> reject(@PathVariable String leadId, @RequestBody(required = false) Map<String, Object> body) {
        // Optional: add CRM note or archive lead; for now, acknowledge
        return ResponseEntity.ok(Map.of("status", "REJECTED", "leadId", leadId));
    }
}
