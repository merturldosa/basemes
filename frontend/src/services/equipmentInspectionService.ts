import apiClient from './api';

export interface EquipmentInspection {
  inspectionId: number;
  tenantId: string;
  tenantName: string;
  equipmentId: number;
  equipmentCode: string;
  equipmentName: string;
  inspectionNo: string;
  inspectionType: string; // DAILY, PERIODIC, PREVENTIVE, CORRECTIVE, BREAKDOWN
  inspectionDate: string;
  inspectionResult: string; // PASS, FAIL, CONDITIONAL
  inspectorUserId?: number;
  inspectorName?: string;
  responsibleUserId?: number;
  responsibleUserName?: string;
  findings?: string;
  abnormalityDetected?: boolean;
  severity?: string; // LOW, MEDIUM, HIGH, CRITICAL
  correctiveAction?: string;
  correctiveActionDate?: string;
  partsReplaced?: string;
  partsCost?: number;
  laborCost?: number;
  totalCost?: number;
  laborHours?: number;
  nextInspectionDate?: string;
  nextInspectionType?: string;
  remarks?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface EquipmentInspectionCreateRequest {
  equipmentId: number;
  inspectionNo: string;
  inspectionType: string;
  inspectionDate: string;
  inspectionResult: string;
  inspectorUserId?: number;
  inspectorName?: string;
  responsibleUserId?: number;
  responsibleUserName?: string;
  findings?: string;
  abnormalityDetected?: boolean;
  severity?: string;
  correctiveAction?: string;
  correctiveActionDate?: string;
  partsReplaced?: string;
  partsCost?: number;
  laborCost?: number;
  laborHours?: number;
  nextInspectionDate?: string;
  nextInspectionType?: string;
  remarks?: string;
}

export interface EquipmentInspectionUpdateRequest {
  inspectionResult?: string;
  findings?: string;
  abnormalityDetected?: boolean;
  severity?: string;
  correctiveAction?: string;
  correctiveActionDate?: string;
  partsReplaced?: string;
  partsCost?: number;
  laborCost?: number;
  remarks?: string;
}

const equipmentInspectionService = {
  /**
   * Get all inspections
   */
  getAll: async (): Promise<EquipmentInspection[]> => {
    const response = await apiClient.get<EquipmentInspection[]>('/api/equipment-inspections');
    return response.data;
  },

  /**
   * Get inspection by ID
   */
  getById: async (inspectionId: number): Promise<EquipmentInspection> => {
    const response = await apiClient.get<EquipmentInspection>(`/api/equipment-inspections/${inspectionId}`);
    return response.data;
  },

  /**
   * Get inspections by equipment
   */
  getByEquipment: async (equipmentId: number): Promise<EquipmentInspection[]> => {
    const response = await apiClient.get<EquipmentInspection[]>(`/api/equipment-inspections/equipment/${equipmentId}`);
    return response.data;
  },

  /**
   * Get inspections by type
   */
  getByType: async (inspectionType: string): Promise<EquipmentInspection[]> => {
    const response = await apiClient.get<EquipmentInspection[]>(`/api/equipment-inspections/type/${inspectionType}`);
    return response.data;
  },

  /**
   * Get inspections by result
   */
  getByResult: async (inspectionResult: string): Promise<EquipmentInspection[]> => {
    const response = await apiClient.get<EquipmentInspection[]>(`/api/equipment-inspections/result/${inspectionResult}`);
    return response.data;
  },

  /**
   * Create inspection
   */
  create: async (data: EquipmentInspectionCreateRequest): Promise<EquipmentInspection> => {
    const response = await apiClient.post<EquipmentInspection>('/api/equipment-inspections', data);
    return response.data;
  },

  /**
   * Update inspection
   */
  update: async (inspectionId: number, data: EquipmentInspectionUpdateRequest): Promise<EquipmentInspection> => {
    const response = await apiClient.put<EquipmentInspection>(`/api/equipment-inspections/${inspectionId}`, data);
    return response.data;
  },

  /**
   * Complete inspection
   */
  complete: async (inspectionId: number): Promise<EquipmentInspection> => {
    const response = await apiClient.patch<EquipmentInspection>(`/api/equipment-inspections/${inspectionId}/complete`);
    return response.data;
  },

  /**
   * Delete inspection
   */
  delete: async (inspectionId: number): Promise<void> => {
    await apiClient.delete(`/api/equipment-inspections/${inspectionId}`);
  }
};

export default equipmentInspectionService;
