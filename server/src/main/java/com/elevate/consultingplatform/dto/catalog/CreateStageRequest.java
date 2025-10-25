package com.elevate.consultingplatform.dto.catalog;

import com.elevate.consultingplatform.entity.catalog.StageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateStageRequest {
    @NotNull
    private Long segmentId;
    @NotBlank
    @Size(max = 255)
    private String name;
    @Size(max = 4000)
    private String description;
    private Integer orderIndex;
    private Boolean isActive = true;
    @NotNull
    private StageType type;
    @Size(max = 1000)
    private String contentUrl;
    @Size(max = 255)
    private String lmsCourseId;
    private String assessmentConfig; // JSON
    private String aiPromptTemplate; // TEXT
    private Integer durationMinutes;
    private String metadata; // JSON
}
