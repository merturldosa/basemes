-- ====================================================================
-- Migration: V029__create_work_progress_schema.sql
-- Description: Create Work Progress and Pause/Resume Schema for POP
-- Author: SoftIce MES Development Team (Moon Myung-seop)
-- Date: 2026-02-04
-- ====================================================================

-- ====================================================================
-- Table: mes.si_work_progress
-- Description: Real-time work progress tracking (minute/hour level)
--              Each work session creates one record
-- ====================================================================

CREATE TABLE mes.si_work_progress (
    -- Primary Key
    progress_id BIGSERIAL PRIMARY KEY,

    -- Multi-tenancy
    tenant_id VARCHAR(50) NOT NULL,

    -- Work Order Reference
    work_order_id BIGINT NOT NULL,

    -- Operator Information
    operator_user_id BIGINT NOT NULL,

    -- Work Date and Time
    record_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME,

    -- Production Quantities
    produced_quantity DECIMAL(15,3) NOT NULL DEFAULT 0,
    good_quantity DECIMAL(15,3) NOT NULL DEFAULT 0,
    defect_quantity DECIMAL(15,3) NOT NULL DEFAULT 0,

    -- Work Status: IN_PROGRESS, PAUSED, COMPLETED
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',

    -- Pause Statistics
    pause_count INTEGER NOT NULL DEFAULT 0,
    total_pause_duration INTEGER NOT NULL DEFAULT 0, -- Total pause duration in minutes

    -- Work Notes
    work_notes TEXT,

    -- Active Status (current active session)
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Equipment Information (Optional)
    equipment_id BIGINT,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_si_work_progress_tenant
        FOREIGN KEY (tenant_id) REFERENCES core.si_tenants(tenant_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_si_work_progress_work_order
        FOREIGN KEY (work_order_id) REFERENCES mes.si_work_orders(work_order_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_si_work_progress_operator
        FOREIGN KEY (operator_user_id) REFERENCES core.si_users(user_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_si_work_progress_equipment
        FOREIGN KEY (equipment_id) REFERENCES eqp.si_equipments(equipment_id)
        ON DELETE SET NULL
);

-- Indexes for Performance
CREATE INDEX idx_si_work_progress_work_order ON mes.si_work_progress(work_order_id);
CREATE INDEX idx_si_work_progress_operator ON mes.si_work_progress(operator_user_id);
CREATE INDEX idx_si_work_progress_active ON mes.si_work_progress(work_order_id, is_active);
CREATE INDEX idx_si_work_progress_operator_date ON mes.si_work_progress(operator_user_id, record_date);
CREATE INDEX idx_si_work_progress_tenant ON mes.si_work_progress(tenant_id);
CREATE INDEX idx_si_work_progress_status ON mes.si_work_progress(status);
CREATE INDEX idx_si_work_progress_date ON mes.si_work_progress(record_date);

-- Comments
COMMENT ON TABLE mes.si_work_progress IS 'Real-time work progress tracking for POP system';
COMMENT ON COLUMN mes.si_work_progress.progress_id IS 'Work progress ID (Primary Key)';
COMMENT ON COLUMN mes.si_work_progress.tenant_id IS 'Tenant ID';
COMMENT ON COLUMN mes.si_work_progress.work_order_id IS 'Work order reference';
COMMENT ON COLUMN mes.si_work_progress.operator_user_id IS 'Operator user ID';
COMMENT ON COLUMN mes.si_work_progress.record_date IS 'Work date';
COMMENT ON COLUMN mes.si_work_progress.start_time IS 'Work start time';
COMMENT ON COLUMN mes.si_work_progress.end_time IS 'Work end time (NULL if in progress)';
COMMENT ON COLUMN mes.si_work_progress.produced_quantity IS 'Total produced quantity';
COMMENT ON COLUMN mes.si_work_progress.good_quantity IS 'Good quantity';
COMMENT ON COLUMN mes.si_work_progress.defect_quantity IS 'Defect quantity';
COMMENT ON COLUMN mes.si_work_progress.status IS 'Work status (IN_PROGRESS, PAUSED, COMPLETED)';
COMMENT ON COLUMN mes.si_work_progress.pause_count IS 'Number of pauses';
COMMENT ON COLUMN mes.si_work_progress.total_pause_duration IS 'Total pause duration in minutes';
COMMENT ON COLUMN mes.si_work_progress.is_active IS 'Active work session flag';
COMMENT ON COLUMN mes.si_work_progress.equipment_id IS 'Equipment used (optional)';

-- ====================================================================
-- Table: mes.si_pause_resume_history
-- Description: Pause and resume event history
--              Tracks all pause/resume events with reasons
-- ====================================================================

CREATE TABLE mes.si_pause_resume_history (
    -- Primary Key
    pause_resume_id BIGSERIAL PRIMARY KEY,

    -- Multi-tenancy
    tenant_id VARCHAR(50) NOT NULL,

    -- Work Progress Reference
    progress_id BIGINT NOT NULL,

    -- Pause Time
    pause_time TIMESTAMP NOT NULL,

    -- Resume Time (NULL if not resumed yet)
    resume_time TIMESTAMP,

    -- Pause Reason
    pause_reason VARCHAR(500),

    -- Pause Type: BREAK, EQUIPMENT_CHECK, MATERIAL_WAIT, OTHER
    pause_type VARCHAR(50),

    -- Duration (calculated when resumed, in minutes)
    duration_minutes INTEGER,

    -- Approval Information (some pauses may require approval)
    requires_approval BOOLEAN NOT NULL DEFAULT FALSE,
    approved_by_user_id BIGINT,
    approval_time TIMESTAMP,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_si_pause_resume_tenant
        FOREIGN KEY (tenant_id) REFERENCES core.si_tenants(tenant_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_si_pause_resume_progress
        FOREIGN KEY (progress_id) REFERENCES mes.si_work_progress(progress_id)
        ON DELETE CASCADE
);

-- Indexes for Performance
CREATE INDEX idx_si_pause_resume_progress ON mes.si_pause_resume_history(progress_id);
CREATE INDEX idx_si_pause_resume_active ON mes.si_pause_resume_history(progress_id, resume_time);
CREATE INDEX idx_si_pause_resume_tenant ON mes.si_pause_resume_history(tenant_id);
CREATE INDEX idx_si_pause_resume_type ON mes.si_pause_resume_history(pause_type);
CREATE INDEX idx_si_pause_resume_pause_time ON mes.si_pause_resume_history(pause_time);

-- Comments
COMMENT ON TABLE mes.si_pause_resume_history IS 'Pause and resume event tracking for work sessions';
COMMENT ON COLUMN mes.si_pause_resume_history.pause_resume_id IS 'Pause/Resume ID (Primary Key)';
COMMENT ON COLUMN mes.si_pause_resume_history.tenant_id IS 'Tenant ID';
COMMENT ON COLUMN mes.si_pause_resume_history.progress_id IS 'Work progress reference';
COMMENT ON COLUMN mes.si_pause_resume_history.pause_time IS 'Pause start time';
COMMENT ON COLUMN mes.si_pause_resume_history.resume_time IS 'Resume time (NULL if not resumed)';
COMMENT ON COLUMN mes.si_pause_resume_history.pause_reason IS 'Reason for pause';
COMMENT ON COLUMN mes.si_pause_resume_history.pause_type IS 'Pause type (BREAK, EQUIPMENT_CHECK, MATERIAL_WAIT, OTHER)';
COMMENT ON COLUMN mes.si_pause_resume_history.duration_minutes IS 'Pause duration in minutes (calculated on resume)';
COMMENT ON COLUMN mes.si_pause_resume_history.requires_approval IS 'Whether this pause requires approval';
COMMENT ON COLUMN mes.si_pause_resume_history.approved_by_user_id IS 'User who approved the pause';
COMMENT ON COLUMN mes.si_pause_resume_history.approval_time IS 'Approval timestamp';

-- ====================================================================
-- Triggers for updated_at
-- ====================================================================

-- Trigger for si_work_progress
CREATE OR REPLACE FUNCTION mes.update_si_work_progress_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_si_work_progress_updated_at
    BEFORE UPDATE ON mes.si_work_progress
    FOR EACH ROW
    EXECUTE FUNCTION mes.update_si_work_progress_updated_at();

-- Trigger for si_pause_resume_history
CREATE OR REPLACE FUNCTION mes.update_si_pause_resume_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_si_pause_resume_updated_at
    BEFORE UPDATE ON mes.si_pause_resume_history
    FOR EACH ROW
    EXECUTE FUNCTION mes.update_si_pause_resume_updated_at();

-- ====================================================================
-- Sample Data (Optional - for testing)
-- ====================================================================

-- Note: Sample data would be inserted here if needed for testing
-- Currently skipped as this will be populated by the application

-- ====================================================================
-- End of Migration V029
-- ====================================================================
