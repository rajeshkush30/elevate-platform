package com.elevate.consultingplatform.dto.questionnaire.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SegmentUpsertRequest {
    private String name;
    private Integer order;
}
