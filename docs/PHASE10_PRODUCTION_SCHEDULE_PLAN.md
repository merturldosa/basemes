# Phase 10: 생산 일정 관리 (Production Schedule) 구현 계획

**작성일**: 2026-01-27
**작성자**: SoIce MES Development Team
**프로젝트**: SoIce MES Platform

---

## 1. 개요

### 1.1 목표
WorkOrder와 ProcessRouting을 연동하여 상세한 생산 일정을 계획하고 시각화하는 시스템 구축

### 1.2 배경
- **현재 상태**:
  - WorkOrder: 단일 공정만 참조 (process_id)
  - ProcessRouting: 제품별 전체 공정 순서 정의
  - 연결 고리 부재: WorkOrder가 ProcessRouting 활용하지 못함

- **문제점**:
  - 복합 공정 제품의 전체 일정 수립 불가
  - 공정별 세부 일정 관리 어려움
  - 리소스(설비, 인원) 가용성 체크 불가
  - Gantt Chart 시각화 어려움

### 1.3 기대 효과
- 제품 생산의 전체 공정 일정 자동 계산
- 리소스 충돌 사전 감지
- 일정 최적화
- 직관적인 Gantt Chart 시각화

---

## 2. 시스템 아키텍처

### 2.1 데이터 흐름

```
Product → ProcessRouting → WorkOrder → ProductionSchedule
                ↓              ↓              ↓
          RoutingSteps    Planned Dates   Detailed Timeline
                                              ↓
                                         Gantt Chart
```

### 2.2 핵심 개념

#### WorkOrder 확장
- **추가 필드**: `routing_id` (ProcessRouting 참조)
- **의미**: 이 작업지시가 어떤 라우팅(공정 순서)을 따를지 지정
- **효과**: 단일 공정 → 전체 공정 흐름 관리

#### ProductionSchedule (신규)
- **목적**: WorkOrder의 각 공정 단계별 상세 일정
- **관계**:
  - WorkOrder (1) ← (N) ProductionSchedule
  - ProcessRoutingStep (1) ← (N) ProductionSchedule
- **핵심 정보**:
  - 각 공정 단계의 시작/종료 시간
  - 할당된 설비, 작업자
  - 실제 실행 시간 (actual)
  - 상태 (SCHEDULED, IN_PROGRESS, COMPLETED, DELAYED)

---

## 3. 데이터베이스 설계

### 3.1 WorkOrder 확장

**변경 사항**:
```sql
ALTER TABLE mes.si_work_orders
ADD COLUMN routing_id BIGINT,
ADD CONSTRAINT fk_work_order_routing
    FOREIGN KEY (routing_id)
    REFERENCES mes.si_process_routings(routing_id)
    ON DELETE SET NULL;

CREATE INDEX idx_work_order_routing ON mes.si_work_orders(routing_id);
```

**마이그레이션 전략**:
- 기존 WorkOrder는 routing_id = NULL (단일 공정)
- 신규 WorkOrder는 routing_id 지정 가능
- 하위 호환성 유지

### 3.2 ProductionSchedule 테이블

```sql
CREATE TABLE mes.si_production_schedules (
    -- Primary Key
    schedule_id BIGSERIAL PRIMARY KEY,

    -- 테넌트 정보
    tenant_id VARCHAR(50) NOT NULL,

    -- 작업 지시 정보
    work_order_id BIGINT NOT NULL,

    -- 공정 라우팅 단계 정보
    routing_step_id BIGINT NOT NULL,
    sequence_order INTEGER NOT NULL,

    -- 계획 일정
    planned_start_time TIMESTAMP NOT NULL,
    planned_end_time TIMESTAMP NOT NULL,
    planned_duration INTEGER NOT NULL,  -- 분 단위

    -- 실제 일정
    actual_start_time TIMESTAMP,
    actual_end_time TIMESTAMP,
    actual_duration INTEGER,

    -- 리소스 할당
    assigned_equipment_id BIGINT,
    assigned_workers INTEGER,  -- 할당된 작업자 수
    assigned_user_id BIGINT,   -- 담당자

    -- 상태: SCHEDULED, READY, IN_PROGRESS, COMPLETED, DELAYED, CANCELLED
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',

    -- 진행률 (%)
    progress_rate DECIMAL(5, 2) DEFAULT 0.00,

    -- 지연 정보
    is_delayed BOOLEAN DEFAULT false,
    delay_minutes INTEGER DEFAULT 0,
    delay_reason TEXT,

    -- 비고
    remarks TEXT,

    -- 감사 정보
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_schedule_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES core.si_tenants(tenant_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_schedule_work_order
        FOREIGN KEY (work_order_id)
        REFERENCES mes.si_work_orders(work_order_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_schedule_routing_step
        FOREIGN KEY (routing_step_id)
        REFERENCES mes.si_process_routing_steps(routing_step_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_schedule_equipment
        FOREIGN KEY (assigned_equipment_id)
        REFERENCES equipment.si_equipments(equipment_id)
        ON DELETE SET NULL,

    CONSTRAINT fk_schedule_user
        FOREIGN KEY (assigned_user_id)
        REFERENCES core.si_users(user_id)
        ON DELETE SET NULL,

    -- Unique Constraints
    CONSTRAINT uk_schedule_work_order_step
        UNIQUE (work_order_id, routing_step_id),

    -- Check Constraints
    CONSTRAINT chk_schedule_times
        CHECK (planned_end_time > planned_start_time),

    CONSTRAINT chk_schedule_duration
        CHECK (planned_duration > 0),

    CONSTRAINT chk_schedule_progress
        CHECK (progress_rate >= 0 AND progress_rate <= 100),

    CONSTRAINT chk_schedule_delay
        CHECK (delay_minutes >= 0)
);

-- Indexes
CREATE INDEX idx_schedule_tenant ON mes.si_production_schedules(tenant_id);
CREATE INDEX idx_schedule_work_order ON mes.si_production_schedules(work_order_id);
CREATE INDEX idx_schedule_routing_step ON mes.si_production_schedules(routing_step_id);
CREATE INDEX idx_schedule_status ON mes.si_production_schedules(status);
CREATE INDEX idx_schedule_planned_time ON mes.si_production_schedules(planned_start_time, planned_end_time);
CREATE INDEX idx_schedule_equipment ON mes.si_production_schedules(assigned_equipment_id);
CREATE INDEX idx_schedule_sequence ON mes.si_production_schedules(work_order_id, sequence_order);

-- Comments
COMMENT ON TABLE mes.si_production_schedules IS '생산 일정 테이블';
COMMENT ON COLUMN mes.si_production_schedules.schedule_id IS '일정 ID (PK)';
COMMENT ON COLUMN mes.si_production_schedules.work_order_id IS '작업 지시 ID (FK)';
COMMENT ON COLUMN mes.si_production_schedules.routing_step_id IS '공정 라우팅 단계 ID (FK)';
COMMENT ON COLUMN mes.si_production_schedules.sequence_order IS '공정 순서';
COMMENT ON COLUMN mes.si_production_schedules.planned_duration IS '계획 소요 시간 (분)';
COMMENT ON COLUMN mes.si_production_schedules.progress_rate IS '진행률 (%)';
COMMENT ON COLUMN mes.si_production_schedules.is_delayed IS '지연 여부';
COMMENT ON COLUMN mes.si_production_schedules.delay_minutes IS '지연 시간 (분)';
```

### 3.3 트리거

**updated_at 자동 갱신**:
```sql
CREATE OR REPLACE FUNCTION mes.update_schedule_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_schedule_updated_at
    BEFORE UPDATE ON mes.si_production_schedules
    FOR EACH ROW
    EXECUTE FUNCTION mes.update_schedule_updated_at();
```

**actual_duration 자동 계산**:
```sql
CREATE OR REPLACE FUNCTION mes.calculate_actual_duration()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.actual_start_time IS NOT NULL AND NEW.actual_end_time IS NOT NULL THEN
        NEW.actual_duration := EXTRACT(EPOCH FROM (NEW.actual_end_time - NEW.actual_start_time)) / 60;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_calculate_actual_duration
    BEFORE INSERT OR UPDATE ON mes.si_production_schedules
    FOR EACH ROW
    EXECUTE FUNCTION mes.calculate_actual_duration();
```

**지연 자동 감지**:
```sql
CREATE OR REPLACE FUNCTION mes.check_schedule_delay()
RETURNS TRIGGER AS $$
BEGIN
    -- 현재 시간이 계획 종료 시간을 초과했는데 완료되지 않은 경우
    IF NEW.status NOT IN ('COMPLETED', 'CANCELLED')
       AND CURRENT_TIMESTAMP > NEW.planned_end_time THEN
        NEW.is_delayed := true;
        NEW.delay_minutes := EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - NEW.planned_end_time)) / 60;
    ELSE
        NEW.is_delayed := false;
        NEW.delay_minutes := 0;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_check_delay
    BEFORE INSERT OR UPDATE ON mes.si_production_schedules
    FOR EACH ROW
    EXECUTE FUNCTION mes.check_schedule_delay();
```

---

## 4. 백엔드 구현

### 4.1 Entity Layer

**WorkOrderEntity.java 수정**:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "routing_id")
private ProcessRoutingEntity routing;
```

**ProductionScheduleEntity.java**:
```java
@Entity
@Table(schema = "mes", name = "si_production_schedules")
public class ProductionScheduleEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private TenantEntity tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private WorkOrderEntity workOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routing_step_id")
    private ProcessRoutingStepEntity routingStep;

    private Integer sequenceOrder;

    // 계획 일정
    private LocalDateTime plannedStartTime;
    private LocalDateTime plannedEndTime;
    private Integer plannedDuration;

    // 실제 일정
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private Integer actualDuration;

    // 리소스
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_equipment_id")
    private EquipmentEntity assignedEquipment;

    private Integer assignedWorkers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private UserEntity assignedUser;

    // 상태
    private String status; // SCHEDULED, READY, IN_PROGRESS, COMPLETED, DELAYED, CANCELLED
    private BigDecimal progressRate;

    // 지연
    private Boolean isDelayed;
    private Integer delayMinutes;
    private String delayReason;

    private String remarks;
}
```

### 4.2 Service Layer

**ProductionScheduleService.java**:
```java
@Service
public class ProductionScheduleService {

    /**
     * WorkOrder에서 자동으로 일정 생성
     * - WorkOrder의 routing을 기반으로
     * - 각 routing step마다 Schedule 생성
     * - 시간 계산: 이전 단계 종료 시간 + 현재 단계 소요 시간
     */
    @Transactional
    public List<ProductionScheduleEntity> generateSchedulesFromWorkOrder(Long workOrderId) {
        WorkOrderEntity workOrder = workOrderRepository.findById(workOrderId)
            .orElseThrow(() -> new IllegalArgumentException("WorkOrder not found"));

        if (workOrder.getRouting() == null) {
            throw new IllegalArgumentException("WorkOrder must have a routing");
        }

        List<ProductionScheduleEntity> schedules = new ArrayList<>();
        LocalDateTime currentTime = workOrder.getPlannedStartDate();

        for (ProcessRoutingStepEntity step : workOrder.getRouting().getSteps()) {
            // 시간 계산
            int totalMinutes = step.getStandardTime()
                             + step.getSetupTime()
                             + step.getWaitTime();

            LocalDateTime startTime = currentTime;
            LocalDateTime endTime = currentTime.plusMinutes(totalMinutes);

            // Schedule 생성
            ProductionScheduleEntity schedule = ProductionScheduleEntity.builder()
                .tenant(workOrder.getTenant())
                .workOrder(workOrder)
                .routingStep(step)
                .sequenceOrder(step.getSequenceOrder())
                .plannedStartTime(startTime)
                .plannedEndTime(endTime)
                .plannedDuration(totalMinutes)
                .assignedEquipment(step.getEquipment())
                .assignedWorkers(step.getRequiredWorkers())
                .status("SCHEDULED")
                .progressRate(BigDecimal.ZERO)
                .isDelayed(false)
                .delayMinutes(0)
                .build();

            schedules.add(schedule);

            // 다음 공정 시작 시간 설정
            currentTime = endTime;
        }

        return scheduleRepository.saveAll(schedules);
    }

    /**
     * 리소스 충돌 체크
     */
    public List<ResourceConflict> checkResourceConflicts(Long scheduleId) {
        // 같은 설비, 같은 시간대에 다른 일정이 있는지 체크
    }

    /**
     * Gantt Chart 데이터 조회
     */
    public GanttChartData getGanttChartData(String tenantId, LocalDate startDate, LocalDate endDate) {
        // 기간 내 모든 일정 조회
        // WorkOrder별, 공정별 그룹화
        // Gantt Chart 형식으로 변환
    }
}
```

### 4.3 Controller Layer

**ProductionScheduleController.java**:
```java
@RestController
@RequestMapping("/api/schedules")
public class ProductionScheduleController {

    // 일정 목록 조회
    @GetMapping
    public List<ScheduleResponse> getSchedules(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate
    );

    // WorkOrder 기반 일정 자동 생성
    @PostMapping("/generate/{workOrderId}")
    public List<ScheduleResponse> generateSchedules(@PathVariable Long workOrderId);

    // 일정 수정 (시간 조정)
    @PutMapping("/{scheduleId}")
    public ScheduleResponse updateSchedule(@PathVariable Long scheduleId, @RequestBody ScheduleUpdateRequest request);

    // Gantt Chart 데이터
    @GetMapping("/gantt")
    public GanttChartData getGanttChart(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate
    );

    // 리소스 충돌 체크
    @GetMapping("/{scheduleId}/conflicts")
    public List<ResourceConflict> checkConflicts(@PathVariable Long scheduleId);

    // 일정 상태 변경
    @PostMapping("/{scheduleId}/status")
    public ScheduleResponse updateStatus(@PathVariable Long scheduleId, @RequestParam String status);
}
```

---

## 5. 프론트엔드 구현

### 5.1 Gantt Chart 라이브러리 선택

**추천 라이브러리**:

1. **dhtmlx-gantt** (유료/무료 버전)
   - 장점: 매우 강력한 기능, 드래그앤드롭, 리소스 뷰
   - 단점: 라이선스 비용

2. **frappe-gantt** (무료 오픈소스)
   - 장점: 가볍고 간단, 무료
   - 단점: 기능 제한적

3. **react-gantt-chart** (무료)
   - 장점: React 전용, 타입스크립트 지원
   - 단점: 커뮤니티 작음

**선택**: **frappe-gantt** (시작용) → 필요시 dhtmlx-gantt로 업그레이드

### 5.2 UI 컴포넌트

**ProductionSchedulePage.tsx**:
```typescript
// 주요 기능:
// 1. Gantt Chart 시각화
// 2. WorkOrder 선택 → 일정 자동 생성
// 3. 일정 드래그 앤 드롭 조정
// 4. 리소스(설비) 뷰
// 5. 지연 일정 하이라이트
// 6. 리소스 충돌 경고
```

**화면 구성**:
```
+------------------------------------------+
|  [ 기간 선택 ]  [ WorkOrder 선택 ]      |
|  [ 일정 생성 ]  [ 리소스 뷰 전환 ]      |
+------------------------------------------+
|                                          |
|           Gantt Chart Area               |
|                                          |
|  WO-001 │ ■■■■■■■■■■              |
|    공정1  │ ■■■■                      |
|    공정2  │       ■■■■■              |
|    공정3  │             ■■■            |
|                                          |
|  WO-002 │     ■■■■■■■              |
|    공정1  │     ■■■                    |
|    공정2  │         ■■■■              |
|                                          |
+------------------------------------------+
|  [ 범례 ]                                |
|  ■ 예정  ■ 진행중  ■ 완료  ■ 지연      |
+------------------------------------------+
```

---

## 6. 주요 기능

### 6.1 일정 자동 생성
- WorkOrder 생성 시 routing 선택
- "일정 생성" 버튼 클릭
- 각 공정 단계별 일정 자동 계산
- 리소스 가용성 체크 후 할당

### 6.2 일정 조정
- Gantt Chart에서 드래그 앤 드롭
- 시작/종료 시간 수정
- 리소스 재할당
- 충돌 자동 감지 및 경고

### 6.3 리소스 관리
- 설비별 가용 시간 표시
- 인원 할당 현황
- 충돌 감지 (같은 설비, 같은 시간)
- 대체 리소스 제안

### 6.4 진행 상황 추적
- 실시간 진행률 업데이트
- 지연 일정 자동 감지
- 알림 및 경고
- 대시보드 통계

---

## 7. 구현 순서

### Phase 1: 데이터베이스 (3-4시간)
1. WorkOrder에 routing_id 추가 마이그레이션
2. ProductionSchedule 테이블 생성
3. 트리거 구현
4. 테스트 데이터 작성

### Phase 2: 백엔드 Entity & Repository (2-3시간)
1. WorkOrderEntity 수정
2. ProductionScheduleEntity 작성
3. ProductionScheduleRepository 작성

### Phase 3: 백엔드 Service (4-5시간)
1. ScheduleGenerationService (일정 자동 생성 로직)
2. ResourceConflictChecker (충돌 감지)
3. ProductionScheduleService (CRUD)

### Phase 4: 백엔드 Controller (3-4시간)
1. REST API 구현
2. DTO 작성
3. API 테스트

### Phase 5: 프론트엔드 (8-10시간)
1. frappe-gantt 설치 및 통합
2. ProductionSchedulePage 기본 구조
3. Gantt Chart 렌더링
4. 일정 생성 UI
5. 일정 조정 UI
6. 리소스 뷰

### Phase 6: 통합 테스트 (3-4시간)
1. WorkOrder → Routing → Schedule 플로우 테스트
2. 리소스 충돌 시나리오 테스트
3. 지연 감지 테스트

### Phase 7: 문서화 (2-3시간)
1. API 문서
2. 사용자 가이드
3. 구현 보고서

**총 예상 시간**: 25-33시간 (약 3-4일)

---

## 8. 위험 요소 및 대응

### 위험 1: Gantt Chart 성능
- **문제**: 수백 개의 일정 표시 시 성능 저하
- **대응**: 가상화(virtualization), 날짜 범위 제한, 페이지네이션

### 위험 2: 리소스 충돌 복잡도
- **문제**: 복잡한 충돌 시나리오 (병렬 공정, 대체 설비)
- **대응**: 단계적 구현 (먼저 단순 충돌만, 이후 확장)

### 위험 3: 기존 WorkOrder 호환성
- **문제**: 기존 WorkOrder는 routing_id = NULL
- **대응**: NULL 허용, 단일 공정 WorkOrder도 지원

---

## 9. 성공 기준

### 9.1 기능적 요구사항
- ✅ WorkOrder에서 자동 일정 생성
- ✅ Gantt Chart 시각화
- ✅ 일정 수정 (드래그 앤 드롭)
- ✅ 리소스 충돌 감지
- ✅ 지연 자동 감지

### 9.2 비기능적 요구사항
- ✅ 100개 일정 1초 이내 렌더링
- ✅ 직관적인 UI/UX
- ✅ 모바일 반응형

---

## 10. 향후 확장 가능성

### 10.1 고급 기능
- AI 기반 일정 최적화
- What-if 시나리오 분석
- 자동 일정 재조정
- 예측 분석 (지연 가능성 예측)

### 10.2 통합
- ERP 시스템 연동
- IoT 설비 데이터 실시간 반영
- 모바일 앱 (일정 확인, 실적 입력)

---

**End of Plan**
