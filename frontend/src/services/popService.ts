import apiClient from './api';

/**
 * POP Service
 * Point of Production - Field operations API
 * @author Moon Myung-seop
 */

// ==================== Types ====================

export interface WorkProgress {
  progressId: number;
  tenantId: string;
  workOrderId: number;
  workOrderNo: string;
  productName: string;
  productCode: string;
  processName: string;
  operatorUserId: number;
  operatorUserName: string;
  recordDate: string;
  startTime: string;
  endTime?: string;
  producedQuantity: number;
  goodQuantity: number;
  defectQuantity: number;
  plannedQuantity: number;
  completionRate: number;
  defectRate: number;
  status: 'IN_PROGRESS' | 'PAUSED' | 'COMPLETED';
  pauseCount: number;
  totalPauseDuration: number;
  workNotes?: string;
  isActive: boolean;
  equipmentId?: number;
  equipmentName?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ProductionStatistics {
  date: string;
  tenantId: string;
  operatorUserId?: number;
  operatorUserName?: string;
  totalProduced: number;
  totalGood: number;
  totalDefects: number;
  completedWorkOrders: number;
  inProgressWorkOrders: number;
  defectRate: number;
  yieldRate: number;
  totalWorkMinutes: number;
  totalPauseMinutes: number;
  efficiency: number;
  equipmentCount?: number;
  utilizationRate?: number;
  hourlyBreakdown?: HourlyProduction[];
}

export interface HourlyProduction {
  hour: number;
  produced: number;
  defects: number;
}

export interface WorkProgressRecordRequest {
  progressId: number;
  quantity: number;
  notes?: string;
}

export interface DefectRecordRequest {
  progressId: number;
  defectQuantity: number;
  defectType: string;
  defectReason?: string;
  defectLocation?: string;
  severity?: 'CRITICAL' | 'MAJOR' | 'MINOR';
  notes?: string;
}

export interface PauseWorkRequest {
  pauseReason?: string;
  pauseType?: 'BREAK' | 'EQUIPMENT_CHECK' | 'MATERIAL_WAIT' | 'OTHER';
  requiresApproval?: boolean;
}

export interface ScanBarcodeRequest {
  barcode: string;
  type: 'WORK_ORDER' | 'MATERIAL' | 'PRODUCT' | 'LOT';
}

// ==================== Service ====================

const popService = {
  /**
   * Get active work orders for operator
   * GET /api/pop/work-orders/active
   */
  getActiveWorkOrders: async (operatorId?: number): Promise<any[]> => {
    const params = operatorId ? { operatorId } : {};
    const response = await apiClient.get<any[]>('/pop/work-orders/active', params);
    return response.data;
  },

  /**
   * Start work order
   * POST /api/pop/work-orders/{id}/start
   */
  startWorkOrder: async (workOrderId: number, operatorId: number): Promise<WorkProgress> => {
    const response = await apiClient.post<WorkProgress>(
      `/pop/work-orders/${workOrderId}/start`,
      null,
      { operatorId }
    );
    return response.data;
  },

  /**
   * Record work progress (production quantity)
   * POST /api/pop/work-progress/record
   */
  recordProgress: async (request: WorkProgressRecordRequest): Promise<WorkProgress> => {
    const response = await apiClient.post<WorkProgress>('/pop/work-progress/record', request);
    return response.data;
  },

  /**
   * Record defect
   * POST /api/pop/work-progress/defect
   */
  recordDefect: async (request: DefectRecordRequest): Promise<any> => {
    const response = await apiClient.post<any>('/pop/work-progress/defect', request);
    return response.data;
  },

  /**
   * Pause work
   * POST /api/pop/work-orders/{id}/pause
   */
  pauseWork: async (workOrderId: number, request?: PauseWorkRequest): Promise<WorkProgress> => {
    const response = await apiClient.post<WorkProgress>(
      `/pop/work-orders/${workOrderId}/pause`,
      request || {}
    );
    return response.data;
  },

  /**
   * Resume work
   * POST /api/pop/work-orders/{id}/resume
   */
  resumeWork: async (workOrderId: number): Promise<WorkProgress> => {
    const response = await apiClient.post<WorkProgress>(`/pop/work-orders/${workOrderId}/resume`);
    return response.data;
  },

  /**
   * Complete work order
   * POST /api/pop/work-orders/{id}/complete
   */
  completeWorkOrder: async (workOrderId: number, remarks?: string): Promise<any> => {
    const params = remarks ? { remarks } : {};
    const response = await apiClient.post<any>(`/pop/work-orders/${workOrderId}/complete`, null, params);
    return response.data;
  },

  /**
   * Get work progress by work order
   * GET /api/pop/work-orders/{id}/progress
   */
  getWorkProgress: async (workOrderId: number): Promise<WorkProgress> => {
    const response = await apiClient.get<WorkProgress>(`/pop/work-orders/${workOrderId}/progress`);
    return response.data;
  },

  /**
   * Get today's production statistics
   * GET /api/pop/statistics/today
   */
  getTodayStatistics: async (operatorId?: number): Promise<ProductionStatistics> => {
    const params = operatorId ? { operatorId } : {};
    const response = await apiClient.get<ProductionStatistics>('/pop/statistics/today', params);
    return response.data;
  },

  /**
   * Scan barcode
   * POST /api/pop/scan
   */
  scanBarcode: async (request: ScanBarcodeRequest): Promise<any> => {
    const response = await apiClient.post<any>('/pop/scan', null, {
      barcode: request.barcode,
      type: request.type
    });
    return response.data;
  },
};

export default popService;
