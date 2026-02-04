import apiClient from './api';

export interface PurchaseOrderItem {
  purchaseOrderItemId?: number;
  lineNo: number;
  materialId: number;
  materialCode?: string;
  materialName?: string;
  orderedQuantity: number;
  receivedQuantity?: number;
  unit: string;
  unitPrice?: number;
  amount?: number;
  requiredDate?: string;
  purchaseRequestId?: number;
  purchaseRequestNo?: string;
  remarks?: string;
}

export interface PurchaseOrder {
  purchaseOrderId: number;
  tenantId: string;
  tenantName: string;
  orderNo: string;
  orderDate: string;
  supplierId: number;
  supplierCode: string;
  supplierName: string;
  expectedDeliveryDate?: string;
  deliveryAddress?: string;
  paymentTerms?: string;
  currency?: string;
  status: string;  // DRAFT, CONFIRMED, PARTIALLY_RECEIVED, RECEIVED, CANCELLED
  totalAmount?: number;
  buyerUserId: number;
  buyerUsername: string;
  buyerFullName: string;
  remarks?: string;
  items: PurchaseOrderItem[];
  createdAt: string;
  updatedAt: string;
}

export interface PurchaseOrderCreateRequest {
  orderNo: string;
  supplierId: number;
  buyerUserId: number;
  expectedDeliveryDate?: string;
  deliveryAddress?: string;
  paymentTerms?: string;
  currency?: string;
  remarks?: string;
  items: PurchaseOrderItem[];
}

export interface PurchaseOrderUpdateRequest {
  expectedDeliveryDate?: string;
  deliveryAddress?: string;
  paymentTerms?: string;
  currency?: string;
  remarks?: string;
  items?: PurchaseOrderItem[];
}

const purchaseOrderService = {
  getAll: async (): Promise<PurchaseOrder[]> => {
    const response = await apiClient.get<PurchaseOrder[]>('/purchase-orders');
    return response.data;
  },

  getByStatus: async (status: string): Promise<PurchaseOrder[]> => {
    const response = await apiClient.get<PurchaseOrder[]>(`/purchase-orders/status/${status}`);
    return response.data;
  },

  getBySupplier: async (supplierId: number): Promise<PurchaseOrder[]> => {
    const response = await apiClient.get<PurchaseOrder[]>(`/purchase-orders/supplier/${supplierId}`);
    return response.data;
  },

  getById: async (purchaseOrderId: number): Promise<PurchaseOrder> => {
    const response = await apiClient.get<PurchaseOrder>(`/purchase-orders/${purchaseOrderId}`);
    return response.data;
  },

  create: async (request: PurchaseOrderCreateRequest): Promise<PurchaseOrder> => {
    const response = await apiClient.post<PurchaseOrder>('/purchase-orders', request);
    return response.data;
  },

  update: async (purchaseOrderId: number, request: PurchaseOrderUpdateRequest): Promise<PurchaseOrder> => {
    const response = await apiClient.put<PurchaseOrder>(`/purchase-orders/${purchaseOrderId}`, request);
    return response.data;
  },

  confirm: async (purchaseOrderId: number): Promise<PurchaseOrder> => {
    const response = await apiClient.post<PurchaseOrder>(`/purchase-orders/${purchaseOrderId}/confirm`);
    return response.data;
  },

  cancel: async (purchaseOrderId: number): Promise<PurchaseOrder> => {
    const response = await apiClient.post<PurchaseOrder>(`/purchase-orders/${purchaseOrderId}/cancel`);
    return response.data;
  },

  delete: async (purchaseOrderId: number): Promise<void> => {
    await apiClient.delete(`/purchase-orders/${purchaseOrderId}`);
  },
};

export default purchaseOrderService;
