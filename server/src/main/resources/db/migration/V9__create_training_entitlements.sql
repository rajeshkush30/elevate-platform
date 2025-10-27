-- Create training_entitlements table to match TrainingEntitlement entity (and BaseEntity columns)
CREATE TABLE IF NOT EXISTS training_entitlements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    scope_key VARCHAR(255) NOT NULL,
    source VARCHAR(100) NOT NULL,
    order_ref VARCHAR(255) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- BaseEntity columns
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NULL,
    updated_by VARCHAR(255) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    INDEX idx_entitlement_user_scope (user_id, scope_key, active),
    CONSTRAINT fk_training_entitlements_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT='Grants training access (entitlements) to users.';
