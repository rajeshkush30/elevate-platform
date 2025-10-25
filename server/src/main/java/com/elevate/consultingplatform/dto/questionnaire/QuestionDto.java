package com.elevate.consultingplatform.dto.questionnaire;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionDto {
    private Long id;
    private String text;
    private String[] options;
    private Integer weight;
}
