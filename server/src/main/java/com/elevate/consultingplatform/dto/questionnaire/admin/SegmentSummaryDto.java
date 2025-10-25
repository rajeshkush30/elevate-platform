package com.elevate.consultingplatform.dto.questionnaire.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SegmentSummaryDto {
    private Long id;
    private String name;
    private Integer order;
}
