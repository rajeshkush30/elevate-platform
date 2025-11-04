package com.elevate.consultingplatform.service.ai;

import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.entity.strategy.ConsultationDraft;

import java.util.Map;

public interface ConsultationService {
    ConsultationDraft generateIfAbsent(User user);
    Map<String, Object> status(User user);
    ConsultationDraft approve(Long draftId);
}
