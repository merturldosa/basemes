import apiClient from './api';

export interface AfterSales {
  afterSalesId: number;
  tenantId: string;
  tenantName: string;
  asNo: string;
  receiptDate: string;
  customerId: number;
  customerCode: string;
  customerName: string;
  contactPerson?: string;
  contactPhone?: string;
  contactEmail?: string;
  productId: number;
  productCode: string;
  productName: string;
  serialNo?: string;
  lotNo?: string;
  salesOrderId?: number;
  salesOrderNo?: string;
  shippingId?: number;
  purchaseDate?: string;
  warrantyStatus?: string;
  issueCategory?: string;
  issueDescription: string;
  symptom?: string;
  serviceType?: string;
  serviceStatus: string;
  priority?: string;
  assignedEngineerId?: number;
  assignedEngineerName?: string;
  assignedDate?: string;
  diagnosis?: string;
  serviceAction?: string;
  partsReplaced?: string;
  serviceStartDate?: string;
  serviceEndDate?: string;
  serviceCost: number;
  partsCost: number;
  totalCost: number;
  chargeToCustomer: number;
  resolutionDescription?: string;
  customerSatisfaction?: string;
  remarks?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AfterSalesRequest {
  asNo: string;
  receiptDate: string;
  customerId: number;
  contactPerson?: string;
  contactPhone?: string;
  contactEmail?: string;
  productId: number;
  serialNo?: string;
  lotNo?: string;
  salesOrderId?: number;
  shippingId?: number;
  purchaseDate?: string;
  warrantyStatus?: string;
  issueCategory?: string;
  issueDescription: string;
  symptom?: string;
  serviceType?: string;
  serviceStatus?: string;
  priority?: string;
  assignedEngineerId?: number;
  serviceCost?: number;
  partsCost?: number;
  chargeToCustomer?: number;
  remarks?: string;
}

class AfterSalesService {
  async getAll(): Promise<AfterSales[]> {
    const response = await apiClient.get<AfterSales[]>('/after-sales');
    return response.data;
  }

  async getById(id: number): Promise<AfterSales> {
    const response = await apiClient.get<AfterSales>(`/after-sales/${id}`);
    return response.data;
  }

  async getByStatus(serviceStatus: string): Promise<AfterSales[]> {
    const response = await apiClient.get<AfterSales[]>(`/after-sales/status/${serviceStatus}`);
    return response.data;
  }

  async getByPriority(priority: string): Promise<AfterSales[]> {
    const response = await apiClient.get<AfterSales[]>(`/after-sales/priority/${priority}`);
    return response.data;
  }

  async create(request: AfterSalesRequest): Promise<AfterSales> {
    const response = await apiClient.post<AfterSales>('/after-sales', request);
    return response.data;
  }

  async update(id: number, request: Partial<AfterSalesRequest>): Promise<AfterSales> {
    const response = await apiClient.put<AfterSales>(`/after-sales/${id}`, request);
    return response.data;
  }

  async start(id: number): Promise<AfterSales> {
    const response = await apiClient.post<AfterSales>(`/after-sales/${id}/start`);
    return response.data;
  }

  async complete(id: number): Promise<AfterSales> {
    const response = await apiClient.post<AfterSales>(`/after-sales/${id}/complete`);
    return response.data;
  }

  async close(id: number): Promise<AfterSales> {
    const response = await apiClient.post<AfterSales>(`/after-sales/${id}/close`);
    return response.data;
  }

  async delete(id: number): Promise<void> {
    await apiClient.delete(`/after-sales/${id}`);
  }
}

export default new AfterSalesService();
