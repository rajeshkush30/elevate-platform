package com.elevate.consultingplatform.service.strategy.impl;

import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.entity.strategy.StrategySubmission;
import com.elevate.consultingplatform.repository.UserRepository;
import com.elevate.consultingplatform.repository.strategy.StrategySubmissionRepository;
import com.elevate.consultingplatform.service.strategy.StrategyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StrategyServiceImpl implements StrategyService {

    private final StrategySubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public StrategySubmission submit(User user, String payloadJson, Long clientAssessmentId) {
        // Ensure we have a managed/persisted user entity
        User persisted = user;
        if (user.getId() == null && user.getEmail() != null) {
            persisted = userRepository.findByEmail(user.getEmail()).orElseThrow();
        }
        StrategySubmission sub = StrategySubmission.builder()
                .user(persisted)
                .payload(payloadJson)
                .clientAssessmentId(clientAssessmentId)
                .build();
        return submissionRepository.save(sub);
    }

    @Override
    @Transactional(readOnly = true)
    public StrategySubmission latestFor(User user) {
        User persisted = user;
        if (user.getId() == null && user.getEmail() != null) {
            persisted = userRepository.findByEmail(user.getEmail()).orElseThrow();
        }
        return submissionRepository.findFirstByUserOrderByIdDesc(persisted).orElse(null);
    }
}
