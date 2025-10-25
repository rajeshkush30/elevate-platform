package com.elevate.consultingplatform.dto.assessment;

import lombok.Data;

@Data
public class UpdateStageRuleRequest {
    private Long questionnaireId; // optional, null = global
    private Double minScore;
    private Double maxScore;
    private Long targetStageId;
    private Integer priority;
}
