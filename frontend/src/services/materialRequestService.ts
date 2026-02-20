/**
 * Material Request Service
 * 불출 신청 관리 서비스
 * @author Moon Myung-seop
 */

import apiClient from './api';

export interface MaterialRequestItem {
  materialRequestItemId?: number;
  productCode: string;
  productName: string;
  requestedQuantity: number;
  approvedQuantity: number;
  issuedQuantity: number;
  unit: string;
  lotNo?: string;
  remarks?: string;
}

export interface MaterialRequest {
  materialRequestId: number;
  requestNo: string;
  requestDate: string;
  requestStatus: string; // PENDING, APPROVED, REJECTED, ISSUED, COMPLETED, CANCELLED
  priority: string; // URGENT, HIGH, NORMAL, LOW
  purpose: string;
  workOrderNo?: string;
  workOrderId?: number;
  requesterName: string;
  requesterId: number;
  warehouseCode: string;
  warehouseName: string;
  warehouseId: number;
  approverName?: string;
  approverId?: number;
  approvedDate?: string;
  requiredDate: string;
  issuedDate?: string;
  completedDate?: string;
  totalRequestedQuantity: number;
  totalApprovedQuantity: number;
  totalIssuedQuantity: number;
  remarks?: string;
  rejectionReason?: string;
  cancellationReason?: string;
  items?: MaterialRequestItem[];
  createdAt: string;
  updatedAt: string;
}

export interface MaterialRequestCreateRequest {
  workOrderId?: number;
  warehouseId: number;
  requiredDate: string;
  priority: string;
  purpose: string;
  remarks?: string;
  items: {
    productId: number;
    requestedQuantity: number;
    remarks?: string;
  }[];
}

export interface MaterialRequestListParams {
  status?: string;
  workOrderId?: number;
  warehouseId?: number;
  requesterId?: number;
  page?: number;
  size?: number;
}

const materialRequestService = {
  /**
   * 불출 신청 목록 조회
   */
  async getMaterialRequests(params?: MaterialRequestListParams) {
    const response = await apiClient.get<MaterialRequest[]>('/api/material-requests', { params });
    return response.data;
  },

  /**
   * 불출 신청 상세 조회
   */
  async getMaterialRequest(id: number) {
    const response = await apiClient.get<MaterialRequest>(`/api/material-requests/${id}`);
    return response.data;
  },

  /**
   * 긴급 불출 신청 조회
   */
  async getUrgentRequests() {
    const response = await apiClient.get<MaterialRequest[]>('/api/material-requests/urgent');
    return response.data;
  },

  /**
   * 창고별 대기 불출 신청 조회
   */
  async getPendingRequestsByWarehouse(warehouseId: number) {
    const response = await apiClient.get<MaterialRequest[]>(
      `/api/material-requests/warehouse/${warehouseId}/pending`
    );
    return response.data;
  },

  /**
   * 불출 신청 생성
   */
  async createMaterialRequest(request: MaterialRequestCreateRequest) {
    const response = await apiClient.post<MaterialRequest>('/api/material-requests', request);
    return response.data;
  },

  /**
   * 불출 신청 승인
   */
  async approveMaterialRequest(id: number, approverId: number, remarks?: string) {
    const response = await apiClient.post<MaterialRequest>(
      `/api/material-requests/${id}/approve`,
      null,
      {
        params: { approverId, remarks },
      }
    );
    return response.data;
  },

  /**
   * 불출 신청 거부
   */
  async rejectMaterialRequest(id: number, approverId: number, reason: string) {
    const response = await apiClient.post<MaterialRequest>(
      `/api/material-requests/${id}/reject`,
      null,
      {
        params: { approverId, reason },
      }
    );
    return response.data;
  },

  /**
   * 불출 지시
   */
  async issueMaterialRequest(id: number, issuerId: number, remarks?: string) {
    const response = await apiClient.post<MaterialRequest>(
      `/api/material-requests/${id}/issue`,
      null,
      {
        params: { issuerId, remarks },
      }
    );
    return response.data;
  },

  /**
   * 불출 완료
   */
  async completeMaterialRequest(id: number, completerId: number, remarks?: string) {
    const response = await apiClient.post<MaterialRequest>(
      `/api/material-requests/${id}/complete`,
      null,
      {
        params: { completerId, remarks },
      }
    );
    return response.data;
  },

  /**
   * 불출 신청 취소
   */
  async cancelMaterialRequest(id: number, reason: string) {
    const response = await apiClient.post<MaterialRequest>(
      `/api/material-requests/${id}/cancel`,
      null,
      {
        params: { reason },
      }
    );
    return response.data;
  },
};

export default materialRequestService;
