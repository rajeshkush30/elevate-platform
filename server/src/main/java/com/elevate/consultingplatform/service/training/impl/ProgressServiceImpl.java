package com.elevate.consultingplatform.service.training.impl;

import com.elevate.consultingplatform.dto.catalog.ModuleTreeResponse;
import com.elevate.consultingplatform.dto.training.StageCompleteRequest;
import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.entity.catalog.Module;
import com.elevate.consultingplatform.entity.catalog.Stage;
import com.elevate.consultingplatform.entity.training.ProgressStatus;
import com.elevate.consultingplatform.entity.training.UserStageProgress;
import com.elevate.consultingplatform.mapper.CatalogTreeMapper;
import com.elevate.consultingplatform.repository.UserRepository;
import com.elevate.consultingplatform.repository.catalog.StageRepository;
import com.elevate.consultingplatform.repository.training.UserModuleAssignmentRepository;
import com.elevate.consultingplatform.repository.training.UserStageProgressRepository;
import com.elevate.consultingplatform.service.training.ProgressService;
import com.elevate.consultingplatform.service.lms.LmsGateway;
import com.elevate.consultingplatform.dto.training.StageStartResponse;
import com.elevate.consultingplatform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private final UserRepository userRepository;
    private final UserModuleAssignmentRepository assignmentRepository;
    private final UserStageProgressRepository progressRepository;
    private final StageRepository stageRepository;
    private final CatalogTreeMapper catalogTreeMapper;
    private final LmsGateway lmsGateway;

    @Override
    @Transactional(readOnly = true)
    public List<ModuleTreeResponse> getAssignedTreeForCurrentUser() {
        User u = currentUser();
        var assignments = assignmentRepository.findByUser(u);
        List<Module> modules = assignments.stream().map(a -> a.getModule()).collect(Collectors.toList());
        return modules.stream().map(catalogTreeMapper::toTree).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StageStartResponse startStage(Long stageId) {
        User u = currentUser();
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Stage not found"));
        UserStageProgress p = progressRepository.findByUserAndStage(u, stage)
                .orElse(UserStageProgress.builder().user(u).stage(stage).build());
        p.setStatus(ProgressStatus.IN_PROGRESS);
        if (p.getStartedAt() == null) {
            p.setStartedAt(LocalDateTime.now());
        }
        progressRepository.save(p);

        // LMS hook for TRAINING stages
        if (stage.getType() == com.elevate.consultingplatform.entity.catalog.StageType.TRAINING) {
            lmsGateway.enroll(u, stage);
            String url = lmsGateway.launchUrl(u, stage);
            return StageStartResponse.builder().launchUrl(url).build();
        }
        return StageStartResponse.builder().launchUrl(null).build();
    }

    @Override
    @Transactional
    public void completeStage(Long stageId, StageCompleteRequest req) {
        User u = currentUser();
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Stage not found"));
        UserStageProgress p = progressRepository.findByUserAndStage(u, stage)
                .orElse(UserStageProgress.builder().user(u).stage(stage).build());
        if (p.getStartedAt() == null) {
            p.setStartedAt(LocalDateTime.now());
        }
        p.setStatus(ProgressStatus.COMPLETED);
        p.setCompletedAt(LocalDateTime.now());
        p.setScore(req.getScore());
        p.setEvidenceUrl(req.getEvidenceUrl());
        progressRepository.save(p);
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalStateException("User not found: " + email));
    }
}
