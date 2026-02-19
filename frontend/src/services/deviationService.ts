import apiClient from './api';

export interface Deviation {
  deviationId: number;
  tenantId: string;
  tenantName: string;
  deviationNo: string;
  equipmentId: number;
  equipmentCode?: string;
  equipmentName?: string;
  parameterName: string;
  standardValue?: number;
  actualValue?: number;
  deviationValue?: number;
  detectedAt: string;
  detectedByUserId?: number;
  detectedByUserName?: string;
  severity?: string;
  description?: string;
  rootCause?: string;
  correctiveAction?: string;
  preventiveAction?: string;
  status: string;
  resolvedAt?: string;
  resolvedByUserId?: number;
  resolvedByUserName?: string;
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

export interface DeviationCreateRequest {
  deviationNo: string;
  equipmentId: number;
  parameterName: string;
  detectedAt: string;
  standardValue?: number;
  actualValue?: number;
  deviationValue?: number;
  detectedByUserId?: number;
  severity?: string;
  description?: string;
  remarks?: string;
}

export interface DeviationUpdateRequest {
  parameterName?: string;
  standardValue?: number;
  actualValue?: number;
  deviationValue?: number;
  severity?: string;
  description?: string;
  rootCause?: string;
  correctiveAction?: string;
  preventiveAction?: string;
  status?: string;
  remarks?: string;
}

const deviationService = {
  /**
   * Get all deviations
   */
  getAll: async (): Promise<Deviation[]> => {
    const response = await apiClient.get<Deviation[]>('/deviations');
    return response.data;
  },

  /**
   * Get deviation by ID
   */
  getById: async (deviationId: number): Promise<Deviation> => {
    const response = await apiClient.get<Deviation>(`/deviations/${deviationId}`);
    return response.data;
  },

  /**
   * Get deviations by status
   */
  getByStatus: async (status: string): Promise<Deviation[]> => {
    const response = await apiClient.get<Deviation[]>(`/deviations/status/${status}`);
    return response.data;
  },

  /**
   * Get deviations by equipment
   */
  getByEquipment: async (equipmentId: number): Promise<Deviation[]> => {
    const response = await apiClient.get<Deviation[]>(`/deviations/equipment/${equipmentId}`);
    return response.data;
  },

  /**
   * Create deviation
   */
  create: async (data: DeviationCreateRequest): Promise<Deviation> => {
    const response = await apiClient.post<Deviation>('/deviations', data);
    return response.data;
  },

  /**
   * Update deviation
   */
  update: async (deviationId: number, data: DeviationUpdateRequest): Promise<Deviation> => {
    const response = await apiClient.put<Deviation>(`/deviations/${deviationId}`, data);
    return response.data;
  },

  /**
   * Change deviation status
   */
  changeStatus: async (deviationId: number, status: string): Promise<Deviation> => {
    const response = await apiClient.patch<Deviation>(`/deviations/${deviationId}/status?status=${encodeURIComponent(status)}`);
    return response.data;
  },

  /**
   * Delete deviation
   */
  delete: async (deviationId: number): Promise<void> => {
    await apiClient.delete(`/deviations/${deviationId}`);
  }
};

export default deviationService;
