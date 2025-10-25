package com.elevate.consultingplatform.dto.questionnaire;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionResponse {
    private Long submissionId;
    private String stage;
    private Integer score;
    private String summary;
}
