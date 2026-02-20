/**
 * Material Handover Service
 * 자재 인수인계 관리 서비스
 * @author Moon Myung-seop
 */

import apiClient from './api';

export interface MaterialHandover {
  materialHandoverId: number;
  handoverNo: string;
  materialRequestNo: string;
  materialRequestId: number;
  handoverDate: string;
  handoverStatus: string; // PENDING, CONFIRMED, REJECTED
  delivererName: string;
  delivererId: number;
  receiverName: string;
  receiverId: number;
  productCode: string;
  productName: string;
  quantity: number;
  unit: string;
  lotNo?: string;
  fromLocation: string;
  toLocation: string;
  remarks?: string;
  confirmedDate?: string;
  rejectionReason?: string;
  createdAt: string;
  updatedAt: string;
}

export interface MaterialHandoverListParams {
  status?: string;
  materialRequestId?: number;
  delivererId?: number;
  receiverId?: number;
  page?: number;
  size?: number;
}

const materialHandoverService = {
  /**
   * 인수인계 목록 조회
   */
  async getMaterialHandovers(params?: MaterialHandoverListParams) {
    const response = await apiClient.get<MaterialHandover[]>('/api/material-handovers', { params });
    return response.data;
  },

  /**
   * 인수인계 상세 조회
   */
  async getMaterialHandover(id: number) {
    const response = await apiClient.get<MaterialHandover>(`/api/material-handovers/${id}`);
    return response.data;
  },

  /**
   * 내 대기 인수인계 조회 (로그인 사용자)
   */
  async getMyPendingHandovers(receiverId: number) {
    const response = await apiClient.get<MaterialHandover[]>('/api/material-handovers/my-pending', {
      params: { receiverId },
    });
    return response.data;
  },

  /**
   * 인수 확인
   */
  async confirmHandover(id: number, receiverId: number, remarks?: string) {
    const response = await apiClient.post<MaterialHandover>(
      `/api/material-handovers/${id}/confirm`,
      null,
      {
        params: { receiverId, remarks },
      }
    );
    return response.data;
  },

  /**
   * 인수 거부
   */
  async rejectHandover(id: number, receiverId: number, reason: string) {
    const response = await apiClient.post<MaterialHandover>(
      `/api/material-handovers/${id}/reject`,
      null,
      {
        params: { receiverId, reason },
      }
    );
    return response.data;
  },
};

export default materialHandoverService;
