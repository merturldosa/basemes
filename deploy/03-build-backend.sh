#!/bin/bash
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# Phase 2 Deployment - Step 3: Build Backend
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# Author: Moon Myung-seop
# Date: 2026-02-05
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

set -e

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "SoIce MES Phase 2 - Backend Build"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

cd "$(dirname "$0")/../backend"

echo "Current directory: $(pwd)"
echo ""

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}✗${NC} Maven is not installed"
    exit 1
fi

echo "Maven version:"
mvn --version
echo ""

# Clean
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 1: Clean"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
mvn clean
echo -e "${GREEN}✓${NC} Clean complete"
echo ""

# Compile
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 2: Compile"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
mvn compile
echo -e "${GREEN}✓${NC} Compilation complete"
echo ""

# Test (optional)
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 3: Test (optional)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
read -p "Run tests? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Running POPIntegrationTest..."
    mvn test -Dtest=POPIntegrationTest
    echo -e "${GREEN}✓${NC} Tests complete"
else
    echo "Tests skipped"
fi
echo ""

# Package
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 4: Package"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
mvn package -DskipTests
echo -e "${GREEN}✓${NC} Package complete"
echo ""

# Check JAR
echo "Checking JAR file..."
JAR_FILE=$(find target -name "*.jar" -not -name "*-sources.jar" | head -1)
if [ -f "$JAR_FILE" ]; then
    echo -e "${GREEN}✓${NC} JAR file created: $JAR_FILE"
    ls -lh "$JAR_FILE"
else
    echo -e "${RED}✗${NC} JAR file not found"
    exit 1
fi
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${GREEN}Backend Build Complete!${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "To run the backend:"
echo "  cd backend"
echo "  mvn spring-boot:run"
echo ""
echo "Or run the JAR:"
echo "  java -jar $JAR_FILE"
echo ""
echo "Next step: ./deploy/04-build-frontend.sh"
echo ""
