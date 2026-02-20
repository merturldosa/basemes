-- ================================================================
-- V030: Optimize Work Progress and POP Performance Indexes
-- Phase 2 Day 7: Performance Optimization
-- Author: Moon Myung-seop
-- Date: 2025-02-05
-- ================================================================

-- Work Progress Query Optimization
-- Index for active work progress queries (frequently used in POP)
CREATE INDEX IF NOT EXISTS idx_work_progress_active
ON mes.sd_work_progress(work_order_id, is_active)
WHERE is_active = TRUE;

-- Index for operator-specific daily queries
CREATE INDEX IF NOT EXISTS idx_work_progress_operator_date
ON mes.sd_work_progress(operator_user_id, record_date, status);

-- Index for work order progress lookup
CREATE INDEX IF NOT EXISTS idx_work_progress_work_order
ON mes.sd_work_progress(tenant_id, work_order_id, record_date DESC);

-- Index for today's statistics queries
CREATE INDEX IF NOT EXISTS idx_work_progress_today_stats
ON mes.sd_work_progress(tenant_id, record_date, status)
WHERE record_date = CURRENT_DATE;

-- Pause/Resume History Optimization
-- Index for active pause lookups (resume_time IS NULL means currently paused)
CREATE INDEX IF NOT EXISTS idx_pause_resume_active
ON mes.sd_pause_resume_history(progress_id, resume_time)
WHERE resume_time IS NULL;

-- Index for pause history by work progress
CREATE INDEX IF NOT EXISTS idx_pause_resume_by_progress
ON mes.sd_pause_resume_history(progress_id, pause_time DESC);

-- Index for pause analytics
CREATE INDEX IF NOT EXISTS idx_pause_resume_analytics
ON mes.sd_pause_resume_history(tenant_id, pause_time, pause_type);

-- Work Order Performance Optimization
-- Index for assigned user active work orders
CREATE INDEX IF NOT EXISTS idx_work_order_assigned_active
ON mes.sd_work_orders(tenant_id, assigned_user_id, status)
WHERE status IN ('READY', 'IN_PROGRESS', 'PAUSED');

-- Index for work order number lookups (barcode scanning)
CREATE INDEX IF NOT EXISTS idx_work_order_no_lookup
ON mes.sd_work_orders(tenant_id, work_order_no)
WHERE is_deleted = FALSE;

-- Index for planned date range queries
CREATE INDEX IF NOT EXISTS idx_work_order_planned_dates
ON mes.sd_work_orders(tenant_id, planned_start_date, status)
WHERE is_deleted = FALSE;

-- Defect Records Optimization
-- Index for work order defects
CREATE INDEX IF NOT EXISTS idx_defect_work_order
ON qms.sd_defects(tenant_id, reference_type, reference_id, occurrence_date DESC)
WHERE reference_type = 'WORK_ORDER';

-- Index for defect type analytics
CREATE INDEX IF NOT EXISTS idx_defect_type_analytics
ON qms.sd_defects(tenant_id, defect_type, occurrence_date DESC, severity);

-- Index for today's defects
CREATE INDEX IF NOT EXISTS idx_defect_today
ON qms.sd_defects(tenant_id, occurrence_date, status)
WHERE occurrence_date = CURRENT_DATE;

-- SOP Execution Optimization
-- Index for work order SOP executions
CREATE INDEX IF NOT EXISTS idx_sop_execution_work_order
ON qms.sd_sop_execution(tenant_id, work_order_id, execution_status);

-- Index for active SOP executions
CREATE INDEX IF NOT EXISTS idx_sop_execution_active
ON qms.sd_sop_execution(tenant_id, execution_status, execution_date DESC)
WHERE execution_status IN ('IN_PROGRESS', 'PENDING');

-- Index for SOP execution steps by execution
CREATE INDEX IF NOT EXISTS idx_sop_exec_step_by_execution
ON qms.sd_sop_execution_step(execution_id, step_number);

-- Index for failed SOP steps
CREATE INDEX IF NOT EXISTS idx_sop_exec_step_failed
ON qms.sd_sop_execution_step(execution_id, step_status, check_result)
WHERE step_status = 'FAILED' OR check_result = FALSE;

-- Work Result Optimization
-- Index for work order results
CREATE INDEX IF NOT EXISTS idx_work_result_work_order
ON mes.sd_work_result(tenant_id, work_order_id, completion_date DESC);

-- Index for operator performance analytics
CREATE INDEX IF NOT EXISTS idx_work_result_operator
ON mes.sd_work_result(tenant_id, operator_user_id, completion_date DESC);

-- Index for daily production statistics
CREATE INDEX IF NOT EXISTS idx_work_result_daily_stats
ON mes.sd_work_result(tenant_id, completion_date, product_id)
WHERE completion_date >= CURRENT_DATE - INTERVAL '30 days';

-- Inventory Transaction Optimization (for work completion)
-- Index for product inventory lookups
CREATE INDEX IF NOT EXISTS idx_inventory_product_lookup
ON wms.sd_inventory(tenant_id, product_id, warehouse_id, lot_id)
WHERE is_deleted = FALSE;

-- Index for recent inventory transactions
CREATE INDEX IF NOT EXISTS idx_inventory_txn_recent
ON wms.sd_inventory_transaction(tenant_id, transaction_date DESC, transaction_type)
WHERE transaction_date >= CURRENT_DATE - INTERVAL '7 days';

-- Composite index for POP dashboard queries
CREATE INDEX IF NOT EXISTS idx_pop_dashboard_composite
ON mes.sd_work_progress(tenant_id, record_date, status, operator_user_id)
INCLUDE (produced_quantity, good_quantity, defect_quantity);

-- ================================================================
-- Performance Statistics
-- ================================================================

-- Analyze tables after index creation for query planner optimization
ANALYZE mes.sd_work_progress;
ANALYZE mes.sd_pause_resume_history;
ANALYZE mes.sd_work_orders;
ANALYZE mes.sd_work_result;
ANALYZE qms.sd_defects;
ANALYZE qms.sd_sop_execution;
ANALYZE qms.sd_sop_execution_step;
ANALYZE wms.sd_inventory;
ANALYZE wms.sd_inventory_transaction;

-- ================================================================
-- Comments for Documentation
-- ================================================================

COMMENT ON INDEX mes.idx_work_progress_active IS
'Optimizes active work progress queries for POP system';

COMMENT ON INDEX mes.idx_work_progress_operator_date IS
'Optimizes operator-specific daily production queries';

COMMENT ON INDEX mes.idx_pause_resume_active IS
'Quickly finds currently paused work (resume_time IS NULL)';

COMMENT ON INDEX mes.idx_work_order_assigned_active IS
'Optimizes active work order list for assigned operators';

COMMENT ON INDEX qms.idx_defect_work_order IS
'Fast lookup of defects by work order for quality analysis';

COMMENT ON INDEX qms.idx_sop_execution_work_order IS
'Links SOP executions to work orders efficiently';

COMMENT ON INDEX mes.idx_pop_dashboard_composite IS
'Composite index for POP dashboard real-time statistics with INCLUDE clause for covering index';

-- ================================================================
-- Migration Complete
-- ================================================================
