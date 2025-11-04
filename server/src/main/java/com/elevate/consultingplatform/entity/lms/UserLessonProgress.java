package com.elevate.consultingplatform.entity.lms;

import com.elevate.consultingplatform.common.BaseEntity;
import com.elevate.consultingplatform.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_lesson_progress", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_lesson", columnNames = {"user_id","lesson_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserLessonProgress extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "seconds_watched")
    private Integer secondsWatched;

    @Column(name = "last_position_seconds")
    private Integer lastPositionSeconds;

    @Column(name = "attempts")
    private Integer attempts;

    @Column(name = "score")
    private Integer score;

    @Column(name = "evidence_url", columnDefinition = "TEXT")
    private String evidenceUrl;
}
