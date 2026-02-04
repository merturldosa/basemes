import api from './api';

export interface PhysicalInventoryCreateRequest {
  warehouseId: number;
  inventoryDate: string;
  plannedByUserId: number;
  remarks?: string;
}

export interface PhysicalInventoryCountRequest {
  itemId: number;
  countedQuantity: number;
  countedByUserId: number;
}

export interface PhysicalInventoryItem {
  physicalInventoryItemId: number;
  productId: number;
  productCode: string;
  productName: string;
  lotId?: number;
  lotNo?: string;
  expiryDate?: string;
  location?: string;
  systemQuantity: number;
  countedQuantity?: number;
  differenceQuantity?: number;
  adjustmentStatus: string;
  adjustmentTransactionId?: number;
  countedByUserId?: number;
  countedAt?: string;
  remarks?: string;
  unit: string;
}

export interface PhysicalInventory {
  physicalInventoryId: number;
  inventoryNo: string;
  inventoryDate: string;
  warehouseId: number;
  warehouseCode: string;
  warehouseName: string;
  inventoryStatus: string;
  plannedByUserId?: number;
  approvedByUserId?: number;
  approvalDate?: string;
  remarks?: string;
  items: PhysicalInventoryItem[];
  statistics: {
    totalItems: number;
    countedItems: number;
    itemsRequiringAdjustment: number;
    approvedAdjustments: number;
    rejectedAdjustments: number;
  };
  createdAt: string;
  updatedAt: string;
}

/**
 * 실사 관리 서비스
 */
export const physicalInventoryService = {
  /**
   * 실사 계획 생성
   */
  create: async (request: PhysicalInventoryCreateRequest): Promise<{ data: PhysicalInventory }> => {
    return api.post('/physical-inventories', request);
  },

  /**
   * 실사 목록 조회
   */
  getAll: async (): Promise<{ data: PhysicalInventory[] }> => {
    return api.get('/physical-inventories');
  },

  /**
   * 실사 상세 조회
   */
  getById: async (id: number): Promise<{ data: PhysicalInventory }> => {
    return api.get(`/physical-inventories/${id}`);
  },

  /**
   * 실사 수량 입력
   */
  updateCount: async (
    physicalInventoryId: number,
    request: PhysicalInventoryCountRequest
  ): Promise<{ data: PhysicalInventory }> => {
    return api.post(`/physical-inventories/${physicalInventoryId}/count`, request);
  },

  /**
   * 실사 완료
   */
  complete: async (physicalInventoryId: number): Promise<{ data: PhysicalInventory }> => {
    return api.post(`/physical-inventories/${physicalInventoryId}/complete`);
  },

  /**
   * 재고 조정 승인
   */
  approveAdjustment: async (
    physicalInventoryId: number,
    itemId: number,
    approverId: number
  ): Promise<{ data: PhysicalInventory }> => {
    return api.post(
      `/physical-inventories/${physicalInventoryId}/items/${itemId}/approve`,
      null,
      { params: { approverId } }
    );
  },

  /**
   * 재고 조정 거부
   */
  rejectAdjustment: async (
    physicalInventoryId: number,
    itemId: number,
    approverId: number,
    reason: string
  ): Promise<{ data: PhysicalInventory }> => {
    return api.post(
      `/physical-inventories/${physicalInventoryId}/items/${itemId}/reject`,
      null,
      { params: { approverId, reason } }
    );
  }
};
