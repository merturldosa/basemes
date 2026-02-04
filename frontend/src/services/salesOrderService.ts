import apiClient from './api';

export interface SalesOrderItem {
  salesOrderItemId?: number;
  lineNo: number;
  productId?: number;
  productCode?: string;
  productName?: string;
  materialId?: number;
  materialCode?: string;
  materialName?: string;
  orderedQuantity: number;
  deliveredQuantity?: number;
  unit: string;
  unitPrice?: number;
  amount?: number;
  requestedDate?: string;
  remarks?: string;
}

export interface SalesOrder {
  salesOrderId: number;
  tenantId: string;
  orderNo: string;
  orderDate: string;
  customerId: number;
  customerCode: string;
  customerName: string;
  requestedDeliveryDate?: string;
  deliveryAddress?: string;
  paymentTerms?: string;
  currency?: string;
  status: string;
  totalAmount?: number;
  salesUserId: number;
  salesUserName: string;
  remarks?: string;
  items: SalesOrderItem[];
  createdAt: string;
  updatedAt: string;
}

export interface SalesOrderCreateRequest {
  orderNo: string;
  orderDate: string;
  customerId: number;
  salesUserId: number;
  requestedDeliveryDate?: string;
  deliveryAddress?: string;
  paymentTerms?: string;
  currency?: string;
  remarks?: string;
  items: SalesOrderItem[];
}

export interface SalesOrderUpdateRequest {
  orderDate: string;
  customerId: number;
  requestedDeliveryDate?: string;
  deliveryAddress?: string;
  paymentTerms?: string;
  currency?: string;
  remarks?: string;
  items: SalesOrderItem[];
}

class SalesOrderService {
  async getAll(): Promise<SalesOrder[]> {
    const response = await apiClient.get<SalesOrder[]>('/sales-orders');
    return response.data;
  }

  async getById(id: number): Promise<SalesOrder> {
    const response = await apiClient.get<SalesOrder>(`/sales-orders/${id}`);
    return response.data;
  }

  async getByStatus(status: string): Promise<SalesOrder[]> {
    const response = await apiClient.get<SalesOrder[]>(`/sales-orders/status/${status}`);
    return response.data;
  }

  async getByCustomer(customerId: number): Promise<SalesOrder[]> {
    const response = await apiClient.get<SalesOrder[]>(`/sales-orders/customer/${customerId}`);
    return response.data;
  }

  async getByDateRange(startDate: string, endDate: string): Promise<SalesOrder[]> {
    const response = await apiClient.get<SalesOrder[]>('/sales-orders/date-range', {
      params: { startDate, endDate }
    });
    return response.data;
  }

  async create(request: SalesOrderCreateRequest): Promise<SalesOrder> {
    const response = await apiClient.post<SalesOrder>('/sales-orders', request);
    return response.data;
  }

  async update(id: number, request: SalesOrderUpdateRequest): Promise<SalesOrder> {
    const response = await apiClient.put<SalesOrder>(`/sales-orders/${id}`, request);
    return response.data;
  }

  async delete(id: number): Promise<void> {
    await apiClient.delete(`/sales-orders/${id}`);
  }

  async confirm(id: number): Promise<SalesOrder> {
    const response = await apiClient.post<SalesOrder>(`/sales-orders/${id}/confirm`);
    return response.data;
  }

  async cancel(id: number): Promise<SalesOrder> {
    const response = await apiClient.post<SalesOrder>(`/sales-orders/${id}/cancel`);
    return response.data;
  }
}

export default new SalesOrderService();
