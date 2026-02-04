import apiClient from './api';

export interface ShippingItem {
  shippingItemId?: number;
  salesOrderItemId?: number;
  productId: number;
  productCode?: string;
  productName?: string;
  orderedQuantity?: number;
  shippedQuantity: number;
  unitPrice?: number;
  lineAmount?: number;
  lotNo?: string;
  expiryDate?: string;
  inspectionStatus?: string;
  qualityInspectionId?: number;
  remarks?: string;
}

export interface Shipping {
  shippingId: number;
  tenantId: string;
  shippingNo: string;
  shippingDate: string;
  salesOrderId?: number;
  salesOrderNo?: string;
  customerId?: number;
  customerCode?: string;
  customerName?: string;
  warehouseId: number;
  warehouseCode?: string;
  warehouseName?: string;
  shippingType: string;
  shippingStatus: string;
  totalQuantity?: number;
  totalAmount?: number;
  shipperUserId?: number;
  shipperName?: string;
  deliveryAddress?: string;
  trackingNumber?: string;
  carrierName?: string;
  remarks?: string;
  isActive: boolean;
  items: ShippingItem[];
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface ShippingRequest {
  shippingNo: string;
  shippingDate: string;
  salesOrderId?: number;
  customerId?: number;
  warehouseId: number;
  shippingType: string;
  shippingStatus?: string;
  totalQuantity?: number;
  totalAmount?: number;
  shipperUserId?: number;
  shipperName?: string;
  deliveryAddress?: string;
  trackingNumber?: string;
  carrierName?: string;
  remarks?: string;
  items: ShippingItem[];
}

class ShippingService {
  async getAll(): Promise<Shipping[]> {
    const response = await apiClient.get<Shipping[]>('/shippings');
    return response.data;
  }

  async getById(id: number): Promise<Shipping> {
    const response = await apiClient.get<Shipping>(`/shippings/${id}`);
    return response.data;
  }

  async getByStatus(status: string): Promise<Shipping[]> {
    const response = await apiClient.get<Shipping[]>(`/shippings/status/${status}`);
    return response.data;
  }

  async create(request: ShippingRequest): Promise<Shipping> {
    const response = await apiClient.post<Shipping>('/shippings', request);
    return response.data;
  }

  async delete(id: number): Promise<void> {
    await apiClient.delete(`/shippings/${id}`);
  }

  async complete(id: number): Promise<Shipping> {
    const response = await apiClient.post<Shipping>(`/shippings/${id}/complete`);
    return response.data;
  }
}

export default new ShippingService();
