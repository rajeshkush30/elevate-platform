package com.elevate.consultingplatform.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private long totalClients;
    private long activeClients;
    private long totalQuestionnaires;
    private long totalAssignments;
    private long pendingAssessments;
    private long completedAssessments;
}
