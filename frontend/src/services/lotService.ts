import apiClient from './api';

export interface Lot {
  lotId: number;
  tenantId: string;
  tenantName: string;
  lotNo: string;
  productId: number;
  productCode: string;
  productName: string;
  workOrderId?: number;
  workOrderNo?: string;
  initialQuantity: number;
  currentQuantity: number;
  unit: string;
  manufactureDate?: string;
  expiryDate?: string;
  qualityStatus: string;  // PENDING, PASSED, FAILED, QUARANTINE
  supplier?: string;
  supplierLotNo?: string;
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

export interface LotCreateRequest {
  lotNo: string;
  productId: number;
  workOrderId?: number;
  initialQuantity: number;
  currentQuantity: number;
  unit: string;
  manufactureDate?: string;
  expiryDate?: string;
  qualityStatus?: string;
  supplier?: string;
  supplierLotNo?: string;
  remarks?: string;
}

export interface LotUpdateRequest {
  lotId: number;
  currentQuantity: number;
  manufactureDate?: string;
  expiryDate?: string;
  qualityStatus: string;
  supplier?: string;
  supplierLotNo?: string;
  remarks?: string;
}

const lotService = {
  getAll: async (): Promise<Lot[]> => {
    const response = await apiClient.get<Lot[]>('/lots');
    return response.data;
  },

  getByProduct: async (productId: number): Promise<Lot[]> => {
    const response = await apiClient.get<Lot[]>(`/lots/product/${productId}`);
    return response.data;
  },

  getByQualityStatus: async (qualityStatus: string): Promise<Lot[]> => {
    const response = await apiClient.get<Lot[]>(`/lots/quality-status/${qualityStatus}`);
    return response.data;
  },

  getById: async (lotId: number): Promise<Lot> => {
    const response = await apiClient.get<Lot>(`/lots/${lotId}`);
    return response.data;
  },

  create: async (request: LotCreateRequest): Promise<Lot> => {
    const response = await apiClient.post<Lot>('/lots', request);
    return response.data;
  },

  update: async (lotId: number, request: LotUpdateRequest): Promise<Lot> => {
    const response = await apiClient.put<Lot>(`/lots/${lotId}`, request);
    return response.data;
  },

  delete: async (lotId: number): Promise<void> => {
    await apiClient.delete(`/lots/${lotId}`);
  },

  updateQualityStatus: async (lotId: number, qualityStatus: string): Promise<Lot> => {
    const response = await apiClient.post<Lot>(`/lots/${lotId}/quality-status`, null, {
      params: { qualityStatus }
    });
    return response.data;
  },
};

export default lotService;
