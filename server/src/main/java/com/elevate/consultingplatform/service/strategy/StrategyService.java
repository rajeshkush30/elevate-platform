package com.elevate.consultingplatform.service.strategy;

import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.entity.strategy.StrategySubmission;

public interface StrategyService {
    StrategySubmission submit(User user, String payloadJson, Long clientAssessmentId);
    StrategySubmission latestFor(User user);
}
