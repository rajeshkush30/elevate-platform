package com.elevate.consultingplatform.dto.training;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateAssignmentRequest {
    private Long userId;
    private Long moduleId;
    private LocalDateTime dueAt;
}
