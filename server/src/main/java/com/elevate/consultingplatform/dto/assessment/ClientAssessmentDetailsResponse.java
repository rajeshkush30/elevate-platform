package com.elevate.consultingplatform.dto.assessment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientAssessmentDetailsResponse {
    private Long clientAssessmentId;
    private String assessmentName;
    private List<QuestionDto> questions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionDto {
        private Long id;
        private String text;
        private String type;
        private Double weight;
        private List<OptionDto> options;
        private ExistingAnswer existingAnswer;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OptionDto {
        private Long id;
        private String text;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExistingAnswer {
        private String answerText;
        private List<Long> optionIds;
    }
}
