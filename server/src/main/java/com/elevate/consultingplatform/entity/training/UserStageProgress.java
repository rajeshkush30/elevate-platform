package com.elevate.consultingplatform.entity.training;

import com.elevate.consultingplatform.common.BaseEntity;
import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.entity.catalog.Stage;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_stage_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class UserStageProgress extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProgressStatus status = ProgressStatus.NOT_STARTED;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "score")
    private Double score;

    @Column(name = "evidence_url")
    private String evidenceUrl;

    @Column(name = "last_checkpoint", columnDefinition = "TEXT")
    private String lastCheckpoint; // JSON
}
