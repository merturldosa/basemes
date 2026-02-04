import apiClient from './api';

export interface Defect {
  defectId: number;
  tenantId: string;
  tenantName: string;
  defectNo: string;
  defectDate: string;
  sourceType: string;
  workOrderId?: number;
  workOrderNo?: string;
  workResultId?: number;
  goodsReceiptId?: number;
  goodsReceiptNo?: string;
  shippingId?: number;
  shippingNo?: string;
  qualityInspectionId?: number;
  qualityInspectionNo?: string;
  productId: number;
  productCode: string;
  productName: string;
  defectType?: string;
  defectCategory?: string;
  defectLocation?: string;
  defectDescription?: string;
  defectQuantity: number;
  lotNo?: string;
  severity?: string;
  status: string;
  responsibleDepartmentId?: number;
  responsibleDepartmentName?: string;
  responsibleUserId?: number;
  responsibleUserName?: string;
  rootCause?: string;
  correctiveAction?: string;
  preventiveAction?: string;
  actionDate?: string;
  reporterUserId?: number;
  reporterName?: string;
  defectCost: number;
  remarks?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface DefectRequest {
  defectNo: string;
  defectDate: string;
  sourceType: string;
  workOrderId?: number;
  workResultId?: number;
  goodsReceiptId?: number;
  shippingId?: number;
  qualityInspectionId?: number;
  productId: number;
  defectType?: string;
  defectCategory?: string;
  defectLocation?: string;
  defectDescription?: string;
  defectQuantity?: number;
  lotNo?: string;
  severity?: string;
  status?: string;
  responsibleDepartmentId?: number;
  responsibleUserId?: number;
  rootCause?: string;
  correctiveAction?: string;
  preventiveAction?: string;
  reporterUserId?: number;
  defectCost?: number;
  remarks?: string;
}

class DefectService {
  async getAll(): Promise<Defect[]> {
    const response = await apiClient.get<Defect[]>('/defects');
    return response.data;
  }

  async getById(id: number): Promise<Defect> {
    const response = await apiClient.get<Defect>(`/defects/${id}`);
    return response.data;
  }

  async getByStatus(status: string): Promise<Defect[]> {
    const response = await apiClient.get<Defect[]>(`/defects/status/${status}`);
    return response.data;
  }

  async getBySourceType(sourceType: string): Promise<Defect[]> {
    const response = await apiClient.get<Defect[]>(`/defects/source-type/${sourceType}`);
    return response.data;
  }

  async create(request: DefectRequest): Promise<Defect> {
    const response = await apiClient.post<Defect>('/defects', request);
    return response.data;
  }

  async update(id: number, request: Partial<DefectRequest>): Promise<Defect> {
    const response = await apiClient.put<Defect>(`/defects/${id}`, request);
    return response.data;
  }

  async close(id: number): Promise<Defect> {
    const response = await apiClient.post<Defect>(`/defects/${id}/close`);
    return response.data;
  }

  async delete(id: number): Promise<void> {
    await apiClient.delete(`/defects/${id}`);
  }
}

export default new DefectService();
