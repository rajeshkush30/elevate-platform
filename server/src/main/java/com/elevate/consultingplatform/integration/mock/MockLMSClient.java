package com.elevate.consultingplatform.integration.mock;

import com.elevate.consultingplatform.integration.LMSClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@Profile({"mock", "default"})
public class MockLMSClient implements LMSClient {
    @Override
    public AssignResponse assignModules(AssignRequest req) {
        return new AssignResponse(req.modules().stream()
                .map(m -> new Enrollment(m.externalId(), "ASSIGNED", "ENR-" + m.externalId()))
                .toList());
    }

    @Override
    public StatusResponse getStatus(Long clientId) {
        return new StatusResponse(List.of(
                Map.of("externalId", "LMS_MOD_101", "status", "IN_PROGRESS", "completion", 55)
        ));
    }
}
