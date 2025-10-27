package com.elevate.consultingplatform.service.billing;

import com.elevate.consultingplatform.entity.TrainingEntitlement;
import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.repository.TrainingEntitlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrainingEntitlementService {

    private final TrainingEntitlementRepository entitlementRepository;

    public Optional<TrainingEntitlement> findActive(User user, String scopeKey) {
        return entitlementRepository.findActiveByUserAndScope(user, scopeKey);
    }

    public TrainingEntitlement grant(User user, String scopeKey, String source, String orderRef) {
        var existing = entitlementRepository.findActiveByUserAndScope(user, scopeKey);
        if (existing.isPresent()) return existing.get();
        TrainingEntitlement e = TrainingEntitlement.builder()
                .user(user)
                .scopeKey(scopeKey)
                .source(source)
                .orderRef(orderRef)
                .active(true)
                .build();
        return entitlementRepository.save(e);
    }
}
