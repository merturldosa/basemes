import apiClient from './api';

export interface DeliveryItem {
  deliveryItemId?: number;
  lineNo: number;
  salesOrderItemId: number;
  productId?: number;
  productCode?: string;
  productName?: string;
  materialId?: number;
  materialCode?: string;
  materialName?: string;
  deliveredQuantity: number;
  unit: string;
  lotId?: number;
  lotNo?: string;
  location?: string;
  remarks?: string;
}

export interface Delivery {
  deliveryId: number;
  tenantId: string;
  deliveryNo: string;
  deliveryDate: string;
  salesOrderId: number;
  salesOrderNo: string;
  customerId: number;
  customerName: string;
  warehouseId: number;
  warehouseCode: string;
  warehouseName: string;
  qualityCheckStatus?: string;
  inspectorUserId?: number;
  inspectorName?: string;
  inspectionDate?: string;
  shippingMethod?: string;
  trackingNo?: string;
  carrier?: string;
  status: string;
  shipperUserId: number;
  shipperName: string;
  remarks?: string;
  items: DeliveryItem[];
  createdAt: string;
  updatedAt: string;
}

export interface DeliveryCreateRequest {
  deliveryNo: string;
  deliveryDate: string;
  salesOrderId: number;
  warehouseId: number;
  shipperUserId: number;
  inspectorUserId?: number;
  shippingMethod?: string;
  trackingNo?: string;
  carrier?: string;
  remarks?: string;
  items: DeliveryItem[];
}

export interface DeliveryUpdateRequest {
  deliveryDate: string;
  qualityCheckStatus?: string;
  inspectorUserId?: number;
  inspectionDate?: string;
  shippingMethod?: string;
  trackingNo?: string;
  carrier?: string;
  remarks?: string;
}

class DeliveryService {
  async getAll(): Promise<Delivery[]> {
    const response = await apiClient.get<Delivery[]>('/deliveries');
    return response.data;
  }

  async getById(id: number): Promise<Delivery> {
    const response = await apiClient.get<Delivery>(`/deliveries/${id}`);
    return response.data;
  }

  async getByStatus(status: string): Promise<Delivery[]> {
    const response = await apiClient.get<Delivery[]>(`/deliveries/status/${status}`);
    return response.data;
  }

  async getBySalesOrder(salesOrderId: number): Promise<Delivery[]> {
    const response = await apiClient.get<Delivery[]>(`/deliveries/sales-order/${salesOrderId}`);
    return response.data;
  }

  async getByQualityCheckStatus(qualityCheckStatus: string): Promise<Delivery[]> {
    const response = await apiClient.get<Delivery[]>(`/deliveries/quality-check-status/${qualityCheckStatus}`);
    return response.data;
  }

  async getByDateRange(startDate: string, endDate: string): Promise<Delivery[]> {
    const response = await apiClient.get<Delivery[]>('/deliveries/date-range', {
      params: { startDate, endDate }
    });
    return response.data;
  }

  async create(request: DeliveryCreateRequest): Promise<Delivery> {
    const response = await apiClient.post<Delivery>('/deliveries', request);
    return response.data;
  }

  async update(id: number, request: DeliveryUpdateRequest): Promise<Delivery> {
    const response = await apiClient.put<Delivery>(`/deliveries/${id}`, request);
    return response.data;
  }

  async delete(id: number): Promise<void> {
    await apiClient.delete(`/deliveries/${id}`);
  }

  async complete(id: number): Promise<Delivery> {
    const response = await apiClient.post<Delivery>(`/deliveries/${id}/complete`);
    return response.data;
  }

  async updateQualityCheckStatus(id: number, qualityCheckStatus: string, inspectorUserId: number): Promise<Delivery> {
    const response = await apiClient.post<Delivery>(`/deliveries/${id}/quality-check`, null, {
      params: { qualityCheckStatus, inspectorUserId }
    });
    return response.data;
  }
}

export default new DeliveryService();
