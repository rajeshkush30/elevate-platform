package com.elevate.consultingplatform.entity.strategy;

import com.elevate.consultingplatform.common.BaseEntity;
import com.elevate.consultingplatform.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "strategy_submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class StrategySubmission extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Optional link to the latest client assessment (if applicable)
    @Column(name = "client_assessment_id")
    private Long clientAssessmentId;

    // Raw JSON payload of answers submitted from client
    @Lob
    @Column(name = "payload", columnDefinition = "LONGTEXT")
    private String payload;
}
