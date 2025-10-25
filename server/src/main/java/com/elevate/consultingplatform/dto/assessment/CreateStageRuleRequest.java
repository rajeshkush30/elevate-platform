package com.elevate.consultingplatform.dto.assessment;

import lombok.Data;

@Data
public class CreateStageRuleRequest {
    private Long questionnaireId; // optional, null = global
    private Double minScore;
    private Double maxScore;
    private Long targetStageId;
    private Integer priority;
}
