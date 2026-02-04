import apiClient from './api';

// Backend DTO 구조에 맞춤
export interface GoodsReceiptItem {
  goodsReceiptItemId?: number;
  purchaseOrderItemId?: number;
  productId: number;
  productCode?: string;
  productName?: string;
  orderedQuantity?: number;
  receivedQuantity: number;
  unitPrice?: number;
  lineAmount?: number;
  lotNo?: string;
  expiryDate?: string;
  inspectionStatus?: string;  // NOT_REQUIRED, PENDING, PASS, FAIL
  qualityInspectionId?: number;
  remarks?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface GoodsReceipt {
  goodsReceiptId: number;
  tenantId: string;
  tenantName: string;
  receiptNo: string;
  receiptDate: string;
  receiptType: string;  // PURCHASE, RETURN, TRANSFER, OTHER
  receiptStatus: string;  // PENDING, INSPECTING, COMPLETED, REJECTED, CANCELLED
  purchaseOrderId?: number;
  purchaseOrderNo?: string;
  supplierId?: number;
  supplierCode?: string;
  supplierName?: string;
  warehouseId: number;
  warehouseCode: string;
  warehouseName: string;
  receiverUserId?: number;
  receiverUserName?: string;
  receiverName?: string;
  totalQuantity?: number;
  totalAmount?: number;
  items: GoodsReceiptItem[];
  remarks?: string;
  isActive: boolean;
  createdAt: string;
  createdBy?: string;
  updatedAt: string;
  updatedBy?: string;
}

export interface GoodsReceiptItemRequest {
  purchaseOrderItemId?: number;
  productId: number;
  receivedQuantity: number;
  lotNo?: string;
  expiryDate?: string;
  inspectionStatus?: string;
  remarks?: string;
}

export interface GoodsReceiptCreateRequest {
  receiptNo?: string;  // Auto-generated if not provided
  receiptDate: string;
  purchaseOrderId?: number;
  supplierId?: number;
  warehouseId: number;
  receiptType: string;
  receiverUserId?: number;
  items: GoodsReceiptItemRequest[];
  remarks?: string;
}

// Used by ReceivingPage
export interface GoodsReceiptRequest {
  receiptNo: string;
  receiptDate: string;
  purchaseOrderId?: number;
  warehouseId: number;
  receiptType: string;
  receiptStatus: string;
  receiverName: string;
  remarks: string;
  items: any[];
}

const goodsReceiptService = {
  getAll: async (): Promise<GoodsReceipt[]> => {
    const response = await apiClient.get<GoodsReceipt[]>('/goods-receipts');
    return response.data;
  },

  getByStatus: async (status: string): Promise<GoodsReceipt[]> => {
    const response = await apiClient.get<GoodsReceipt[]>('/goods-receipts', { status });
    return response.data;
  },

  getByPurchaseOrder: async (purchaseOrderId: number): Promise<GoodsReceipt[]> => {
    const response = await apiClient.get<GoodsReceipt[]>('/goods-receipts', { purchaseOrderId });
    return response.data;
  },

  getByWarehouse: async (warehouseId: number): Promise<GoodsReceipt[]> => {
    const response = await apiClient.get<GoodsReceipt[]>('/goods-receipts', { warehouseId });
    return response.data;
  },

  getByDateRange: async (startDate: string, endDate: string): Promise<GoodsReceipt[]> => {
    const response = await apiClient.get<GoodsReceipt[]>('/goods-receipts/date-range', {
      startDate,
      endDate,
    });
    return response.data;
  },

  getById: async (goodsReceiptId: number): Promise<GoodsReceipt> => {
    const response = await apiClient.get<GoodsReceipt>(`/goods-receipts/${goodsReceiptId}`);
    return response.data;
  },

  create: async (request: GoodsReceiptCreateRequest): Promise<GoodsReceipt> => {
    const response = await apiClient.post<GoodsReceipt>('/goods-receipts', request);
    return response.data;
  },

  update: async (goodsReceiptId: number, request: GoodsReceiptCreateRequest): Promise<GoodsReceipt> => {
    const response = await apiClient.put<GoodsReceipt>(`/goods-receipts/${goodsReceiptId}`, request);
    return response.data;
  },

  complete: async (goodsReceiptId: number): Promise<GoodsReceipt> => {
    const response = await apiClient.post<GoodsReceipt>(`/goods-receipts/${goodsReceiptId}/complete`, {});
    return response.data;
  },

  cancel: async (goodsReceiptId: number, reason?: string): Promise<GoodsReceipt> => {
    const response = await apiClient.post<GoodsReceipt>(
      `/goods-receipts/${goodsReceiptId}/cancel`,
      {},
      { params: { reason } }
    );
    return response.data;
  },
};

export default goodsReceiptService;
