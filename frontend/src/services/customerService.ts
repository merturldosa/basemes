import apiClient from './api';

export interface Customer {
  customerId: number;
  tenantId: string;
  tenantName: string;
  customerCode: string;
  customerName: string;
  customerType: string;  // DOMESTIC, OVERSEAS, BOTH
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
  creditLimit?: number;
  currency?: string;
  taxType?: string;  // TAXABLE, EXEMPT, ZERO_RATE
  isActive: boolean;
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CustomerCreateRequest {
  customerCode: string;
  customerName: string;
  customerType?: string;
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
  creditLimit?: number;
  currency?: string;
  taxType?: string;
  isActive?: boolean;
  remarks?: string;
}

export interface CustomerUpdateRequest {
  customerId: number;
  customerName: string;
  customerType: string;
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
  creditLimit?: number;
  currency?: string;
  taxType?: string;
  isActive: boolean;
  remarks?: string;
}

const customerService = {
  getAll: async (): Promise<Customer[]> => {
    const response = await apiClient.get<Customer[]>('/customers');
    return response.data;
  },

  getActive: async (): Promise<Customer[]> => {
    const response = await apiClient.get<Customer[]>('/customers/active');
    return response.data;
  },

  getByType: async (customerType: string): Promise<Customer[]> => {
    const response = await apiClient.get<Customer[]>(`/customers/type/${customerType}`);
    return response.data;
  },

  getById: async (customerId: number): Promise<Customer> => {
    const response = await apiClient.get<Customer>(`/customers/${customerId}`);
    return response.data;
  },

  create: async (request: CustomerCreateRequest): Promise<Customer> => {
    const response = await apiClient.post<Customer>('/customers', request);
    return response.data;
  },

  update: async (customerId: number, request: CustomerUpdateRequest): Promise<Customer> => {
    const response = await apiClient.put<Customer>(`/customers/${customerId}`, request);
    return response.data;
  },

  delete: async (customerId: number): Promise<void> => {
    await apiClient.delete(`/customers/${customerId}`);
  },

  toggleActive: async (customerId: number): Promise<Customer> => {
    const response = await apiClient.post<Customer>(`/customers/${customerId}/toggle-active`);
    return response.data;
  },
};

export default customerService;
