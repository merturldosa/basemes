import apiClient from './api';

export interface Equipment {
  equipmentId: number;
  tenantId: string;
  tenantName: string;
  equipmentCode: string;
  equipmentName: string;
  equipmentType: string; // MACHINE, MOLD, TOOL, FACILITY, VEHICLE, OTHER
  equipmentCategory?: string;
  siteId?: number;
  siteCode?: string;
  siteName?: string;
  departmentId?: number;
  departmentCode?: string;
  departmentName?: string;
  manufacturer?: string;
  modelName?: string;
  serialNo?: string;
  location?: string;
  purchaseDate?: string;
  purchasePrice?: number;
  capacity?: string;
  powerRating?: number;
  weight?: number;
  status: string; // OPERATIONAL, STOPPED, MAINTENANCE, BREAKDOWN, RETIRED
  maintenanceCycleDays?: number;
  lastMaintenanceDate?: string;
  nextMaintenanceDate?: string;
  standardCycleTime?: number;
  actualOeeTarget?: number;
  warrantyPeriod?: string;
  warrantyExpiryDate?: string;
  remarks?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface EquipmentCreateRequest {
  equipmentCode: string;
  equipmentName: string;
  equipmentType: string;
  equipmentCategory?: string;
  siteId?: number;
  departmentId?: number;
  manufacturer?: string;
  modelName?: string;
  serialNo?: string;
  location?: string;
  purchaseDate?: string;
  purchasePrice?: number;
  capacity?: string;
  powerRating?: number;
  weight?: number;
  status: string;
  maintenanceCycleDays?: number;
  lastMaintenanceDate?: string;
  nextMaintenanceDate?: string;
  standardCycleTime?: number;
  actualOeeTarget?: number;
  warrantyPeriod?: string;
  warrantyExpiryDate?: string;
  remarks?: string;
}

export interface EquipmentUpdateRequest {
  equipmentName?: string;
  equipmentType?: string;
  equipmentCategory?: string;
  siteId?: number;
  departmentId?: number;
  manufacturer?: string;
  modelName?: string;
  serialNo?: string;
  location?: string;
  capacity?: string;
  powerRating?: number;
  weight?: number;
  status?: string;
  maintenanceCycleDays?: number;
  lastMaintenanceDate?: string;
  nextMaintenanceDate?: string;
  standardCycleTime?: number;
  actualOeeTarget?: number;
  warrantyPeriod?: string;
  warrantyExpiryDate?: string;
  remarks?: string;
}

const equipmentService = {
  /**
   * Get all equipments
   */
  getAll: async (): Promise<Equipment[]> => {
    const response = await apiClient.get<Equipment[]>('/api/equipments');
    return response.data;
  },

  /**
   * Get active equipments
   */
  getActive: async (): Promise<Equipment[]> => {
    const response = await apiClient.get<Equipment[]>('/api/equipments/active');
    return response.data;
  },

  /**
   * Get equipment by ID
   */
  getById: async (equipmentId: number): Promise<Equipment> => {
    const response = await apiClient.get<Equipment>(`/api/equipments/${equipmentId}`);
    return response.data;
  },

  /**
   * Get equipments by status
   */
  getByStatus: async (status: string): Promise<Equipment[]> => {
    const response = await apiClient.get<Equipment[]>(`/api/equipments/status/${status}`);
    return response.data;
  },

  /**
   * Get equipments by type
   */
  getByType: async (equipmentType: string): Promise<Equipment[]> => {
    const response = await apiClient.get<Equipment[]>(`/api/equipments/type/${equipmentType}`);
    return response.data;
  },

  /**
   * Create equipment
   */
  create: async (data: EquipmentCreateRequest): Promise<Equipment> => {
    const response = await apiClient.post<Equipment>('/api/equipments', data);
    return response.data;
  },

  /**
   * Update equipment
   */
  update: async (equipmentId: number, data: EquipmentUpdateRequest): Promise<Equipment> => {
    const response = await apiClient.put<Equipment>(`/api/equipments/${equipmentId}`, data);
    return response.data;
  },

  /**
   * Change equipment status
   */
  changeStatus: async (equipmentId: number, status: string): Promise<Equipment> => {
    const response = await apiClient.patch<Equipment>(`/api/equipments/${equipmentId}/status`, null, {
      params: { status }
    });
    return response.data;
  },

  /**
   * Activate equipment
   */
  activate: async (equipmentId: number): Promise<Equipment> => {
    const response = await apiClient.patch<Equipment>(`/api/equipments/${equipmentId}/activate`);
    return response.data;
  },

  /**
   * Deactivate equipment
   */
  deactivate: async (equipmentId: number): Promise<Equipment> => {
    const response = await apiClient.patch<Equipment>(`/api/equipments/${equipmentId}/deactivate`);
    return response.data;
  },

  /**
   * Delete equipment
   */
  delete: async (equipmentId: number): Promise<void> => {
    await apiClient.delete(`/api/equipments/${equipmentId}`);
  }
};

export default equipmentService;
