package com.elevate.consultingplatform.dto.assessment;

import lombok.Data;

import java.util.List;

@Data
public class SaveAnswersRequest {
    private boolean submit; // if true, finalize and score
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        private Long questionId;
        private String answerText;      // for TEXT/NUMBER
        private List<Long> optionIds;   // for SINGLE/MULTI
    }
}
