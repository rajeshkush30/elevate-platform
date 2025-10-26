package com.elevate.consultingplatform.entity.assessment;

import com.elevate.consultingplatform.common.BaseEntity;
import com.elevate.consultingplatform.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "client_assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ClientAssessment extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonIgnore // do not serialize client details in client responses
    private User client;

    @ManyToOne(optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssessmentStatus status = AssessmentStatus.ASSIGNED;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "score", precision = 10, scale = 2)
    private BigDecimal score;
}
