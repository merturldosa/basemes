import apiClient from './api';

export interface Claim {
  claimId: number;
  tenantId: string;
  tenantName: string;
  claimNo: string;
  claimDate: string;
  customerId: number;
  customerCode: string;
  customerName: string;
  contactPerson?: string;
  contactPhone?: string;
  contactEmail?: string;
  productId?: number;
  productCode?: string;
  productName?: string;
  lotNo?: string;
  salesOrderId?: number;
  salesOrderNo?: string;
  shippingId?: number;
  claimType?: string;
  claimCategory?: string;
  claimDescription: string;
  claimedQuantity: number;
  claimedAmount: number;
  severity?: string;
  priority?: string;
  status: string;
  responsibleDepartmentId?: number;
  responsibleDepartmentName?: string;
  responsibleUserId?: number;
  responsibleUserName?: string;
  assignedDate?: string;
  investigationFindings?: string;
  rootCauseAnalysis?: string;
  resolutionType?: string;
  resolutionDescription?: string;
  resolutionAmount: number;
  resolutionDate?: string;
  correctiveAction?: string;
  preventiveAction?: string;
  actionCompletionDate?: string;
  customerAcceptance?: string;
  customerFeedback?: string;
  claimCost: number;
  compensationAmount: number;
  remarks?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ClaimRequest {
  claimNo: string;
  claimDate: string;
  customerId: number;
  contactPerson?: string;
  contactPhone?: string;
  contactEmail?: string;
  productId?: number;
  lotNo?: string;
  salesOrderId?: number;
  shippingId?: number;
  claimType?: string;
  claimCategory?: string;
  claimDescription: string;
  claimedQuantity?: number;
  claimedAmount?: number;
  severity?: string;
  priority?: string;
  status?: string;
  responsibleDepartmentId?: number;
  responsibleUserId?: number;
  remarks?: string;
}

class ClaimService {
  async getAll(): Promise<Claim[]> {
    const response = await apiClient.get<Claim[]>('/claims');
    return response.data;
  }

  async getById(id: number): Promise<Claim> {
    const response = await apiClient.get<Claim>(`/claims/${id}`);
    return response.data;
  }

  async getByStatus(status: string): Promise<Claim[]> {
    const response = await apiClient.get<Claim[]>(`/claims/status/${status}`);
    return response.data;
  }

  async getByClaimType(claimType: string): Promise<Claim[]> {
    const response = await apiClient.get<Claim[]>(`/claims/type/${claimType}`);
    return response.data;
  }

  async create(request: ClaimRequest): Promise<Claim> {
    const response = await apiClient.post<Claim>('/claims', request);
    return response.data;
  }

  async update(id: number, request: Partial<ClaimRequest>): Promise<Claim> {
    const response = await apiClient.put<Claim>(`/claims/${id}`, request);
    return response.data;
  }

  async investigate(id: number): Promise<Claim> {
    const response = await apiClient.post<Claim>(`/claims/${id}/investigate`);
    return response.data;
  }

  async resolve(id: number): Promise<Claim> {
    const response = await apiClient.post<Claim>(`/claims/${id}/resolve`);
    return response.data;
  }

  async close(id: number): Promise<Claim> {
    const response = await apiClient.post<Claim>(`/claims/${id}/close`);
    return response.data;
  }

  async delete(id: number): Promise<void> {
    await apiClient.delete(`/claims/${id}`);
  }
}

export default new ClaimService();
