#!/bin/bash
# Weighing API End-to-End Test Script
# Tests complete workflow of weighing management

BASE_URL="http://localhost:8080"
API_URL="$BASE_URL/api"
TENANT_ID="tenant1"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "======================================================"
echo "  Weighing API E2E Test"
echo "  Testing GMP-compliant weighing management system"
echo "======================================================"
echo ""

# Function to print test results
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ PASS${NC}: $2"
    else
        echo -e "${RED}✗ FAIL${NC}: $2"
    fi
}

# Test 1: Authentication (if required)
echo "Test 1: Authentication"
echo "-----------------------------------------------------"
# Try to get an auth token (modify based on your auth setup)
# For now, we'll try without authentication and see what happens
echo "Attempting to access API without authentication..."
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -H "X-Tenant-ID: $TENANT_ID" "$API_URL/weighings")
echo "Response code: $RESPONSE"

if [ "$RESPONSE" == "401" ] || [ "$RESPONSE" == "403" ]; then
    echo -e "${YELLOW}⚠ Authentication required${NC}"
    echo "Please provide authentication credentials"
    echo ""
    echo "If you have test credentials, update this script with:"
    echo "  - Login endpoint"
    echo "  - Test username/password"
    echo "  - JWT token extraction"
    exit 1
elif [ "$RESPONSE" == "200" ]; then
    echo -e "${GREEN}✓ API accessible without authentication (test mode)${NC}"
else
    echo -e "${YELLOW}⚠ Unexpected response: $RESPONSE${NC}"
fi
echo ""

# Test 2: Create Weighing
echo "Test 2: Create Weighing with Auto-Calculation"
echo "-----------------------------------------------------"
CREATE_PAYLOAD='{
  "weighingType": "INCOMING",
  "productId": 1,
  "tareWeight": 50.0,
  "grossWeight": 1050.0,
  "expectedWeight": 1000.0,
  "unit": "kg",
  "operatorUserId": 1,
  "tolerancePercentage": 2.0,
  "temperature": 22.5,
  "humidity": 45.0,
  "remarks": "E2E Test - Initial weighing"
}'

echo "Creating weighing..."
CREATE_RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: $TENANT_ID" \
  -d "$CREATE_PAYLOAD" \
  "$API_URL/weighings")

echo "Response:"
echo "$CREATE_RESPONSE" | python -m json.tool 2>/dev/null || echo "$CREATE_RESPONSE"
echo ""

# Extract weighing ID (if successful)
WEIGHING_ID=$(echo "$CREATE_RESPONSE" | grep -o '"weighingId":[0-9]*' | grep -o '[0-9]*' | head -1)

if [ -n "$WEIGHING_ID" ]; then
    echo -e "${GREEN}✓ Weighing created with ID: $WEIGHING_ID${NC}"

    # Verify calculations
    NET_WEIGHT=$(echo "$CREATE_RESPONSE" | grep -o '"netWeight":[0-9.]*' | grep -o '[0-9.]*' | head -1)
    VARIANCE=$(echo "$CREATE_RESPONSE" | grep -o '"variance":[0-9.-]*' | grep -o '[0-9.-]*' | head -1)

    echo "  - Net Weight: $NET_WEIGHT (expected: 1000.0)"
    echo "  - Variance: $VARIANCE (expected: 0.0)"
else
    echo -e "${RED}✗ Failed to create weighing${NC}"
    echo "Stopping tests..."
    exit 1
fi
echo ""

# Test 3: Get Weighing Detail
echo "Test 3: Get Weighing Detail"
echo "-----------------------------------------------------"
DETAIL_RESPONSE=$(curl -s -H "X-Tenant-ID: $TENANT_ID" "$API_URL/weighings/$WEIGHING_ID")
echo "Response:"
echo "$DETAIL_RESPONSE" | python -m json.tool 2>/dev/null || echo "$DETAIL_RESPONSE"
echo ""

# Test 4: Get All Weighings
echo "Test 4: Get All Weighings"
echo "-----------------------------------------------------"
LIST_RESPONSE=$(curl -s -H "X-Tenant-ID: $TENANT_ID" "$API_URL/weighings")
echo "Response:"
echo "$LIST_RESPONSE" | python -m json.tool 2>/dev/null || echo "$LIST_RESPONSE"
echo ""

# Test 5: Update Weighing
echo "Test 5: Update Weighing"
echo "-----------------------------------------------------"
UPDATE_PAYLOAD='{
  "tareWeight": 55.0,
  "grossWeight": 1055.0,
  "remarks": "E2E Test - Updated weights"
}'

echo "Updating weighing..."
UPDATE_RESPONSE=$(curl -s -X PUT \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: $TENANT_ID" \
  -d "$UPDATE_PAYLOAD" \
  "$API_URL/weighings/$WEIGHING_ID")

echo "Response:"
echo "$UPDATE_RESPONSE" | python -m json.tool 2>/dev/null || echo "$UPDATE_RESPONSE"

# Verify recalculation
NEW_NET_WEIGHT=$(echo "$UPDATE_RESPONSE" | grep -o '"netWeight":[0-9.]*' | grep -o '[0-9.]*' | head -1)
echo "  - New Net Weight: $NEW_NET_WEIGHT (expected: 1000.0)"
echo ""

# Test 6: Verify Weighing (Dual Verification)
echo "Test 6: Verify Weighing (GMP Dual Verification)"
echo "-----------------------------------------------------"
VERIFY_PAYLOAD='{
  "verifierUserId": 2,
  "action": "VERIFY",
  "remarks": "E2E Test - Verified by different user"
}'

echo "Verifying weighing..."
VERIFY_RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: $TENANT_ID" \
  -d "$VERIFY_PAYLOAD" \
  "$API_URL/weighings/$WEIGHING_ID/verify")

echo "Response:"
echo "$VERIFY_RESPONSE" | python -m json.tool 2>/dev/null || echo "$VERIFY_RESPONSE"

VERIFICATION_STATUS=$(echo "$VERIFY_RESPONSE" | grep -o '"verificationStatus":"[A-Z]*"' | cut -d'"' -f4)
echo "  - Verification Status: $VERIFICATION_STATUS (expected: VERIFIED)"
echo ""

# Test 7: Get Pending Verification Queue
echo "Test 7: Get Pending Verification Queue"
echo "-----------------------------------------------------"
PENDING_RESPONSE=$(curl -s -H "X-Tenant-ID: $TENANT_ID" "$API_URL/weighings/pending-verification")
echo "Response:"
echo "$PENDING_RESPONSE" | python -m json.tool 2>/dev/null || echo "$PENDING_RESPONSE"
echo ""

# Test 8: Create Weighing with Tolerance Exceeded
echo "Test 8: Create Weighing with Tolerance Exceeded"
echo "-----------------------------------------------------"
TOLERANCE_PAYLOAD='{
  "weighingType": "INCOMING",
  "productId": 1,
  "tareWeight": 50.0,
  "grossWeight": 1100.0,
  "expectedWeight": 1000.0,
  "unit": "kg",
  "operatorUserId": 1,
  "tolerancePercentage": 2.0,
  "remarks": "E2E Test - High variance"
}'

echo "Creating weighing with high variance..."
TOLERANCE_RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: $TENANT_ID" \
  -d "$TOLERANCE_PAYLOAD" \
  "$API_URL/weighings")

TOLERANCE_EXCEEDED=$(echo "$TOLERANCE_RESPONSE" | grep -o '"toleranceExceeded":[a-z]*' | cut -d':' -f2)
echo "  - Tolerance Exceeded: $TOLERANCE_EXCEEDED (expected: true)"
echo ""

# Test 9: Get Tolerance Exceeded Weighings
echo "Test 9: Get Tolerance Exceeded Weighings"
echo "-----------------------------------------------------"
EXCEEDED_RESPONSE=$(curl -s -H "X-Tenant-ID: $TENANT_ID" "$API_URL/weighings/tolerance-exceeded")
echo "Response:"
echo "$EXCEEDED_RESPONSE" | python -m json.tool 2>/dev/null || echo "$EXCEEDED_RESPONSE"
echo ""

# Summary
echo "======================================================"
echo "  Test Summary"
echo "======================================================"
echo "All manual tests completed."
echo ""
echo "Next Steps:"
echo "1. Verify database records in PostgreSQL"
echo "2. Check audit logs for all operations"
echo "3. Test with different tenant IDs"
echo "4. Test error scenarios (invalid data, missing fields)"
echo "5. Test with real scale integration (if available)"
echo ""
echo "For database verification, run:"
echo "  psql -U postgres -d soice_mes_db -c \"SELECT * FROM wms.si_weighings ORDER BY created_at DESC LIMIT 10;\""
echo ""
