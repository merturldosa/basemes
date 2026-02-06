import apiClient from './api';

export interface EquipmentOperation {
  operationId: number;
  tenantId: string;
  tenantName: string;
  equipmentId: number;
  equipmentCode: string;
  equipmentName: string;
  operationDate: string;
  startTime: string;
  endTime?: string;
  operationHours?: number;
  workOrderId?: number;
  workOrderNo?: string;
  workResultId?: number;
  operatorUserId?: number;
  operatorName?: string;
  productionQuantity?: number;
  goodQuantity?: number;
  defectQuantity?: number;
  operationStatus: string; // RUNNING, STOPPED, PAUSED, COMPLETED
  stopReason?: string;
  stopDurationMinutes?: number;
  cycleTime?: number;
  utilizationRate?: number;
  performanceRate?: number;
  qualityRate?: number;
  oee?: number;
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

export interface EquipmentOperationCreateRequest {
  equipmentId: number;
  operationDate: string;
  startTime: string;
  endTime?: string;
  operationHours?: number;
  workOrderId?: number;
  workResultId?: number;
  operatorUserId?: number;
  operatorName?: string;
  productionQuantity?: number;
  goodQuantity?: number;
  defectQuantity?: number;
  operationStatus?: string;
  stopReason?: string;
  stopDurationMinutes?: number;
  cycleTime?: number;
  remarks?: string;
}

export interface EquipmentOperationUpdateRequest {
  endTime?: string;
  productionQuantity?: number;
  goodQuantity?: number;
  defectQuantity?: number;
  operationStatus?: string;
  stopReason?: string;
  stopDurationMinutes?: number;
  cycleTime?: number;
  remarks?: string;
}

const equipmentOperationService = {
  /**
   * Get all operations
   */
  getAll: async (): Promise<EquipmentOperation[]> => {
    const response = await apiClient.get<EquipmentOperation[]>('/equipment-operations');
    return response.data;
  },

  /**
   * Get operation by ID
   */
  getById: async (operationId: number): Promise<EquipmentOperation> => {
    const response = await apiClient.get<EquipmentOperation>(`/api/equipment-operations/${operationId}`);
    return response.data;
  },

  /**
   * Get operations by equipment
   */
  getByEquipment: async (equipmentId: number): Promise<EquipmentOperation[]> => {
    const response = await apiClient.get<EquipmentOperation[]>(`/api/equipment-operations/equipment/${equipmentId}`);
    return response.data;
  },

  /**
   * Get operations by date range
   */
  getByDateRange: async (startDate: string, endDate: string): Promise<EquipmentOperation[]> => {
    const response = await apiClient.get<EquipmentOperation[]>('/equipment-operations/date-range', {
      params: { startDate, endDate }
    });
    return response.data;
  },

  /**
   * Get operations by status
   */
  getByStatus: async (operationStatus: string): Promise<EquipmentOperation[]> => {
    const response = await apiClient.get<EquipmentOperation[]>(`/api/equipment-operations/status/${operationStatus}`);
    return response.data;
  },

  /**
   * Create operation
   */
  create: async (data: EquipmentOperationCreateRequest): Promise<EquipmentOperation> => {
    const response = await apiClient.post<EquipmentOperation>('/equipment-operations', data);
    return response.data;
  },

  /**
   * Update operation
   */
  update: async (operationId: number, data: EquipmentOperationUpdateRequest): Promise<EquipmentOperation> => {
    const response = await apiClient.put<EquipmentOperation>(`/api/equipment-operations/${operationId}`, data);
    return response.data;
  },

  /**
   * Complete operation (calculate OEE)
   */
  complete: async (operationId: number): Promise<EquipmentOperation> => {
    const response = await apiClient.patch<EquipmentOperation>(`/api/equipment-operations/${operationId}/complete`);
    return response.data;
  },

  /**
   * Delete operation
   */
  delete: async (operationId: number): Promise<void> => {
    await apiClient.delete(`/api/equipment-operations/${operationId}`);
  }
};

export default equipmentOperationService;
