import apiClient from './api';

export interface PurchaseRequest {
  purchaseRequestId: number;
  tenantId: string;
  tenantName: string;
  requestNo: string;
  requestDate: string;
  requesterUserId: number;
  requesterUsername: string;
  requesterFullName: string;
  department?: string;
  materialId: number;
  materialCode: string;
  materialName: string;
  requestedQuantity: number;
  unit: string;
  requiredDate?: string;
  purpose?: string;
  status: string;  // PENDING, APPROVED, REJECTED, ORDERED
  approverUserId?: number;
  approverUsername?: string;
  approverFullName?: string;
  approvalDate?: string;
  approvalComment?: string;
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PurchaseRequestCreateRequest {
  requestNo: string;
  requesterUserId: number;
  department?: string;
  materialId: number;
  requestedQuantity: number;
  requiredDate?: string;
  purpose?: string;
  remarks?: string;
}

const purchaseRequestService = {
  getAll: async (): Promise<PurchaseRequest[]> => {
    const response = await apiClient.get<PurchaseRequest[]>('/purchase-requests');
    return response.data;
  },

  getByStatus: async (status: string): Promise<PurchaseRequest[]> => {
    const response = await apiClient.get<PurchaseRequest[]>(`/purchase-requests/status/${status}`);
    return response.data;
  },

  getById: async (purchaseRequestId: number): Promise<PurchaseRequest> => {
    const response = await apiClient.get<PurchaseRequest>(`/purchase-requests/${purchaseRequestId}`);
    return response.data;
  },

  create: async (request: PurchaseRequestCreateRequest): Promise<PurchaseRequest> => {
    const response = await apiClient.post<PurchaseRequest>('/purchase-requests', request);
    return response.data;
  },

  approve: async (purchaseRequestId: number, approverUserId: number, approvalComment?: string): Promise<PurchaseRequest> => {
    const response = await apiClient.post<PurchaseRequest>(
      `/purchase-requests/${purchaseRequestId}/approve`,
      null,
      { params: { approverUserId, approvalComment } }
    );
    return response.data;
  },

  reject: async (purchaseRequestId: number, approverUserId: number, approvalComment?: string): Promise<PurchaseRequest> => {
    const response = await apiClient.post<PurchaseRequest>(
      `/purchase-requests/${purchaseRequestId}/reject`,
      null,
      { params: { approverUserId, approvalComment } }
    );
    return response.data;
  },

  delete: async (purchaseRequestId: number): Promise<void> => {
    await apiClient.delete(`/purchase-requests/${purchaseRequestId}`);
  },
};

export default purchaseRequestService;
