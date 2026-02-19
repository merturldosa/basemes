import apiClient from './api';

export interface Consumable {
  consumableId: number;
  tenantId: string;
  tenantName: string;
  consumableCode: string;
  consumableName: string;
  category?: string;
  equipmentId?: number;
  equipmentCode?: string;
  equipmentName?: string;
  unit?: string;
  currentStock?: number;
  minimumStock?: number;
  maximumStock?: number;
  unitPrice?: number;
  supplier?: string;
  leadTimeDays?: number;
  lastReplenishedDate?: string;
  status: string;
  remarks?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ConsumableCreateRequest {
  consumableCode: string;
  consumableName: string;
  category?: string;
  equipmentId?: number;
  unit?: string;
  currentStock?: number;
  minimumStock?: number;
  maximumStock?: number;
  unitPrice?: number;
  supplier?: string;
  leadTimeDays?: number;
  status?: string;
  remarks?: string;
}

export interface ConsumableUpdateRequest {
  consumableName?: string;
  category?: string;
  equipmentId?: number;
  unit?: string;
  currentStock?: number;
  minimumStock?: number;
  maximumStock?: number;
  unitPrice?: number;
  supplier?: string;
  leadTimeDays?: number;
  status?: string;
  remarks?: string;
}

const consumableService = {
  /**
   * Get all consumables
   */
  getAll: async (): Promise<Consumable[]> => {
    const response = await apiClient.get<Consumable[]>('/consumables');
    return response.data;
  },

  /**
   * Get consumable by ID
   */
  getById: async (consumableId: number): Promise<Consumable> => {
    const response = await apiClient.get<Consumable>(`/consumables/${consumableId}`);
    return response.data;
  },

  /**
   * Get low stock consumables
   */
  getLowStock: async (): Promise<Consumable[]> => {
    const response = await apiClient.get<Consumable[]>('/consumables/low-stock');
    return response.data;
  },

  /**
   * Create consumable
   */
  create: async (data: ConsumableCreateRequest): Promise<Consumable> => {
    const response = await apiClient.post<Consumable>('/consumables', data);
    return response.data;
  },

  /**
   * Update consumable
   */
  update: async (consumableId: number, data: ConsumableUpdateRequest): Promise<Consumable> => {
    const response = await apiClient.put<Consumable>(`/consumables/${consumableId}`, data);
    return response.data;
  },

  /**
   * Adjust consumable stock
   */
  adjustStock: async (consumableId: number, quantity: number): Promise<Consumable> => {
    const response = await apiClient.post<Consumable>(`/consumables/${consumableId}/adjust-stock?quantity=${encodeURIComponent(quantity)}`);
    return response.data;
  },

  /**
   * Delete consumable
   */
  delete: async (consumableId: number): Promise<void> => {
    await apiClient.delete(`/consumables/${consumableId}`);
  }
};

export default consumableService;
