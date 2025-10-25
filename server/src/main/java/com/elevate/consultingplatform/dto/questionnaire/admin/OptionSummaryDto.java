package com.elevate.consultingplatform.dto.questionnaire.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionSummaryDto {
    private Long id;
    private Long questionId;
    private String label;
    private String value;
    private Integer order;
}
