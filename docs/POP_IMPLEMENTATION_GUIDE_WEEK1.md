# Week 1: POP 현장 프로그램 구현 가이드

**작성일**: 2026-02-04
**대상**: SoIce MES Phase 1 - Week 1 (Day 1-7)
**목표**: POP 현장 프로그램 완성 (33% → 75%)
**예상 기간**: 7일

---

## 📋 목차

1. [개요](#1-개요)
2. [Day 1-2: 현장 작업 등록 화면](#2-day-1-2-현장-작업-등록-화면)
3. [Day 3-4: 반제품 입출고](#3-day-3-4-반제품-입출고)
4. [Day 5-6: IQC/OQC POP 화면](#4-day-5-6-iqcoqc-pop-화면)
5. [Day 7: 통합 및 테스트](#5-day-7-통합-및-테스트)
6. [API 명세서](#6-api-명세서)
7. [데이터 모델](#7-데이터-모델)
8. [테스트 시나리오](#8-테스트-시나리오)

---

## 1. 개요

### 1.1 현재 상태

**기존 구현**:
- ✅ POPHomePage - 메인 대시보드
- ✅ POPWorkOrderPage - 작업 지시 (Mock 데이터)
- ✅ POPScannerPage - 바코드 스캔
- ✅ POPSOPPage - SOP 체크리스트
- ✅ POPPerformancePage - 실적 현황

**문제점**:
- ❌ Backend API 없음 (Mock 데이터만 사용)
- ❌ 실제 DB 연동 없음
- ❌ 반제품 입출고 기능 없음
- ❌ IQC/OQC POP 화면 없음
- ❌ 실시간 작업 실적 기록 없음

### 1.2 목표

**Week 1 완료 시**:
- ✅ Backend POP API 완성 (10+ 엔드포인트)
- ✅ 실시간 작업 실적 기록
- ✅ 반제품 입출고 관리
- ✅ IQC/OQC POP 화면
- ✅ 바코드 스캔 통합
- ✅ 오프라인 모드 지원

### 1.3 기술 스택

**Backend**:
- Spring Boot 3.2+
- JPA/Hibernate
- PostgreSQL 16
- WebSocket (실시간 업데이트)

**Frontend**:
- React 18 + TypeScript
- Material-UI v5
- React Query (데이터 페칭)
- IndexedDB (오프라인)

---

## 2. Day 1-2: 현장 작업 등록 화면

### 2.1 Backend 구현

#### 2.1.1 POPController.java

**파일 위치**: `backend/src/main/java/kr/co/softice/mes/api/controller/POPController.java`

**엔드포인트**:

```java
/**
 * POP Controller
 * Point of Production - Field operations API
 */
@RestController
@RequestMapping("/api/pop")
@RequiredArgsConstructor
public class POPController {

    private final POPService popService;

    /**
     * 1. Get active work orders for operator
     * GET /api/pop/work-orders/active
     */
    @GetMapping("/work-orders/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<WorkOrderResponse>>> getActiveWorkOrders(
            @RequestParam(required = false) Long operatorId) {

        String tenantId = TenantContext.getCurrentTenant();
        List<WorkOrderEntity> workOrders = popService.getActiveWorkOrders(tenantId, operatorId);

        return ResponseEntity.ok(ApiResponse.success(
            workOrders.stream()
                .map(this::toWorkOrderResponse)
                .collect(Collectors.toList())
        ));
    }

    /**
     * 2. Start work order
     * POST /api/pop/work-orders/{id}/start
     */
    @PostMapping("/work-orders/{id}/start")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WorkProgressResponse>> startWorkOrder(
            @PathVariable Long id,
            @RequestBody WorkStartRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        WorkProgressEntity progress = popService.startWorkOrder(tenantId, id, request);

        return ResponseEntity.ok(ApiResponse.success(toWorkProgressResponse(progress)));
    }

    /**
     * 3. Record work progress (production quantity)
     * POST /api/pop/work-progress/record
     */
    @PostMapping("/work-progress/record")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WorkProgressResponse>> recordProgress(
            @RequestBody WorkProgressRecordRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        WorkProgressEntity progress = popService.recordProgress(tenantId, request);

        return ResponseEntity.ok(ApiResponse.success(toWorkProgressResponse(progress)));
    }

    /**
     * 4. Record defect
     * POST /api/pop/work-progress/defect
     */
    @PostMapping("/work-progress/defect")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DefectRecordResponse>> recordDefect(
            @RequestBody DefectRecordRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        DefectEntity defect = popService.recordDefect(tenantId, request);

        return ResponseEntity.ok(ApiResponse.success(toDefectRecordResponse(defect)));
    }

    /**
     * 5. Pause work
     * POST /api/pop/work-orders/{id}/pause
     */
    @PostMapping("/work-orders/{id}/pause")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WorkProgressResponse>> pauseWork(
            @PathVariable Long id,
            @RequestBody WorkPauseRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        WorkProgressEntity progress = popService.pauseWork(tenantId, id, request);

        return ResponseEntity.ok(ApiResponse.success(toWorkProgressResponse(progress)));
    }

    /**
     * 6. Resume work
     * POST /api/pop/work-orders/{id}/resume
     */
    @PostMapping("/work-orders/{id}/resume")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WorkProgressResponse>> resumeWork(
            @PathVariable Long id) {

        String tenantId = TenantContext.getCurrentTenant();
        WorkProgressEntity progress = popService.resumeWork(tenantId, id);

        return ResponseEntity.ok(ApiResponse.success(toWorkProgressResponse(progress)));
    }

    /**
     * 7. Complete work order
     * POST /api/pop/work-orders/{id}/complete
     */
    @PostMapping("/work-orders/{id}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> completeWorkOrder(
            @PathVariable Long id,
            @RequestBody WorkCompleteRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        WorkOrderEntity workOrder = popService.completeWorkOrder(tenantId, id, request);

        return ResponseEntity.ok(ApiResponse.success(toWorkOrderResponse(workOrder)));
    }

    /**
     * 8. Get work progress by work order
     * GET /api/pop/work-orders/{id}/progress
     */
    @GetMapping("/work-orders/{id}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WorkProgressResponse>> getWorkProgress(
            @PathVariable Long id) {

        String tenantId = TenantContext.getCurrentTenant();
        WorkProgressEntity progress = popService.getWorkProgress(tenantId, id);

        return ResponseEntity.ok(ApiResponse.success(toWorkProgressResponse(progress)));
    }

    /**
     * 9. Get today's production statistics
     * GET /api/pop/statistics/today
     */
    @GetMapping("/statistics/today")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProductionStatisticsResponse>> getTodayStatistics(
            @RequestParam(required = false) Long operatorId) {

        String tenantId = TenantContext.getCurrentTenant();
        ProductionStatisticsResponse stats = popService.getTodayStatistics(tenantId, operatorId);

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 10. Scan barcode (work order, material, product)
     * POST /api/pop/scan
     */
    @PostMapping("/scan")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ScanResultResponse>> scanBarcode(
            @RequestBody BarcodeScanRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        ScanResultResponse result = popService.scanBarcode(tenantId, request);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
```

#### 2.1.2 POPService.java

**파일 위치**: `backend/src/main/java/kr/co/softice/mes/domain/service/POPService.java`

**핵심 로직**:

```java
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class POPService {

    private final WorkOrderRepository workOrderRepository;
    private final WorkProgressRepository workProgressRepository;
    private final DefectRepository defectRepository;
    private final InventoryService inventoryService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Get active work orders for operator
     */
    @Transactional(readOnly = true)
    public List<WorkOrderEntity> getActiveWorkOrders(String tenantId, Long operatorId) {
        if (operatorId != null) {
            return workOrderRepository.findByTenantIdAndOperatorIdAndStatus(
                tenantId, operatorId, "IN_PROGRESS", "READY"
            );
        } else {
            return workOrderRepository.findByTenantIdAndStatus(
                tenantId, "IN_PROGRESS", "READY"
            );
        }
    }

    /**
     * Start work order
     */
    public WorkProgressEntity startWorkOrder(String tenantId, Long workOrderId, WorkStartRequest request) {
        // 1. Get work order
        WorkOrderEntity workOrder = workOrderRepository.findById(workOrderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.WORK_ORDER_NOT_FOUND));

        // 2. Validate status
        if (!"READY".equals(workOrder.getStatus())) {
            throw new BusinessException(ErrorCode.WORK_ORDER_ALREADY_STARTED);
        }

        // 3. Update work order status
        workOrder.setStatus("IN_PROGRESS");
        workOrder.setActualStartDate(LocalDateTime.now());
        workOrder.setOperatorUserId(request.getOperatorId());
        workOrderRepository.save(workOrder);

        // 4. Create work progress record
        WorkProgressEntity progress = WorkProgressEntity.builder()
            .tenant(workOrder.getTenant())
            .workOrder(workOrder)
            .operatorUserId(request.getOperatorId())
            .startTime(LocalDateTime.now())
            .producedQuantity(0)
            .defectQuantity(0)
            .status("IN_PROGRESS")
            .build();

        WorkProgressEntity saved = workProgressRepository.save(progress);

        // 5. Broadcast real-time update
        broadcastWorkOrderUpdate(tenantId, workOrder);

        log.info("Work order started: {} by operator {}", workOrderId, request.getOperatorId());

        return saved;
    }

    /**
     * Record work progress (production quantity)
     */
    public WorkProgressEntity recordProgress(String tenantId, WorkProgressRecordRequest request) {
        // 1. Get work progress
        WorkProgressEntity progress = workProgressRepository.findById(request.getProgressId())
            .orElseThrow(() -> new BusinessException(ErrorCode.WORK_PROGRESS_NOT_FOUND));

        // 2. Update produced quantity
        int newQuantity = progress.getProducedQuantity() + request.getQuantity();
        progress.setProducedQuantity(newQuantity);
        progress.setLastUpdateTime(LocalDateTime.now());

        WorkProgressEntity saved = workProgressRepository.save(progress);

        // 3. Update work order produced quantity
        WorkOrderEntity workOrder = progress.getWorkOrder();
        workOrder.setProducedQuantity(newQuantity);
        workOrderRepository.save(workOrder);

        // 4. Broadcast real-time update
        broadcastWorkProgressUpdate(tenantId, saved);

        log.info("Work progress recorded: {} units for work order {}",
                 request.getQuantity(), workOrder.getWorkOrderId());

        return saved;
    }

    /**
     * Record defect
     */
    public DefectEntity recordDefect(String tenantId, DefectRecordRequest request) {
        // 1. Get work progress
        WorkProgressEntity progress = workProgressRepository.findById(request.getProgressId())
            .orElseThrow(() -> new BusinessException(ErrorCode.WORK_PROGRESS_NOT_FOUND));

        // 2. Create defect record
        DefectEntity defect = DefectEntity.builder()
            .tenant(progress.getTenant())
            .workOrder(progress.getWorkOrder())
            .defectType(request.getDefectType())
            .defectQuantity(request.getQuantity())
            .defectReason(request.getReason())
            .detectedDate(LocalDateTime.now())
            .detectedBy(request.getOperatorId())
            .status("DETECTED")
            .build();

        DefectEntity saved = defectRepository.save(defect);

        // 3. Update work progress defect quantity
        int newDefectQuantity = progress.getDefectQuantity() + request.getQuantity();
        progress.setDefectQuantity(newDefectQuantity);
        workProgressRepository.save(progress);

        // 4. Update work order defect quantity
        WorkOrderEntity workOrder = progress.getWorkOrder();
        workOrder.setDefectQuantity(newDefectQuantity);
        workOrderRepository.save(workOrder);

        // 5. Broadcast real-time update
        broadcastDefectUpdate(tenantId, saved);

        log.info("Defect recorded: {} units for work order {}",
                 request.getQuantity(), workOrder.getWorkOrderId());

        return saved;
    }

    /**
     * Pause work
     */
    public WorkProgressEntity pauseWork(String tenantId, Long workOrderId, WorkPauseRequest request) {
        WorkProgressEntity progress = getCurrentProgress(workOrderId);

        progress.setStatus("PAUSED");
        progress.setPauseTime(LocalDateTime.now());
        progress.setPauseReason(request.getReason());

        WorkProgressEntity saved = workProgressRepository.save(progress);

        broadcastWorkProgressUpdate(tenantId, saved);

        return saved;
    }

    /**
     * Resume work
     */
    public WorkProgressEntity resumeWork(String tenantId, Long workOrderId) {
        WorkProgressEntity progress = getCurrentProgress(workOrderId);

        progress.setStatus("IN_PROGRESS");
        progress.setResumeTime(LocalDateTime.now());

        WorkProgressEntity saved = workProgressRepository.save(progress);

        broadcastWorkProgressUpdate(tenantId, saved);

        return saved;
    }

    /**
     * Complete work order
     */
    public WorkOrderEntity completeWorkOrder(String tenantId, Long workOrderId, WorkCompleteRequest request) {
        // 1. Get work order and progress
        WorkOrderEntity workOrder = workOrderRepository.findById(workOrderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.WORK_ORDER_NOT_FOUND));

        WorkProgressEntity progress = getCurrentProgress(workOrderId);

        // 2. Update work order
        workOrder.setStatus("COMPLETED");
        workOrder.setActualEndDate(LocalDateTime.now());
        workOrder.setProducedQuantity(progress.getProducedQuantity());
        workOrder.setDefectQuantity(progress.getDefectQuantity());

        WorkOrderEntity saved = workOrderRepository.save(workOrder);

        // 3. Complete progress
        progress.setStatus("COMPLETED");
        progress.setEndTime(LocalDateTime.now());
        workProgressRepository.save(progress);

        // 4. Update inventory (finished goods)
        inventoryService.recordProduction(
            tenantId,
            workOrder.getProductId(),
            progress.getProducedQuantity() - progress.getDefectQuantity(),
            workOrder.getLotId(),
            "PRODUCTION_COMPLETE",
            workOrderId
        );

        // 5. Broadcast real-time update
        broadcastWorkOrderUpdate(tenantId, saved);

        log.info("Work order completed: {} with {} units produced",
                 workOrderId, progress.getProducedQuantity());

        return saved;
    }

    /**
     * Get work progress by work order
     */
    @Transactional(readOnly = true)
    public WorkProgressEntity getWorkProgress(String tenantId, Long workOrderId) {
        return getCurrentProgress(workOrderId);
    }

    /**
     * Get today's production statistics
     */
    @Transactional(readOnly = true)
    public ProductionStatisticsResponse getTodayStatistics(String tenantId, Long operatorId) {
        LocalDateTime startOfDay = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<WorkProgressEntity> todayProgress;
        if (operatorId != null) {
            todayProgress = workProgressRepository.findByTenantIdAndOperatorIdAndDateRange(
                tenantId, operatorId, startOfDay, endOfDay
            );
        } else {
            todayProgress = workProgressRepository.findByTenantIdAndDateRange(
                tenantId, startOfDay, endOfDay
            );
        }

        int totalProduced = todayProgress.stream()
            .mapToInt(WorkProgressEntity::getProducedQuantity)
            .sum();

        int totalDefects = todayProgress.stream()
            .mapToInt(WorkProgressEntity::getDefectQuantity)
            .sum();

        long completedOrders = todayProgress.stream()
            .filter(p -> "COMPLETED".equals(p.getStatus()))
            .count();

        return ProductionStatisticsResponse.builder()
            .date(startOfDay.toLocalDate())
            .totalProduced(totalProduced)
            .totalDefects(totalDefects)
            .completedOrders(completedOrders)
            .defectRate(totalProduced > 0 ? (double) totalDefects / totalProduced * 100 : 0)
            .build();
    }

    /**
     * Scan barcode
     */
    @Transactional(readOnly = true)
    public ScanResultResponse scanBarcode(String tenantId, BarcodeScanRequest request) {
        String barcode = request.getBarcode();
        String type = request.getType(); // WORK_ORDER, MATERIAL, PRODUCT, LOT

        // Try to find entity by barcode
        switch (type) {
            case "WORK_ORDER":
                WorkOrderEntity workOrder = workOrderRepository.findByTenantIdAndWorkOrderNo(tenantId, barcode)
                    .orElseThrow(() -> new BusinessException(ErrorCode.WORK_ORDER_NOT_FOUND));
                return ScanResultResponse.workOrder(workOrder);

            case "MATERIAL":
                MaterialEntity material = materialRepository.findByTenantIdAndMaterialCode(tenantId, barcode)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MATERIAL_NOT_FOUND));
                return ScanResultResponse.material(material);

            case "PRODUCT":
                ProductEntity product = productRepository.findByTenantIdAndProductCode(tenantId, barcode)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
                return ScanResultResponse.product(product);

            case "LOT":
                LotEntity lot = lotRepository.findByTenantIdAndLotNo(tenantId, barcode)
                    .orElseThrow(() -> new BusinessException(ErrorCode.LOT_NOT_FOUND));
                return ScanResultResponse.lot(lot);

            default:
                throw new BusinessException(ErrorCode.INVALID_SCAN_TYPE);
        }
    }

    // Helper methods

    private WorkProgressEntity getCurrentProgress(Long workOrderId) {
        return workProgressRepository.findByWorkOrderIdAndStatus(workOrderId, "IN_PROGRESS", "PAUSED")
            .orElseThrow(() -> new BusinessException(ErrorCode.WORK_PROGRESS_NOT_FOUND));
    }

    private void broadcastWorkOrderUpdate(String tenantId, WorkOrderEntity workOrder) {
        messagingTemplate.convertAndSend(
            "/topic/work-orders/" + tenantId,
            workOrder
        );
    }

    private void broadcastWorkProgressUpdate(String tenantId, WorkProgressEntity progress) {
        messagingTemplate.convertAndSend(
            "/topic/work-progress/" + tenantId,
            progress
        );
    }

    private void broadcastDefectUpdate(String tenantId, DefectEntity defect) {
        messagingTemplate.convertAndSend(
            "/topic/defects/" + tenantId,
            defect
        );
    }
}
```

### 2.2 Frontend 구현

#### 2.2.1 POPWorkProgressPage.tsx (신규)

**파일 위치**: `frontend/src/pages/pop/POPWorkProgressPage.tsx`

**기능**:
- 진행 중인 모든 작업 현황 실시간 표시
- 작업자별 생산 실적
- 라인별 현황
- 목표 대비 진행률

**화면 구조**:

```tsx
/**
 * POP Work Progress Page
 * Real-time monitoring of all work orders in progress
 */

import React, { useEffect, useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  LinearProgress,
  Chip,
  Avatar,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  Divider,
} from '@mui/material';
import {
  TrendingUp as TrendingUpIcon,
  CheckCircle as CheckIcon,
  Warning as WarningIcon,
  Person as PersonIcon,
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { popService } from '@/services/popService';

interface WorkProgress {
  workOrderId: number;
  workOrderNo: string;
  productName: string;
  operatorName: string;
  targetQuantity: number;
  producedQuantity: number;
  defectQuantity: number;
  progress: number;
  status: string;
  startTime: string;
  elapsedTime: string;
}

const POPWorkProgressPage: React.FC = () => {
  const [workProgressList, setWorkProgressList] = useState<WorkProgress[]>([]);

  // Fetch work progress (real-time updates every 5 seconds)
  const { data, isLoading } = useQuery({
    queryKey: ['popWorkProgress'],
    queryFn: () => popService.getActiveWorkProgress(),
    refetchInterval: 5000, // Real-time update every 5 seconds
  });

  useEffect(() => {
    if (data?.data) {
      setWorkProgressList(data.data);
    }
  }, [data]);

  // Calculate summary statistics
  const totalProduced = workProgressList.reduce((sum, wp) => sum + wp.producedQuantity, 0);
  const totalDefects = workProgressList.reduce((sum, wp) => sum + wp.defectQuantity, 0);
  const activeWorkOrders = workProgressList.filter(wp => wp.status === 'IN_PROGRESS').length;

  const getProgressColor = (progress: number) => {
    if (progress >= 80) return 'success';
    if (progress >= 50) return 'warning';
    return 'error';
  };

  const formatElapsedTime = (elapsedTime: string) => {
    // Format: "HH:mm:ss"
    return elapsedTime;
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Typography variant="h4" gutterBottom fontWeight="bold">
        작업 진행 현황
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        모든 작업 지시의 실시간 진행 상황
      </Typography>

      {/* Summary Cards */}
      <Grid container spacing={2} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={4}>
          <Card sx={{ bgcolor: 'primary.main', color: 'white' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <TrendingUpIcon sx={{ mr: 1 }} />
                <Typography variant="subtitle2">진행 중</Typography>
              </Box>
              <Typography variant="h3" fontWeight="bold">
                {activeWorkOrders}
              </Typography>
              <Typography variant="body2">작업 지시</Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={4}>
          <Card sx={{ bgcolor: 'success.main', color: 'white' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <CheckIcon sx={{ mr: 1 }} />
                <Typography variant="subtitle2">금일 생산</Typography>
              </Box>
              <Typography variant="h3" fontWeight="bold">
                {totalProduced.toLocaleString()}
              </Typography>
              <Typography variant="body2">EA</Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={4}>
          <Card sx={{ bgcolor: 'error.main', color: 'white' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <WarningIcon sx={{ mr: 1 }} />
                <Typography variant="subtitle2">불량</Typography>
              </Box>
              <Typography variant="h3" fontWeight="bold">
                {totalDefects.toLocaleString()}
              </Typography>
              <Typography variant="body2">
                ({totalProduced > 0 ? ((totalDefects / totalProduced) * 100).toFixed(1) : 0}%)
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Work Progress List */}
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom fontWeight="bold">
            작업 지시 목록
          </Typography>

          {isLoading ? (
            <Typography>로딩 중...</Typography>
          ) : workProgressList.length === 0 ? (
            <Typography color="text.secondary" sx={{ py: 4, textAlign: 'center' }}>
              진행 중인 작업이 없습니다
            </Typography>
          ) : (
            <List>
              {workProgressList.map((wp, index) => (
                <React.Fragment key={wp.workOrderId}>
                  {index > 0 && <Divider />}
                  <ListItem alignItems="flex-start" sx={{ py: 2 }}>
                    <ListItemAvatar>
                      <Avatar sx={{ bgcolor: 'primary.main' }}>
                        <PersonIcon />
                      </Avatar>
                    </ListItemAvatar>
                    <ListItemText
                      primary={
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                          <Typography variant="h6" fontWeight="bold">
                            {wp.workOrderNo}
                          </Typography>
                          <Chip
                            label={wp.status === 'IN_PROGRESS' ? '진행중' : '일시정지'}
                            color={wp.status === 'IN_PROGRESS' ? 'success' : 'warning'}
                            size="small"
                          />
                        </Box>
                      }
                      secondary={
                        <Box>
                          <Typography variant="body2" color="text.secondary" gutterBottom>
                            {wp.productName} | 작업자: {wp.operatorName} | 경과 시간: {formatElapsedTime(wp.elapsedTime)}
                          </Typography>

                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
                            <Typography variant="h6" color="primary" fontWeight="bold">
                              {wp.producedQuantity.toLocaleString()}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                              / {wp.targetQuantity.toLocaleString()} EA
                            </Typography>
                            <Typography variant="body2" color="error.main">
                              (불량: {wp.defectQuantity})
                            </Typography>
                          </Box>

                          <LinearProgress
                            variant="determinate"
                            value={wp.progress}
                            color={getProgressColor(wp.progress)}
                            sx={{ height: 8, borderRadius: 1 }}
                          />
                          <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5 }}>
                            {wp.progress.toFixed(1)}% 완료
                          </Typography>
                        </Box>
                      }
                    />
                  </ListItem>
                </React.Fragment>
              ))}
            </List>
          )}
        </CardContent>
      </Card>
    </Box>
  );
};

export default POPWorkProgressPage;
```

#### 2.2.2 popService.ts 통합

**파일 위치**: `frontend/src/services/popService.ts`

```typescript
/**
 * POP Service
 * API integration for Point of Production operations
 */

import axios from 'axios';

const API_BASE = '/api/pop';

export interface WorkOrderResponse {
  workOrderId: number;
  workOrderNo: string;
  productCode: string;
  productName: string;
  targetQuantity: number;
  producedQuantity: number;
  defectQuantity: number;
  status: string;
  startTime?: string;
  endTime?: string;
  operatorName?: string;
}

export interface WorkProgressRecordRequest {
  progressId: number;
  quantity: number;
  operatorId: number;
}

export interface DefectRecordRequest {
  progressId: number;
  quantity: number;
  defectType: string;
  reason: string;
  operatorId: number;
}

export interface WorkStartRequest {
  operatorId: number;
  equipmentId?: number;
}

export interface WorkCompleteRequest {
  remarks?: string;
}

export interface ProductionStatisticsResponse {
  date: string;
  totalProduced: number;
  totalDefects: number;
  completedOrders: number;
  defectRate: number;
}

class POPService {
  /**
   * Get active work orders
   */
  async getActiveWorkOrders(operatorId?: number) {
    const params = operatorId ? { operatorId } : {};
    return axios.get(`${API_BASE}/work-orders/active`, { params });
  }

  /**
   * Start work order
   */
  async startWorkOrder(workOrderId: number, request: WorkStartRequest) {
    return axios.post(`${API_BASE}/work-orders/${workOrderId}/start`, request);
  }

  /**
   * Record work progress
   */
  async recordProgress(request: WorkProgressRecordRequest) {
    return axios.post(`${API_BASE}/work-progress/record`, request);
  }

  /**
   * Record defect
   */
  async recordDefect(request: DefectRecordRequest) {
    return axios.post(`${API_BASE}/work-progress/defect`, request);
  }

  /**
   * Pause work
   */
  async pauseWork(workOrderId: number, reason: string) {
    return axios.post(`${API_BASE}/work-orders/${workOrderId}/pause`, { reason });
  }

  /**
   * Resume work
   */
  async resumeWork(workOrderId: number) {
    return axios.post(`${API_BASE}/work-orders/${workOrderId}/resume`);
  }

  /**
   * Complete work order
   */
  async completeWorkOrder(workOrderId: number, request: WorkCompleteRequest) {
    return axios.post(`${API_BASE}/work-orders/${workOrderId}/complete`, request);
  }

  /**
   * Get work progress
   */
  async getWorkProgress(workOrderId: number) {
    return axios.get(`${API_BASE}/work-orders/${workOrderId}/progress`);
  }

  /**
   * Get active work progress (for monitoring page)
   */
  async getActiveWorkProgress() {
    return axios.get(`${API_BASE}/work-progress/active`);
  }

  /**
   * Get today's statistics
   */
  async getTodayStatistics(operatorId?: number) {
    const params = operatorId ? { operatorId } : {};
    return axios.get(`${API_BASE}/statistics/today`, { params });
  }

  /**
   * Scan barcode
   */
  async scanBarcode(barcode: string, type: 'WORK_ORDER' | 'MATERIAL' | 'PRODUCT' | 'LOT') {
    return axios.post(`${API_BASE}/scan`, { barcode, type });
  }
}

export const popService = new POPService();
export default popService;
```

### 2.3 기존 POPWorkOrderPage 업데이트

**변경 사항**:
1. Mock 데이터 제거
2. popService를 통한 실제 API 호출
3. React Query 사용
4. WebSocket 실시간 업데이트 연동

**주요 수정 코드**:

```typescript
// Before (Mock)
useEffect(() => {
  setWorkOrders([
    { workOrderId: 1, workOrderNo: 'WO-20260204-001', ... }
  ]);
}, []);

// After (Real API)
const { data: workOrdersData } = useQuery({
  queryKey: ['activeWorkOrders'],
  queryFn: () => popService.getActiveWorkOrders(),
  refetchInterval: 30000, // Refresh every 30 seconds
});

useEffect(() => {
  if (workOrdersData?.data) {
    setWorkOrders(workOrdersData.data);
  }
}, [workOrdersData]);

// Start work with API
const handleStartWork = async () => {
  if (selectedWorkOrder) {
    try {
      const response = await popService.startWorkOrder(
        selectedWorkOrder.workOrderId,
        { operatorId: currentUser.userId }
      );
      setIsWorking(true);
      setSelectedWorkOrder(response.data.data);
    } catch (error) {
      console.error('Failed to start work:', error);
      alert('작업 시작 실패');
    }
  }
};

// Record production with API
const handleRecordProduction = async (quantity: number) => {
  if (selectedWorkOrder && currentProgress) {
    try {
      await popService.recordProgress({
        progressId: currentProgress.progressId,
        quantity,
        operatorId: currentUser.userId,
      });

      // Update local state
      setSelectedWorkOrder({
        ...selectedWorkOrder,
        producedQuantity: selectedWorkOrder.producedQuantity + quantity,
      });

      // Haptic feedback
      if (navigator.vibrate) {
        navigator.vibrate(100);
      }
    } catch (error) {
      console.error('Failed to record production:', error);
      alert('실적 입력 실패');
    }
  }
};
```

### 2.4 테스트

**단위 테스트**:
```java
@Test
void testStartWorkOrder() {
    // Given
    WorkOrderEntity workOrder = createTestWorkOrder();
    WorkStartRequest request = new WorkStartRequest(1L, null);

    // When
    WorkProgressEntity progress = popService.startWorkOrder("tenant1", workOrder.getWorkOrderId(), request);

    // Then
    assertNotNull(progress);
    assertEquals("IN_PROGRESS", progress.getStatus());
    assertEquals(0, progress.getProducedQuantity());
}

@Test
void testRecordProgress() {
    // Given
    WorkProgressEntity progress = createTestProgress();
    WorkProgressRecordRequest request = new WorkProgressRecordRequest(progress.getProgressId(), 10, 1L);

    // When
    WorkProgressEntity updated = popService.recordProgress("tenant1", request);

    // Then
    assertEquals(10, updated.getProducedQuantity());
}
```

**통합 테스트**:
```java
@Test
void testCompleteWorkflow() {
    // 1. Start work
    WorkProgressEntity progress = popService.startWorkOrder(tenantId, workOrderId, startRequest);
    assertEquals("IN_PROGRESS", progress.getStatus());

    // 2. Record progress multiple times
    popService.recordProgress(tenantId, new WorkProgressRecordRequest(progress.getProgressId(), 50, 1L));
    popService.recordProgress(tenantId, new WorkProgressRecordRequest(progress.getProgressId(), 30, 1L));

    // 3. Record defect
    popService.recordDefect(tenantId, new DefectRecordRequest(progress.getProgressId(), 5, "SCRATCH", "Minor scratch", 1L));

    // 4. Complete work
    WorkOrderEntity completed = popService.completeWorkOrder(tenantId, workOrderId, new WorkCompleteRequest());

    // Verify
    assertEquals("COMPLETED", completed.getStatus());
    assertEquals(80, completed.getProducedQuantity());
    assertEquals(5, completed.getDefectQuantity());
}
```

---

## 3. Day 3-4: 반제품 입출고

### 3.1 개요

**목표**: POP 현장에서 반제품 입출고를 쉽게 처리할 수 있는 터치 최적화 UI 구현

**주요 기능**:
- 반제품 바코드 스캔
- 입출고 수량 입력 (큰 숫자 키패드)
- 위치 지정 (창고/로케이션)
- 실시간 재고 반영
- LOT 추적

### 3.2 Backend API

#### SemiProductController.java (일부)

```java
@RestController
@RequestMapping("/api/pop/semi-products")
@RequiredArgsConstructor
public class SemiProductController {

    /**
     * Receive semi-product (입고)
     */
    @PostMapping("/receive")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SemiProductTransactionResponse>> receiveSemiProduct(
            @RequestBody SemiProductReceiveRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        SemiProductTransactionEntity transaction = semiProductService.receiveSemiProduct(tenantId, request);

        return ResponseEntity.ok(ApiResponse.success(toTransactionResponse(transaction)));
    }

    /**
     * Issue semi-product (출고)
     */
    @PostMapping("/issue")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SemiProductTransactionResponse>> issueSemiProduct(
            @RequestBody SemiProductIssueRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        SemiProductTransactionEntity transaction = semiProductService.issueSemiProduct(tenantId, request);

        return ResponseEntity.ok(ApiResponse.success(toTransactionResponse(transaction)));
    }

    /**
     * Get semi-product inventory
     */
    @GetMapping("/inventory")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<SemiProductInventoryResponse>>> getSemiProductInventory(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId) {

        String tenantId = TenantContext.getCurrentTenant();
        List<SemiProductInventoryResponse> inventory = semiProductService.getSemiProductInventory(
            tenantId, productId, warehouseId
        );

        return ResponseEntity.ok(ApiResponse.success(inventory));
    }
}
```

### 3.3 Frontend 화면

#### POPSemiProductInPage.tsx

**화면 구성**:
```
┌────────────────────────────────────┐
│  반제품 입고                       │
├────────────────────────────────────┤
│  [바코드 스캔] 또는 수동 입력      │
│                                    │
│  제품: [________] 🔍               │
│  LOT:  [________]                  │
│                                    │
│  수량 입력                         │
│  ┌──────────────────────┐         │
│  │      1000            │         │
│  └──────────────────────┘         │
│                                    │
│  [7] [8] [9]                      │
│  [4] [5] [6]                      │
│  [1] [2] [3]                      │
│  [C] [0] [OK]                     │
│                                    │
│  위치: [창고 A] ▼ [위치 1-A] ▼    │
│                                    │
│  [입고 처리]                       │
└────────────────────────────────────┘
```

**핵심 코드**:
```typescript
const POPSemiProductInPage: React.FC = () => {
  const [barcode, setBarcode] = useState('');
  const [quantity, setQuantity] = useState('');
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [selectedWarehouse, setSelectedWarehouse] = useState<Warehouse | null>(null);
  const [selectedLocation, setSelectedLocation] = useState<Location | null>(null);

  const handleNumberPad = (digit: string) => {
    if (digit === 'C') {
      setQuantity('');
    } else if (digit === 'OK') {
      handleReceive();
    } else {
      setQuantity(prev => prev + digit);
    }
  };

  const handleReceive = async () => {
    if (!selectedProduct || !quantity) {
      alert('제품과 수량을 입력하세요');
      return;
    }

    try {
      await semiProductService.receiveSemiProduct({
        productId: selectedProduct.productId,
        quantity: parseInt(quantity),
        warehouseId: selectedWarehouse?.warehouseId,
        locationId: selectedLocation?.locationId,
        operatorId: currentUser.userId,
      });

      alert('입고 처리 완료');
      setQuantity('');
      setBarcode('');
      setSelectedProduct(null);
    } catch (error) {
      alert('입고 처리 실패');
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* Number Pad */}
      <Grid container spacing={1} sx={{ mb: 3 }}>
        {['7', '8', '9', '4', '5', '6', '1', '2', '3', 'C', '0', 'OK'].map(digit => (
          <Grid item xs={4} key={digit}>
            <Button
              variant="contained"
              size="large"
              fullWidth
              onClick={() => handleNumberPad(digit)}
              sx={{ py: 3, fontSize: '1.5rem', fontWeight: 'bold' }}
              color={digit === 'OK' ? 'success' : digit === 'C' ? 'error' : 'primary'}
            >
              {digit}
            </Button>
          </Grid>
        ))}
      </Grid>

      {/* Receive Button */}
      <Button
        variant="contained"
        size="large"
        fullWidth
        onClick={handleReceive}
        disabled={!selectedProduct || !quantity}
        sx={{ py: 3, fontSize: '1.2rem' }}
      >
        입고 처리
      </Button>
    </Box>
  );
};
```

---

## 4. Day 5-6: IQC/OQC POP 화면

### 4.1 개요

**목표**: 현장에서 빠르게 입하검사(IQC)와 출하검사(OQC)를 수행할 수 있는 터치 최적화 UI

**주요 기능**:
- 검사 항목 체크리스트
- 측정값 입력 (숫자 키패드)
- 사진 촬영 (불량 증거)
- 즉시 판정 (합격/불합격)
- 적부 라벨 출력

### 4.2 화면 구성

#### POPIQCPage.tsx

```
┌────────────────────────────────────┐
│  IQC 입하 검사                     │
├────────────────────────────────────┤
│  입하 정보                         │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━       │
│  입하번호: GR-20260204-001        │
│  품목: 원자재 A                    │
│  수량: 1000 EA                     │
│                                    │
│  검사 항목 (5/10 완료)             │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━       │
│  ☑ 외관 검사      [합격]          │
│  ☑ 무게 측정      1.05 kg ✓       │
│  ☑ 치수 측정      10.2 mm ✓       │
│  □ 강도 시험      [입력]          │
│  □ 색상 확인      [입력]          │
│                                    │
│  [📷 사진 촬영]  [합격]  [불합격]  │
└────────────────────────────────────┘
```

**핵심 로직**:
```typescript
const POPIQCPage: React.FC = () => {
  const [inspectionItems, setInspectionItems] = useState<InspectionItem[]>([]);
  const [currentItemIndex, setCurrentItemIndex] = useState(0);
  const [measurementValue, setMeasurementValue] = useState('');
  const [photos, setPhotos] = useState<File[]>([]);

  const handlePass = async () => {
    try {
      await qualityService.completeIQC({
        goodsReceiptId: currentGoodsReceipt.goodsReceiptId,
        result: 'PASS',
        inspectionItems: inspectionItems.map(item => ({
          itemId: item.itemId,
          result: item.result,
          measurementValue: item.measurementValue,
          remarks: item.remarks,
        })),
        inspectorId: currentUser.userId,
      });

      // Print pass label
      await printPassLabel(currentGoodsReceipt);

      alert('검사 완료 - 합격');
      navigate('/pop/iqc');
    } catch (error) {
      alert('검사 처리 실패');
    }
  };

  const handleFail = async () => {
    try {
      await qualityService.completeIQC({
        goodsReceiptId: currentGoodsReceipt.goodsReceiptId,
        result: 'FAIL',
        inspectionItems,
        photos,
        inspectorId: currentUser.userId,
      });

      // Print fail label (red)
      await printFailLabel(currentGoodsReceipt);

      alert('검사 완료 - 불합격\n격리 구역으로 이동하세요');
      navigate('/pop/iqc');
    } catch (error) {
      alert('검사 처리 실패');
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* Inspection items checklist */}
      {/* Pass/Fail buttons */}
    </Box>
  );
};
```

---

## 5. Day 7: 통합 및 테스트

### 5.1 통합 테스트 시나리오

**시나리오 1: 완전한 생산 워크플로우**
```
1. 작업자 로그인
2. 바코드로 작업 지시 스캔
3. 작업 시작
4. 자재 스캔 및 투입 확인
5. 실적 입력 (빠른 버튼)
6. 불량 발생 → 불량 등록
7. 작업 완료
8. 반제품 입고
9. LOT 생성 확인
10. 재고 반영 확인
```

**시나리오 2: IQC → 입고 → OQC → 출하**
```
1. 입하 도착
2. POP IQC 검사
3. 합격 → 입고 → 재고 반영
4. 출하 요청
5. POP OQC 검사
6. 합격 → 출하 → 재고 차감
```

### 5.2 성능 테스트

**목표**:
- API 응답 시간: < 500ms
- UI 반응 시간: < 100ms (터치)
- 동시 사용자: 50명
- 바코드 스캔 속도: < 1초

### 5.3 사용자 문서

**POP 사용자 가이드** 작성:
- 작업 시작 방법
- 실적 입력 방법
- 불량 등록 방법
- 반제품 입출고 방법
- IQC/OQC 검사 방법
- 문제 해결 (FAQ)

---

## 6. API 명세서

### 6.1 작업 지시 API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/pop/work-orders/active | 활성 작업 지시 조회 | 인증 |
| POST | /api/pop/work-orders/{id}/start | 작업 시작 | 인증 |
| POST | /api/pop/work-progress/record | 실적 기록 | 인증 |
| POST | /api/pop/work-progress/defect | 불량 기록 | 인증 |
| POST | /api/pop/work-orders/{id}/pause | 작업 일시정지 | 인증 |
| POST | /api/pop/work-orders/{id}/resume | 작업 재개 | 인증 |
| POST | /api/pop/work-orders/{id}/complete | 작업 완료 | 인증 |
| GET | /api/pop/work-orders/{id}/progress | 작업 진행 현황 | 인증 |
| GET | /api/pop/statistics/today | 금일 통계 | 인증 |
| POST | /api/pop/scan | 바코드 스캔 | 인증 |

### 6.2 반제품 API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| POST | /api/pop/semi-products/receive | 반제품 입고 | 인증 |
| POST | /api/pop/semi-products/issue | 반제품 출고 | 인증 |
| GET | /api/pop/semi-products/inventory | 반제품 재고 조회 | 인증 |

### 6.3 IQC/OQC API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/pop/iqc/pending | IQC 대기 목록 | 인증 |
| POST | /api/pop/iqc/{id}/inspect | IQC 검사 수행 | 인증 |
| GET | /api/pop/oqc/pending | OQC 대기 목록 | 인증 |
| POST | /api/pop/oqc/{id}/inspect | OQC 검사 수행 | 인증 |

---

## 7. 데이터 모델

### 7.1 WorkProgressEntity

```java
@Entity
@Table(name = "si_work_progress")
public class WorkProgressEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long progressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private TenantEntity tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private WorkOrderEntity workOrder;

    @Column(name = "operator_user_id")
    private Long operatorUserId;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "pause_time")
    private LocalDateTime pauseTime;

    @Column(name = "resume_time")
    private LocalDateTime resumeTime;

    @Column(name = "produced_quantity")
    private Integer producedQuantity;

    @Column(name = "defect_quantity")
    private Integer defectQuantity;

    @Column(name = "status", length = 30)
    private String status; // IN_PROGRESS, PAUSED, COMPLETED

    @Column(name = "pause_reason")
    private String pauseReason;

    @Column(name = "last_update_time")
    private LocalDateTime lastUpdateTime;
}
```

### 7.2 SemiProductTransactionEntity

```java
@Entity
@Table(name = "si_semi_product_transactions")
public class SemiProductTransactionEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private TenantEntity tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    private LotEntity lot;

    @Column(name = "transaction_type", length = 30)
    private String transactionType; // RECEIVE, ISSUE

    @Column(name = "quantity")
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private WarehouseEntity warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private LocationEntity location;

    @Column(name = "operator_user_id")
    private Long operatorUserId;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "remarks")
    private String remarks;
}
```

---

## 8. 테스트 시나리오

### 8.1 작업 실적 기록 테스트

```java
@Test
void testWorkProgressWorkflow() {
    // 1. Start work
    WorkStartRequest startRequest = new WorkStartRequest(operatorId, equipmentId);
    WorkProgressEntity progress = popService.startWorkOrder(tenantId, workOrderId, startRequest);

    assertNotNull(progress);
    assertEquals("IN_PROGRESS", progress.getStatus());
    assertEquals(0, progress.getProducedQuantity());

    // 2. Record progress (50 units)
    WorkProgressRecordRequest recordRequest1 = new WorkProgressRecordRequest(
        progress.getProgressId(), 50, operatorId
    );
    WorkProgressEntity updated1 = popService.recordProgress(tenantId, recordRequest1);
    assertEquals(50, updated1.getProducedQuantity());

    // 3. Record more progress (30 units)
    WorkProgressRecordRequest recordRequest2 = new WorkProgressRecordRequest(
        progress.getProgressId(), 30, operatorId
    );
    WorkProgressEntity updated2 = popService.recordProgress(tenantId, recordRequest2);
    assertEquals(80, updated2.getProducedQuantity());

    // 4. Record defect (5 units)
    DefectRecordRequest defectRequest = new DefectRecordRequest(
        progress.getProgressId(), 5, "SCRATCH", "Minor scratch", operatorId
    );
    DefectEntity defect = popService.recordDefect(tenantId, defectRequest);
    assertEquals(5, defect.getDefectQuantity());

    // 5. Complete work
    WorkCompleteRequest completeRequest = new WorkCompleteRequest("Work completed successfully");
    WorkOrderEntity completed = popService.completeWorkOrder(tenantId, workOrderId, completeRequest);

    assertEquals("COMPLETED", completed.getStatus());
    assertEquals(80, completed.getProducedQuantity());
    assertEquals(5, completed.getDefectQuantity());
    assertEquals(75, completed.getGoodQuantity()); // 80 - 5
}
```

### 8.2 반제품 입출고 테스트

```java
@Test
void testSemiProductInOut() {
    // 1. Receive semi-product
    SemiProductReceiveRequest receiveRequest = new SemiProductReceiveRequest(
        productId, 100, warehouseId, locationId, operatorId
    );
    SemiProductTransactionEntity receiveTransaction =
        semiProductService.receiveSemiProduct(tenantId, receiveRequest);

    assertEquals("RECEIVE", receiveTransaction.getTransactionType());
    assertEquals(100, receiveTransaction.getQuantity());

    // 2. Check inventory increased
    InventoryEntity inventory = inventoryService.getInventoryByProduct(tenantId, productId, warehouseId);
    assertEquals(100, inventory.getQuantity());

    // 3. Issue semi-product
    SemiProductIssueRequest issueRequest = new SemiProductIssueRequest(
        productId, 60, warehouseId, operatorId, workOrderId
    );
    SemiProductTransactionEntity issueTransaction =
        semiProductService.issueSemiProduct(tenantId, issueRequest);

    assertEquals("ISSUE", issueTransaction.getTransactionType());
    assertEquals(60, issueTransaction.getQuantity());

    // 4. Check inventory decreased
    InventoryEntity updatedInventory = inventoryService.getInventoryByProduct(tenantId, productId, warehouseId);
    assertEquals(40, updatedInventory.getQuantity()); // 100 - 60
}
```

---

## 9. 완료 체크리스트

### Day 1-2
- [ ] POPController.java 구현 (10 엔드포인트)
- [ ] POPService.java 구현 (핵심 로직)
- [ ] WorkProgressEntity, DTOs 생성
- [ ] POPWorkProgressPage.tsx 구현
- [ ] popService.ts 통합
- [ ] 기존 POPWorkOrderPage API 연동
- [ ] 단위 테스트 작성 (10+ 테스트)

### Day 3-4
- [ ] SemiProductController.java 구현
- [ ] SemiProductService.java 구현
- [ ] POPSemiProductInPage.tsx 구현
- [ ] POPSemiProductOutPage.tsx 구현
- [ ] 재고 연동 검증
- [ ] LOT 추적 연동
- [ ] 단위 테스트 작성

### Day 5-6
- [ ] POPIQCPage.tsx 구현
- [ ] POPOQCPage.tsx 구현
- [ ] IQC/OQC POP 엔드포인트 추가
- [ ] 검사 체크리스트 구현
- [ ] 사진 촬영 기능
- [ ] 라벨 출력 연동
- [ ] 단위 테스트 작성

### Day 7
- [ ] 전체 워크플로우 통합 테스트
- [ ] 바코드 스캔 통합 검증
- [ ] 실시간 업데이트 검증
- [ ] 성능 테스트
- [ ] POP 사용자 가이드 작성
- [ ] Week 1 완료 보고서 작성

---

## 10. 예상 완성도

**Week 1 완료 후**:
- POP 현장 프로그램: **33% → 75%** (+42%p)
- 전체 프로젝트: **76% → 78%** (+2%p)

**다음 단계**: Week 2 - 생산관리 워크플로우 (불출지시, 생산기록서 승인, LOT 분할)

---

**문서 작성**: Claude Code (Sonnet 4.5)
**작성일**: 2026-02-04
**문의**: msmoon@softice.co.kr
