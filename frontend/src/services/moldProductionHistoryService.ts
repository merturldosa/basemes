import apiClient from './api';

export interface MoldProductionHistory {
  historyId: number;
  tenantId: string;
  tenantName: string;
  moldId: number;
  moldCode: string;
  moldName: string;
  workOrderId?: number;
  workOrderNo?: string;
  workResultId?: number;
  productionDate: string;
  shotCount: number;
  cumulativeShotCount?: number;
  productionQuantity?: number;
  goodQuantity?: number;
  defectQuantity?: number;
  operatorUserId?: number;
  operatorUsername?: string;
  operatorName?: string;
  remarks?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface MoldProductionHistoryCreateRequest {
  moldId: number;
  workOrderId?: number;
  workResultId?: number;
  productionDate: string;
  shotCount: number;
  productionQuantity?: number;
  goodQuantity?: number;
  defectQuantity?: number;
  operatorUserId?: number;
  operatorName?: string;
  remarks?: string;
}

export interface MoldProductionHistoryUpdateRequest {
  productionDate?: string;
  shotCount?: number;
  productionQuantity?: number;
  goodQuantity?: number;
  defectQuantity?: number;
  operatorUserId?: number;
  operatorName?: string;
  remarks?: string;
}

const moldProductionHistoryService = {
  /**
   * Get all mold production histories
   */
  getAll: async (): Promise<MoldProductionHistory[]> => {
    const response = await apiClient.get<MoldProductionHistory[]>('/mold-production-histories');
    return response.data;
  },

  /**
   * Get production history by ID
   */
  getById: async (historyId: number): Promise<MoldProductionHistory> => {
    const response = await apiClient.get<MoldProductionHistory>(`/api/mold-production-histories/${historyId}`);
    return response.data;
  },

  /**
   * Get histories by mold
   */
  getByMold: async (moldId: number): Promise<MoldProductionHistory[]> => {
    const response = await apiClient.get<MoldProductionHistory[]>(`/api/mold-production-histories/mold/${moldId}`);
    return response.data;
  },

  /**
   * Get histories by date range
   */
  getByDateRange: async (startDate: string, endDate: string): Promise<MoldProductionHistory[]> => {
    const response = await apiClient.get<MoldProductionHistory[]>('/mold-production-histories/date-range', {
      params: { startDate, endDate }
    });
    return response.data;
  },

  /**
   * Get histories by work order
   */
  getByWorkOrder: async (workOrderId: number): Promise<MoldProductionHistory[]> => {
    const response = await apiClient.get<MoldProductionHistory[]>(`/api/mold-production-histories/work-order/${workOrderId}`);
    return response.data;
  },

  /**
   * Create production history
   */
  create: async (data: MoldProductionHistoryCreateRequest): Promise<MoldProductionHistory> => {
    const response = await apiClient.post<MoldProductionHistory>('/mold-production-histories', data);
    return response.data;
  },

  /**
   * Update production history
   */
  update: async (historyId: number, data: MoldProductionHistoryUpdateRequest): Promise<MoldProductionHistory> => {
    const response = await apiClient.put<MoldProductionHistory>(`/api/mold-production-histories/${historyId}`, data);
    return response.data;
  },

  /**
   * Delete production history
   */
  delete: async (historyId: number): Promise<void> => {
    await apiClient.delete(`/api/mold-production-histories/${historyId}`);
  }
};

export default moldProductionHistoryService;
