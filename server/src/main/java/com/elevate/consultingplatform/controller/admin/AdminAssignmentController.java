package com.elevate.consultingplatform.controller.admin;

import com.elevate.consultingplatform.dto.training.CreateAssignmentRequest;
import com.elevate.consultingplatform.service.training.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminAssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping("/assignments")
    public ResponseEntity<Void> createAssignment(@RequestBody CreateAssignmentRequest req) {
        Long id = assignmentService.createAssignment(req);
        return ResponseEntity.created(URI.create("/api/v1/admin/assignments/" + id)).build();
    }
}
