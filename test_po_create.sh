#!/bin/bash

# Get token
TOKEN=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{"tenantId":"DEMO001","username":"admin","password":"admin123"}' \
  http://localhost:8080/api/auth/login | grep -o '"accessToken":"[^"]*"' | sed 's/"accessToken":"\(.*\)"/\1/')

echo "Token: ${TOKEN:0:50}..."

# Create PO
echo ""
echo "Creating Purchase Order..."
PO_RESPONSE=$(curl -v -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: DEMO001" \
  -H "Content-Type: application/json" \
  -d '{
  "orderNo": "PO-TEST-001",
  "orderDate": "2026-01-25",
  "supplierId": 1,
  "items": [{
    "productId": 1,
    "orderedQuantity": 1000,
    "unitPrice": 5000,
    "lineAmount": 5000000
  }],
  "totalAmount": 5000000,
  "deliveryDate": "2026-01-30",
  "orderStatus": "APPROVED"
}' \
  http://localhost:8080/api/purchase-orders 2>&1)

echo "$PO_RESPONSE"
