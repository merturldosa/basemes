#!/bin/bash

# Get token
TOKEN=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{"tenantId":"DEMO001","username":"admin","password":"admin123"}' \
  http://localhost:8080/api/auth/login | grep -o '"accessToken":"[^"]*"' | sed 's/"accessToken":"\(.*\)"/\1/')

echo "Token obtained: ${TOKEN:0:50}..."
echo ""

# Create Supplier 1
echo "Creating Supplier 1 (ABC 전자부품)..."
SUPPLIER1_RESPONSE=$(curl -s -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: DEMO001" \
  -H "Content-Type: application/json" \
  -d '{
  "supplierCode": "SUP-001",
  "supplierName": "ABC 전자부품",
  "businessNumber": "123-45-67890",
  "representativeName": "박공급",
  "contactPerson": "이담당",
  "contactPhone": "02-1234-5678",
  "contactEmail": "contact@abc-elec.com",
  "address": "서울시 금천구 가산디지털1로 123",
  "supplierType": "RAW_MATERIAL",
  "paymentTerms": "NET30",
  "isActive": true
}' \
  http://localhost:8080/api/suppliers 2>&1)

echo "$SUPPLIER1_RESPONSE"
echo ""

# Create Customer 1
echo "Creating Customer 1 (대한전자)..."
CUSTOMER1_RESPONSE=$(curl -s -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: DEMO001" \
  -H "Content-Type: application/json" \
  -d '{
  "customerCode": "CUST-001",
  "customerName": "대한전자",
  "businessNumber": "111-22-33444",
  "representativeName": "김대표",
  "contactPerson": "박구매",
  "contactPhone": "02-2222-3333",
  "contactEmail": "purchase@daehan.com",
  "address": "서울시 강남구 테헤란로 789",
  "customerType": "MANUFACTURER",
  "paymentTerms": "NET30",
  "isActive": true
}' \
  http://localhost:8080/api/customers 2>&1)

echo "$CUSTOMER1_RESPONSE"
echo ""

# Create Warehouse 1
echo "Creating Warehouse 1 (원자재 창고)..."
WAREHOUSE1_RESPONSE=$(curl -s -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: DEMO001" \
  -H "Content-Type: application/json" \
  -d '{
  "warehouseCode": "WH-RAW",
  "warehouseName": "원자재 창고",
  "warehouseType": "RAW_MATERIAL",
  "location": "1동 1층",
  "managerName": "김재고",
  "capacity": 10000.00,
  "isActive": true
}' \
  http://localhost:8080/api/warehouses 2>&1)

echo "$WAREHOUSE1_RESPONSE"
echo ""

echo "Test data creation completed!"
