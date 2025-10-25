package com.elevate.consultingplatform.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateModuleRequest {
    @NotBlank
    @Size(max = 255)
    private String name;
    @Size(max = 2000)
    private String description;
    private Integer orderIndex;
    private Boolean isActive = true;
}
