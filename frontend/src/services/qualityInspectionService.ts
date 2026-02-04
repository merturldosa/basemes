import apiClient from './api';

export interface QualityInspection {
  qualityInspectionId: number;
  qualityStandardId: number;
  standardCode: string;
  standardName: string;
  workOrderId?: number;
  workOrderNo?: string;
  workResultId?: number;
  productId: number;
  productCode: string;
  productName: string;
  inspectionNo: string;
  inspectionDate: string;
  inspectionType: string;  // INCOMING, IN_PROCESS, OUTGOING, FINAL
  inspectorUserId: number;
  inspectorUsername: string;
  inspectorName: string;
  inspectedQuantity: number;
  passedQuantity: number;
  failedQuantity: number;
  measuredValue?: number;
  measurementUnit?: string;
  inspectionResult: string;  // PASS, FAIL, CONDITIONAL
  defectType?: string;
  defectReason?: string;
  defectLocation?: string;
  correctiveAction?: string;
  correctiveActionDate?: string;
  remarks?: string;
  tenantId: string;
  tenantName: string;
  createdAt: string;
  updatedAt: string;
}

export interface QualityInspectionCreateRequest {
  qualityStandardId: number;
  workOrderId?: number;
  workResultId?: number;
  productId: number;
  inspectionNo: string;
  inspectionDate: string;
  inspectionType: string;  // INCOMING, IN_PROCESS, OUTGOING, FINAL
  inspectorUserId: number;
  inspectedQuantity: number;
  passedQuantity?: number;
  failedQuantity?: number;
  measuredValue?: number;
  measurementUnit?: string;
  inspectionResult: string;  // PASS, FAIL, CONDITIONAL
  defectType?: string;
  defectReason?: string;
  defectLocation?: string;
  correctiveAction?: string;
  correctiveActionDate?: string;
  remarks?: string;
}

export interface QualityInspectionUpdateRequest {
  qualityInspectionId: number;
  qualityStandardId: number;
  workOrderId?: number;
  workResultId?: number;
  productId: number;
  inspectionDate: string;
  inspectionType: string;
  inspectorUserId: number;
  inspectedQuantity: number;
  passedQuantity?: number;
  failedQuantity?: number;
  measuredValue?: number;
  measurementUnit?: string;
  inspectionResult: string;
  defectType?: string;
  defectReason?: string;
  defectLocation?: string;
  correctiveAction?: string;
  correctiveActionDate?: string;
  remarks?: string;
}

const qualityInspectionService = {
  // Get all quality inspections
  getQualityInspections: async (): Promise<QualityInspection[]> => {
    const response = await apiClient.get<QualityInspection[]>('/quality-inspections');
    return response.data;
  },

  // Get quality inspection by ID
  getQualityInspection: async (id: number): Promise<QualityInspection> => {
    const response = await apiClient.get<QualityInspection>(`/quality-inspections/${id}`);
    return response.data;
  },

  // Get quality inspections by work order ID
  getQualityInspectionsByWorkOrder: async (workOrderId: number): Promise<QualityInspection[]> => {
    const response = await apiClient.get<QualityInspection[]>(`/quality-inspections/work-order/${workOrderId}`);
    return response.data;
  },

  // Get quality inspections by result
  getQualityInspectionsByResult: async (result: string): Promise<QualityInspection[]> => {
    const response = await apiClient.get<QualityInspection[]>(`/quality-inspections/result/${result}`);
    return response.data;
  },

  // Create new quality inspection
  createQualityInspection: async (request: QualityInspectionCreateRequest): Promise<QualityInspection> => {
    const response = await apiClient.post<QualityInspection>('/quality-inspections', request);
    return response.data;
  },

  // Update quality inspection
  updateQualityInspection: async (id: number, request: QualityInspectionUpdateRequest): Promise<QualityInspection> => {
    const response = await apiClient.put<QualityInspection>(`/quality-inspections/${id}`, request);
    return response.data;
  },

  // Delete quality inspection
  deleteQualityInspection: async (id: number): Promise<void> => {
    await apiClient.delete<void>(`/quality-inspections/${id}`);
  },
};

export default qualityInspectionService;
