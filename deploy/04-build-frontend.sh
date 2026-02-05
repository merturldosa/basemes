#!/bin/bash
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# Phase 2 Deployment - Step 4: Build Frontend
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# Author: Moon Myung-seop
# Date: 2026-02-05
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

set -e

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "SoIce MES Phase 2 - Frontend Build"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

cd "$(dirname "$0")/../frontend"

echo "Current directory: $(pwd)"
echo ""

# Check Node.js and npm
if ! command -v node &> /dev/null; then
    echo -e "${RED}✗${NC} Node.js is not installed"
    exit 1
fi

if ! command -v npm &> /dev/null; then
    echo -e "${RED}✗${NC} npm is not installed"
    exit 1
fi

echo "Node.js version:"
node --version
echo ""

echo "npm version:"
npm --version
echo ""

# Install dependencies
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 1: Install Dependencies"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ -d "node_modules" ]; then
    echo "node_modules exists. Checking for updates..."
    npm install
else
    echo "Installing dependencies for the first time..."
    npm install
fi

echo -e "${GREEN}✓${NC} Dependencies installed"
echo ""

# Lint (optional)
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 2: Lint (optional)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
read -p "Run linter? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    npm run lint
    echo -e "${GREEN}✓${NC} Linting complete"
else
    echo "Linting skipped"
fi
echo ""

# Test (optional)
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 3: Test (optional)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
read -p "Run tests? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    npm run test:unit
    echo -e "${GREEN}✓${NC} Tests complete"
else
    echo "Tests skipped"
fi
echo ""

# Build
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 4: Build"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
npm run build
echo -e "${GREEN}✓${NC} Build complete"
echo ""

# Check dist directory
echo "Checking build output..."
if [ -d "dist" ]; then
    echo -e "${GREEN}✓${NC} dist directory created"
    echo ""
    echo "Build statistics:"
    du -sh dist
    echo ""
    echo "File count:"
    find dist -type f | wc -l
else
    echo -e "${RED}✗${NC} dist directory not found"
    exit 1
fi
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${GREEN}Frontend Build Complete!${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "To preview the build:"
echo "  npm run preview"
echo ""
echo "To deploy to production:"
echo "  1. Copy dist/ to your web server"
echo "  2. Configure nginx/apache to serve the files"
echo "  3. Set up SSL certificate"
echo ""
echo "For development:"
echo "  npm run dev"
echo ""
echo "Next step: Deploy both backend and frontend"
echo ""
