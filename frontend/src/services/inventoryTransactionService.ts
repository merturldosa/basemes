import apiClient from './api';

export interface InventoryTransaction {
  transactionId: number;
  tenantId: string;
  tenantName: string;
  transactionNo: string;
  transactionType: string;  // IN_RECEIVE, IN_PRODUCTION, IN_RETURN, OUT_ISSUE, OUT_SCRAP, MOVE, ADJUST
  transactionDate: string;
  warehouseId: number;
  warehouseCode: string;
  warehouseName: string;
  productId: number;
  productCode: string;
  productName: string;
  lotId?: number;
  lotNo?: string;
  quantity: number;
  unit: string;
  fromWarehouseId?: number;
  fromWarehouseCode?: string;
  fromWarehouseName?: string;
  toWarehouseId?: number;
  toWarehouseCode?: string;
  toWarehouseName?: string;
  workOrderId?: number;
  workOrderNo?: string;
  qualityInspectionId?: number;
  inspectionNo?: string;
  transactionUserId: number;
  transactionUserName: string;
  approvalStatus: string;
  approvedById?: number;
  approvedByName?: string;
  approvedDate?: string;
  referenceNo?: string;
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

export interface InventoryTransactionCreateRequest {
  transactionNo: string;
  transactionType: string;
  transactionDate: string;
  warehouseId: number;
  productId: number;
  lotId?: number;
  quantity: number;
  unit: string;
  fromWarehouseId?: number;
  toWarehouseId?: number;
  workOrderId?: number;
  qualityInspectionId?: number;
  transactionUserId: number;
  referenceNo?: string;
  remarks?: string;
}

const inventoryTransactionService = {
  getAll: async (): Promise<InventoryTransaction[]> => {
    const response = await apiClient.get<InventoryTransaction[]>('/inventory-transactions');
    return response.data;
  },

  getById: async (transactionId: number): Promise<InventoryTransaction> => {
    const response = await apiClient.get<InventoryTransaction>(`/inventory-transactions/${transactionId}`);
    return response.data;
  },

  getByDateRange: async (startDate: string, endDate: string): Promise<InventoryTransaction[]> => {
    const response = await apiClient.get<InventoryTransaction[]>('/inventory-transactions/date-range', {
      params: { startDate, endDate }
    });
    return response.data;
  },

  create: async (request: InventoryTransactionCreateRequest): Promise<InventoryTransaction> => {
    const response = await apiClient.post<InventoryTransaction>('/inventory-transactions', request);
    return response.data;
  },
};

export default inventoryTransactionService;
