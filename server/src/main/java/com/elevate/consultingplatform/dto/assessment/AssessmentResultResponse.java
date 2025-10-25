package com.elevate.consultingplatform.dto.assessment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentResultResponse {
    private Long attemptId;
    private Double totalScore;
    private Long recommendedStageId;
}
