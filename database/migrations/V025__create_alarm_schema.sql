-- V025: Create Alarm/Notification Schema
-- 알람 및 알림 스키마 생성
-- Author: Moon Myung-seop
-- Date: 2026-01-25

-- ==================== Alarm Template Table ====================
-- 알람 템플릿 테이블 (알람 메시지 템플릿 정의)

CREATE TABLE common.alarm_templates (
    template_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    template_code VARCHAR(50) NOT NULL,
    template_name VARCHAR(200) NOT NULL,

    -- Alarm Type
    alarm_type VARCHAR(50) NOT NULL,  -- SYSTEM, APPROVAL, QUALITY, PRODUCTION, INVENTORY, DELIVERY, etc.
    event_type VARCHAR(100) NOT NULL,  -- APPROVAL_REQUEST, QUALITY_FAILED, STOCK_LOW, ORDER_DELAYED, etc.

    -- Message Template
    title_template TEXT NOT NULL,  -- e.g., "[결재 요청] {{documentType}} {{documentNo}}"
    message_template TEXT NOT NULL,  -- e.g., "{{requesterName}}님이 결재를 요청했습니다."

    -- Channel Settings
    enable_email BOOLEAN DEFAULT FALSE,
    enable_sms BOOLEAN DEFAULT FALSE,
    enable_push BOOLEAN DEFAULT TRUE,
    enable_system BOOLEAN DEFAULT TRUE,

    -- Priority
    priority VARCHAR(20) DEFAULT 'NORMAL',  -- LOW, NORMAL, HIGH, URGENT

    -- Status
    is_active BOOLEAN DEFAULT TRUE,

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_alarm_template_code UNIQUE (tenant_id, template_code)
);

CREATE INDEX idx_alarm_template_tenant ON common.alarm_templates(tenant_id);
CREATE INDEX idx_alarm_template_type ON common.alarm_templates(alarm_type);
CREATE INDEX idx_alarm_template_event ON common.alarm_templates(event_type);

COMMENT ON TABLE common.alarm_templates IS '알람 템플릿';
COMMENT ON COLUMN common.alarm_templates.alarm_type IS '알람 타입: SYSTEM, APPROVAL, QUALITY, PRODUCTION, INVENTORY, DELIVERY';
COMMENT ON COLUMN common.alarm_templates.priority IS '우선순위: LOW, NORMAL, HIGH, URGENT';

-- ==================== Alarm Setting Table ====================
-- 알람 설정 테이블 (사용자별 알람 수신 설정)

CREATE TABLE common.alarm_settings (
    setting_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,

    -- Alarm Type Settings
    alarm_type VARCHAR(50) NOT NULL,

    -- Channel Settings
    enable_email BOOLEAN DEFAULT FALSE,
    enable_sms BOOLEAN DEFAULT FALSE,
    enable_push BOOLEAN DEFAULT TRUE,
    enable_system BOOLEAN DEFAULT TRUE,

    -- Quiet Hours
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    enable_quiet_hours BOOLEAN DEFAULT FALSE,

    -- Status
    is_active BOOLEAN DEFAULT TRUE,

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_alarm_setting_user_type UNIQUE (tenant_id, user_id, alarm_type)
);

CREATE INDEX idx_alarm_setting_tenant ON common.alarm_settings(tenant_id);
CREATE INDEX idx_alarm_setting_user ON common.alarm_settings(user_id);
CREATE INDEX idx_alarm_setting_type ON common.alarm_settings(alarm_type);

COMMENT ON TABLE common.alarm_settings IS '알람 설정 (사용자별 수신 설정)';
COMMENT ON COLUMN common.alarm_settings.quiet_hours_start IS '알림 금지 시작 시간';
COMMENT ON COLUMN common.alarm_settings.quiet_hours_end IS '알림 금지 종료 시간';

-- ==================== Alarm History Table ====================
-- 알람 이력 테이블 (발송된 알람 기록)

CREATE TABLE common.alarm_history (
    alarm_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    template_id BIGINT REFERENCES common.alarm_templates(template_id),

    -- Recipient
    recipient_user_id BIGINT NOT NULL,
    recipient_name VARCHAR(100),
    recipient_email VARCHAR(200),
    recipient_phone VARCHAR(50),

    -- Alarm Info
    alarm_type VARCHAR(50) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    priority VARCHAR(20) DEFAULT 'NORMAL',

    -- Content
    title VARCHAR(500) NOT NULL,
    message TEXT NOT NULL,

    -- Reference
    reference_type VARCHAR(50),  -- APPROVAL_INSTANCE, QUALITY_INSPECTION, WORK_ORDER, etc.
    reference_id BIGINT,
    reference_no VARCHAR(100),

    -- Channels
    sent_via_email BOOLEAN DEFAULT FALSE,
    sent_via_sms BOOLEAN DEFAULT FALSE,
    sent_via_push BOOLEAN DEFAULT FALSE,
    sent_via_system BOOLEAN DEFAULT FALSE,

    -- Status
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,

    -- Send Status
    send_status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING, SENT, FAILED
    sent_at TIMESTAMP,
    failed_reason TEXT,

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_alarm_history_channel CHECK (
        sent_via_email = TRUE OR sent_via_sms = TRUE OR
        sent_via_push = TRUE OR sent_via_system = TRUE
    )
);

CREATE INDEX idx_alarm_history_tenant ON common.alarm_history(tenant_id);
CREATE INDEX idx_alarm_history_recipient ON common.alarm_history(recipient_user_id);
CREATE INDEX idx_alarm_history_type ON common.alarm_history(alarm_type);
CREATE INDEX idx_alarm_history_reference ON common.alarm_history(reference_type, reference_id);
CREATE INDEX idx_alarm_history_created ON common.alarm_history(created_at DESC);
CREATE INDEX idx_alarm_history_unread ON common.alarm_history(recipient_user_id, is_read) WHERE is_read = FALSE;

COMMENT ON TABLE common.alarm_history IS '알람 이력 (발송 기록)';
COMMENT ON COLUMN common.alarm_history.send_status IS '발송 상태: PENDING, SENT, FAILED';

-- ==================== Alarm Subscription Table ====================
-- 알람 구독 테이블 (특정 이벤트/문서 구독)

CREATE TABLE common.alarm_subscriptions (
    subscription_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,

    -- Subscription Target
    subscription_type VARCHAR(50) NOT NULL,  -- DOCUMENT, TASK, PROJECT, EQUIPMENT, etc.
    target_type VARCHAR(50) NOT NULL,  -- PURCHASE_ORDER, WORK_ORDER, QUALITY_INSPECTION, etc.
    target_id BIGINT NOT NULL,

    -- Subscription Events
    subscribed_events TEXT,  -- JSON array: ["STATUS_CHANGED", "APPROVAL_COMPLETED", ...]

    -- Status
    is_active BOOLEAN DEFAULT TRUE,

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_alarm_subscription UNIQUE (tenant_id, user_id, subscription_type, target_type, target_id)
);

CREATE INDEX idx_alarm_subscription_tenant ON common.alarm_subscriptions(tenant_id);
CREATE INDEX idx_alarm_subscription_user ON common.alarm_subscriptions(user_id);
CREATE INDEX idx_alarm_subscription_target ON common.alarm_subscriptions(target_type, target_id);

COMMENT ON TABLE common.alarm_subscriptions IS '알람 구독 (특정 문서/이벤트 구독)';

-- ==================== Sample Data ====================

-- Sample Alarm Templates
INSERT INTO common.alarm_templates (
    tenant_id, template_code, template_name, alarm_type, event_type,
    title_template, message_template,
    enable_email, enable_sms, enable_push, enable_system, priority, is_active
) VALUES
-- Approval Alarms
('TENANT001', 'APPROVAL_REQUEST', '결재 요청', 'APPROVAL', 'APPROVAL_REQUEST',
 '[결재 요청] {{documentType}} {{documentNo}}',
 '{{requesterName}}님이 {{documentType}} 결재를 요청했습니다.',
 FALSE, FALSE, TRUE, TRUE, 'NORMAL', TRUE),

('TENANT001', 'APPROVAL_APPROVED', '결재 승인', 'APPROVAL', 'APPROVAL_APPROVED',
 '[결재 승인] {{documentType}} {{documentNo}}',
 '{{approverName}}님이 결재를 승인했습니다.',
 FALSE, FALSE, TRUE, TRUE, 'NORMAL', TRUE),

('TENANT001', 'APPROVAL_REJECTED', '결재 반려', 'APPROVAL', 'APPROVAL_REJECTED',
 '[결재 반려] {{documentType}} {{documentNo}}',
 '{{approverName}}님이 결재를 반려했습니다. 사유: {{reason}}',
 TRUE, FALSE, TRUE, TRUE, 'HIGH', TRUE),

-- Quality Alarms
('TENANT001', 'QUALITY_FAILED', '품질 검사 불합격', 'QUALITY', 'QUALITY_FAILED',
 '[품질 불합격] {{productName}} LOT {{lotNo}}',
 '품질 검사 결과 불합격되었습니다. 불량률: {{defectRate}}%',
 TRUE, FALSE, TRUE, TRUE, 'HIGH', TRUE),

('TENANT001', 'QUALITY_PENDING', '품질 검사 대기', 'QUALITY', 'QUALITY_PENDING',
 '[품질 검사] {{productName}} LOT {{lotNo}}',
 '품질 검사가 요청되었습니다.',
 FALSE, FALSE, TRUE, TRUE, 'NORMAL', TRUE),

-- Production Alarms
('TENANT001', 'PRODUCTION_STARTED', '생산 시작', 'PRODUCTION', 'PRODUCTION_STARTED',
 '[생산 시작] 작업 지시 {{workOrderNo}}',
 '작업 지시 {{workOrderNo}}의 생산이 시작되었습니다.',
 FALSE, FALSE, TRUE, TRUE, 'NORMAL', TRUE),

('TENANT001', 'PRODUCTION_COMPLETED', '생산 완료', 'PRODUCTION', 'PRODUCTION_COMPLETED',
 '[생산 완료] 작업 지시 {{workOrderNo}}',
 '작업 지시 {{workOrderNo}}의 생산이 완료되었습니다. 생산량: {{quantity}}',
 FALSE, FALSE, TRUE, TRUE, 'NORMAL', TRUE),

('TENANT001', 'PRODUCTION_DELAYED', '생산 지연', 'PRODUCTION', 'PRODUCTION_DELAYED',
 '[생산 지연] 작업 지시 {{workOrderNo}}',
 '작업 지시 {{workOrderNo}}의 생산이 지연되고 있습니다.',
 TRUE, FALSE, TRUE, TRUE, 'HIGH', TRUE),

-- Inventory Alarms
('TENANT001', 'STOCK_LOW', '재고 부족', 'INVENTORY', 'STOCK_LOW',
 '[재고 부족] {{productName}}',
 '{{productName}}의 재고가 부족합니다. 현재 재고: {{currentStock}}, 안전 재고: {{safetyStock}}',
 TRUE, FALSE, TRUE, TRUE, 'HIGH', TRUE),

('TENANT001', 'STOCK_OUT', '재고 소진', 'INVENTORY', 'STOCK_OUT',
 '[재고 소진] {{productName}}',
 '{{productName}}의 재고가 소진되었습니다.',
 TRUE, TRUE, TRUE, TRUE, 'URGENT', TRUE),

-- Delivery Alarms
('TENANT001', 'DELIVERY_SCHEDULED', '출하 예정', 'DELIVERY', 'DELIVERY_SCHEDULED',
 '[출하 예정] 판매 주문 {{salesOrderNo}}',
 '판매 주문 {{salesOrderNo}}의 출하가 예정되어 있습니다. 예정일: {{scheduledDate}}',
 FALSE, FALSE, TRUE, TRUE, 'NORMAL', TRUE),

('TENANT001', 'DELIVERY_DELAYED', '출하 지연', 'DELIVERY', 'DELIVERY_DELAYED',
 '[출하 지연] 판매 주문 {{salesOrderNo}}',
 '판매 주문 {{salesOrderNo}}의 출하가 지연되고 있습니다.',
 TRUE, FALSE, TRUE, TRUE, 'HIGH', TRUE),

-- System Alarms
('TENANT001', 'SYSTEM_MAINTENANCE', '시스템 점검', 'SYSTEM', 'SYSTEM_MAINTENANCE',
 '[시스템 점검] 예정 안내',
 '시스템 점검이 예정되어 있습니다. 일시: {{maintenanceTime}}',
 TRUE, FALSE, TRUE, TRUE, 'HIGH', TRUE),

('TENANT001', 'SYSTEM_ERROR', '시스템 오류', 'SYSTEM', 'SYSTEM_ERROR',
 '[시스템 오류] {{errorType}}',
 '시스템 오류가 발생했습니다. 메시지: {{errorMessage}}',
 TRUE, FALSE, TRUE, TRUE, 'URGENT', TRUE);

-- Sample Alarm Settings (Default for users)
INSERT INTO common.alarm_settings (
    tenant_id, user_id, alarm_type,
    enable_email, enable_sms, enable_push, enable_system,
    enable_quiet_hours, quiet_hours_start, quiet_hours_end, is_active
) VALUES
-- User 1 settings
('TENANT001', 1, 'APPROVAL', FALSE, FALSE, TRUE, TRUE, TRUE, '22:00:00', '08:00:00', TRUE),
('TENANT001', 1, 'QUALITY', TRUE, FALSE, TRUE, TRUE, FALSE, NULL, NULL, TRUE),
('TENANT001', 1, 'PRODUCTION', FALSE, FALSE, TRUE, TRUE, FALSE, NULL, NULL, TRUE),
('TENANT001', 1, 'INVENTORY', TRUE, FALSE, TRUE, TRUE, FALSE, NULL, NULL, TRUE),
('TENANT001', 1, 'DELIVERY', FALSE, FALSE, TRUE, TRUE, FALSE, NULL, NULL, TRUE),
('TENANT001', 1, 'SYSTEM', TRUE, FALSE, TRUE, TRUE, FALSE, NULL, NULL, TRUE);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_alarm_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers
CREATE TRIGGER update_alarm_template_updated_at
    BEFORE UPDATE ON common.alarm_templates
    FOR EACH ROW
    EXECUTE FUNCTION update_alarm_updated_at();

CREATE TRIGGER update_alarm_setting_updated_at
    BEFORE UPDATE ON common.alarm_settings
    FOR EACH ROW
    EXECUTE FUNCTION update_alarm_updated_at();

CREATE TRIGGER update_alarm_subscription_updated_at
    BEFORE UPDATE ON common.alarm_subscriptions
    FOR EACH ROW
    EXECUTE FUNCTION update_alarm_updated_at();

-- Create view for unread alarms
CREATE VIEW common.v_unread_alarms AS
SELECT
    ah.alarm_id,
    ah.tenant_id,
    ah.recipient_user_id,
    ah.alarm_type,
    ah.event_type,
    ah.priority,
    ah.title,
    ah.message,
    ah.reference_type,
    ah.reference_id,
    ah.reference_no,
    ah.created_at,
    EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - ah.created_at)) / 3600 AS hours_ago
FROM common.alarm_history ah
WHERE ah.is_read = FALSE
  AND ah.send_status = 'SENT'
ORDER BY ah.created_at DESC;

COMMENT ON VIEW common.v_unread_alarms IS '읽지 않은 알람 뷰';
