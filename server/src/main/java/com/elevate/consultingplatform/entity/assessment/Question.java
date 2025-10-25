package com.elevate.consultingplatform.entity.assessment;

    import com.elevate.consultingplatform.common.BaseEntity;
    import com.elevate.consultingplatform.entity.catalog.Segment;
    import jakarta.persistence.*;
    import lombok.*;
    import lombok.experimental.SuperBuilder;

    @Entity(name = "AssessmentQuestion")
    @Table(name = "assessment_questions")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    @EqualsAndHashCode(callSuper = true)
    public class Question extends BaseEntity {

        @ManyToOne(optional = false)
        @JoinColumn(name = "questionnaire_id", nullable = false)
        private Questionnaire questionnaire;

    @ManyToOne
    @JoinColumn(name = "segment_id")
    private Segment segment; // Optional: attribute question to a segment
    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "weight")
    private Double weight; // contribution to score (0..1)

    @Column(name = "options_json", columnDefinition = "TEXT")
    private String optionsJson; // optional config

    @Column(name = "type")
    private String type; // e.g., SCALE, MCQ, TEXT

    @Column(name = "order_index")
    private Integer orderIndex;
}
