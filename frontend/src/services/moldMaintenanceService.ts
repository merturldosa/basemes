import apiClient from './api';

export interface MoldMaintenance {
  maintenanceId: number;
  tenantId: string;
  tenantName: string;
  moldId: number;
  moldCode: string;
  moldName: string;
  maintenanceNo: string;
  maintenanceType: string; // DAILY_CHECK, PERIODIC, SHOT_BASED, EMERGENCY_REPAIR, OVERHAUL
  maintenanceDate: string;
  shotCountBefore?: number;
  shotCountAfter?: number;
  shotCountReset: boolean;
  maintenanceContent?: string;
  partsReplaced?: string;
  findings?: string;
  correctiveAction?: string;
  partsCost?: number;
  laborCost?: number;
  totalCost?: number;
  laborHours?: number;
  maintenanceResult?: string; // COMPLETED, PARTIAL, FAILED
  technicianUserId?: number;
  technicianUsername?: string;
  technicianName?: string;
  nextMaintenanceDate?: string;
  remarks?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface MoldMaintenanceCreateRequest {
  moldId: number;
  maintenanceNo: string;
  maintenanceType: string;
  maintenanceDate: string;
  shotCountBefore?: number;
  shotCountReset?: boolean;
  shotCountAfter?: number;
  maintenanceContent?: string;
  partsReplaced?: string;
  findings?: string;
  correctiveAction?: string;
  partsCost?: number;
  laborCost?: number;
  laborHours?: number;
  maintenanceResult?: string;
  technicianUserId?: number;
  technicianName?: string;
  nextMaintenanceDate?: string;
  remarks?: string;
}

export interface MoldMaintenanceUpdateRequest {
  maintenanceType?: string;
  maintenanceDate?: string;
  shotCountBefore?: number;
  shotCountReset?: boolean;
  shotCountAfter?: number;
  maintenanceContent?: string;
  partsReplaced?: string;
  findings?: string;
  correctiveAction?: string;
  partsCost?: number;
  laborCost?: number;
  laborHours?: number;
  maintenanceResult?: string;
  technicianUserId?: number;
  technicianName?: string;
  nextMaintenanceDate?: string;
  remarks?: string;
}

const moldMaintenanceService = {
  /**
   * Get all mold maintenances
   */
  getAll: async (): Promise<MoldMaintenance[]> => {
    const response = await apiClient.get<MoldMaintenance[]>('/api/mold-maintenances');
    return response.data;
  },

  /**
   * Get mold maintenance by ID
   */
  getById: async (maintenanceId: number): Promise<MoldMaintenance> => {
    const response = await apiClient.get<MoldMaintenance>(`/api/mold-maintenances/${maintenanceId}`);
    return response.data;
  },

  /**
   * Get maintenances by mold
   */
  getByMold: async (moldId: number): Promise<MoldMaintenance[]> => {
    const response = await apiClient.get<MoldMaintenance[]>(`/api/mold-maintenances/mold/${moldId}`);
    return response.data;
  },

  /**
   * Get maintenances by type
   */
  getByType: async (maintenanceType: string): Promise<MoldMaintenance[]> => {
    const response = await apiClient.get<MoldMaintenance[]>(`/api/mold-maintenances/type/${maintenanceType}`);
    return response.data;
  },

  /**
   * Get maintenances by date range
   */
  getByDateRange: async (startDate: string, endDate: string): Promise<MoldMaintenance[]> => {
    const response = await apiClient.get<MoldMaintenance[]>('/api/mold-maintenances/date-range', {
      params: { startDate, endDate }
    });
    return response.data;
  },

  /**
   * Create mold maintenance
   */
  create: async (data: MoldMaintenanceCreateRequest): Promise<MoldMaintenance> => {
    const response = await apiClient.post<MoldMaintenance>('/api/mold-maintenances', data);
    return response.data;
  },

  /**
   * Update mold maintenance
   */
  update: async (maintenanceId: number, data: MoldMaintenanceUpdateRequest): Promise<MoldMaintenance> => {
    const response = await apiClient.put<MoldMaintenance>(`/api/mold-maintenances/${maintenanceId}`, data);
    return response.data;
  },

  /**
   * Delete mold maintenance
   */
  delete: async (maintenanceId: number): Promise<void> => {
    await apiClient.delete(`/api/mold-maintenances/${maintenanceId}`);
  }
};

export default moldMaintenanceService;
