package com.elevate.consultingplatform.entity.assessment;

import com.elevate.consultingplatform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Answer extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private AssessmentAttempt attempt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "value", columnDefinition = "TEXT")
    private String value; // raw answer

    @Column(name = "score")
    private Double score; // pre-scored or computed at finalize
}
