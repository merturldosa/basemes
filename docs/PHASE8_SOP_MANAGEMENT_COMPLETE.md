# Phase 8: SOP Management Implementation - Complete

**작성자**: Moon Myung-seop (문명섭)
**작성일**: 2026-01-25
**프로젝트**: SoIce MES Platform

---

## 목차

1. [개요](#1-개요)
2. [구현 범위](#2-구현-범위)
3. [데이터베이스 스키마](#3-데이터베이스-스키마)
4. [엔티티 (Entities)](#4-엔티티-entities)
5. [레포지토리 (Repositories)](#5-레포지토리-repositories)
6. [서비스 (Services)](#6-서비스-services)
7. [컨트롤러 (Controllers)](#7-컨트롤러-controllers)
8. [API 명세](#8-api-명세)
9. [비즈니스 로직](#9-비즈니스-로직)
10. [사용 시나리오](#10-사용-시나리오)
11. [다음 단계](#11-다음-단계)

---

## 1. 개요

### 1.1 목적

SOP (Standard Operating Procedure, 표준 작업 절차) 관리 모듈은 제조 현장에서 작업 절차를 표준화하고 실행 기록을 추적하기 위한 시스템입니다.

### 1.2 주요 기능

- **문서 양식 관리**: 재사용 가능한 문서 템플릿 관리
- **SOP 작성**: 단계별 작업 절차 정의
- **버전 관리**: SOP 버전 추적 및 관리
- **승인 워크플로우**: DRAFT → PENDING → APPROVED 프로세스
- **실행 기록**: SOP 실행 과정 추적
- **단계별 체크리스트**: 각 단계의 체크리스트 및 증빙 자료 관리
- **검토 주기 관리**: 정기 검토 일정 관리

### 1.3 적용 분야

- **생산 관리** (PRODUCTION): 제품 생산 표준 절차
- **창고 관리** (WAREHOUSE): 입출하 작업 절차
- **품질 관리** (QUALITY): 검사 및 품질 관리 절차
- **설비 관리** (FACILITY): 설비 운영 및 유지보수 절차
- **안전 관리** (SAFETY): 안전 작업 절차
- **유지보수** (MAINTENANCE): 정기/비정기 유지보수 절차

---

## 2. 구현 범위

### 2.1 구현 완료 항목

#### 데이터베이스
- ✅ V022__create_sop_schema.sql (5개 테이블)
  - document_templates
  - sops
  - sop_steps
  - sop_executions
  - sop_execution_steps

#### 백엔드
- ✅ **엔티티 (5개)**
  - DocumentTemplateEntity
  - SOPEntity
  - SOPStepEntity
  - SOPExecutionEntity
  - SOPExecutionStepEntity

- ✅ **레포지토리 (5개)**
  - DocumentTemplateRepository
  - SOPRepository
  - SOPStepRepository
  - SOPExecutionRepository
  - SOPExecutionStepRepository

- ✅ **서비스 (2개)**
  - DocumentTemplateService
  - SOPService

- ✅ **컨트롤러 (2개)**
  - DocumentTemplateController (15개 엔드포인트)
  - SOPController (23개 엔드포인트)

### 2.2 미구현 항목 (다음 단계)

- ⏳ **프론트엔드**
  - SOPsPage.tsx (SOP 목록 및 상세)
  - SOPExecutionPage.tsx (실행 화면)
  - sopService.ts (API 서비스)

- ⏳ **고급 기능**
  - 파일 업로드 (문서, 이미지, 동영상)
  - 디지털 서명
  - QR 코드 생성 (SOP 실행 추적용)
  - 모바일 반응형 UI

---

## 3. 데이터베이스 스키마

### 3.1 ERD 구조

```
document_templates (문서 양식)
    ↓ (1:N)
sops (표준 작업 절차)
    ↓ (1:N)
sop_steps (SOP 단계)
    ↑ (자기 참조: prerequisite_step_id)

sops
    ↓ (1:N)
sop_executions (실행 기록)
    ↓ (1:N)
sop_execution_steps (실행 단계 결과)
    ↑ (N:1)
sop_steps
```

### 3.2 테이블 상세

#### 3.2.1 document_templates (문서 양식)

```sql
CREATE TABLE common.document_templates (
    template_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    template_code VARCHAR(50) NOT NULL,
    template_name VARCHAR(200) NOT NULL,
    description TEXT,

    -- Template classification
    template_type VARCHAR(50) NOT NULL,  -- SOP, CHECKLIST, INSPECTION_SHEET, REPORT
    category VARCHAR(50),                -- PRODUCTION, WAREHOUSE, QUALITY, FACILITY

    -- File information
    file_name VARCHAR(255),
    file_path VARCHAR(500),
    file_type VARCHAR(50),               -- EXCEL, WORD, PDF, HTML
    file_size BIGINT,
    template_content TEXT,               -- For HTML templates

    -- Version management
    version VARCHAR(20) DEFAULT '1.0',
    is_latest BOOLEAN DEFAULT TRUE,

    -- Metadata
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_template_code UNIQUE (tenant_id, template_code, version)
);
```

**인덱스**:
- idx_template_tenant (tenant_id)
- idx_template_type (template_type)
- idx_template_category (category)
- idx_template_active (is_active)
- idx_template_latest (is_latest)

#### 3.2.2 sops (표준 작업 절차)

```sql
CREATE TABLE common.sops (
    sop_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    sop_code VARCHAR(50) NOT NULL,
    sop_name VARCHAR(200) NOT NULL,
    description TEXT,

    -- Classification
    sop_type VARCHAR(50) NOT NULL,       -- PRODUCTION, WAREHOUSE, QUALITY, FACILITY, SAFETY, MAINTENANCE
    category VARCHAR(50),
    target_process VARCHAR(100),         -- Target process/operation

    -- Template link
    template_id BIGINT REFERENCES document_templates(template_id),

    -- Version management
    version VARCHAR(20) DEFAULT '1.0',
    revision_date DATE,
    effective_date DATE,
    review_date DATE,
    next_review_date DATE,

    -- Approval workflow
    approval_status VARCHAR(50) DEFAULT 'DRAFT',  -- DRAFT, PENDING, APPROVED, REJECTED, OBSOLETE
    approved_by BIGINT REFERENCES users(user_id),
    approved_at TIMESTAMP,

    -- Document information
    document_url VARCHAR(500),
    attachments JSONB,                   -- Array of attachment info

    -- Access control
    required_role VARCHAR(100),
    restricted BOOLEAN DEFAULT FALSE,

    -- Metadata
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_sop_code UNIQUE (tenant_id, sop_code, version)
);
```

**인덱스**:
- idx_sop_tenant (tenant_id)
- idx_sop_type (sop_type)
- idx_sop_category (category)
- idx_sop_status (approval_status)
- idx_sop_active (is_active)
- idx_sop_effective (effective_date)

#### 3.2.3 sop_steps (SOP 단계)

```sql
CREATE TABLE common.sop_steps (
    sop_step_id BIGSERIAL PRIMARY KEY,
    sop_id BIGINT NOT NULL REFERENCES sops(sop_id) ON DELETE CASCADE,
    step_number INTEGER NOT NULL,
    step_title VARCHAR(200) NOT NULL,
    step_description TEXT,

    -- Step details
    step_type VARCHAR(50),               -- PREPARATION, EXECUTION, INSPECTION, DOCUMENTATION, SAFETY
    estimated_duration INTEGER,          -- Minutes

    -- Instructions
    detailed_instruction TEXT,
    caution_notes TEXT,
    quality_points TEXT,

    -- Media
    image_urls JSONB,                    -- Array of image URLs
    video_url VARCHAR(500),

    -- Checklist items
    checklist_items JSONB,               -- Array of checklist items

    -- Dependencies
    prerequisite_step_id BIGINT REFERENCES sop_steps(sop_step_id) ON DELETE SET NULL,

    -- Flags
    is_critical BOOLEAN DEFAULT FALSE,
    is_mandatory BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_sop_step UNIQUE (sop_id, step_number)
);
```

**인덱스**:
- idx_sop_step_sop (sop_id)
- idx_sop_step_number (step_number)
- idx_sop_step_critical (is_critical)

#### 3.2.4 sop_executions (실행 기록)

```sql
CREATE TABLE common.sop_executions (
    execution_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    sop_id BIGINT NOT NULL REFERENCES sops(sop_id) ON DELETE CASCADE,

    -- Execution info
    execution_no VARCHAR(50) NOT NULL,
    execution_date TIMESTAMP NOT NULL,
    executor_id BIGINT NOT NULL REFERENCES users(user_id),
    executor_name VARCHAR(100),

    -- Context (what triggered this execution)
    reference_type VARCHAR(50),          -- WORK_ORDER, INSPECTION, MAINTENANCE, etc.
    reference_id BIGINT,
    reference_no VARCHAR(50),

    -- Execution status
    execution_status VARCHAR(50) DEFAULT 'IN_PROGRESS',  -- IN_PROGRESS, COMPLETED, FAILED, CANCELLED
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration INTEGER,                    -- Minutes

    -- Completion data
    completion_rate DECIMAL(5,2),        -- Percentage
    steps_completed INTEGER,
    steps_total INTEGER,

    -- Review
    reviewer_id BIGINT REFERENCES users(user_id),
    review_status VARCHAR(50),           -- PENDING, APPROVED, REJECTED
    review_comments TEXT,
    reviewed_at TIMESTAMP,

    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_sop_exec_no UNIQUE (tenant_id, execution_no)
);
```

**인덱스**:
- idx_sop_exec_tenant (tenant_id)
- idx_sop_exec_sop (sop_id)
- idx_sop_exec_date (execution_date)
- idx_sop_exec_executor (executor_id)
- idx_sop_exec_status (execution_status)
- idx_sop_exec_reference (reference_type, reference_id)

#### 3.2.5 sop_execution_steps (실행 단계 결과)

```sql
CREATE TABLE common.sop_execution_steps (
    execution_step_id BIGSERIAL PRIMARY KEY,
    execution_id BIGINT NOT NULL REFERENCES sop_executions(execution_id) ON DELETE CASCADE,
    sop_step_id BIGINT NOT NULL REFERENCES sop_steps(sop_step_id) ON DELETE CASCADE,

    step_number INTEGER NOT NULL,
    step_status VARCHAR(50) DEFAULT 'PENDING',  -- PENDING, IN_PROGRESS, COMPLETED, SKIPPED, FAILED

    -- Execution data
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    duration INTEGER,                    -- Minutes

    -- Results
    result_value TEXT,
    checklist_results JSONB,             -- Results for each checklist item

    -- Evidence
    photos JSONB,                        -- Array of photo URLs
    signature VARCHAR(500),              -- Digital signature or approver name

    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_sop_exec_step UNIQUE (execution_id, sop_step_id)
);
```

**인덱스**:
- idx_sop_exec_step_exec (execution_id)
- idx_sop_exec_step_sop_step (sop_step_id)
- idx_sop_exec_step_status (step_status)

---

## 4. 엔티티 (Entities)

### 4.1 DocumentTemplateEntity

**파일**: `backend/src/main/java/kr/co/softice/mes/domain/entity/DocumentTemplateEntity.java`

**주요 필드**:
- `templateId` (PK)
- `tenant` (FK → TenantEntity)
- `templateCode`, `templateName`
- `templateType`, `category`
- `fileName`, `filePath`, `fileType`, `fileSize`
- `templateContent` (HTML 템플릿용)
- `version`, `isLatest`

**관계**:
- `@ManyToOne` → TenantEntity

### 4.2 SOPEntity

**파일**: `backend/src/main/java/kr/co/softice/mes/domain/entity/SOPEntity.java`

**주요 필드**:
- `sopId` (PK)
- `tenant` (FK → TenantEntity)
- `sopCode`, `sopName`
- `sopType`, `category`, `targetProcess`
- `template` (FK → DocumentTemplateEntity)
- `version`, `revisionDate`, `effectiveDate`, `reviewDate`, `nextReviewDate`
- `approvalStatus`, `approvedBy`, `approvedAt`
- `documentUrl`, `attachments` (JSONB)
- `requiredRole`, `restricted`

**관계**:
- `@ManyToOne` → TenantEntity, DocumentTemplateEntity, UserEntity (approvedBy)
- `@OneToMany` → SOPStepEntity (steps), SOPExecutionEntity (executions)

**헬퍼 메서드**:
- `approve(UserEntity approver)`: SOP 승인
- `reject()`: SOP 반려
- `markObsolete()`: SOP 폐기
- `isEditable()`: 편집 가능 여부 확인
- `isExecutable()`: 실행 가능 여부 확인

### 4.3 SOPStepEntity

**파일**: `backend/src/main/java/kr/co/softice/mes/domain/entity/SOPStepEntity.java`

**주요 필드**:
- `sopStepId` (PK)
- `sop` (FK → SOPEntity)
- `stepNumber`, `stepTitle`, `stepDescription`
- `stepType`, `estimatedDuration`
- `detailedInstruction`, `cautionNotes`, `qualityPoints`
- `imageUrls`, `videoUrl` (JSONB/VARCHAR)
- `checklistItems` (JSONB)
- `prerequisiteStep` (FK → SOPStepEntity, 자기 참조)
- `isCritical`, `isMandatory`

**관계**:
- `@ManyToOne` → SOPEntity, SOPStepEntity (prerequisiteStep)
- `@OneToMany` → SOPExecutionStepEntity (executionResults)

**헬퍼 메서드**:
- `canStart(SOPExecutionEntity execution)`: 단계 시작 가능 여부 (전제 단계 완료 확인)
- `isSkippable()`: 건너뛰기 가능 여부

### 4.4 SOPExecutionEntity

**파일**: `backend/src/main/java/kr/co/softice/mes/domain/entity/SOPExecutionEntity.java`

**주요 필드**:
- `executionId` (PK)
- `tenant` (FK → TenantEntity)
- `sop` (FK → SOPEntity)
- `executionNo`, `executionDate`
- `executor` (FK → UserEntity), `executorName`
- `referenceType`, `referenceId`, `referenceNo` (작업 지시, 검사 등 참조)
- `executionStatus`, `startTime`, `endTime`, `duration`
- `completionRate`, `stepsCompleted`, `stepsTotal`
- `reviewer`, `reviewStatus`, `reviewComments`, `reviewedAt`

**관계**:
- `@ManyToOne` → TenantEntity, SOPEntity, UserEntity (executor, reviewer)
- `@OneToMany` → SOPExecutionStepEntity (executionSteps)

**헬퍼 메서드**:
- `start()`: 실행 시작
- `complete()`: 실행 완료
- `fail(String reason)`: 실행 실패
- `cancel(String reason)`: 실행 취소
- `updateCompletionRate()`: 완료율 계산
- `approveReview()`: 검토 승인
- `rejectReview()`: 검토 반려
- `areAllCriticalStepsCompleted()`: 모든 중요 단계 완료 확인

### 4.5 SOPExecutionStepEntity

**파일**: `backend/src/main/java/kr/co/softice/mes/domain/entity/SOPExecutionStepEntity.java`

**주요 필드**:
- `executionStepId` (PK)
- `execution` (FK → SOPExecutionEntity)
- `sopStep` (FK → SOPStepEntity)
- `stepNumber`, `stepStatus`
- `startedAt`, `completedAt`, `duration`
- `resultValue`, `checklistResults` (JSONB)
- `photos` (JSONB), `signature`

**관계**:
- `@ManyToOne` → SOPExecutionEntity, SOPStepEntity

**헬퍼 메서드**:
- `start()`: 단계 시작
- `complete(String resultValue)`: 단계 완료
- `skip(String reason)`: 단계 건너뛰기
- `fail(String reason)`: 단계 실패
- `addPhoto(String photoUrl)`: 증빙 사진 추가
- `setDigitalSignature()`: 디지털 서명 설정
- `isComplete()`: 완료 여부 확인
- `canStart()`: 시작 가능 여부 확인

---

## 5. 레포지토리 (Repositories)

### 5.1 DocumentTemplateRepository

**파일**: `backend/src/main/java/kr/co/softice/mes/domain/repository/DocumentTemplateRepository.java`

**주요 메서드**:
- `findAllByTenantId(String tenantId)`: 테넌트의 모든 템플릿 조회
- `findActiveByTenantId(String tenantId)`: 활성 템플릿 조회
- `findByTenantIdAndTemplateType(String tenantId, String templateType)`: 유형별 조회
- `findByTenantIdAndCategory(String tenantId, String category)`: 카테고리별 조회
- `findLatestByTenantIdAndTemplateCode(String tenantId, String templateCode)`: 최신 버전 조회
- `findByTenantIdAndTemplateCodeAndVersion(...)`: 특정 버전 조회
- `findAllVersionsByTenantIdAndTemplateCode(...)`: 전체 버전 조회
- `existsByTenantIdAndTemplateCode(...)`: 중복 확인

### 5.2 SOPRepository

**파일**: `backend/src/main/java/kr/co/softice/mes/domain/repository/SOPRepository.java`

**주요 메서드**:
- `findAllByTenantIdWithSteps(String tenantId)`: 단계 포함 전체 조회 (JOIN FETCH)
- `findActiveByTenantId(String tenantId)`: 활성 SOP 조회
- `findApprovedByTenantId(String tenantId)`: 승인된 SOP 조회
- `findByTenantIdAndSopType(...)`: 유형별 조회
- `findByTenantIdAndCategory(...)`: 카테고리별 조회
- `findByIdWithSteps(Long sopId)`: 단계 포함 상세 조회
- `findLatestBySopCode(String tenantId, String sopCode)`: 최신 버전 조회
- `findAllVersionsByTenantIdAndSopCode(...)`: 전체 버전 조회
- `findPendingApprovalByTenantId(String tenantId)`: 승인 대기 SOP 조회
- `findRequiringReview(String tenantId, LocalDate today)`: 검토 필요 SOP 조회
- `findByTenantIdAndTargetProcess(...)`: 대상 공정별 조회
- `existsByTenantIdAndSopCode(...)`: 중복 확인

### 5.3 SOPStepRepository

**파일**: `backend/src/main/java/kr/co/softice/mes/domain/repository/SOPStepRepository.java`

**주요 메서드**:
- `findBySopId(Long sopId)`: SOP의 모든 단계 조회
- `findCriticalStepsBySopId(Long sopId)`: 중요 단계 조회
- `findMandatoryStepsBySopId(Long sopId)`: 필수 단계 조회
- `findBySopIdAndStepType(...)`: 단계 유형별 조회
- `findBySopIdAndStepNumber(...)`: 단계 번호로 조회
- `findStepsWithPrerequisitesBySopId(Long sopId)`: 전제 조건 있는 단계 조회
- `findDependentSteps(Long stepId)`: 해당 단계에 의존하는 단계 조회
- `countBySopId(Long sopId)`: 단계 수 조회
- `countCriticalStepsBySopId(Long sopId)`: 중요 단계 수 조회
- `getMaxStepNumberBySopId(Long sopId)`: 최대 단계 번호 조회
- `existsBySopIdAndStepNumber(...)`: 단계 번호 중복 확인

### 5.4 SOPExecutionRepository

**파일**: `backend/src/main/java/kr/co/softice/mes/domain/repository/SOPExecutionRepository.java`

**주요 메서드**:
- `findAllByTenantId(String tenantId)`: 전체 실행 기록 조회
- `findByIdWithSteps(Long executionId)`: 단계 포함 실행 기록 조회
- `findBySopId(Long sopId)`: SOP의 실행 기록 조회
- `findByExecutorId(Long executorId)`: 실행자별 조회
- `findByTenantIdAndStatus(...)`: 상태별 조회
- `findInProgressByTenantId(String tenantId)`: 진행 중인 실행 조회
- `findByTenantIdAndReference(...)`: 참조별 조회 (작업지시, 검사 등)
- `findByTenantIdAndDateRange(...)`: 기간별 조회
- `findPendingReviewByTenantId(String tenantId)`: 검토 대기 중인 실행 조회
- `findByTenantIdAndExecutionNo(...)`: 실행 번호로 조회
- `countBySopId(Long sopId)`: SOP 실행 횟수
- `countCompletedBySopId(Long sopId)`: 완료된 실행 횟수
- `findLatestByReference(...)`: 최근 실행 기록 조회
- `findExecutionNumbersWithPrefix(...)`: 실행 번호 생성용 조회

### 5.5 SOPExecutionStepRepository

**파일**: `backend/src/main/java/kr/co/softice/mes/domain/repository/SOPExecutionStepRepository.java`

**주요 메서드**:
- `findByExecutionId(Long executionId)`: 실행의 모든 단계 결과 조회
- `findByExecutionIdAndSopStepId(...)`: 특정 단계 결과 조회
- `findByExecutionIdAndStatus(...)`: 상태별 단계 조회
- `findCompletedByExecutionId(Long executionId)`: 완료된 단계 조회
- `findPendingByExecutionId(Long executionId)`: 대기 중인 단계 조회
- `findFailedByExecutionId(Long executionId)`: 실패한 단계 조회
- `findNextPendingStep(Long executionId)`: 다음 대기 단계 조회
- `findWithPhotosByExecutionId(Long executionId)`: 사진 증빙이 있는 단계 조회
- `findWithSignatureByExecutionId(Long executionId)`: 서명이 있는 단계 조회
- `countByExecutionIdAndStatus(...)`: 상태별 단계 수 조회
- `countCompletedByExecutionId(Long executionId)`: 완료된 단계 수 조회
- `areAllCriticalStepsCompleted(Long executionId)`: 모든 중요 단계 완료 여부
- `areAllMandatoryStepsCompleted(Long executionId)`: 모든 필수 단계 완료 여부
- `findByExecutionIdAndStepNumber(...)`: 단계 번호로 조회

---

## 6. 서비스 (Services)

### 6.1 DocumentTemplateService

**파일**: `backend/src/main/java/kr/co/softice/mes/domain/service/DocumentTemplateService.java`

**주요 메서드**:

#### 조회
- `findAllTemplates(String tenantId)`: 모든 템플릿 조회
- `findActiveTemplates(String tenantId)`: 활성 템플릿 조회
- `findTemplatesByType(String tenantId, String templateType)`: 유형별 조회
- `findTemplatesByCategory(String tenantId, String category)`: 카테고리별 조회
- `findTemplateById(Long templateId)`: ID로 조회
- `findLatestTemplateByCode(String tenantId, String templateCode)`: 최신 버전 조회
- `findTemplateByCodeAndVersion(...)`: 특정 버전 조회
- `findAllVersions(String tenantId, String templateCode)`: 전체 버전 조회

#### 생성/수정/삭제
- `createTemplate(DocumentTemplateEntity template)`: 템플릿 생성
  - 템플릿 코드 중복 검증
  - `isLatest = true` 설정
- `updateTemplate(DocumentTemplateEntity template)`: 템플릿 수정
- `createNewVersion(String tenantId, String templateCode, String newVersion)`: 새 버전 생성
  - 현재 최신 버전의 `isLatest = false` 처리
  - 현재 버전을 복사하여 새 버전 생성
- `deleteTemplate(Long templateId)`: 템플릿 삭제 (Soft Delete, 비활성화)
- `activateTemplate(Long templateId)`: 템플릿 활성화
- `deactivateTemplate(Long templateId)`: 템플릿 비활성화

### 6.2 SOPService

**파일**: `backend/src/main/java/kr/co/softice/mes/domain/service/SOPService.java`

**주요 메서드**:

#### SOP CRUD
- `findAllSOPs(String tenantId)`: 모든 SOP 조회 (단계 포함)
- `findActiveSOPs(String tenantId)`: 활성 SOP 조회
- `findApprovedSOPs(String tenantId)`: 승인된 SOP 조회
- `findSOPsByType(String tenantId, String sopType)`: 유형별 조회
- `findSOPsByCategory(String tenantId, String category)`: 카테고리별 조회
- `findSOPById(Long sopId)`: ID로 조회 (단계 포함)
- `findLatestSOPByCode(String tenantId, String sopCode)`: 최신 버전 조회
- `findSOPsByTargetProcess(String tenantId, String targetProcess)`: 대상 공정별 조회
- `findSOPsRequiringReview(String tenantId)`: 검토 필요 SOP 조회
- `findSOPsPendingApproval(String tenantId)`: 승인 대기 SOP 조회
- `createSOP(SOPEntity sop)`: SOP 생성
  - SOP 코드 중복 검증
  - 기본 상태: DRAFT
- `updateSOP(SOPEntity sop)`: SOP 수정
  - 편집 가능 여부 확인 (`isEditable()`)
- `deleteSOP(Long sopId)`: SOP 삭제 (Soft Delete)
  - 승인된 SOP는 삭제 불가 (폐기 처리 필요)

#### SOP 승인 워크플로우
- `submitForApproval(Long sopId)`: 승인 요청 제출
  - 단계가 있는지 검증
  - 상태: DRAFT → PENDING
  - `revisionDate` 설정
- `approveSOP(Long sopId, Long approverId)`: SOP 승인
  - 상태: PENDING → APPROVED
  - 승인자, 승인일시 기록
  - `effectiveDate` 설정
  - `nextReviewDate` 설정 (1년 후)
- `rejectSOP(Long sopId)`: SOP 반려
  - 상태: PENDING → REJECTED
- `markObsolete(Long sopId)`: SOP 폐기
  - 상태: APPROVED → OBSOLETE
  - `isActive = false`

#### SOP 단계 관리
- `addStep(Long sopId, SOPStepEntity step)`: 단계 추가
  - 편집 가능한 SOP만 단계 추가 가능
  - 단계 번호 자동 생성 (최대 번호 + 1)
- `updateStep(SOPStepEntity step)`: 단계 수정
  - 편집 가능한 SOP만 수정 가능
- `deleteStep(Long stepId)`: 단계 삭제
  - 편집 가능한 SOP만 삭제 가능
  - 다른 단계가 이 단계에 의존하는지 확인 (prerequisite)

#### SOP 실행
- `startExecution(Long sopId, Long executorId, String referenceType, Long referenceId, String referenceNo)`: 실행 시작
  - SOP 실행 가능 여부 확인 (`isExecutable()`)
  - 실행 번호 자동 생성 (SOPE-YYYYMMDD-0001)
  - 모든 SOP 단계에 대해 실행 단계 레코드 생성 (PENDING 상태)
- `startExecutionStep(Long executionId, Long stepId)`: 실행 단계 시작
  - 시작 가능 여부 확인 (`canStart()` - 전제 단계 완료 확인)
  - 상태: PENDING → IN_PROGRESS
- `completeExecutionStep(Long executionStepId, String resultValue, String checklistResults)`: 실행 단계 완료
  - 상태: IN_PROGRESS → COMPLETED
  - 결과값, 체크리스트 결과 기록
  - 부모 실행의 완료율 자동 업데이트
- `completeExecution(Long executionId)`: SOP 실행 완료
  - 모든 필수 단계 완료 여부 확인
  - 상태: IN_PROGRESS → COMPLETED
  - 완료율 100%, 종료 시각 기록
- `cancelExecution(Long executionId, String reason)`: SOP 실행 취소
  - 상태: IN_PROGRESS → CANCELLED
  - 취소 사유 기록

#### 헬퍼 메서드
- `generateExecutionNumber(String tenantId)`: 실행 번호 생성
  - 형식: SOPE-YYYYMMDD-0001
  - 같은 날짜의 마지막 번호 조회 후 +1

---

## 7. 컨트롤러 (Controllers)

### 7.1 DocumentTemplateController

**파일**: `backend/src/main/java/kr/co/softice/mes/api/controller/DocumentTemplateController.java`

**엔드포인트 (15개)**:

#### 조회
```
GET /api/document-templates
    → 문서 양식 목록 조회
    권한: isAuthenticated()

GET /api/document-templates/active
    → 활성 문서 양식 목록 조회
    권한: isAuthenticated()

GET /api/document-templates/{id}
    → 문서 양식 상세 조회
    권한: isAuthenticated()

GET /api/document-templates/type/{templateType}
    → 유형별 문서 양식 조회
    권한: isAuthenticated()

GET /api/document-templates/category/{category}
    → 카테고리별 문서 양식 조회
    권한: isAuthenticated()

GET /api/document-templates/by-code/{templateCode}
    → 코드로 최신 버전 조회
    권한: isAuthenticated()

GET /api/document-templates/versions/{templateCode}
    → 문서 양식 전체 버전 조회
    권한: isAuthenticated()
```

#### 생성/수정/삭제
```
POST /api/document-templates
    → 문서 양식 생성
    권한: ADMIN, SYSTEM_ADMIN, QUALITY_MANAGER

PUT /api/document-templates/{id}
    → 문서 양식 수정
    권한: ADMIN, SYSTEM_ADMIN, QUALITY_MANAGER

POST /api/document-templates/{templateCode}/new-version?newVersion={version}
    → 새 버전 생성
    권한: ADMIN, SYSTEM_ADMIN, QUALITY_MANAGER

DELETE /api/document-templates/{id}
    → 문서 양식 삭제 (비활성화)
    권한: ADMIN, SYSTEM_ADMIN

POST /api/document-templates/{id}/activate
    → 문서 양식 활성화
    권한: ADMIN, SYSTEM_ADMIN, QUALITY_MANAGER

POST /api/document-templates/{id}/deactivate
    → 문서 양식 비활성화
    권한: ADMIN, SYSTEM_ADMIN, QUALITY_MANAGER
```

### 7.2 SOPController

**파일**: `backend/src/main/java/kr/co/softice/mes/api/controller/SOPController.java`

**엔드포인트 (23개)**:

#### SOP CRUD
```
GET /api/sops
    → SOP 목록 조회
    권한: isAuthenticated()

GET /api/sops/active
    → 활성 SOP 목록 조회
    권한: isAuthenticated()

GET /api/sops/approved
    → 승인된 SOP 목록 조회
    권한: isAuthenticated()

GET /api/sops/{id}
    → SOP 상세 조회 (단계 포함)
    권한: isAuthenticated()

GET /api/sops/type/{sopType}
    → 유형별 SOP 조회
    권한: isAuthenticated()

GET /api/sops/category/{category}
    → 카테고리별 SOP 조회
    권한: isAuthenticated()

GET /api/sops/process/{targetProcess}
    → 대상 공정별 SOP 조회
    권한: isAuthenticated()

GET /api/sops/requiring-review
    → 검토 필요 SOP 조회
    권한: ADMIN, QUALITY_MANAGER

GET /api/sops/pending-approval
    → 승인 대기 SOP 조회
    권한: ADMIN, QUALITY_MANAGER

POST /api/sops
    → SOP 생성
    권한: ADMIN, SYSTEM_ADMIN, QUALITY_MANAGER

PUT /api/sops/{id}
    → SOP 수정
    권한: ADMIN, SYSTEM_ADMIN, QUALITY_MANAGER

DELETE /api/sops/{id}
    → SOP 삭제 (비활성화)
    권한: ADMIN, SYSTEM_ADMIN
```

#### SOP 승인 워크플로우
```
POST /api/sops/{id}/submit
    → SOP 승인 요청
    권한: ADMIN, QUALITY_MANAGER

POST /api/sops/{id}/approve?approverId={userId}
    → SOP 승인
    권한: ADMIN, SYSTEM_ADMIN

POST /api/sops/{id}/reject
    → SOP 반려
    권한: ADMIN, SYSTEM_ADMIN

POST /api/sops/{id}/obsolete
    → SOP 폐기
    권한: ADMIN, SYSTEM_ADMIN, QUALITY_MANAGER
```

#### SOP 단계 관리
```
POST /api/sops/{sopId}/steps
    → SOP 단계 추가
    권한: ADMIN, QUALITY_MANAGER

PUT /api/sops/steps/{stepId}
    → SOP 단계 수정
    권한: ADMIN, QUALITY_MANAGER

DELETE /api/sops/steps/{stepId}
    → SOP 단계 삭제
    권한: ADMIN, QUALITY_MANAGER
```

#### SOP 실행
```
POST /api/sops/{sopId}/executions
    → SOP 실행 시작
    권한: isAuthenticated()
    Body: { executorId, referenceType, referenceId, referenceNo }

POST /api/sops/executions/{executionId}/steps/{stepId}/start
    → 실행 단계 시작
    권한: isAuthenticated()

POST /api/sops/executions/steps/{executionStepId}/complete
    → 실행 단계 완료
    권한: isAuthenticated()
    Body: { resultValue, checklistResults }

POST /api/sops/executions/{executionId}/complete
    → SOP 실행 완료
    권한: isAuthenticated()

POST /api/sops/executions/{executionId}/cancel?reason={reason}
    → SOP 실행 취소
    권한: isAuthenticated()
```

---

## 8. API 명세

### 8.1 요청/응답 예시

#### 8.1.1 SOP 생성

**요청**:
```http
POST /api/sops
Content-Type: application/json
Authorization: Bearer {token}

{
  "sopCode": "SOP-PROD-001",
  "sopName": "사출 성형 작업 표준",
  "description": "사출 성형 공정의 표준 작업 절차입니다.",
  "sopType": "PRODUCTION",
  "category": "MOLDING",
  "targetProcess": "사출 성형",
  "version": "1.0",
  "requiredRole": "OPERATOR",
  "restricted": false
}
```

**응답**:
```json
{
  "success": true,
  "message": "SOP 생성 성공",
  "data": {
    "sopId": 1,
    "sopCode": "SOP-PROD-001",
    "sopName": "사출 성형 작업 표준",
    "description": "사출 성형 공정의 표준 작업 절차입니다.",
    "sopType": "PRODUCTION",
    "category": "MOLDING",
    "targetProcess": "사출 성형",
    "version": "1.0",
    "approvalStatus": "DRAFT",
    "requiredRole": "OPERATOR",
    "restricted": false,
    "isActive": true,
    "createdAt": "2026-01-25T10:30:00"
  }
}
```

#### 8.1.2 SOP 단계 추가

**요청**:
```http
POST /api/sops/1/steps
Content-Type: application/json

{
  "stepTitle": "금형 점검",
  "stepDescription": "금형 상태를 점검하고 이상 유무를 확인합니다.",
  "stepType": "PREPARATION",
  "estimatedDuration": 10,
  "detailedInstruction": "1. 금형 표면 청결 상태 확인\n2. 크랙 및 손상 여부 점검\n3. 냉각 라인 점검",
  "cautionNotes": "금형 온도가 높을 수 있으니 주의하세요.",
  "qualityPoints": "크랙이 발견되면 즉시 작업을 중단하고 보고하세요.",
  "checklistItems": "[{\"item\": \"표면 청결\", \"required\": true}, {\"item\": \"크랙 없음\", \"required\": true}]",
  "isCritical": true,
  "isMandatory": true
}
```

**응답**:
```json
{
  "success": true,
  "message": "SOP 단계 추가 성공",
  "data": {
    "sopStepId": 1,
    "stepNumber": 1,
    "stepTitle": "금형 점검",
    "stepDescription": "금형 상태를 점검하고 이상 유무를 확인합니다.",
    "stepType": "PREPARATION",
    "estimatedDuration": 10,
    "isCritical": true,
    "isMandatory": true
  }
}
```

#### 8.1.3 SOP 실행 시작

**요청**:
```http
POST /api/sops/1/executions
Content-Type: application/json

{
  "executorId": 5,
  "referenceType": "WORK_ORDER",
  "referenceId": 123,
  "referenceNo": "WO-20260125-001"
}
```

**응답**:
```json
{
  "success": true,
  "message": "SOP 실행 시작 성공",
  "data": {
    "executionId": 1,
    "executionNo": "SOPE-20260125-0001",
    "executionDate": "2026-01-25T11:00:00",
    "executorId": 5,
    "executorName": "김작업",
    "referenceType": "WORK_ORDER",
    "referenceId": 123,
    "referenceNo": "WO-20260125-001",
    "executionStatus": "IN_PROGRESS",
    "startTime": "2026-01-25T11:00:00",
    "stepsTotal": 5,
    "stepsCompleted": 0,
    "completionRate": 0.00
  }
}
```

#### 8.1.4 실행 단계 완료

**요청**:
```http
POST /api/sops/executions/steps/1/complete
Content-Type: application/json

{
  "resultValue": "정상",
  "checklistResults": "[{\"item\": \"표면 청결\", \"checked\": true}, {\"item\": \"크랙 없음\", \"checked\": true}]"
}
```

**응답**:
```json
{
  "success": true,
  "message": "실행 단계 완료 성공",
  "data": {
    "executionStepId": 1,
    "stepNumber": 1,
    "stepStatus": "COMPLETED",
    "startedAt": "2026-01-25T11:00:00",
    "completedAt": "2026-01-25T11:08:00",
    "duration": 8,
    "resultValue": "정상",
    "checklistResults": "[{\"item\": \"표면 청결\", \"checked\": true}, {\"item\": \"크랙 없음\", \"checked\": true}]"
  }
}
```

---

## 9. 비즈니스 로직

### 9.1 SOP 생명주기 (Lifecycle)

```
[생성] → DRAFT (임시 저장)
         ↓ submitForApproval()
      PENDING (승인 대기)
         ↓ approveSOP()
      APPROVED (승인 완료, 실행 가능)
         ↓ markObsolete()
      OBSOLETE (폐기)

만약 반려되면:
PENDING → REJECTED (반려)
         ↓ 수정 후 다시 submitForApproval()
      PENDING (재승인 대기)
```

### 9.2 단계 실행 로직 (Step Execution Logic)

#### 9.2.1 전제 조건 (Prerequisite) 확인

```java
// SOPStepEntity.canStart(execution)
public boolean canStart(SOPExecutionEntity execution) {
    if (prerequisiteStep == null) {
        return true; // 전제 조건 없음
    }

    // 전제 단계가 완료되었는지 확인
    return execution.getExecutionSteps().stream()
            .anyMatch(es -> es.getSopStep().equals(prerequisiteStep)
                    && "COMPLETED".equals(es.getStepStatus()));
}
```

**예시**:
- 단계 1: 금형 점검 (전제 조건 없음)
- 단계 2: 재료 투입 (전제 조건: 단계 1 완료)
- 단계 3: 사출 실행 (전제 조건: 단계 2 완료)

단계 2는 단계 1이 완료되기 전에는 시작할 수 없습니다.

#### 9.2.2 중요 단계 (Critical Step) 처리

```java
// SOPExecutionStepEntity.fail(reason)
public void fail(String reason) {
    this.stepStatus = "FAILED";
    this.completedAt = LocalDateTime.now();
    this.remarks = reason;
    calculateDuration();

    // 중요 단계 실패 시 전체 실행 실패 처리
    if (sopStep.getIsCritical()) {
        execution.fail("Critical step failed: " + sopStep.getStepTitle());
    }
}
```

**예시**:
- 단계 1: 금형 점검 (`isCritical = true`)
  - 크랙 발견 시 → 단계 실패 → 전체 SOP 실행 실패
  - 작업 중단, 관리자 알림

#### 9.2.3 완료율 자동 계산

```java
// SOPExecutionEntity.updateCompletionRate()
public void updateCompletionRate() {
    if (stepsTotal == null || stepsTotal == 0) {
        this.completionRate = BigDecimal.ZERO;
        return;
    }

    long completedCount = executionSteps.stream()
            .filter(es -> "COMPLETED".equals(es.getStepStatus()))
            .count();

    this.stepsCompleted = (int) completedCount;
    this.completionRate = BigDecimal.valueOf(completedCount)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(stepsTotal), 2, java.math.RoundingMode.HALF_UP);
}
```

**예시**:
- 총 단계 수: 5
- 완료된 단계: 3
- 완료율: 3 / 5 * 100 = 60.00%

### 9.3 실행 번호 생성 로직

```java
// SOPService.generateExecutionNumber(tenantId)
private String generateExecutionNumber(String tenantId) {
    String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String prefix = "SOPE-" + today + "-";

    List<String> existingNumbers = sopExecutionRepository.findExecutionNumbersWithPrefix(tenantId, prefix);

    if (existingNumbers.isEmpty()) {
        return prefix + "0001";
    }

    // 마지막 실행 번호에서 시퀀스 추출
    String lastNumber = existingNumbers.get(0);
    int lastSequence = Integer.parseInt(lastNumber.substring(lastNumber.lastIndexOf("-") + 1));

    return prefix + String.format("%04d", lastSequence + 1);
}
```

**예시**:
- 2026-01-25 첫 번째 실행: `SOPE-20260125-0001`
- 2026-01-25 두 번째 실행: `SOPE-20260125-0002`
- 2026-01-26 첫 번째 실행: `SOPE-20260126-0001`

---

## 10. 사용 시나리오

### 10.1 시나리오 1: 생산 공정 SOP 작성 및 승인

**목표**: 사출 성형 작업의 표준 절차 수립

#### Step 1: 품질 관리자가 SOP 생성

```http
POST /api/sops
{
  "sopCode": "SOP-PROD-MOLDING-001",
  "sopName": "사출 성형 작업 표준",
  "sopType": "PRODUCTION",
  "category": "MOLDING",
  "targetProcess": "사출 성형",
  "requiredRole": "OPERATOR"
}
```

결과: `sopId = 1`, `approvalStatus = DRAFT`

#### Step 2: 단계 추가 (5단계)

```http
POST /api/sops/1/steps
{
  "stepTitle": "1. 금형 점검",
  "stepType": "PREPARATION",
  "isCritical": true,
  "checklistItems": "[{\"item\": \"표면 청결\"}, {\"item\": \"크랙 없음\"}]"
}

POST /api/sops/1/steps
{
  "stepTitle": "2. 재료 투입",
  "stepType": "EXECUTION",
  "prerequisiteStep": { "sopStepId": 1 }
}

POST /api/sops/1/steps
{
  "stepTitle": "3. 사출 실행",
  "stepType": "EXECUTION",
  "isCritical": true,
  "prerequisiteStep": { "sopStepId": 2 }
}

POST /api/sops/1/steps
{
  "stepTitle": "4. 품질 검사",
  "stepType": "INSPECTION",
  "isMandatory": true,
  "prerequisiteStep": { "sopStepId": 3 }
}

POST /api/sops/1/steps
{
  "stepTitle": "5. 기록 작성",
  "stepType": "DOCUMENTATION",
  "prerequisiteStep": { "sopStepId": 4 }
}
```

#### Step 3: 승인 요청

```http
POST /api/sops/1/submit
```

결과: `approvalStatus = PENDING`

#### Step 4: 시스템 관리자가 승인

```http
POST /api/sops/1/approve?approverId=2
```

결과:
- `approvalStatus = APPROVED`
- `approvedBy = 2 (시스템 관리자)`
- `approvedAt = 2026-01-25T14:00:00`
- `effectiveDate = 2026-01-25`
- `nextReviewDate = 2027-01-25` (1년 후)

### 10.2 시나리오 2: 작업 지시 생성 시 SOP 실행

**목표**: 작업 지시에 따라 SOP를 실행하고 기록 추적

#### Step 1: 작업 지시 생성 (WO-20260125-001)

작업자: 김작업 (userId = 5)

#### Step 2: SOP 실행 시작

```http
POST /api/sops/1/executions
{
  "executorId": 5,
  "referenceType": "WORK_ORDER",
  "referenceId": 123,
  "referenceNo": "WO-20260125-001"
}
```

결과:
- `executionId = 1`
- `executionNo = SOPE-20260125-0001`
- `executionStatus = IN_PROGRESS`
- 5개의 실행 단계 레코드 자동 생성 (모두 `PENDING` 상태)

#### Step 3: 단계별 실행

**단계 1: 금형 점검**

```http
# 시작
POST /api/sops/executions/1/steps/1/start

# 완료
POST /api/sops/executions/steps/1/complete
{
  "resultValue": "정상",
  "checklistResults": "[{\"item\": \"표면 청결\", \"checked\": true}, {\"item\": \"크랙 없음\", \"checked\": true}]"
}
```

결과: `stepStatus = COMPLETED`, `completionRate = 20.00%`

**단계 2: 재료 투입**

```http
POST /api/sops/executions/1/steps/2/start
POST /api/sops/executions/steps/2/complete
{
  "resultValue": "정상",
  "checklistResults": "[]"
}
```

결과: `completionRate = 40.00%`

**단계 3: 사출 실행**

```http
POST /api/sops/executions/1/steps/3/start
POST /api/sops/executions/steps/3/complete
{
  "resultValue": "정상",
  "checklistResults": "[]"
}
```

결과: `completionRate = 60.00%`

**단계 4: 품질 검사**

```http
POST /api/sops/executions/1/steps/4/start
POST /api/sops/executions/steps/4/complete
{
  "resultValue": "합격",
  "checklistResults": "[{\"item\": \"치수 측정\", \"checked\": true}, {\"item\": \"외관 검사\", \"checked\": true}]"
}
```

결과: `completionRate = 80.00%`

**단계 5: 기록 작성**

```http
POST /api/sops/executions/1/steps/5/start
POST /api/sops/executions/steps/5/complete
{
  "resultValue": "완료",
  "checklistResults": "[]"
}
```

결과: `completionRate = 100.00%`

#### Step 4: SOP 실행 완료

```http
POST /api/sops/executions/1/complete
```

결과:
- `executionStatus = COMPLETED`
- `endTime = 2026-01-25T15:30:00`
- `duration = 90` (90분 소요)

### 10.3 시나리오 3: 중요 단계 실패 처리

**목표**: 중요 단계 실패 시 전체 실행 중단

#### Step 1: SOP 실행 시작 (위와 동일)

#### Step 2: 단계 1 (금형 점검) 실행

```http
POST /api/sops/executions/1/steps/1/start
```

#### Step 3: 금형에서 크랙 발견

```http
POST /api/sops/executions/steps/1/fail
{
  "reason": "금형 표면에 크랙 발견. 작업 중단."
}
```

**백엔드 로직**:
```java
// SOPExecutionStepEntity.fail(reason)
if (sopStep.getIsCritical()) {
    execution.fail("Critical step failed: 금형 점검");
}
```

결과:
- 단계 1: `stepStatus = FAILED`
- 전체 실행: `executionStatus = FAILED`, `remarks = "Critical step failed: 금형 점검"`
- 작업 중단
- 관리자에게 알림 발송 (추후 구현)

#### Step 4: 후속 조치

- 금형 교체 또는 수리
- 새로운 SOP 실행 시작

---

## 11. 다음 단계

### 11.1 우선순위 1 (프론트엔드 구현)

#### SOPsPage.tsx
- SOP 목록 (Master Grid)
- SOP 상세 (단계 포함, Detail Panel)
- SOP 생성/수정 다이얼로그
- 단계 관리 (Drag & Drop 순서 변경)
- 승인 워크플로우 UI (DRAFT → PENDING → APPROVED)

**예상 기능**:
```typescript
const SOPsPage: React.FC = () => {
  const [sops, setSOPs] = useState<SOP[]>([]);
  const [selectedSOP, setSelectedSOP] = useState<SOP | null>(null);

  // Master Grid: SOP 목록
  const sopColumns = [
    { field: 'sopCode', headerName: 'SOP 코드', width: 150 },
    { field: 'sopName', headerName: 'SOP 명칭', width: 300 },
    { field: 'sopType', headerName: '유형', width: 120 },
    { field: 'approvalStatus', headerName: '승인 상태', width: 120,
      renderCell: (params) => <StatusBadge status={params.value} /> },
    { field: 'version', headerName: '버전', width: 100 },
    { field: 'effectiveDate', headerName: '시행일', width: 120 }
  ];

  // Detail Panel: SOP 단계
  const stepColumns = [
    { field: 'stepNumber', headerName: '순서', width: 80 },
    { field: 'stepTitle', headerName: '단계명', width: 300 },
    { field: 'stepType', headerName: '유형', width: 120 },
    { field: 'isCritical', headerName: '중요', width: 80,
      renderCell: (params) => params.value ? '⚠️' : '' }
  ];

  return (
    <Box>
      <DataGrid
        rows={sops}
        columns={sopColumns}
        onRowClick={(params) => setSelectedSOP(params.row)}
      />

      {selectedSOP && (
        <DetailPanel>
          <Typography variant="h6">SOP 단계</Typography>
          <DataGrid
            rows={selectedSOP.steps}
            columns={stepColumns}
          />
        </DetailPanel>
      )}
    </Box>
  );
};
```

#### SOPExecutionPage.tsx
- 실행 가능한 SOP 목록
- 실행 시작 다이얼로그
- 단계별 실행 UI (스텝 인디케이터)
- 체크리스트 입력
- 사진 업로드 (증빙 자료)
- 디지털 서명

**예상 UI**:
```
┌────────────────────────────────────────────────────────┐
│ SOP 실행: 사출 성형 작업 표준 (v1.0)                   │
│ 실행 번호: SOPE-20260125-0001                          │
│ 작업자: 김작업                                         │
│ 작업 지시: WO-20260125-001                             │
├────────────────────────────────────────────────────────┤
│ [●─────○─────○─────○─────○]  20% 완료                │
│  1      2     3     4     5                            │
├────────────────────────────────────────────────────────┤
│ 현재 단계: 1. 금형 점검                                │
│                                                        │
│ 작업 내용:                                             │
│ - 금형 표면 청결 상태 확인                             │
│ - 크랙 및 손상 여부 점검                               │
│ - 냉각 라인 점검                                       │
│                                                        │
│ ⚠️ 주의사항: 금형 온도가 높을 수 있으니 주의하세요.    │
│                                                        │
│ 체크리스트:                                            │
│ ☑ 표면 청결                                            │
│ ☑ 크랙 없음                                            │
│                                                        │
│ 결과: [정상 ▼]                                         │
│                                                        │
│ [사진 업로드] [서명]                                   │
│                                                        │
│ [이전] [건너뛰기] [실패] [완료] →                      │
└────────────────────────────────────────────────────────┘
```

#### sopService.ts
```typescript
class SOPService {
  // SOP CRUD
  async getSOPs(): Promise<SOP[]>;
  async getSOPById(id: number): Promise<SOP>;
  async createSOP(data: SOPCreateRequest): Promise<SOP>;
  async updateSOP(id: number, data: SOPUpdateRequest): Promise<SOP>;
  async deleteSOP(id: number): Promise<void>;

  // 승인 워크플로우
  async submitForApproval(sopId: number): Promise<SOP>;
  async approveSOP(sopId: number, approverId: number): Promise<SOP>;
  async rejectSOP(sopId: number): Promise<SOP>;

  // SOP 실행
  async startExecution(sopId: number, data: ExecutionStartRequest): Promise<SOPExecution>;
  async startExecutionStep(executionId: number, stepId: number): Promise<SOPExecutionStep>;
  async completeExecutionStep(executionStepId: number, data: StepCompleteRequest): Promise<SOPExecutionStep>;
  async completeExecution(executionId: number): Promise<SOPExecution>;
  async cancelExecution(executionId: number, reason: string): Promise<SOPExecution>;
}
```

### 11.2 우선순위 2 (고급 기능)

#### 파일 업로드
- 문서 파일 업로드 (PDF, Word, Excel)
- 이미지 업로드 (단계별 가이드 이미지)
- 동영상 업로드 (작업 시연 영상)

#### 디지털 서명
- 단계 완료 시 서명 입력
- 서명 이미지 저장
- 서명 검증

#### QR 코드
- SOP별 QR 코드 생성
- 모바일 앱에서 QR 스캔하여 실행 시작
- 작업 지시 연동

#### 모바일 UI
- 현장 작업자용 모바일 반응형 UI
- 터치 친화적 인터페이스
- 오프라인 지원 (Progressive Web App)

### 11.3 우선순위 3 (통합 및 확장)

#### 작업 지시 연동
- 작업 지시 생성 시 SOP 자동 연결
- SOP 실행 완료 시 작업 지시 상태 업데이트

#### 품질 검사 연동
- SOP 단계 중 검사 단계에서 품질 검사 생성
- 검사 결과에 따라 단계 완료/실패 처리

#### 설비 관리 연동
- 설비별 운영 SOP 연결
- 설비 유지보수 SOP 실행 기록 추적

#### 교육 훈련 연동
- SOP를 교육 콘텐츠로 활용
- 작업자 교육 이수 기록

#### 알림 시스템
- 검토 기한 도래 알림
- 중요 단계 실패 시 관리자 알림
- 승인 대기 알림

---

## 완성도

### 백엔드: 100% ✅
- ✅ 데이터베이스 스키마 (5개 테이블)
- ✅ 엔티티 (5개)
- ✅ 레포지토리 (5개)
- ✅ 서비스 (2개)
- ✅ 컨트롤러 (2개, 38개 엔드포인트)

### 프론트엔드: 0% ⏳
- ⏳ SOPsPage.tsx
- ⏳ SOPExecutionPage.tsx
- ⏳ sopService.ts

### 전체 완성도: 60%

**다음 작업**: 프론트엔드 구현 (SOPs 목록 페이지부터 시작)

---

**문서 작성**: Moon Myung-seop (문명섭)
**최종 수정**: 2026-01-25
