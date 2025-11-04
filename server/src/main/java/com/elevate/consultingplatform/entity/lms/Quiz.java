package com.elevate.consultingplatform.entity.lms;

import com.elevate.consultingplatform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "quiz")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Quiz extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "lesson_id", nullable = false, unique = true)
    private Lesson lesson;

    @Column(name = "pass_score", nullable = false)
    private Integer passScore = 70;
}
