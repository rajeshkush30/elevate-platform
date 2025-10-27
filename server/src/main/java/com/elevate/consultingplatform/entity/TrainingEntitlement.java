package com.elevate.consultingplatform.entity;

import com.elevate.consultingplatform.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "training_entitlements", indexes = {
        @Index(name = "idx_entitlement_user_scope", columnList = "user_id, scope_key, active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TrainingEntitlement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    // Free-form scope key to allow simple mapping (e.g., "STAGE123" or "Grow")
    @Column(name = "scope_key", nullable = false)
    private String scopeKey;

    @Column(name = "source", nullable = false)
    private String source; // e.g., ZOHO_PAYMENT

    @Column(name = "order_ref")
    private String orderRef; // platformOrderId or Zoho payment id

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "granted_at", updatable = false)
    private LocalDateTime grantedAt;
}
