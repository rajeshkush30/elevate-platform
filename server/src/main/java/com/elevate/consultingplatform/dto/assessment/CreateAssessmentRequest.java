package com.elevate.consultingplatform.dto.assessment;

import lombok.Data;

@Data
public class CreateAssessmentRequest {
    private Long stageId;
    private String name;
    private String description;
}
