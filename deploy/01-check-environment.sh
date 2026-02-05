#!/bin/bash
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# Phase 2 Deployment - Step 1: Environment Check
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# Author: Moon Myung-seop
# Date: 2026-02-05
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

set -e

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "SoIce MES Phase 2 - Environment Check"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check function
check_command() {
    if command -v $1 &> /dev/null; then
        echo -e "${GREEN}✓${NC} $1 is installed"
        $1 --version | head -1
        return 0
    else
        echo -e "${RED}✗${NC} $1 is NOT installed"
        return 1
    fi
}

# Check PostgreSQL
check_postgres() {
    echo ""
    echo "Checking PostgreSQL..."
    if command -v psql &> /dev/null; then
        echo -e "${GREEN}✓${NC} PostgreSQL client is installed"
        psql --version

        # Try to connect
        if psql -U mes_admin -d soice_mes_dev -c "SELECT version();" &> /dev/null; then
            echo -e "${GREEN}✓${NC} Database connection successful"
        else
            echo -e "${YELLOW}⚠${NC} Cannot connect to database (may not be running)"
        fi
    else
        echo -e "${RED}✗${NC} PostgreSQL is NOT installed"
    fi
}

echo "1. Checking Java..."
check_command java

echo ""
echo "2. Checking Maven..."
check_command mvn

echo ""
echo "3. Checking Node.js..."
check_command node

echo ""
echo "4. Checking npm..."
check_command npm

echo ""
echo "5. Checking Git..."
check_command git

check_postgres

echo ""
echo "6. Checking Git status..."
cd "$(dirname "$0")/.."
git status --short

echo ""
echo "7. Checking latest commit..."
git log --oneline -1

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Environment Check Complete"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Next steps:"
echo "1. Ensure PostgreSQL is running"
echo "2. Run: ./deploy/02-migrate-database.sh"
echo "3. Run: ./deploy/03-build-backend.sh"
echo "4. Run: ./deploy/04-build-frontend.sh"
echo ""
