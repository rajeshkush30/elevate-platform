package com.elevate.consultingplatform.dto.questionnaire.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionSummaryDto {
    private Long id;
    private Long segmentId;
    private String text;
    private Integer weight;
    private Integer order;
}
