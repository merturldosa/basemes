#!/bin/bash
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# Phase 2 Deployment - Master Deployment Script
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# Author: Moon Myung-seop
# Date: 2026-02-05
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

SCRIPT_DIR="$(dirname "$0")"

echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                                                        ║${NC}"
echo -e "${BLUE}║      SoIce MES Phase 2 - Complete Deployment          ║${NC}"
echo -e "${BLUE}║                                                        ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""

# Step 1: Environment Check
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Step 1/4: Environment Check${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

if [ -f "$SCRIPT_DIR/01-check-environment.sh" ]; then
    bash "$SCRIPT_DIR/01-check-environment.sh"
else
    echo -e "${RED}✗${NC} 01-check-environment.sh not found"
    exit 1
fi

echo ""
read -p "Continue with database migration? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Deployment cancelled"
    exit 0
fi

# Step 2: Database Migration
echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Step 2/4: Database Migration${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

if [ -f "$SCRIPT_DIR/02-migrate-database.sh" ]; then
    bash "$SCRIPT_DIR/02-migrate-database.sh"
else
    echo -e "${RED}✗${NC} 02-migrate-database.sh not found"
    exit 1
fi

echo ""
read -p "Continue with backend build? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Deployment stopped after database migration"
    exit 0
fi

# Step 3: Backend Build
echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Step 3/4: Backend Build${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

if [ -f "$SCRIPT_DIR/03-build-backend.sh" ]; then
    bash "$SCRIPT_DIR/03-build-backend.sh"
else
    echo -e "${RED}✗${NC} 03-build-backend.sh not found"
    exit 1
fi

echo ""
read -p "Continue with frontend build? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Deployment stopped after backend build"
    exit 0
fi

# Step 4: Frontend Build
echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Step 4/4: Frontend Build${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

if [ -f "$SCRIPT_DIR/04-build-frontend.sh" ]; then
    bash "$SCRIPT_DIR/04-build-frontend.sh"
else
    echo -e "${RED}✗${NC} 04-build-frontend.sh not found"
    exit 1
fi

# Completion
echo ""
echo -e "${GREEN}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║                                                        ║${NC}"
echo -e "${GREEN}║            DEPLOYMENT COMPLETED SUCCESSFULLY           ║${NC}"
echo -e "${GREEN}║                                                        ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${YELLOW}Next Steps:${NC}"
echo ""
echo "1. Start the backend application:"
echo "   cd backend"
echo "   mvn spring-boot:run"
echo "   OR"
echo "   java -jar target/soice-mes-0.0.1-SNAPSHOT.jar"
echo ""
echo "2. The frontend is built and ready in frontend/dist/"
echo "   For development:"
echo "     cd frontend"
echo "     npm run dev"
echo ""
echo "   For production deployment:"
echo "     - Copy frontend/dist/ to your web server"
echo "     - Configure nginx/apache"
echo "     - Set up SSL certificate"
echo ""
echo "3. Access the application:"
echo "   - Backend API: http://localhost:8080"
echo "   - Frontend (dev): http://localhost:5173"
echo "   - Frontend (prod): http://your-domain.com"
echo ""
echo "4. Review documentation:"
echo "   - docs/POP_OPERATOR_QUICK_START.md - 운영자 가이드"
echo "   - docs/POP_API_REFERENCE.md - API 레퍼런스"
echo "   - docs/POP_MOBILE_OPTIMIZATION_GUIDE.md - 모바일 최적화"
echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
