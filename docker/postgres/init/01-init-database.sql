-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- SDS MES - Database Initialization Script
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- Purpose: Initialize database extensions, schemas, and roles
-- Author: Moon Myung-seop <msmoon@softice.co.kr>
-- Date: 2026-01-17
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

\echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'
\echo 'SDS MES Database Initialization Starting...'
\echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 1. Extensions (PostgreSQL 확장 기능)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

\echo ''
\echo '1. Installing PostgreSQL Extensions...'

-- UUID 생성 지원
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
\echo '  ✓ uuid-ossp extension installed'

-- 암호화 함수
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
\echo '  ✓ pgcrypto extension installed'

-- Full-text search (한국어 지원)
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
\echo '  ✓ pg_trgm extension installed'

-- 시계열 데이터 (TimescaleDB - 선택사항)
-- CREATE EXTENSION IF NOT EXISTS "timescaledb" CASCADE;
-- \echo '  ✓ timescaledb extension installed'

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 2. Schemas (스키마 생성)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

\echo ''
\echo '2. Creating Schemas...'

-- 공통 스키마
CREATE SCHEMA IF NOT EXISTS common;
COMMENT ON SCHEMA common IS '공통 관리 스키마 (사용자, 권한, 코드 등)';
\echo '  ✓ common schema created'

-- 생산관리 (MES)
CREATE SCHEMA IF NOT EXISTS mes;
COMMENT ON SCHEMA mes IS '생산관리 스키마';
\echo '  ✓ mes schema created'

-- 품질관리 (QMS)
CREATE SCHEMA IF NOT EXISTS qms;
COMMENT ON SCHEMA qms IS '품질관리 스키마';
\echo '  ✓ qms schema created'

-- 창고관리 (WMS)
CREATE SCHEMA IF NOT EXISTS wms;
COMMENT ON SCHEMA wms IS '창고관리 스키마';
\echo '  ✓ wms schema created'

-- 설비관리 (EMS)
CREATE SCHEMA IF NOT EXISTS ems;
COMMENT ON SCHEMA ems IS '설비관리 스키마';
\echo '  ✓ ems schema created'

-- 시험관리 (LIMS)
CREATE SCHEMA IF NOT EXISTS lims;
COMMENT ON SCHEMA lims IS '시험관리 스키마';
\echo '  ✓ lims schema created'

-- Audit 스키마
CREATE SCHEMA IF NOT EXISTS audit;
COMMENT ON SCHEMA audit IS '감사 추적 스키마';
\echo '  ✓ audit schema created'

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 3. Roles and Permissions (역할 및 권한)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

\echo ''
\echo '3. Creating Database Roles...'

-- Application User (애플리케이션 전용 사용자)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'mes_app') THEN
        CREATE ROLE mes_app WITH LOGIN PASSWORD 'mes_app_password_dev_2026';
        \echo '  ✓ mes_app role created'
    ELSE
        \echo '  ℹ mes_app role already exists'
    END IF;
END
$$;

-- Read-Only User (읽기 전용 사용자)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'mes_readonly') THEN
        CREATE ROLE mes_readonly WITH LOGIN PASSWORD 'mes_readonly_password_2026';
        \echo '  ✓ mes_readonly role created'
    ELSE
        \echo '  ℹ mes_readonly role already exists'
    END IF;
END
$$;

-- Grant permissions
GRANT USAGE ON SCHEMA common, mes, qms, wms, ems, lims, audit TO mes_app;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA common, mes, qms, wms, ems, lims, audit TO mes_app;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA common, mes, qms, wms, ems, lims, audit TO mes_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA common, mes, qms, wms, ems, lims, audit GRANT ALL ON TABLES TO mes_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA common, mes, qms, wms, ems, lims, audit GRANT ALL ON SEQUENCES TO mes_app;

GRANT USAGE ON SCHEMA common, mes, qms, wms, ems, lims, audit TO mes_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA common, mes, qms, wms, ems, lims, audit TO mes_readonly;
ALTER DEFAULT PRIVILEGES IN SCHEMA common, mes, qms, wms, ems, lims, audit GRANT SELECT ON TABLES TO mes_readonly;

\echo '  ✓ Permissions granted'

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 4. Common Functions (공통 함수)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

\echo ''
\echo '4. Creating Common Functions...'

-- Updated timestamp trigger function
CREATE OR REPLACE FUNCTION common.update_modified_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

\echo '  ✓ update_modified_timestamp function created'

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 5. Database Settings
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

\echo ''
\echo '5. Configuring Database Settings...'

-- Timezone
SET TIMEZONE='Asia/Seoul';
\echo '  ✓ Timezone set to Asia/Seoul'

-- Client encoding
SET client_encoding = 'UTF8';
\echo '  ✓ Client encoding set to UTF8'

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- Completion
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

\echo ''
\echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'
\echo '✅ SDS MES Database Initialization Completed Successfully!'
\echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'
\echo ''
\echo 'Database: sds_mes_dev'
\echo 'Schemas: common, mes, qms, wms, ems, lims, audit'
\echo 'Users: mes_admin (admin), mes_app (app), mes_readonly (readonly)'
\echo ''
