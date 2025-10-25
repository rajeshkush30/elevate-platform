package com.elevate.consultingplatform.dto.questionnaire;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SegmentDto {
    private Long id;
    private String name;
    private Integer order;
    private List<QuestionDto> questions;
}
