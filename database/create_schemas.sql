-- ============================================================================
-- SoIce MES - Create All Required Schemas
-- This script creates all schemas required by the application
-- Author: Moon Myung-seop
-- Date: 2026-01-28
-- ============================================================================

-- Core schemas
CREATE SCHEMA IF NOT EXISTS common;
CREATE SCHEMA IF NOT EXISTS core;
CREATE SCHEMA IF NOT EXISTS mes;

-- Business modules
CREATE SCHEMA IF NOT EXISTS business;
CREATE SCHEMA IF NOT EXISTS inventory;
CREATE SCHEMA IF NOT EXISTS bom;
CREATE SCHEMA IF NOT EXISTS material;
CREATE SCHEMA IF NOT EXISTS purchase;
CREATE SCHEMA IF NOT EXISTS sales;

-- Quality and warehouse
CREATE SCHEMA IF NOT EXISTS qms;
CREATE SCHEMA IF NOT EXISTS wms;

-- Equipment
CREATE SCHEMA IF NOT EXISTS equipment;

-- Grant all privileges to mes_admin
GRANT ALL PRIVILEGES ON SCHEMA common TO mes_admin;
GRANT ALL PRIVILEGES ON SCHEMA core TO mes_admin;
GRANT ALL PRIVILEGES ON SCHEMA mes TO mes_admin;
GRANT ALL PRIVILEGES ON SCHEMA business TO mes_admin;
GRANT ALL PRIVILEGES ON SCHEMA inventory TO mes_admin;
GRANT ALL PRIVILEGES ON SCHEMA bom TO mes_admin;
GRANT ALL PRIVILEGES ON SCHEMA material TO mes_admin;
GRANT ALL PRIVILEGES ON SCHEMA purchase TO mes_admin;
GRANT ALL PRIVILEGES ON SCHEMA sales TO mes_admin;
GRANT ALL PRIVILEGES ON SCHEMA qms TO mes_admin;
GRANT ALL PRIVILEGES ON SCHEMA wms TO mes_admin;
GRANT ALL PRIVILEGES ON SCHEMA equipment TO mes_admin;

-- Set default search path
ALTER DATABASE soice_mes_dev SET search_path TO common, core, mes, business, inventory, bom, material, purchase, sales, qms, wms, equipment, public;

-- ============================================================================
-- End of schema creation
-- ============================================================================
