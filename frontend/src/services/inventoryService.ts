import apiClient from './api';

export interface Inventory {
  inventoryId: number;
  tenantId: string;
  tenantName: string;
  warehouseId: number;
  warehouseCode: string;
  warehouseName: string;
  productId: number;
  productCode: string;
  productName: string;
  lotId?: number;
  lotNo?: string;
  availableQuantity: number;
  reservedQuantity: number;
  unit: string;
  location?: string;
  lastTransactionDate?: string;
  lastTransactionType?: string;
  createdAt: string;
  updatedAt: string;
}

const inventoryService = {
  getAll: async (): Promise<Inventory[]> => {
    const response = await apiClient.get<Inventory[]>('/inventory');
    return response.data;
  },

  getInventoryStatus: async (params?: {
    page?: number;
    size?: number;
    search?: string;
  }): Promise<{ content: Inventory[] }> => {
    const response = await apiClient.get<{ content: Inventory[] }>('/inventory/status', params);
    return response.data;
  },

  getByWarehouse: async (warehouseId: number): Promise<Inventory[]> => {
    const response = await apiClient.get<Inventory[]>(`/inventory/warehouse/${warehouseId}`);
    return response.data;
  },

  getByProduct: async (productId: number): Promise<Inventory[]> => {
    const response = await apiClient.get<Inventory[]>(`/inventory/product/${productId}`);
    return response.data;
  },

  getById: async (inventoryId: number): Promise<Inventory> => {
    const response = await apiClient.get<Inventory>(`/inventory/${inventoryId}`);
    return response.data;
  },

  delete: async (inventoryId: number): Promise<void> => {
    await apiClient.delete(`/inventory/${inventoryId}`);
  },

  getLowStock: async (threshold?: number): Promise<Inventory[]> => {
    const response = await apiClient.get<Inventory[]>('/inventory/low-stock', {
      params: { threshold: threshold || 100 },
    });
    return response.data;
  },

  reserve: async (request: {
    productId: number;
    warehouseId: number;
    lotId?: number;
    quantity: number;
    workOrderId?: number;
    remarks?: string;
  }): Promise<Inventory> => {
    const response = await apiClient.post<Inventory>('/inventory/reserve', request);
    return response.data;
  },

  release: async (request: {
    productId: number;
    warehouseId: number;
    lotId?: number;
    quantity: number;
    remarks?: string;
  }): Promise<Inventory> => {
    const response = await apiClient.post<Inventory>('/inventory/release', request);
    return response.data;
  },
};

export default inventoryService;
