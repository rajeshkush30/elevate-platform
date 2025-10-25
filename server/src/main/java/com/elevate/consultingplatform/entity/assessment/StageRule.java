package com.elevate.consultingplatform.entity.assessment;

import com.elevate.consultingplatform.common.BaseEntity;
import com.elevate.consultingplatform.entity.catalog.Stage;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stage_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class StageRule extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "questionnaire_id")
    private Questionnaire questionnaire; // null => global

    @Column(name = "min_score", nullable = false)
    private Double minScore;

    @Column(name = "max_score", nullable = false)
    private Double maxScore;

    @ManyToOne(optional = false)
    @JoinColumn(name = "target_stage_id", nullable = false)
    private Stage targetStage;

    @Column(name = "priority")
    private Integer priority; // lower = higher priority
}
