import apiClient from './api';

export interface Breakdown {
  breakdownId: number;
  tenantId: string;
  tenantName: string;
  breakdownNo: string;
  equipmentId: number;
  equipmentCode?: string;
  equipmentName?: string;
  downtimeId?: number;
  reportedAt: string;
  reportedByUserId?: number;
  reportedByUserName?: string;
  failureType?: string;
  severity?: string;
  description: string;
  assignedUserId?: number;
  assignedUserName?: string;
  assignedAt?: string;
  repairStartedAt?: string;
  repairCompletedAt?: string;
  repairDurationMinutes?: number;
  repairDescription?: string;
  partsUsed?: string;
  repairCost?: number;
  rootCause?: string;
  preventiveAction?: string;
  status: string;
  closedAt?: string;
  closedByUserId?: number;
  closedByUserName?: string;
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

export interface BreakdownCreateRequest {
  breakdownNo: string;
  equipmentId: number;
  reportedAt: string;
  description: string;
  downtimeId?: number;
  reportedByUserId?: number;
  failureType?: string;
  severity?: string;
  remarks?: string;
}

export interface BreakdownUpdateRequest {
  failureType?: string;
  severity?: string;
  description?: string;
  assignedUserId?: number;
  repairDescription?: string;
  partsUsed?: string;
  repairCost?: number;
  rootCause?: string;
  preventiveAction?: string;
  remarks?: string;
}

export interface BreakdownStatistics {
  totalBreakdowns: number;
  byStatus: Record<string, number>;
  byFailureType: Record<string, number>;
  bySeverity: Record<string, number>;
  mtbfHours?: number;
  mttrMinutes?: number;
  failureRate?: number;
  topEquipments: {
    equipmentId: number;
    equipmentCode: string;
    equipmentName: string;
    breakdownCount: number;
  }[];
}

export interface BreakdownTrend {
  month: string;
  breakdownCount: number;
  avgRepairMinutes?: number;
}

const breakdownService = {
  /**
   * Get all breakdowns
   */
  getAll: async (): Promise<Breakdown[]> => {
    const response = await apiClient.get<Breakdown[]>('/breakdowns');
    return response.data;
  },

  /**
   * Get breakdown by ID
   */
  getById: async (breakdownId: number): Promise<Breakdown> => {
    const response = await apiClient.get<Breakdown>(`/breakdowns/${breakdownId}`);
    return response.data;
  },

  /**
   * Get breakdowns by status
   */
  getByStatus: async (status: string): Promise<Breakdown[]> => {
    const response = await apiClient.get<Breakdown[]>(`/breakdowns/status/${status}`);
    return response.data;
  },

  /**
   * Get breakdowns by equipment
   */
  getByEquipment: async (equipmentId: number): Promise<Breakdown[]> => {
    const response = await apiClient.get<Breakdown[]>(`/breakdowns/equipment/${equipmentId}`);
    return response.data;
  },

  /**
   * Create breakdown
   */
  create: async (data: BreakdownCreateRequest): Promise<Breakdown> => {
    const response = await apiClient.post<Breakdown>('/breakdowns', data);
    return response.data;
  },

  /**
   * Update breakdown
   */
  update: async (breakdownId: number, data: BreakdownUpdateRequest): Promise<Breakdown> => {
    const response = await apiClient.put<Breakdown>(`/breakdowns/${breakdownId}`, data);
    return response.data;
  },

  /**
   * Change breakdown status
   */
  changeStatus: async (breakdownId: number, status: string): Promise<Breakdown> => {
    const response = await apiClient.patch<Breakdown>(`/breakdowns/${breakdownId}/status?status=${encodeURIComponent(status)}`);
    return response.data;
  },

  /**
   * Delete breakdown
   */
  delete: async (breakdownId: number): Promise<void> => {
    await apiClient.delete(`/breakdowns/${breakdownId}`);
  },

  /**
   * Get breakdown statistics
   */
  getStatistics: async (startDate: string, endDate: string): Promise<BreakdownStatistics> => {
    const response = await apiClient.get<BreakdownStatistics>('/breakdown-statistics', {
      params: { startDate, endDate }
    });
    return response.data;
  },

  /**
   * Get monthly breakdown trend
   */
  getMonthlyTrend: async (months: number): Promise<BreakdownTrend[]> => {
    const response = await apiClient.get<BreakdownTrend[]>('/breakdown-statistics/trend', {
      params: { months }
    });
    return response.data;
  }
};

export default breakdownService;
