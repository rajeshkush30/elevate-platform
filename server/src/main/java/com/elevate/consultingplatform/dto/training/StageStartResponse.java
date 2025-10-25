package com.elevate.consultingplatform.dto.training;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StageStartResponse {
    private String launchUrl; // optional LMS URL for TRAINING stages
}
