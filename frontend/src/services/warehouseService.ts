import apiClient from './api';

export interface Warehouse {
  warehouseId: number;
  tenantId: string;
  tenantName: string;
  warehouseCode: string;
  warehouseName: string;
  warehouseType: string;  // RAW_MATERIAL, WORK_IN_PROCESS, FINISHED_GOODS, QUARANTINE, SCRAP
  location?: string;
  managerUserId: number;
  managerUserName: string;
  contactNumber?: string;
  capacity?: number;
  unit?: string;
  isActive: boolean;
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

export interface WarehouseCreateRequest {
  warehouseCode: string;
  warehouseName: string;
  warehouseType: string;
  location?: string;
  managerUserId: number;
  contactNumber?: string;
  capacity?: number;
  unit?: string;
  isActive?: boolean;
  remarks?: string;
}

export interface WarehouseUpdateRequest {
  warehouseId: number;
  warehouseName: string;
  warehouseType: string;
  location?: string;
  managerUserId: number;
  contactNumber?: string;
  capacity?: number;
  unit?: string;
  isActive: boolean;
  remarks?: string;
}

const warehouseService = {
  getAll: async (): Promise<Warehouse[]> => {
    const response = await apiClient.get<Warehouse[]>('/warehouses');
    return response.data;
  },

  getActive: async (): Promise<Warehouse[]> => {
    const response = await apiClient.get<Warehouse[]>('/warehouses/active');
    return response.data;
  },

  getById: async (warehouseId: number): Promise<Warehouse> => {
    const response = await apiClient.get<Warehouse>(`/warehouses/${warehouseId}`);
    return response.data;
  },

  create: async (request: WarehouseCreateRequest): Promise<Warehouse> => {
    const response = await apiClient.post<Warehouse>('/warehouses', request);
    return response.data;
  },

  update: async (warehouseId: number, request: WarehouseUpdateRequest): Promise<Warehouse> => {
    const response = await apiClient.put<Warehouse>(`/warehouses/${warehouseId}`, request);
    return response.data;
  },

  delete: async (warehouseId: number): Promise<void> => {
    await apiClient.delete(`/warehouses/${warehouseId}`);
  },

  toggleActive: async (warehouseId: number): Promise<Warehouse> => {
    const response = await apiClient.post<Warehouse>(`/warehouses/${warehouseId}/toggle-active`);
    return response.data;
  },
};

export default warehouseService;
