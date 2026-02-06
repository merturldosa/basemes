import apiClient from './api';

export interface Downtime {
  downtimeId: number;
  tenantId: string;
  tenantName: string;
  equipmentId: number;
  equipmentCode: string;
  equipmentName: string;
  downtimeCode: string;
  downtimeType: string; // BREAKDOWN, SETUP_CHANGE, MATERIAL_SHORTAGE, QUALITY_ISSUE, PLANNED_MAINTENANCE, UNPLANNED_MAINTENANCE, NO_ORDER, OTHER
  downtimeCategory?: string;
  startTime: string;
  endTime?: string;
  durationMinutes?: number;
  workOrderId?: number;
  workOrderNo?: string;
  operationId?: number;
  responsibleUserId?: number;
  responsibleName?: string;
  cause?: string;
  countermeasure?: string;
  preventiveAction?: string;
  isResolved: boolean;
  resolvedAt?: string;
  remarks?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface DowntimeCreateRequest {
  equipmentId: number;
  downtimeCode: string;
  downtimeType: string;
  downtimeCategory?: string;
  startTime: string;
  endTime?: string;
  workOrderId?: number;
  operationId?: number;
  responsibleUserId?: number;
  responsibleName?: string;
  cause?: string;
  countermeasure?: string;
  preventiveAction?: string;
  remarks?: string;
}

export interface DowntimeUpdateRequest {
  endTime?: string;
  downtimeType?: string;
  downtimeCategory?: string;
  cause?: string;
  countermeasure?: string;
  preventiveAction?: string;
  remarks?: string;
}

const downtimeService = {
  /**
   * Get all downtimes
   */
  getAll: async (): Promise<Downtime[]> => {
    const response = await apiClient.get<Downtime[]>('/downtimes');
    return response.data;
  },

  /**
   * Get downtime by ID
   */
  getById: async (downtimeId: number): Promise<Downtime> => {
    const response = await apiClient.get<Downtime>(`/api/downtimes/${downtimeId}`);
    return response.data;
  },

  /**
   * Get downtimes by equipment
   */
  getByEquipment: async (equipmentId: number): Promise<Downtime[]> => {
    const response = await apiClient.get<Downtime[]>(`/api/downtimes/equipment/${equipmentId}`);
    return response.data;
  },

  /**
   * Get downtimes by type
   */
  getByType: async (downtimeType: string): Promise<Downtime[]> => {
    const response = await apiClient.get<Downtime[]>(`/api/downtimes/type/${downtimeType}`);
    return response.data;
  },

  /**
   * Get downtimes by date range
   */
  getByDateRange: async (startDate: string, endDate: string): Promise<Downtime[]> => {
    const response = await apiClient.get<Downtime[]>('/downtimes/date-range', {
      params: { startDate, endDate }
    });
    return response.data;
  },

  /**
   * Get unresolved downtimes
   */
  getUnresolved: async (): Promise<Downtime[]> => {
    const response = await apiClient.get<Downtime[]>('/downtimes/unresolved');
    return response.data;
  },

  /**
   * Get ongoing downtimes
   */
  getOngoing: async (): Promise<Downtime[]> => {
    const response = await apiClient.get<Downtime[]>('/downtimes/ongoing');
    return response.data;
  },

  /**
   * Create downtime
   */
  create: async (data: DowntimeCreateRequest): Promise<Downtime> => {
    const response = await apiClient.post<Downtime>('/downtimes', data);
    return response.data;
  },

  /**
   * Update downtime
   */
  update: async (downtimeId: number, data: DowntimeUpdateRequest): Promise<Downtime> => {
    const response = await apiClient.put<Downtime>(`/api/downtimes/${downtimeId}`, data);
    return response.data;
  },

  /**
   * End downtime
   */
  end: async (downtimeId: number): Promise<Downtime> => {
    const response = await apiClient.patch<Downtime>(`/api/downtimes/${downtimeId}/end`);
    return response.data;
  },

  /**
   * Resolve downtime
   */
  resolve: async (downtimeId: number): Promise<Downtime> => {
    const response = await apiClient.patch<Downtime>(`/api/downtimes/${downtimeId}/resolve`);
    return response.data;
  },

  /**
   * Activate downtime
   */
  activate: async (downtimeId: number): Promise<Downtime> => {
    const response = await apiClient.patch<Downtime>(`/api/downtimes/${downtimeId}/activate`);
    return response.data;
  },

  /**
   * Deactivate downtime
   */
  deactivate: async (downtimeId: number): Promise<Downtime> => {
    const response = await apiClient.patch<Downtime>(`/api/downtimes/${downtimeId}/deactivate`);
    return response.data;
  },

  /**
   * Delete downtime
   */
  delete: async (downtimeId: number): Promise<void> => {
    await apiClient.delete(`/api/downtimes/${downtimeId}`);
  }
};

export default downtimeService;
