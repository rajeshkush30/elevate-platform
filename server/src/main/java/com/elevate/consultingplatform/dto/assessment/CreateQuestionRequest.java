package com.elevate.consultingplatform.dto.assessment;

import lombok.Data;

@Data
public class CreateQuestionRequest {
    private Long questionnaireId;
    private Long segmentId; // optional
    private String text;
    private Double weight; // 0..1 or use consistent scale
    private String type; // SCALE | MCQ | TEXT
    private String optionsJson; // optional JSON config
}
