package com.elevate.consultingplatform.entity.lms;

import com.elevate.consultingplatform.common.BaseEntity;
import com.elevate.consultingplatform.entity.catalog.Stage;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "course", uniqueConstraints = {
        @UniqueConstraint(name = "uq_course_stage", columnNames = {"stage_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Course extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
