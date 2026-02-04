import apiClient from './api';

export interface Weighing {
  weighingId: number;
  tenantId: string;
  weighingNo: string;
  weighingDate: string;
  weighingType: string; // INCOMING, OUTGOING, PRODUCTION, SAMPLING
  referenceType?: string; // MATERIAL_REQUEST, WORK_ORDER, GOODS_RECEIPT, SHIPPING, QUALITY_INSPECTION
  referenceId?: number;
  productId: number;
  productCode: string;
  productName: string;
  lotId?: number;
  lotNo?: string;
  tareWeight: number;
  grossWeight: number;
  netWeight: number;
  expectedWeight?: number;
  variance?: number;
  variancePercentage?: number;
  unit: string;
  scaleId?: number;
  scaleName?: string;
  operatorUserId: number;
  operatorUsername: string;
  operatorName: string;
  verifierUserId?: number;
  verifierUsername?: string;
  verifierName?: string;
  verificationStatus: string; // PENDING, VERIFIED, REJECTED
  verificationDate?: string;
  toleranceExceeded: boolean;
  tolerancePercentage?: number;
  temperature?: number;
  humidity?: number;
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

export interface WeighingCreateRequest {
  weighingDate?: string;
  weighingType: string;
  referenceType?: string;
  referenceId?: number;
  productId: number;
  lotId?: number;
  tareWeight: number;
  grossWeight: number;
  expectedWeight?: number;
  unit: string;
  scaleId?: number;
  scaleName?: string;
  operatorUserId: number;
  tolerancePercentage?: number;
  temperature?: number;
  humidity?: number;
  remarks?: string;
}

export interface WeighingUpdateRequest {
  weighingDate?: string;
  tareWeight?: number;
  grossWeight?: number;
  expectedWeight?: number;
  scaleId?: number;
  scaleName?: string;
  tolerancePercentage?: number;
  temperature?: number;
  humidity?: number;
  remarks?: string;
}

export interface WeighingVerificationRequest {
  verifierUserId: number;
  action: 'VERIFY' | 'REJECT';
  remarks?: string;
}

const weighingService = {
  getAll: async (): Promise<Weighing[]> => {
    const response = await apiClient.get<Weighing[]>('/weighings');
    return response.data;
  },

  getById: async (weighingId: number): Promise<Weighing> => {
    const response = await apiClient.get<Weighing>(`/weighings/${weighingId}`);
    return response.data;
  },

  getToleranceExceeded: async (): Promise<Weighing[]> => {
    const response = await apiClient.get<Weighing[]>('/weighings/tolerance-exceeded');
    return response.data;
  },

  getByReference: async (referenceType: string, referenceId: number): Promise<Weighing[]> => {
    const response = await apiClient.get<Weighing[]>(`/weighings/reference/${referenceType}/${referenceId}`);
    return response.data;
  },

  create: async (request: WeighingCreateRequest): Promise<Weighing> => {
    const response = await apiClient.post<Weighing>('/weighings', request);
    return response.data;
  },

  update: async (weighingId: number, request: WeighingUpdateRequest): Promise<Weighing> => {
    const response = await apiClient.put<Weighing>(`/weighings/${weighingId}`, request);
    return response.data;
  },

  verify: async (weighingId: number, request: WeighingVerificationRequest): Promise<Weighing> => {
    const response = await apiClient.post<Weighing>(`/weighings/${weighingId}/verify`, request);
    return response.data;
  },

  delete: async (weighingId: number): Promise<void> => {
    await apiClient.delete(`/weighings/${weighingId}`);
  },
};

export default weighingService;
