#!/bin/bash

echo "Fixing import issues in backend..."

# Fix .inventory package imports
find backend/src -name "*.java" -type f -exec sed -i \
  -e 's/kr\.co\.softice\.mes\.domain\.entity\.inventory\./kr.co.softice.mes.domain.entity./g' \
  -e 's/kr\.co\.softice\.mes\.domain\.repository\.inventory\./kr.co.softice.mes.domain.repository./g' \
  {} \;

# Fix TenantContextHolder -> TenantContext
find backend/src -name "*.java" -type f -exec sed -i \
  -e 's/import kr\.co\.softice\.mes\.common\.util\.TenantContextHolder;/import kr.co.softice.mes.common.security.TenantContext;/g' \
  -e 's/TenantContextHolder\.getTenantId()/TenantContext.getCurrentTenant()/g' \
  {} \;

# Fix ResponseDTO -> ApiResponse
find backend/src -name "*.java" -type f -exec sed -i \
  -e 's/import kr\.co\.softice\.mes\.common\.dto\.ResponseDTO;/import kr.co.softice.mes.common.dto.ApiResponse;/g' \
  -e 's/ResponseDTO/ApiResponse/g' \
  {} \;

echo "Import fixes completed!"
