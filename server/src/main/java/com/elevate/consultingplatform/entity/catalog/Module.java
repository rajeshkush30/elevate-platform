package com.elevate.consultingplatform.entity.catalog;

import com.elevate.consultingplatform.common.HierarchyNode;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "modules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Module extends HierarchyNode {

    @Builder.Default
    @OneToMany(mappedBy = "module")
    @JsonManagedReference("module-segment")
    private List<Segment> segments = new ArrayList<>();
}
