package com.elevate.consultingplatform.entity.strategy;

import com.elevate.consultingplatform.common.BaseEntity;
import com.elevate.consultingplatform.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "consultation_drafts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ConsultationDraft extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Lob
    @Column(name = "draft", columnDefinition = "LONGTEXT")
    private String draft;

    @Lob
    @Column(name = "sections", columnDefinition = "LONGTEXT")
    private String sectionsJson;

    @Column(name = "approved")
    private boolean approved;
}
