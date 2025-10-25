package com.elevate.consultingplatform.entity.catalog;

import com.elevate.consultingplatform.common.HierarchyNode;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "segments")
@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Segment extends HierarchyNode {

    @ManyToOne(optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @Builder.Default
    @OneToMany(mappedBy = "segment")
    private List<Stage> stages = new ArrayList<>();
}
