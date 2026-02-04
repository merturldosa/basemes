import apiClient from './api';

export interface Supplier {
  supplierId: number;
  tenantId: string;
  tenantName: string;
  supplierCode: string;
  supplierName: string;
  supplierType: string;  // MATERIAL, SERVICE, EQUIPMENT, BOTH
  businessNumber?: string;
  representativeName?: string;
  industry?: string;
  address?: string;
  postalCode?: string;
  phoneNumber?: string;
  faxNumber?: string;
  email?: string;
  website?: string;
  contactPerson?: string;
  contactPhone?: string;
  contactEmail?: string;
  paymentTerms?: string;  // NET30, NET60, COD, ADVANCE
  currency?: string;
  taxType?: string;  // TAXABLE, EXEMPT, ZERO_RATE
  leadTimeDays?: number;
  minOrderAmount?: number;
  isActive: boolean;
  rating?: string;  // EXCELLENT, GOOD, AVERAGE, POOR
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

export interface SupplierCreateRequest {
  supplierCode: string;
  supplierName: string;
  supplierType?: string;
  businessNumber?: string;
  representativeName?: string;
  industry?: string;
  address?: string;
  postalCode?: string;
  phoneNumber?: string;
  faxNumber?: string;
  email?: string;
  website?: string;
  contactPerson?: string;
  contactPhone?: string;
  contactEmail?: string;
  paymentTerms?: string;
  currency?: string;
  taxType?: string;
  leadTimeDays?: number;
  minOrderAmount?: number;
  isActive?: boolean;
  rating?: string;
  remarks?: string;
}

export interface SupplierUpdateRequest {
  supplierId: number;
  supplierName: string;
  supplierType: string;
  businessNumber?: string;
  representativeName?: string;
  industry?: string;
  address?: string;
  postalCode?: string;
  phoneNumber?: string;
  faxNumber?: string;
  email?: string;
  website?: string;
  contactPerson?: string;
  contactPhone?: string;
  contactEmail?: string;
  paymentTerms?: string;
  currency?: string;
  taxType?: string;
  leadTimeDays?: number;
  minOrderAmount?: number;
  isActive: boolean;
  rating?: string;
  remarks?: string;
}

const supplierService = {
  getAll: async (): Promise<Supplier[]> => {
    const response = await apiClient.get<Supplier[]>('/suppliers');
    return response.data;
  },

  getActive: async (): Promise<Supplier[]> => {
    const response = await apiClient.get<Supplier[]>('/suppliers/active');
    return response.data;
  },

  getByType: async (supplierType: string): Promise<Supplier[]> => {
    const response = await apiClient.get<Supplier[]>(`/suppliers/type/${supplierType}`);
    return response.data;
  },

  getByRating: async (rating: string): Promise<Supplier[]> => {
    const response = await apiClient.get<Supplier[]>(`/suppliers/rating/${rating}`);
    return response.data;
  },

  getById: async (supplierId: number): Promise<Supplier> => {
    const response = await apiClient.get<Supplier>(`/suppliers/${supplierId}`);
    return response.data;
  },

  create: async (request: SupplierCreateRequest): Promise<Supplier> => {
    const response = await apiClient.post<Supplier>('/suppliers', request);
    return response.data;
  },

  update: async (supplierId: number, request: SupplierUpdateRequest): Promise<Supplier> => {
    const response = await apiClient.put<Supplier>(`/suppliers/${supplierId}`, request);
    return response.data;
  },

  delete: async (supplierId: number): Promise<void> => {
    await apiClient.delete(`/suppliers/${supplierId}`);
  },

  toggleActive: async (supplierId: number): Promise<Supplier> => {
    const response = await apiClient.post<Supplier>(`/suppliers/${supplierId}/toggle-active`);
    return response.data;
  },
};

export default supplierService;
