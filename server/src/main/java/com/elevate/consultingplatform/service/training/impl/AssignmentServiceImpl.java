package com.elevate.consultingplatform.service.training.impl;

import com.elevate.consultingplatform.dto.training.CreateAssignmentRequest;
import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.entity.catalog.Module;
import com.elevate.consultingplatform.entity.training.AssignmentStatus;
import com.elevate.consultingplatform.entity.training.UserModuleAssignment;
import com.elevate.consultingplatform.repository.UserRepository;
import com.elevate.consultingplatform.repository.catalog.ModuleRepository;
import com.elevate.consultingplatform.repository.training.UserModuleAssignmentRepository;
import com.elevate.consultingplatform.service.training.AssignmentService;
import com.elevate.consultingplatform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final UserModuleAssignmentRepository assignmentRepository;

    @Override
    @Transactional
    public Long createAssignment(CreateAssignmentRequest req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Module module = moduleRepository.findById(req.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        var existing = assignmentRepository.findByUserAndModule(user, module);
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        UserModuleAssignment a = UserModuleAssignment.builder()
                .user(user)
                .module(module)
                .status(AssignmentStatus.ASSIGNED)
                .assignedAt(LocalDateTime.now())
                .dueAt(req.getDueAt())
                .build();
        return assignmentRepository.save(a).getId();
    }
}
