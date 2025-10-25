package com.elevate.consultingplatform.dto.assessment;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AssignAssessmentRequest {
    private List<Long> clientIds;
    private LocalDate dueDate;
}
