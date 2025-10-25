package com.elevate.consultingplatform.dto.questionnaire.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReorderItemDto {
    private Long id;
    private Integer order;
}
