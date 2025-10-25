package com.elevate.consultingplatform.entity.assessment;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "assessment_answer_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AssessmentAnswerOption {

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Id implements Serializable {
        @Column(name = "answer_id")
        private Long answerId;
        @Column(name = "option_id")
        private Long optionId;
    }

    @EmbeddedId
    private Id id;

    @ManyToOne(optional = false)
    @MapsId("answerId")
    @JoinColumn(name = "answer_id", nullable = false)
    private AssessmentAnswer answer;

    @ManyToOne(optional = false)
    @MapsId("optionId")
    @JoinColumn(name = "option_id", nullable = false)
    private QuestionOption option;
}
