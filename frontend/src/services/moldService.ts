import apiClient from './api';

export interface Mold {
  moldId: number;
  tenantId: string;
  tenantName: string;
  moldCode: string;
  moldName: string;
  moldType: string; // INJECTION, PRESS, DIE_CASTING, FORGING, OTHER
  moldGrade?: string; // A, B, C, S
  cavityCount?: number;
  currentShotCount: number;
  maxShotCount?: number;
  maintenanceShotInterval?: number;
  lastMaintenanceShot?: number;
  siteId?: number;
  siteCode?: string;
  siteName?: string;
  departmentId?: number;
  departmentCode?: string;
  departmentName?: string;
  manufacturer?: string;
  modelName?: string;
  serialNo?: string;
  material?: string;
  weight?: number;
  dimensions?: string;
  manufactureDate?: string;
  purchaseDate?: string;
  purchasePrice?: number;
  firstUseDate?: string;
  warrantyPeriod?: string;
  warrantyExpiryDate?: string;
  status: string; // AVAILABLE, IN_USE, MAINTENANCE, BREAKDOWN, RETIRED
  location?: string;
  remarks?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface MoldCreateRequest {
  moldCode: string;
  moldName: string;
  moldType: string;
  moldGrade?: string;
  cavityCount?: number;
  maxShotCount?: number;
  maintenanceShotInterval?: number;
  siteId?: number;
  departmentId?: number;
  manufacturer?: string;
  modelName?: string;
  serialNo?: string;
  material?: string;
  weight?: number;
  dimensions?: string;
  manufactureDate?: string;
  purchaseDate?: string;
  purchasePrice?: number;
  firstUseDate?: string;
  warrantyPeriod?: string;
  warrantyExpiryDate?: string;
  status: string;
  location?: string;
  remarks?: string;
}

export interface MoldUpdateRequest {
  moldName?: string;
  moldType?: string;
  moldGrade?: string;
  cavityCount?: number;
  maxShotCount?: number;
  maintenanceShotInterval?: number;
  manufacturer?: string;
  modelName?: string;
  serialNo?: string;
  material?: string;
  weight?: number;
  status?: string;
  location?: string;
  remarks?: string;
}

const moldService = {
  /**
   * Get all molds
   */
  getAll: async (): Promise<Mold[]> => {
    const response = await apiClient.get<Mold[]>('/api/molds');
    return response.data;
  },

  /**
   * Get active molds
   */
  getActive: async (): Promise<Mold[]> => {
    const response = await apiClient.get<Mold[]>('/api/molds/active');
    return response.data;
  },

  /**
   * Get mold by ID
   */
  getById: async (moldId: number): Promise<Mold> => {
    const response = await apiClient.get<Mold>(`/api/molds/${moldId}`);
    return response.data;
  },

  /**
   * Get molds by status
   */
  getByStatus: async (status: string): Promise<Mold[]> => {
    const response = await apiClient.get<Mold[]>(`/api/molds/status/${status}`);
    return response.data;
  },

  /**
   * Get molds requiring maintenance
   */
  getRequiringMaintenance: async (): Promise<Mold[]> => {
    const response = await apiClient.get<Mold[]>('/api/molds/requiring-maintenance');
    return response.data;
  },

  /**
   * Create mold
   */
  create: async (data: MoldCreateRequest): Promise<Mold> => {
    const response = await apiClient.post<Mold>('/api/molds', data);
    return response.data;
  },

  /**
   * Update mold
   */
  update: async (moldId: number, data: MoldUpdateRequest): Promise<Mold> => {
    const response = await apiClient.put<Mold>(`/api/molds/${moldId}`, data);
    return response.data;
  },

  /**
   * Change mold status
   */
  changeStatus: async (moldId: number, status: string): Promise<Mold> => {
    const response = await apiClient.patch<Mold>(`/api/molds/${moldId}/status`, null, {
      params: { status }
    });
    return response.data;
  },

  /**
   * Reset shot count
   */
  resetShotCount: async (moldId: number): Promise<Mold> => {
    const response = await apiClient.patch<Mold>(`/api/molds/${moldId}/reset-shot-count`);
    return response.data;
  },

  /**
   * Activate mold
   */
  activate: async (moldId: number): Promise<Mold> => {
    const response = await apiClient.patch<Mold>(`/api/molds/${moldId}/activate`);
    return response.data;
  },

  /**
   * Deactivate mold
   */
  deactivate: async (moldId: number): Promise<Mold> => {
    const response = await apiClient.patch<Mold>(`/api/molds/${moldId}/deactivate`);
    return response.data;
  },

  /**
   * Delete mold
   */
  delete: async (moldId: number): Promise<void> => {
    await apiClient.delete(`/api/molds/${moldId}`);
  }
};

export default moldService;
