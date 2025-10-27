package com.elevate.consultingplatform.integration;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface LMSClient {
    record AssignRequest(Long clientId, String stage, List<Module> modules) {}
    record Module(String externalId, LocalDate dueDate) {}
    record AssignResponse(List<Enrollment> enrollments) {}
    record Enrollment(String externalId, String status, String enrollmentId) {}

    record StatusResponse(List<Map<String, Object>> modules) {}

    AssignResponse assignModules(AssignRequest req);
    StatusResponse getStatus(Long clientId);
}
