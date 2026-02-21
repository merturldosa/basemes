#!/bin/bash
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# Phase 2 Deployment - Step 2: Database Migration
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# Author: Moon Myung-seop
# Date: 2026-02-05
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

set -e

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "SDS MES Phase 2 - Database Migration"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Database configuration
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-sds_mes_dev}"
DB_USER="${DB_USER:-mes_admin}"
DB_PASSWORD="${DB_PASSWORD:-mes_password_dev_2026}"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Export password for psql
export PGPASSWORD=$DB_PASSWORD

echo "Database Configuration:"
echo "  Host: $DB_HOST"
echo "  Port: $DB_PORT"
echo "  Database: $DB_NAME"
echo "  User: $DB_USER"
echo ""

# Test connection
echo "Testing database connection..."
if psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT version();" > /dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} Database connection successful"
else
    echo -e "${RED}✗${NC} Cannot connect to database"
    echo ""
    echo "Please ensure:"
    echo "1. PostgreSQL is running"
    echo "2. Database '$DB_NAME' exists"
    echo "3. User '$DB_USER' has access"
    echo "4. Password is correct"
    exit 1
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Running Migrations"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

cd "$(dirname "$0")/.."

# Check if migrations exist
if [ ! -f "database/migrations/V029__create_work_progress_schema.sql" ]; then
    echo -e "${RED}✗${NC} Migration file V029 not found"
    exit 1
fi

if [ ! -f "database/migrations/V030__optimize_work_progress_indexes.sql" ]; then
    echo -e "${RED}✗${NC} Migration file V030 not found"
    exit 1
fi

# Option 1: Use Flyway (recommended)
echo "Checking for Flyway..."
if command -v flyway &> /dev/null || [ -d "backend" ]; then
    echo "Using Maven Flyway plugin..."
    cd backend
    mvn flyway:info
    echo ""
    read -p "Apply migrations? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        mvn flyway:migrate
        echo -e "${GREEN}✓${NC} Migrations applied via Flyway"
    else
        echo "Migration cancelled"
        exit 0
    fi
    cd ..
else
    # Option 2: Direct SQL execution
    echo "Using direct SQL execution..."

    echo ""
    echo "1/2: Applying V029 - Work Progress Schema..."
    psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME \
         -f database/migrations/V029__create_work_progress_schema.sql
    echo -e "${GREEN}✓${NC} V029 applied"

    echo ""
    echo "2/2: Applying V030 - Performance Indexes..."
    psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME \
         -f database/migrations/V030__optimize_work_progress_indexes.sql
    echo -e "${GREEN}✓${NC} V030 applied"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Verifying Migrations"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "Checking tables..."
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'mes'
  AND table_name IN ('si_work_progress', 'si_pause_resume_history')
ORDER BY table_name;
"

echo ""
echo "Checking indexes..."
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
SELECT indexname
FROM pg_indexes
WHERE schemaname = 'mes'
  AND indexname LIKE 'idx_work_progress%'
ORDER BY indexname;
"

echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}Database Migration Complete!${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo "Next step: ./deploy/03-build-backend.sh"
echo ""

# Clean up
unset PGPASSWORD
