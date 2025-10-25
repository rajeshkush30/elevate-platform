package com.elevate.consultingplatform.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Shared fields for hierarchical catalog entities to avoid duplication.
 * Extends BaseEntity for id/auditing/isActive.
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@SuperBuilder
public abstract class HierarchyNode extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "order_index")
    private Integer orderIndex;
}
