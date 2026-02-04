# Phase 8-5: Alarm/Notification Settings - 완료 보고서

## 개요

**모듈명**: 알람/알림 설정 (Alarm/Notification Settings)
**Phase**: 8-5 (Common Module - 마지막 항목)
**구현 일자**: 2026-01-25
**상태**: ✅ 완료

## 구현 내용

### 1. 데이터베이스 스키마

**파일**: `database/migrations/V025__create_alarm_schema.sql` (~350 lines)

#### 생성된 테이블

1. **alarm_templates** (알람 템플릿)
   - 알람 메시지 템플릿 정의
   - 변수 치환 지원 ({{variable}} 문법)
   - 다중 채널 설정 (Email, SMS, Push, System)
   - 우선순위 설정 (LOW, NORMAL, HIGH, URGENT)

2. **alarm_settings** (알람 설정)
   - 사용자별 알람 수신 설정
   - 채널별 활성화/비활성화
   - 방해 금지 시간 설정
   - 알람 타입별 세부 설정

3. **alarm_history** (알람 이력)
   - 발송된 알람 기록
   - 읽음/읽지 않음 상태 추적
   - 발송 채널 기록
   - 참조 문서 연결 (referenceType, referenceId)

4. **alarm_subscriptions** (알람 구독)
   - 이벤트별 구독 설정
   - 조건부 알람 설정
   - 구독자 관리

#### 샘플 데이터
- **14개 알람 템플릿** 생성
  - 결재 관련: 결재 요청, 승인, 반려
  - 품질 관련: 검사 요청, 불합격
  - 생산 관련: 작업 지시, 작업 완료, 품질 이슈
  - 재고 관련: 저재고 알림, 입하 완료
  - 출하 관련: 출하 요청, 출하 완료
  - 시스템 관련: 시스템 점검, 중요 공지

### 2. Backend 구현

#### Entity (엔티티)

1. **AlarmTemplateEntity.java** (138 lines)
   ```java
   // 주요 기능
   - renderTitle(Map<String, String> variables): 제목 템플릿 렌더링
   - renderMessage(Map<String, String> variables): 메시지 템플릿 렌더링
   - isHighPriority(): 높은 우선순위 여부 확인
   - isEmailEnabled(), isSmsEnabled(), isPushEnabled(), isSystemEnabled()
   ```

2. **AlarmSettingEntity.java** (145 lines)
   ```java
   // 주요 기능
   - isInQuietHours(): 방해 금지 시간 확인 (자정 넘김 처리 포함)
   - getEnabledChannels(): 활성화된 채널 목록 반환
   - isAlarmTypeEnabled(String alarmType): 타입별 활성화 확인
   ```

3. **AlarmHistoryEntity.java** (220 lines)
   ```java
   // 주요 기능
   - markAsRead(): 읽음 처리
   - markAsSent(): 발송 완료 처리
   - markAsFailed(String errorMessage): 발송 실패 처리
   - getHoursSinceCreated(): 생성 후 경과 시간
   ```

#### Repository (레포지토리)

1. **AlarmTemplateRepository.java** (63 lines)
   - findAllByTenantId(): 전체 템플릿 조회
   - findActiveByTenantId(): 활성 템플릿 조회
   - findByTenantIdAndEventType(): 이벤트 타입별 템플릿 조회
   - findByTenantIdAndAlarmType(): 알람 타입별 템플릿 조회

2. **AlarmHistoryRepository.java** (112 lines)
   - findByRecipient(): 수신자별 알람 조회
   - findUnreadByRecipient(): 읽지 않은 알람 조회
   - findRecentByRecipient(): 최근 알람 조회 (7일)
   - countUnreadByRecipient(): 읽지 않은 알람 수
   - countByTypeAndRecipient(): 타입별 알람 수
   - findFailedAlarms(): 발송 실패 알람 조회
   - deleteOldAlarms(): 오래된 알람 삭제 (보관 정책)

#### Service (서비스)

**AlarmService.java** (~250 lines)

주요 메서드:
```java
// 템플릿 관리
- findAllTemplates(String tenantId): 전체 템플릿 조회
- findTemplateByEventType(String tenantId, String eventType): 이벤트별 템플릿 조회

// 알람 발송
- sendAlarm(String tenantId, String eventType, Long recipientUserId, ...): 알람 발송
  * 템플릿 조회
  * 변수 치환
  * 알람 히스토리 생성
  * 다중 채널 발송

- sendViaChannels(AlarmHistoryEntity alarm, AlarmTemplateEntity template): 채널별 발송
- sendViaEmail(AlarmHistoryEntity alarm): 이메일 발송 (TODO: 실제 서비스 연동)
- sendViaSms(AlarmHistoryEntity alarm): SMS 발송 (TODO: 실제 서비스 연동)
- sendViaPush(AlarmHistoryEntity alarm): 푸시 발송 (TODO: 실제 서비스 연동)

// 알람 이력 관리
- findAlarmsByRecipient(String tenantId, Long userId): 사용자 알람 조회
- findUnreadAlarms(String tenantId, Long userId): 읽지 않은 알람 조회
- findRecentAlarms(String tenantId, Long userId): 최근 알람 조회
- countUnreadAlarms(String tenantId, Long userId): 읽지 않은 알람 수
- markAsRead(Long alarmId): 읽음 처리
- markAllAsRead(String tenantId, Long userId): 전체 읽음 처리

// 통계
- getStatistics(String tenantId, Long userId): 알람 통계
  * 읽지 않은 알람 수
  * 전체 알람 수
  * 타입별 알람 수 (결재, 품질, 생산, 재고)
  * 읽음율 계산

// 보관 정책
- deleteOldAlarms(String tenantId, int retentionDays): 오래된 알람 삭제
```

#### Controller (컨트롤러)

**AlarmController.java** (~280 lines)

API 엔드포인트:

```
GET    /api/alarms/templates              - 알람 템플릿 목록 조회 (ADMIN, MANAGER)
GET    /api/alarms                         - 사용자 알람 목록 조회
GET    /api/alarms/unread                  - 읽지 않은 알람 조회
GET    /api/alarms/recent                  - 최근 알람 조회 (7일)
GET    /api/alarms/unread/count            - 읽지 않은 알람 수 조회
PUT    /api/alarms/{alarmId}/read          - 알람 읽음 표시
PUT    /api/alarms/read-all                - 모든 알람 읽음 표시
GET    /api/alarms/statistics              - 알람 통계 조회
```

권한:
- 템플릿 조회: ADMIN, MANAGER
- 알람 조회/관리: ADMIN, MANAGER, USER

### 3. Frontend 구현

#### Service (서비스)

**alarmService.ts** (~200 lines)

주요 인터페이스:
```typescript
interface AlarmTemplate {
  templateId: number;
  templateCode: string;
  templateName: string;
  alarmType: string;
  eventType: string;
  titleTemplate: string;
  messageTemplate: string;
  enableEmail: boolean;
  enableSms: boolean;
  enablePush: boolean;
  enableSystem: boolean;
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
  isActive: boolean;
}

interface AlarmHistory {
  alarmId: number;
  alarmType: string;
  eventType: string;
  priority: string;
  title: string;
  message: string;
  referenceType?: string;
  referenceId?: number;
  referenceNo?: string;
  sentViaEmail: boolean;
  sentViaSms: boolean;
  sentViaPush: boolean;
  sentViaSystem: boolean;
  isRead: boolean;
  readAt?: string;
  sendStatus: 'PENDING' | 'SENT' | 'FAILED';
  createdAt: string;
}

interface AlarmStatistics {
  unreadCount: number;
  totalCount: number;
  approvalCount: number;
  qualityCount: number;
  productionCount: number;
  inventoryCount: number;
  readCount: number;
  readRate: number;
}
```

헬퍼 함수:
```typescript
- getAlarmTypeLabel(type: string): 알람 타입 한글 라벨
- getAlarmTypeColor(type: string): 알람 타입 색상
- getPriorityLabel(priority: string): 우선순위 한글 라벨
- getPriorityColor(priority: string): 우선순위 색상
- formatDateTime(dateStr?: string): 날짜/시간 포맷팅
- getRelativeTime(dateStr: string): 상대 시간 표시 ("방금 전", "5분 전", etc.)
```

#### UI Component

**AlarmPage.tsx** (~550 lines)

구성:
1. **4개 탭**
   - Tab 1: 읽지 않은 알람 (Unread Alarms)
   - Tab 2: 전체 알람 (All Alarms)
   - Tab 3: 최근 알람 (Recent Alarms - 7일)
   - Tab 4: 통계 (Statistics)

2. **통계 카드** (6개)
   - 읽지 않음
   - 전체
   - 결재 알람
   - 품질 알람
   - 생산 알람
   - 재고 알람

3. **주요 기능**
   - 읽지 않은 알람 개수 뱃지
   - 모두 읽음 표시 버튼
   - 알람 상세 다이얼로그
   - 읽음 처리 (개별/전체)
   - 상대 시간 표시 ("방금 전", "5분 전")
   - 알람 타입/우선순위 색상 구분
   - 참조 문서 링크
   - 30초마다 자동 새로고침

4. **DataGrid 컬럼**
   - 알람 타입 (Chip with color)
   - 우선순위 (Chip with color)
   - 제목 (읽지 않음 배지 표시)
   - 내용
   - 시간 (상대 시간)
   - 읽음 상태
   - 작업 (상세 보기, 읽음 표시)

### 4. 주요 기능 특징

#### 4.1 템플릿 기반 메시지
```java
// 템플릿
title: "[결재 요청] {{documentType}} {{documentNo}}"
message: "{{requesterName}}님이 {{documentType}} 결재를 요청했습니다."

// 변수
Map<String, String> variables = {
    "documentType": "구매 주문",
    "documentNo": "PO-20260125-001",
    "requesterName": "홍길동"
}

// 결과
title: "[결재 요청] 구매 주문 PO-20260125-001"
message: "홍길동님이 구매 주문 결재를 요청했습니다."
```

#### 4.2 다중 채널 지원
- **Email**: 이메일 발송 (TODO: SMTP 서비스 연동)
- **SMS**: SMS 발송 (TODO: SMS 게이트웨이 연동)
- **Push**: 푸시 알림 (TODO: FCM/APNS 연동)
- **System**: 시스템 내 알림 (현재 구현됨)

#### 4.3 방해 금지 시간
```java
// 22:00 - 08:00 설정 시
quietHoursStart: "22:00"
quietHoursEnd: "08:00"

// 자정 넘김 처리
if (quietHoursStart.isBefore(quietHoursEnd)) {
    // 일반 케이스: 08:00 - 22:00
    return !now.isBefore(quietHoursStart) && now.isBefore(quietHoursEnd);
} else {
    // 자정 넘김 케이스: 22:00 - 08:00
    return !now.isBefore(quietHoursStart) || now.isBefore(quietHoursEnd);
}
```

#### 4.4 상대 시간 표시
```typescript
const minutes = Math.floor(diff / 60000);
const hours = Math.floor(diff / 3600000);
const days = Math.floor(diff / 86400000);

if (minutes < 1) return '방금 전';
if (minutes < 60) return `${minutes}분 전`;
if (hours < 24) return `${hours}시간 전`;
if (days < 7) return `${days}일 전`;
return formatDateTime(dateStr);
```

## 통합 포인트

### 다른 모듈과의 연동

1. **결재 라인 관리 (Approval)**
   - 결재 요청 시: `APPROVAL_REQUEST` 이벤트
   - 결재 승인 시: `APPROVAL_APPROVED` 이벤트
   - 결재 반려 시: `APPROVAL_REJECTED` 이벤트

2. **품질 관리 (QMS)**
   - 검사 요청 시: `QUALITY_INSPECTION_REQUEST` 이벤트
   - 불합격 발생 시: `QUALITY_INSPECTION_FAILED` 이벤트

3. **생산 관리 (Production)**
   - 작업 지시 시: `PRODUCTION_WORK_ORDER` 이벤트
   - 작업 완료 시: `PRODUCTION_WORK_COMPLETE` 이벤트
   - 품질 이슈 발생 시: `PRODUCTION_QUALITY_ISSUE` 이벤트

4. **재고 관리 (Inventory)**
   - 저재고 발생 시: `INVENTORY_LOW_STOCK` 이벤트
   - 입하 완료 시: `INVENTORY_GOODS_RECEIPT` 이벤트

5. **출하 관리 (Shipping)**
   - 출하 요청 시: `DELIVERY_SHIPPING_REQUEST` 이벤트
   - 출하 완료 시: `DELIVERY_SHIPPING_COMPLETE` 이벤트

6. **시스템**
   - 시스템 점검: `SYSTEM_MAINTENANCE` 이벤트
   - 중요 공지: `SYSTEM_ANNOUNCEMENT` 이벤트

### 사용 예시

```java
// 결재 요청 시 알람 발송
Map<String, String> variables = new HashMap<>();
variables.put("documentType", "구매 주문");
variables.put("documentNo", purchaseOrder.getOrderNo());
variables.put("requesterName", requester.getName());
variables.put("amount", purchaseOrder.getTotalAmount().toString());

alarmService.sendAlarm(
    tenantId,
    "APPROVAL_REQUEST",  // eventType
    approverId,          // recipientUserId
    approver.getName(),  // recipientName
    variables,           // template variables
    "PURCHASE_ORDER",    // referenceType
    purchaseOrder.getId(), // referenceId
    purchaseOrder.getOrderNo() // referenceNo
);
```

## 테스트 시나리오

### 1. 알람 발송 테스트
```
1. 결재 요청 생성
2. 알람 자동 발송 확인
3. 수신자 알람 목록에 표시 확인
4. 읽지 않음 개수 증가 확인
```

### 2. 읽음 처리 테스트
```
1. 알람 상세 보기
2. 자동으로 읽음 처리 확인
3. 읽지 않음 개수 감소 확인
4. 읽은 시간 기록 확인
```

### 3. 방해 금지 시간 테스트
```
1. 사용자 설정에서 방해 금지 시간 설정 (22:00 - 08:00)
2. 22:00 이후 알람 발송
3. 이메일/SMS/푸시 비활성화 확인
4. 시스템 알람만 발송 확인
```

### 4. 통계 테스트
```
1. 다양한 타입의 알람 발송
2. 통계 카드 업데이트 확인
3. 타입별 개수 정확성 확인
4. 읽음율 계산 확인
```

## 코드 통계

| 항목 | 파일 수 | 총 라인 수 |
|------|---------|-----------|
| Database Migration | 1 | ~350 |
| Backend Entities | 3 | ~500 |
| Backend Repositories | 2 | ~175 |
| Backend Service | 1 | ~250 |
| Backend Controller | 1 | ~280 |
| Frontend Service | 1 | ~200 |
| Frontend UI | 1 | ~550 |
| **총계** | **10** | **~2,305** |

## 향후 개선 사항

### 1. 실제 채널 연동
- [ ] SMTP 서버 연동 (이메일 발송)
- [ ] SMS 게이트웨이 연동 (문자 발송)
- [ ] FCM/APNS 연동 (푸시 알림)

### 2. 고급 기능
- [ ] 알람 템플릿 관리 UI (관리자용)
- [ ] 사용자별 알람 설정 UI
- [ ] 알람 구독 관리 UI
- [ ] 일괄 알람 발송 기능
- [ ] 알람 발송 스케줄링

### 3. 성능 최적화
- [ ] 알람 발송 큐 시스템 (비동기 처리)
- [ ] 대량 알람 발송 배치 처리
- [ ] 읽지 않은 알람 캐싱

### 4. 보안 강화
- [ ] 민감 정보 마스킹
- [ ] 알람 접근 권한 검증
- [ ] 발송 실패 재시도 로직

## 완료 체크리스트

- [x] 데이터베이스 스키마 생성
- [x] 샘플 데이터 입력 (14개 템플릿)
- [x] Backend Entity 구현 (3개)
- [x] Backend Repository 구현 (2개)
- [x] Backend Service 구현
- [x] Backend Controller 구현
- [x] Frontend Service 구현
- [x] Frontend UI 구현
- [x] 다중 채널 지원 (구조)
- [x] 방해 금지 시간 처리
- [x] 템플릿 변수 치환
- [x] 읽음/읽지 않음 처리
- [x] 통계 기능
- [x] 자동 새로고침 (30초)
- [x] 상대 시간 표시

## 결론

Phase 8-5: 알람/알림 설정 모듈이 성공적으로 완료되었습니다.

**주요 성과**:
- 템플릿 기반 알람 시스템 구축
- 다중 채널 지원 구조 완성
- 사용자 친화적 UI 제공
- 다른 모듈과의 통합 준비 완료

**다음 단계**:
- Phase 8 전체 모듈 통합 테스트
- Phase 8 완료 문서 작성
- Phase 9 준비 (WMS 모듈 또는 다른 모듈)

---

**작성일**: 2026-01-25
**작성자**: Claude Sonnet 4.5
**문서 버전**: 1.0
