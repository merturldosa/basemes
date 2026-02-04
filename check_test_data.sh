#!/bin/bash

# Get token
TOKEN=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{"tenantId":"DEMO001","username":"admin","password":"admin123"}' \
  http://localhost:8080/api/auth/login | grep -o '"accessToken":"[^"]*"' | sed 's/"accessToken":"\(.*\)"/\1/')

echo "=== Checking Test Data ==="
echo ""

echo "1. Suppliers:"
curl -s -X GET \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: DEMO001" \
  http://localhost:8080/api/suppliers | head -c 500
echo ""
echo ""

echo "2. Products:"
curl -s -X GET \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: DEMO001" \
  http://localhost:8080/api/products | head -c 500
echo ""
echo ""

echo "3. Warehouses:"
curl -s -X GET \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: DEMO001" \
  http://localhost:8080/api/warehouses | head -c 500
echo ""
