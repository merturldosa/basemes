import apiClient from './api';

export interface BomDetail {
  bomDetailId?: number;
  bomId?: number;
  sequence?: number;
  materialProductId: number;
  materialProductCode?: string;
  materialProductName?: string;
  processId?: number;
  processCode?: string;
  processName?: string;
  quantity: number;
  unit: string;
  usageRate?: number;
  scrapRate?: number;
  remarks?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Bom {
  bomId: number;
  tenantId: string;
  tenantName: string;
  productId: number;
  productCode: string;
  productName: string;
  bomCode: string;
  bomName: string;
  version: string;
  effectiveDate: string;
  expiryDate?: string;
  isActive: boolean;
  remarks?: string;
  details?: BomDetail[];
  createdAt: string;
  updatedAt: string;
}

export interface BomCreateRequest {
  productId: number;
  bomCode: string;
  bomName: string;
  version?: string;
  effectiveDate: string;
  expiryDate?: string;
  isActive?: boolean;
  remarks?: string;
  details: BomDetail[];
}

export interface BomUpdateRequest {
  bomId: number;
  bomName: string;
  effectiveDate: string;
  expiryDate?: string;
  isActive: boolean;
  remarks?: string;
  details: BomDetail[];
}

const bomService = {
  getAll: async (): Promise<Bom[]> => {
    const response = await apiClient.get<Bom[]>('/boms');
    return response.data;
  },

  getActive: async (): Promise<Bom[]> => {
    const response = await apiClient.get<Bom[]>('/boms/active');
    return response.data;
  },

  getByProduct: async (productId: number): Promise<Bom[]> => {
    const response = await apiClient.get<Bom[]>(`/boms/product/${productId}`);
    return response.data;
  },

  getById: async (bomId: number): Promise<Bom> => {
    const response = await apiClient.get<Bom>(`/boms/${bomId}`);
    return response.data;
  },

  create: async (request: BomCreateRequest): Promise<Bom> => {
    const response = await apiClient.post<Bom>('/boms', request);
    return response.data;
  },

  update: async (bomId: number, request: BomUpdateRequest): Promise<Bom> => {
    const response = await apiClient.put<Bom>(`/boms/${bomId}`, request);
    return response.data;
  },

  delete: async (bomId: number): Promise<void> => {
    await apiClient.delete(`/boms/${bomId}`);
  },

  toggleActive: async (bomId: number): Promise<Bom> => {
    const response = await apiClient.post<Bom>(`/boms/${bomId}/toggle-active`);
    return response.data;
  },

  copy: async (bomId: number, newVersion: string): Promise<Bom> => {
    const response = await apiClient.post<Bom>(`/boms/${bomId}/copy`, null, {
      params: { newVersion }
    });
    return response.data;
  },
};

export default bomService;
