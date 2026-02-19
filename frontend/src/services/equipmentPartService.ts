import apiClient from './api';

export interface EquipmentPart {
  partId: number;
  tenantId: string;
  tenantName: string;
  equipmentId: number;
  equipmentCode?: string;
  equipmentName?: string;
  partCode: string;
  partName: string;
  partType?: string;
  manufacturer?: string;
  modelName?: string;
  serialNo?: string;
  installationDate?: string;
  expectedLifeDays?: number;
  replacementDate?: string;
  nextReplacementDate?: string;
  replacementCount?: number;
  unitPrice?: number;
  status: string;
  remarks?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface EquipmentPartCreateRequest {
  equipmentId: number;
  partCode: string;
  partName: string;
  partType?: string;
  manufacturer?: string;
  modelName?: string;
  serialNo?: string;
  installationDate?: string;
  expectedLifeDays?: number;
  unitPrice?: number;
  status?: string;
  remarks?: string;
}

export interface EquipmentPartUpdateRequest {
  partName?: string;
  partType?: string;
  manufacturer?: string;
  modelName?: string;
  serialNo?: string;
  installationDate?: string;
  expectedLifeDays?: number;
  unitPrice?: number;
  status?: string;
  remarks?: string;
}

const equipmentPartService = {
  /**
   * Get all equipment parts
   */
  getAll: async (): Promise<EquipmentPart[]> => {
    const response = await apiClient.get<EquipmentPart[]>('/equipment-parts');
    return response.data;
  },

  /**
   * Get equipment part by ID
   */
  getById: async (partId: number): Promise<EquipmentPart> => {
    const response = await apiClient.get<EquipmentPart>(`/equipment-parts/${partId}`);
    return response.data;
  },

  /**
   * Get equipment parts by equipment ID
   */
  getByEquipment: async (equipmentId: number): Promise<EquipmentPart[]> => {
    const response = await apiClient.get<EquipmentPart[]>(`/equipment-parts/equipment/${equipmentId}`);
    return response.data;
  },

  /**
   * Get equipment parts that need replacement
   */
  getNeedsReplacement: async (dueDate: string): Promise<EquipmentPart[]> => {
    const response = await apiClient.get<EquipmentPart[]>('/equipment-parts/needs-replacement', {
      params: { dueDate }
    });
    return response.data;
  },

  /**
   * Create equipment part
   */
  create: async (data: EquipmentPartCreateRequest): Promise<EquipmentPart> => {
    const response = await apiClient.post<EquipmentPart>('/equipment-parts', data);
    return response.data;
  },

  /**
   * Update equipment part
   */
  update: async (partId: number, data: EquipmentPartUpdateRequest): Promise<EquipmentPart> => {
    const response = await apiClient.put<EquipmentPart>(`/equipment-parts/${partId}`, data);
    return response.data;
  },

  /**
   * Record part replacement
   */
  recordReplacement: async (partId: number, replacementDate: string): Promise<EquipmentPart> => {
    const response = await apiClient.post<EquipmentPart>(`/equipment-parts/${partId}/replace?replacementDate=${encodeURIComponent(replacementDate)}`);
    return response.data;
  },

  /**
   * Delete equipment part
   */
  delete: async (partId: number): Promise<void> => {
    await apiClient.delete(`/equipment-parts/${partId}`);
  }
};

export default equipmentPartService;
