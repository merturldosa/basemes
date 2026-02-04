# 백엔드 빌드 이슈 보고서

**작성일**: 2026-01-25 19:30 KST
**작업**: E2E 통합 테스트 준비 중 백엔드 빌드 실패
**상태**: ❌ 빌드 불가

---

## 요약

E2E 통합 테스트를 실행하기 위해 백엔드를 재빌드하려 했으나, 다수의 컴파일 에러로 인해 빌드가 실패했습니다.

**근본 원인**:
- Warehouse, Supplier, Customer Controller가 Spring에 등록되지 않음
- 이유: 백엔드가 빌드되지 않은 상태였음
- 빌드 시도 시 여러 파일에서 import 및 API 변경 이슈 발견

---

## 발견된 주요 이슈

### 1. ResponseDTO → ApiResponse 변경 누락
**영향을 받는 파일**:
- ✅ HolidayController.java - 수정 완료
- ✅ ApprovalController.java - 수정 완료

**수정 내용**:
```bash
# 전체 파일에서 일괄 변경
sed -i 's/ResponseDTO/ApiResponse/g'
```

---

### 2. TenantContextHolder → TenantContext 변경 누락
**영향을 받는 파일**:
- ✅ InventoryAnalysisController.java - 수정 완료
- ✅ BarcodeController.java - 수정 완료
- ✅ LotSelectionController.java - 수정 완료
- ✅ PhysicalInventoryController.java - 수정 완료

**수정 내용**:
```java
// Before
import kr.co.softice.mes.common.util.TenantContextHolder;
String tenantId = TenantContextHolder.getTenantId();

// After
import kr.co.softice.mes.common.security.TenantContext;
String tenantId = TenantContext.getCurrentTenant();
```

---

### 3. 잘못된 패키지 경로 (.inventory 서브패키지 문제)
**문제**: 일부 서비스에서 Entity를 잘못된 패키지에서 import하려 함

**영향을 받는 파일**:
- ✅ InventoryAnalysisService.java - 수정 완료
- ✅ BarcodeService.java - 수정 완료
- ✅ LotSelectionService.java - 수정 완료

**수정 내용**:
```java
// Before (잘못됨)
import kr.co.softice.mes.domain.entity.inventory.InventoryEntity;
import kr.co.softice.mes.domain.repository.inventory.InventoryRepository;

// After (올바름)
import kr.co.softice.mes.domain.entity.InventoryEntity;
import kr.co.softice.mes.domain.repository.InventoryRepository;
```

**참고**: `inventory` 서브패키지는 `PhysicalInventory*` 관련 클래스만 포함

---

### 4. PhysicalInventory 관련 import 문제
**영향을 받는 파일**:
- ⏳ PhysicalInventoryEntity.java - 부분 수정
- ⏳ PhysicalInventoryItemEntity.java - 부분 수정
- ⏳ PhysicalInventoryService.java - 수정 필요
- ⏳ PhysicalInventoryController.java - 수정 필요
- ⏳ PhysicalInventoryRepository.java - 부분 수정

**임시 조치**: 빌드를 위해 PhysicalInventory 관련 파일들을 `backend/temp_exclude/`로 이동

**이유**:
- 반복적인 import 순환 참조 문제
- WMS 테스트에 필수적이지 않음
- 나중에 별도로 수정 가능

---

### 5. ShippingService.java 메서드 호출 오류
**파일**: ShippingService.java:525

**문제**:
```java
.unit(item.getUnit())  // ShippingItemEntity에 getUnit() 없음
```

**수정**:
```java
.unit(item.getProduct().getUnit())  // Product에서 unit 가져옴
```

---

## 수정 완료된 파일 목록

### Controllers (6개)
1. ✅ HolidayController.java - ResponseDTO → ApiResponse
2. ✅ ApprovalController.java - ResponseDTO → ApiResponse
3. ✅ InventoryAnalysisController.java - TenantContextHolder → TenantContext
4. ✅ BarcodeController.java - TenantContextHolder + import
5. ✅ LotSelectionController.java - TenantContextHolder + import
6. ❌ PhysicalInventoryController.java - 임시 제외

### Services (5개)
7. ✅ InventoryAnalysisService.java - import 경로 수정
8. ✅ BarcodeService.java - import 경로 수정
9. ✅ LotSelectionService.java - import 경로 수정
10. ✅ ShippingService.java - getUnit() 수정
11. ❌ PhysicalInventoryService.java - 임시 제외

### Entities (3개)
12. ⏳ PhysicalInventoryEntity.java - WarehouseEntity import 추가 (임시 제외)
13. ⏳ PhysicalInventoryItemEntity.java - LotEntity/InventoryTransactionEntity import 추가 (임시 제외)

### Repositories (1개)
14. ⏳ PhysicalInventoryRepository.java - import 경로 수정 (임시 제외)

---

## 현재 빌드 상태

**마지막 시도**: 2026-01-25 19:30 KST
**결과**: ❌ BUILD FAILURE

**남은 에러**: 추가 에러가 계속 발견됨 (전체 로그 확인 필요)

---

## 권장 조치 사항

### Option 1: 전체 빌드 에러 로그 분석 (추천)
```bash
cd backend
mvn clean compile 2>&1 | tee build_errors.log
# build_errors.log 분석하여 모든 에러 파악
```

### Option 2: 최소 기능 빌드
PhysicalInventory를 제외한 상태에서 빌드가 성공하도록 나머지 에러 수정

### Option 3: Git 히스토리 확인
빌드가 마지막으로 성공했던 커밋으로 돌아가서 차이점 확인

### Option 4: IDE 도구 사용
IntelliJ IDEA 또는 Eclipse에서 프로젝트 열어서 모든 컴파일 에러 한눈에 확인

---

## 임시 제외된 파일

**위치**: `backend/temp_exclude/`

**파일 목록**:
- PhysicalInventoryEntity.java
- PhysicalInventoryItemEntity.java
- PhysicalInventoryRepository.java
- PhysicalInventoryService.java
- PhysicalInventoryController.java

**복원 방법**:
```bash
mv backend/temp_exclude/Physical*.java backend/src/main/java/kr/co/softice/mes/domain/entity/inventory/
mv backend/temp_exclude/PhysicalInventoryRepository.java backend/src/main/java/kr/co/softice/mes/domain/repository/inventory/
mv backend/temp_exclude/PhysicalInventoryService.java backend/src/main/java/kr/co/softice/mes/domain/service/
mv backend/temp_exclude/PhysicalInventoryController.java backend/src/main/java/kr/co/softice/mes/api/controller/
```

---

## 다음 단계

### 즉시 조치
1. **전체 빌드 에러 로그 확인**: 모든 컴파일 에러 목록 확보
2. **IDE에서 프로젝트 열기**: 시각적으로 에러 확인 및 수정
3. **단계적 빌드**: 하나씩 수정하며 빌드 성공 확인

### 중기 조치
1. **CI/CD 파이프라인 구축**: 자동 빌드로 조기 에러 발견
2. **코드 리뷰 프로세스**: API 변경 시 영향도 분석
3. **정적 분석 도구**: Checkstyle, SpotBugs로 잠재 오류 발견

---

## 결론

백엔드 빌드 실패로 인해 E2E 통합 테스트를 진행할 수 없는 상태입니다.

**핵심 문제**:
- ResponseDTO → ApiResponse 마이그레이션 미완료
- TenantContextHolder → TenantContext 마이그레이션 미완료
- import 경로 불일치 (`.inventory` 서브패키지)
- 빌드 검증 부재로 누적된 에러

**예상 작업 시간**:
- 모든 에러 수정 시: 2-3시간
- 최소 기능만 동작: 1시간

**대안**:
- 기존에 빌드된 JAR 파일이 있다면 그것을 사용
- 또는 빌드 성공했던 이전 커밋으로 체크아웃

---

**작성자**: Claude Sonnet 4.5
**작성일**: 2026-01-25 19:30 KST
**문서 버전**: 1.0
