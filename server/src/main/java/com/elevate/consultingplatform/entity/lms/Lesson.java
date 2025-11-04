package com.elevate.consultingplatform.entity.lms;

import com.elevate.consultingplatform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "lesson")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Lesson extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "title", nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private LessonContentType contentType;

    @Column(name = "content_url", columnDefinition = "TEXT", nullable = false)
    private String contentUrl;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;
}
