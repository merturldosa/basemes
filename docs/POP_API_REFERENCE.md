# POP API Reference

**SDS MES - Point of Production API Documentation**
**Version**: 1.0
**Last Updated**: 2025-02-05
**Author**: Moon Myung-seop
**Base URL**: `https://your-domain.com/api`

---

## Table of Contents

1. [Authentication](#authentication)
2. [POP Endpoints](#pop-endpoints)
3. [SOP Operator Endpoints](#sop-operator-endpoints)
4. [WebSocket Topics](#websocket-topics)
5. [Data Models](#data-models)
6. [Error Codes](#error-codes)
7. [Rate Limiting](#rate-limiting)
8. [Examples](#examples)

---

## Authentication

### Headers Required

All API requests must include the following headers:

```http
Authorization: Bearer {access_token}
X-Tenant-ID: {tenant_id}
Content-Type: application/json
```

### Getting Access Token

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "operator1",
  "password": "password123"
}
```

**Response**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "userId": 123,
    "username": "operator1",
    "fullName": "John Doe",
    "tenantId": "tenant1"
  }
}
```

---

## POP Endpoints

### 1. Get Active Work Orders

Get list of active work orders for the operator.

```http
GET /api/pop/work-orders/active?operatorId={operatorId}
```

**Parameters**:
- `operatorId` (optional, integer): Filter by operator ID

**Response** (200 OK):
```json
[
  {
    "workOrderId": 1,
    "workOrderNo": "WO-2024-001",
    "productCode": "PROD-001",
    "productName": "Test Product A",
    "processName": "Assembly",
    "plannedQuantity": 100,
    "actualQuantity": 50,
    "goodQuantity": 48,
    "defectQuantity": 2,
    "status": "IN_PROGRESS",
    "assignedUserId": 123,
    "assignedUserName": "John Doe",
    "plannedStartDate": "2024-01-01T09:00:00",
    "plannedEndDate": "2024-01-01T17:00:00",
    "actualStartDate": "2024-01-01T09:05:00"
  }
]
```

**Status Codes**:
- `200`: Success
- `401`: Unauthorized
- `403`: Forbidden
- `500`: Internal server error

---

### 2. Start Work Order

Start working on a work order.

```http
POST /api/pop/work-orders/{workOrderId}/start
Content-Type: application/json

{
  "operatorId": 123
}
```

**Path Parameters**:
- `workOrderId` (integer, required): Work order ID

**Request Body**:
```json
{
  "operatorId": 123
}
```

**Response** (200 OK):
```json
{
  "progressId": 1,
  "workOrderId": 1,
  "workOrderNo": "WO-2024-001",
  "operatorId": 123,
  "operatorName": "John Doe",
  "recordDate": "2024-01-01",
  "startTime": "09:05:00",
  "status": "IN_PROGRESS",
  "producedQuantity": 0,
  "goodQuantity": 0,
  "defectQuantity": 0,
  "pauseCount": 0,
  "totalPauseDuration": 0
}
```

**Errors**:
- `400`: Work order already started
- `404`: Work order not found
- `409`: Conflict - operator has another active work

---

### 3. Record Production Progress

Record production quantities.

```http
POST /api/pop/work-progress/record
Content-Type: application/json

{
  "progressId": 1,
  "producedQuantity": 50,
  "goodQuantity": 50
}
```

**Request Body**:
```json
{
  "progressId": 1,
  "producedQuantity": 50,
  "goodQuantity": 48
}
```

**Fields**:
- `progressId` (integer, required): Work progress ID
- `producedQuantity` (integer, required): Total produced quantity (increment)
- `goodQuantity` (integer, optional): Good quantity (defaults to producedQuantity)

**Response** (200 OK):
```json
{
  "progressId": 1,
  "workOrderId": 1,
  "workOrderNo": "WO-2024-001",
  "producedQuantity": 50,
  "goodQuantity": 48,
  "defectQuantity": 2,
  "status": "IN_PROGRESS",
  "progressRate": 50.0
}
```

**Notes**:
- Quantities are cumulative (added to existing totals)
- Defect quantity auto-calculated: `producedQuantity - goodQuantity`
- WebSocket broadcast sent to `/topic/work-progress/{tenantId}`

---

### 4. Record Defect

Record defect occurrence.

```http
POST /api/pop/work-progress/defect
Content-Type: application/json

{
  "progressId": 1,
  "defectQuantity": 5,
  "defectType": "외관 불량",
  "defectReason": "표면 스크래치",
  "defectLocation": "상단 모서리",
  "severity": "MINOR",
  "notes": "재작업 가능"
}
```

**Request Body**:
```json
{
  "progressId": 1,
  "defectQuantity": 5,
  "defectType": "외관 불량",
  "defectReason": "표면 스크래치 발견",
  "defectLocation": "상단 모서리",
  "severity": "MINOR",
  "notes": "재작업 가능"
}
```

**Fields**:
- `progressId` (integer, required): Work progress ID
- `defectQuantity` (integer, required): Number of defective items
- `defectType` (string, required): Defect type code
- `defectReason` (string, optional): Reason for defect
- `defectLocation` (string, optional): Location of defect
- `severity` (string, required): CRITICAL, MAJOR, or MINOR
- `notes` (string, optional): Additional notes

**Defect Types**:
- `외관 불량`: Appearance defect
- `치수 불량`: Dimension defect
- `기능 불량`: Functional defect
- `재질 불량`: Material defect
- `조립 불량`: Assembly defect
- `포장 불량`: Packaging defect
- `기타`: Other

**Response** (200 OK):
```json
{
  "defectId": 1,
  "defectNo": "DEF-2024-001",
  "defectQuantity": 5,
  "defectType": "외관 불량",
  "defectReason": "표면 스크래치 발견",
  "severity": "MINOR",
  "status": "REPORTED",
  "occurrenceDate": "2024-01-01T10:30:00",
  "reporterUserId": 123,
  "reporterName": "John Doe"
}
```

**Notes**:
- Creates DefectEntity in database
- Updates work progress defect quantity
- WebSocket broadcast to `/topic/defects/{tenantId}`

---

### 5. Pause Work

Pause work temporarily.

```http
POST /api/pop/work-orders/{workOrderId}/pause
Content-Type: application/json

{
  "pauseReason": "휴식 시간",
  "pauseType": "BREAK"
}
```

**Request Body**:
```json
{
  "pauseReason": "휴식 시간",
  "pauseType": "BREAK"
}
```

**Fields**:
- `pauseReason` (string, optional): Reason for pause
- `pauseType` (string, optional): BREAK, MAINTENANCE, MATERIAL_WAIT, MEETING, OTHER

**Response** (200 OK):
```json
{
  "progressId": 1,
  "workOrderId": 1,
  "status": "PAUSED",
  "pauseCount": 1,
  "currentPauseTime": "2024-01-01T10:00:00"
}
```

**Notes**:
- Creates PauseResumeEntity with pause time
- Updates work progress status to PAUSED
- Timer stops

---

### 6. Resume Work

Resume paused work.

```http
POST /api/pop/work-orders/{workOrderId}/resume
```

**No request body required**

**Response** (200 OK):
```json
{
  "progressId": 1,
  "workOrderId": 1,
  "status": "IN_PROGRESS",
  "pauseCount": 1,
  "totalPauseDuration": 15,
  "lastPauseDuration": 15
}
```

**Notes**:
- Updates PauseResumeEntity with resume time
- Calculates pause duration
- Resumes timer
- Updates total pause duration

---

### 7. Complete Work Order

Mark work order as completed.

```http
POST /api/pop/work-orders/{workOrderId}/complete
Content-Type: application/json

{
  "remarks": "정상 완료"
}
```

**Request Body**:
```json
{
  "remarks": "정상 완료"
}
```

**Fields**:
- `remarks` (string, optional): Completion remarks

**Response** (200 OK):
```json
{
  "workOrderId": 1,
  "workOrderNo": "WO-2024-001",
  "status": "COMPLETED",
  "actualQuantity": 100,
  "goodQuantity": 95,
  "defectQuantity": 5,
  "actualStartDate": "2024-01-01T09:05:00",
  "actualEndDate": "2024-01-01T17:30:00",
  "workResultId": 1
}
```

**What Happens**:
1. Updates WorkOrder status to COMPLETED
2. Creates WorkResultEntity
3. Updates inventory (adds finished goods)
4. Closes WorkProgressEntity
5. WebSocket broadcast

**Validation**:
- All required SOP steps must be completed (if applicable)
- Work must be in IN_PROGRESS or PAUSED status

---

### 8. Get Work Progress

Get current work progress for a work order.

```http
GET /api/pop/work-orders/{workOrderId}/progress
```

**Response** (200 OK):
```json
{
  "progressId": 1,
  "workOrderId": 1,
  "workOrderNo": "WO-2024-001",
  "operatorId": 123,
  "operatorName": "John Doe",
  "recordDate": "2024-01-01",
  "startTime": "09:05:00",
  "status": "IN_PROGRESS",
  "producedQuantity": 50,
  "goodQuantity": 48,
  "defectQuantity": 2,
  "pauseCount": 1,
  "totalPauseDuration": 15,
  "elapsedMinutes": 240,
  "progressRate": 50.0
}
```

**Notes**:
- Returns active work progress only
- If multiple progress records exist, returns latest active one

---

### 9. Get Today's Statistics

Get production statistics for today.

```http
GET /api/pop/statistics/today?operatorId={operatorId}
```

**Parameters**:
- `operatorId` (optional, integer): Filter by operator (if omitted, returns all)

**Response** (200 OK):
```json
{
  "date": "2024-01-01",
  "totalProduced": 500,
  "totalGood": 475,
  "totalDefect": 25,
  "defectRate": 5.0,
  "activeWorkOrders": 3,
  "completedWorkOrders": 2,
  "totalWorkTime": 480,
  "totalPauseTime": 45,
  "utilizationRate": 90.6,
  "operators": [
    {
      "operatorId": 123,
      "operatorName": "John Doe",
      "producedQuantity": 100,
      "goodQuantity": 95,
      "defectQuantity": 5,
      "workOrdersCompleted": 1
    }
  ],
  "defectsByType": [
    {
      "defectType": "외관 불량",
      "quantity": 15,
      "percentage": 60.0
    },
    {
      "defectType": "치수 불량",
      "quantity": 10,
      "percentage": 40.0
    }
  ]
}
```

**Calculated Fields**:
- `defectRate`: (totalDefect / totalProduced) * 100
- `utilizationRate`: (totalWorkTime / (totalWorkTime + totalPauseTime)) * 100

---

### 10. Scan Barcode

Scan and process barcode.

```http
POST /api/pop/scan
Content-Type: application/json

{
  "barcode": "WO-2024-001",
  "barcodeType": "WORK_ORDER"
}
```

**Request Body**:
```json
{
  "barcode": "WO-2024-001",
  "barcodeType": "WORK_ORDER"
}
```

**Barcode Types**:
- `WORK_ORDER`: Work order lookup
- `MATERIAL`: Material verification
- `PRODUCT`: Product identification
- `LOT`: Lot tracking

**Response for WORK_ORDER** (200 OK):
```json
{
  "type": "WORK_ORDER",
  "workOrder": {
    "workOrderId": 1,
    "workOrderNo": "WO-2024-001",
    "productCode": "PROD-001",
    "productName": "Test Product A",
    "status": "READY"
  }
}
```

**Response for MATERIAL** (200 OK):
```json
{
  "type": "MATERIAL",
  "material": {
    "materialId": 1,
    "materialCode": "MAT-001",
    "materialName": "Raw Material A",
    "availableQuantity": 1000
  }
}
```

**Response for PRODUCT** (200 OK):
```json
{
  "type": "PRODUCT",
  "product": {
    "productId": 1,
    "productCode": "PROD-001",
    "productName": "Test Product A",
    "productType": "FG"
  }
}
```

**Response for LOT** (200 OK):
```json
{
  "type": "LOT",
  "lot": {
    "lotId": 1,
    "lotNo": "LOT-2024-001",
    "productCode": "PROD-001",
    "quantity": 100,
    "status": "ACTIVE"
  }
}
```

**Errors**:
- `400`: Invalid barcode format
- `404`: Barcode not found
- `422`: Barcode type mismatch

---

## SOP Operator Endpoints

### 1. Get Work Order SOPs

Get SOPs linked to a work order.

```http
GET /api/sop/operator/work-order/{workOrderId}
```

**Response** (200 OK):
```json
[
  {
    "sopId": 1,
    "sopNo": "SOP-ASSY-001",
    "sopName": "조립 표준 작업",
    "sopType": "PRODUCTION",
    "version": "1.0",
    "stepCount": 5,
    "estimatedTime": 30,
    "steps": [
      {
        "stepId": 1,
        "stepNumber": 1,
        "stepTitle": "원자재 준비",
        "stepDescription": "필요한 원자재를 준비합니다",
        "isMandatory": true,
        "isCritical": true
      }
    ]
  }
]
```

---

### 2. Start SOP Execution

Start SOP execution for a work order.

```http
POST /api/sop/operator/execution/start
Content-Type: application/json

{
  "sopId": 1,
  "workOrderId": 1,
  "operatorId": 123
}
```

**Request Body**:
```json
{
  "sopId": 1,
  "workOrderId": 1,
  "operatorId": 123
}
```

**Response** (200 OK):
```json
{
  "executionId": 1,
  "executionNo": "EXEC-2024-001",
  "sopId": 1,
  "sopNo": "SOP-ASSY-001",
  "sopName": "조립 표준 작업",
  "workOrderId": 1,
  "executionStatus": "IN_PROGRESS",
  "executionDate": "2024-01-01T09:00:00",
  "steps": [
    {
      "executionStepId": 1,
      "stepNumber": 1,
      "stepTitle": "원자재 준비",
      "stepStatus": "PENDING",
      "isMandatory": true,
      "isCritical": true
    }
  ]
}
```

**What Happens**:
- Creates SOPExecutionEntity
- Auto-creates SOPExecutionStepEntity for each SOP step
- Sets status to IN_PROGRESS

---

### 3. Complete SOP Step

Mark SOP step as complete with Pass/Fail result.

```http
PUT /api/sop/operator/execution/{executionId}/step/{executionStepId}/complete
Content-Type: application/json

{
  "passed": true,
  "notes": "정상 완료"
}
```

**Request Body**:
```json
{
  "passed": true,
  "notes": "정상 완료"
}
```

**Fields**:
- `passed` (boolean, required): true for Pass, false for Fail
- `notes` (string, optional): Notes (required if passed=false)

**Response** (204 No Content)

**What Happens**:
- Updates SOPExecutionStepEntity
- Sets stepStatus to COMPLETED (if passed) or FAILED (if not passed)
- Records checkResult and checkNotes
- Updates checkTime

**Validation**:
- If `passed=false`, `notes` is required

---

### 4. Complete SOP Execution

Finalize SOP execution.

```http
POST /api/sop/operator/execution/{executionId}/complete
Content-Type: application/json

{
  "remarks": "모든 단계 완료"
}
```

**Request Body**:
```json
{
  "remarks": "모든 단계 완료"
}
```

**Response** (200 OK):
```json
{
  "executionId": 1,
  "executionNo": "EXEC-2024-001",
  "executionStatus": "COMPLETED",
  "completionRate": 100.0,
  "passedSteps": 5,
  "failedSteps": 0,
  "executionDate": "2024-01-01T09:00:00",
  "endTime": "2024-01-01T09:30:00",
  "duration": 30
}
```

**Validation**:
- All mandatory steps must be completed
- At least one step must be completed

---

## WebSocket Topics

### Connection

```javascript
const socket = new SockJS('https://your-domain.com/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({
  'Authorization': 'Bearer ' + accessToken,
  'X-Tenant-ID': tenantId
}, function(frame) {
  console.log('Connected: ' + frame);
});
```

### Topics

#### 1. Work Order Updates
**Topic**: `/topic/work-orders/{tenantId}`

**Message Format**:
```json
{
  "workOrderId": 1,
  "workOrderNo": "WO-2024-001",
  "status": "IN_PROGRESS",
  "actualQuantity": 50,
  "timestamp": "2024-01-01T10:30:00"
}
```

**When Published**:
- Work order started
- Work order paused
- Work order resumed
- Work order completed

---

#### 2. Work Progress Updates
**Topic**: `/topic/work-progress/{tenantId}`

**Message Format**:
```json
{
  "progressId": 1,
  "workOrderId": 1,
  "producedQuantity": 50,
  "goodQuantity": 48,
  "defectQuantity": 2,
  "timestamp": "2024-01-01T10:30:00"
}
```

**When Published**:
- Production progress recorded
- Work paused/resumed

---

#### 3. Defect Updates
**Topic**: `/topic/defects/{tenantId}`

**Message Format**:
```json
{
  "defectId": 1,
  "workOrderId": 1,
  "defectType": "외관 불량",
  "defectQuantity": 5,
  "severity": "MINOR",
  "timestamp": "2024-01-01T10:30:00"
}
```

**When Published**:
- Defect recorded

---

#### 4. SOP Execution Updates
**Topic**: `/topic/sop-execution/{tenantId}`

**Message Format**:
```json
{
  "executionId": 1,
  "executionStatus": "IN_PROGRESS",
  "completionRate": 40.0,
  "passedSteps": 2,
  "failedSteps": 0,
  "timestamp": "2024-01-01T10:30:00"
}
```

**When Published**:
- SOP execution started
- SOP step completed
- SOP execution completed

---

## Data Models

### WorkOrderResponse

```typescript
interface WorkOrderResponse {
  workOrderId: number;
  workOrderNo: string;
  productCode: string;
  productName: string;
  processName?: string;
  plannedQuantity: number;
  actualQuantity: number;
  goodQuantity: number;
  defectQuantity: number;
  status: 'PENDING' | 'READY' | 'IN_PROGRESS' | 'PAUSED' | 'COMPLETED' | 'CANCELLED';
  assignedUserId?: number;
  assignedUserName?: string;
  plannedStartDate?: string; // ISO 8601
  plannedEndDate?: string;
  actualStartDate?: string;
  actualEndDate?: string;
}
```

### WorkProgressResponse

```typescript
interface WorkProgressResponse {
  progressId: number;
  workOrderId: number;
  workOrderNo: string;
  operatorId: number;
  operatorName: string;
  recordDate: string; // YYYY-MM-DD
  startTime: string; // HH:mm:ss
  endTime?: string;
  status: 'IN_PROGRESS' | 'PAUSED' | 'COMPLETED';
  producedQuantity: number;
  goodQuantity: number;
  defectQuantity: number;
  pauseCount: number;
  totalPauseDuration: number; // minutes
  elapsedMinutes?: number;
  progressRate?: number; // percentage
}
```

### DefectResponse

```typescript
interface DefectResponse {
  defectId: number;
  defectNo: string;
  defectQuantity: number;
  defectType: string;
  defectReason?: string;
  defectLocation?: string;
  severity: 'CRITICAL' | 'MAJOR' | 'MINOR';
  status: 'REPORTED' | 'UNDER_REVIEW' | 'RESOLVED' | 'CLOSED';
  occurrenceDate: string; // ISO 8601
  reporterUserId: number;
  reporterName: string;
  notes?: string;
}
```

### ProductionStatisticsResponse

```typescript
interface ProductionStatisticsResponse {
  date: string; // YYYY-MM-DD
  totalProduced: number;
  totalGood: number;
  totalDefect: number;
  defectRate: number; // percentage
  activeWorkOrders: number;
  completedWorkOrders: number;
  totalWorkTime: number; // minutes
  totalPauseTime: number; // minutes
  utilizationRate: number; // percentage
  operators: OperatorStatistics[];
  defectsByType: DefectTypeStatistics[];
}

interface OperatorStatistics {
  operatorId: number;
  operatorName: string;
  producedQuantity: number;
  goodQuantity: number;
  defectQuantity: number;
  workOrdersCompleted: number;
}

interface DefectTypeStatistics {
  defectType: string;
  quantity: number;
  percentage: number;
}
```

---

## Error Codes

### HTTP Status Codes

| Code | Meaning | Description |
|------|---------|-------------|
| 200 | OK | Request successful |
| 201 | Created | Resource created successfully |
| 204 | No Content | Request successful, no content returned |
| 400 | Bad Request | Invalid request parameters |
| 401 | Unauthorized | Missing or invalid authentication token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource conflict (e.g., already started) |
| 422 | Unprocessable Entity | Validation error |
| 429 | Too Many Requests | Rate limit exceeded |
| 500 | Internal Server Error | Server error |

### Error Response Format

```json
{
  "timestamp": "2024-01-01T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Work order already started",
  "code": "WORK_ORDER_ALREADY_STARTED",
  "path": "/api/pop/work-orders/1/start"
}
```

### Common Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `UNAUTHORIZED` | 401 | Invalid or expired token |
| `FORBIDDEN` | 403 | Insufficient permissions |
| `WORK_ORDER_NOT_FOUND` | 404 | Work order not found |
| `WORK_ORDER_ALREADY_STARTED` | 400 | Work order already in progress |
| `INVALID_OPERATION` | 400 | Operation not allowed in current state |
| `VALIDATION_ERROR` | 422 | Request validation failed |
| `BARCODE_NOT_FOUND` | 404 | Barcode not found in system |
| `SOP_NOT_FOUND` | 404 | SOP not found |
| `STEP_NOT_COMPLETED` | 400 | Required SOP step not completed |

---

## Rate Limiting

### Limits

- **Standard endpoints**: 100 requests per minute per user
- **Statistics endpoints**: 10 requests per minute per user
- **WebSocket connections**: 5 connections per user

### Headers

```http
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1609459200
```

### Rate Limit Exceeded Response

```http
HTTP/1.1 429 Too Many Requests
Content-Type: application/json

{
  "timestamp": "2024-01-01T10:30:00",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Try again in 60 seconds.",
  "code": "RATE_LIMIT_EXCEEDED"
}
```

---

## Examples

### Complete Workflow Example

```javascript
// 1. Login
const loginResponse = await fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'operator1',
    password: 'password123'
  })
});
const { accessToken, user } = await loginResponse.json();

// 2. Get active work orders
const workOrders = await fetch('/api/pop/work-orders/active', {
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'X-Tenant-ID': user.tenantId
  }
});

// 3. Start work order
const progress = await fetch('/api/pop/work-orders/1/start', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'X-Tenant-ID': user.tenantId,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ operatorId: user.userId })
});

// 4. Record production
await fetch('/api/pop/work-progress/record', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'X-Tenant-ID': user.tenantId,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    progressId: 1,
    producedQuantity: 50,
    goodQuantity: 48
  })
});

// 5. Record defect
await fetch('/api/pop/work-progress/defect', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'X-Tenant-ID': user.tenantId,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    progressId: 1,
    defectQuantity: 2,
    defectType: '외관 불량',
    severity: 'MINOR'
  })
});

// 6. Complete work order
await fetch('/api/pop/work-orders/1/complete', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'X-Tenant-ID': user.tenantId,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ remarks: '정상 완료' })
});
```

### WebSocket Example

```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const client = new Client({
  webSocketFactory: () => new SockJS('https://your-domain.com/ws'),
  connectHeaders: {
    'Authorization': `Bearer ${accessToken}`,
    'X-Tenant-ID': tenantId
  },
  onConnect: () => {
    console.log('Connected');

    // Subscribe to work progress updates
    client.subscribe('/topic/work-progress/' + tenantId, (message) => {
      const progress = JSON.parse(message.body);
      console.log('Work progress update:', progress);
      updateDashboard(progress);
    });

    // Subscribe to defect updates
    client.subscribe('/topic/defects/' + tenantId, (message) => {
      const defect = JSON.parse(message.body);
      console.log('Defect reported:', defect);
      showDefectAlert(defect);
    });
  },
  onDisconnect: () => {
    console.log('Disconnected');
  }
});

client.activate();
```

---

## Support

**Developer Documentation**: https://docs.your-company.com/api
**Support Email**: dev-support@your-company.com
**API Status**: https://status.your-company.com

---

**Version**: 1.0
**Last Updated**: 2025-02-05
**Maintained by**: SoftIce Co., Ltd.
