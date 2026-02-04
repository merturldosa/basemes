import apiClient from './api';

export interface Material {
  materialId: number;
  tenantId: string;
  tenantName: string;
  materialCode: string;
  materialName: string;
  materialType: string;  // RAW_MATERIAL, SUB_MATERIAL, SEMI_FINISHED, FINISHED_PRODUCT
  specification?: string;
  model?: string;
  unit: string;
  standardPrice?: number;
  currentPrice?: number;
  currency?: string;
  supplierId?: number;
  supplierCode?: string;
  supplierName?: string;
  leadTimeDays?: number;
  minStockQuantity?: number;
  maxStockQuantity?: number;
  safetyStockQuantity?: number;
  reorderPoint?: number;
  storageLocation?: string;
  lotManaged: boolean;
  shelfLifeDays?: number;
  isActive: boolean;
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

export interface MaterialCreateRequest {
  materialCode: string;
  materialName: string;
  materialType: string;
  specification?: string;
  model?: string;
  unit: string;
  standardPrice?: number;
  currentPrice?: number;
  currency?: string;
  supplierId?: number;
  leadTimeDays?: number;
  minStockQuantity?: number;
  maxStockQuantity?: number;
  safetyStockQuantity?: number;
  reorderPoint?: number;
  storageLocation?: string;
  lotManaged?: boolean;
  shelfLifeDays?: number;
  isActive?: boolean;
  remarks?: string;
}

export interface MaterialUpdateRequest {
  materialId: number;
  materialName: string;
  materialType: string;
  specification?: string;
  model?: string;
  unit: string;
  standardPrice?: number;
  currentPrice?: number;
  currency?: string;
  supplierId?: number;
  leadTimeDays?: number;
  minStockQuantity?: number;
  maxStockQuantity?: number;
  safetyStockQuantity?: number;
  reorderPoint?: number;
  storageLocation?: string;
  lotManaged: boolean;
  shelfLifeDays?: number;
  isActive: boolean;
  remarks?: string;
}

const materialService = {
  getAll: async (): Promise<Material[]> => {
    const response = await apiClient.get<Material[]>('/materials');
    return response.data;
  },

  getActive: async (): Promise<Material[]> => {
    const response = await apiClient.get<Material[]>('/materials/active');
    return response.data;
  },

  getByType: async (materialType: string): Promise<Material[]> => {
    const response = await apiClient.get<Material[]>(`/materials/type/${materialType}`);
    return response.data;
  },

  getBySupplier: async (supplierId: number): Promise<Material[]> => {
    const response = await apiClient.get<Material[]>(`/materials/supplier/${supplierId}`);
    return response.data;
  },

  getById: async (materialId: number): Promise<Material> => {
    const response = await apiClient.get<Material>(`/materials/${materialId}`);
    return response.data;
  },

  create: async (request: MaterialCreateRequest): Promise<Material> => {
    const response = await apiClient.post<Material>('/materials', request);
    return response.data;
  },

  update: async (materialId: number, request: MaterialUpdateRequest): Promise<Material> => {
    const response = await apiClient.put<Material>(`/materials/${materialId}`, request);
    return response.data;
  },

  delete: async (materialId: number): Promise<void> => {
    await apiClient.delete(`/materials/${materialId}`);
  },

  toggleActive: async (materialId: number): Promise<Material> => {
    const response = await apiClient.post<Material>(`/materials/${materialId}/toggle-active`);
    return response.data;
  },
};

export default materialService;
