package com.elevate.consultingplatform.entity.assessment;

import com.elevate.consultingplatform.common.BaseEntity;
import com.elevate.consultingplatform.entity.catalog.Stage;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Assessment extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "stage_id", nullable = false)
    @JsonBackReference("stage-assessment")
    private Stage stage;

    @ManyToOne(optional = true)
    @JoinColumn(name = "questionnaire_id")
    @JsonIgnore // avoid serializing Hibernate proxy; return questionnaireId via DTO if needed
    private Questionnaire questionnaire; // Link existing questions via questionnaire

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
