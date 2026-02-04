import apiClient from './api';

export interface QualityStandard {
  qualityStandardId: number;
  productId: number;
  productCode: string;
  productName: string;
  standardCode: string;
  standardName: string;
  standardVersion: string;
  inspectionType: string;  // INCOMING, IN_PROCESS, OUTGOING, FINAL
  inspectionMethod?: string;
  minValue?: number;
  maxValue?: number;
  targetValue?: number;
  toleranceValue?: number;
  unit?: string;
  measurementItem?: string;
  measurementEquipment?: string;
  samplingMethod?: string;
  sampleSize?: number;
  isActive: boolean;
  effectiveDate: string;
  expiryDate?: string;
  remarks?: string;
  tenantId: string;
  tenantName: string;
  createdAt: string;
  updatedAt: string;
}

export interface QualityStandardCreateRequest {
  productId: number;
  standardCode: string;
  standardName: string;
  standardVersion?: string;
  inspectionType: string;  // INCOMING, IN_PROCESS, OUTGOING, FINAL
  inspectionMethod?: string;
  minValue?: number;
  maxValue?: number;
  targetValue?: number;
  toleranceValue?: number;
  unit?: string;
  measurementItem?: string;
  measurementEquipment?: string;
  samplingMethod?: string;
  sampleSize?: number;
  isActive?: boolean;
  effectiveDate: string;
  expiryDate?: string;
  remarks?: string;
}

export interface QualityStandardUpdateRequest {
  qualityStandardId: number;
  productId: number;
  standardName: string;
  inspectionType: string;
  inspectionMethod?: string;
  minValue?: number;
  maxValue?: number;
  targetValue?: number;
  toleranceValue?: number;
  unit?: string;
  measurementItem?: string;
  measurementEquipment?: string;
  samplingMethod?: string;
  sampleSize?: number;
  isActive?: boolean;
  effectiveDate: string;
  expiryDate?: string;
  remarks?: string;
}

const qualityStandardService = {
  // Get all quality standards
  getQualityStandards: async (): Promise<QualityStandard[]> => {
    const response = await apiClient.get<QualityStandard[]>('/quality-standards');
    return response.data;
  },

  // Get active quality standards only
  getActiveQualityStandards: async (): Promise<QualityStandard[]> => {
    const response = await apiClient.get<QualityStandard[]>('/quality-standards/active');
    return response.data;
  },

  // Get quality standard by ID
  getQualityStandard: async (id: number): Promise<QualityStandard> => {
    const response = await apiClient.get<QualityStandard>(`/quality-standards/${id}`);
    return response.data;
  },

  // Get quality standards by product ID
  getQualityStandardsByProduct: async (productId: number): Promise<QualityStandard[]> => {
    const response = await apiClient.get<QualityStandard[]>(`/quality-standards/product/${productId}`);
    return response.data;
  },

  // Create new quality standard
  createQualityStandard: async (request: QualityStandardCreateRequest): Promise<QualityStandard> => {
    const response = await apiClient.post<QualityStandard>('/quality-standards', request);
    return response.data;
  },

  // Update quality standard
  updateQualityStandard: async (id: number, request: QualityStandardUpdateRequest): Promise<QualityStandard> => {
    const response = await apiClient.put<QualityStandard>(`/quality-standards/${id}`, request);
    return response.data;
  },

  // Delete quality standard
  deleteQualityStandard: async (id: number): Promise<void> => {
    await apiClient.delete<void>(`/quality-standards/${id}`);
  },

  // Activate quality standard
  activateQualityStandard: async (id: number): Promise<QualityStandard> => {
    const response = await apiClient.post<QualityStandard>(`/quality-standards/${id}/activate`);
    return response.data;
  },

  // Deactivate quality standard
  deactivateQualityStandard: async (id: number): Promise<QualityStandard> => {
    const response = await apiClient.post<QualityStandard>(`/quality-standards/${id}/deactivate`);
    return response.data;
  },
};

export default qualityStandardService;
