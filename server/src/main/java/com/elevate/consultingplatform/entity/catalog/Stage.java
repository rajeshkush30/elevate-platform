package com.elevate.consultingplatform.entity.catalog;

import com.elevate.consultingplatform.common.HierarchyNode;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import com.elevate.consultingplatform.entity.assessment.Assessment;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Stage extends HierarchyNode {

    @ManyToOne(optional = false)
    @JoinColumn(name = "segment_id", nullable = false)
    @JsonBackReference("segment-stage")
    private Segment segment;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private StageType type;

    @Column(name = "content_url")
    private String contentUrl; // LMS or internal content URL

    @Column(name = "lms_course_id")
    private String lmsCourseId; // external LMS identifier

    @Column(name = "assessment_config", columnDefinition = "TEXT")
    private String assessmentConfig; // JSON

    @Column(name = "ai_prompt_template", columnDefinition = "TEXT")
    private String aiPromptTemplate; // prompt for AI stages

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // extra JSON, future-proofing

    @OneToMany(mappedBy = "stage")
    @Builder.Default
    @JsonManagedReference("stage-assessment")
    private List<Assessment> assessments = new ArrayList<>();
}
