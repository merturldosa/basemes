import apiClient from './api';

export interface RoutingStep {
  routingStepId?: number;
  routingId?: number;
  sequenceOrder?: number;
  processId: number;
  processCode?: string;
  processName?: string;
  standardTime: number;
  setupTime?: number;
  waitTime?: number;
  requiredWorkers?: number;
  equipmentId?: number;
  equipmentCode?: string;
  equipmentName?: string;
  isParallel?: boolean;
  parallelGroup?: number;
  isOptional?: boolean;
  alternateProcessId?: number;
  alternateProcessCode?: string;
  alternateProcessName?: string;
  qualityCheckRequired?: boolean;
  qualityStandard?: string;
  remarks?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProcessRouting {
  routingId: number;
  tenantId: string;
  tenantName: string;
  productId: number;
  productCode: string;
  productName: string;
  routingCode: string;
  routingName: string;
  version: string;
  effectiveDate: string;
  expiryDate?: string;
  isActive: boolean;
  totalStandardTime?: number;
  remarks?: string;
  steps: RoutingStep[];
  createdAt: string;
  updatedAt: string;
}

export interface RoutingCreateRequest {
  productId: number;
  routingCode: string;
  routingName: string;
  version?: string;
  effectiveDate: string;
  expiryDate?: string;
  isActive?: boolean;
  remarks?: string;
  steps: RoutingStep[];
}

export interface RoutingUpdateRequest {
  routingId: number;
  routingName: string;
  effectiveDate: string;
  expiryDate?: string;
  isActive: boolean;
  remarks?: string;
  steps: RoutingStep[];
}

const processRoutingService = {
  getAll: async (): Promise<ProcessRouting[]> => {
    const response = await apiClient.get<ProcessRouting[]>('/routings');
    return response.data;
  },

  getActive: async (): Promise<ProcessRouting[]> => {
    const response = await apiClient.get<ProcessRouting[]>('/routings/active');
    return response.data;
  },

  getByProduct: async (productId: number): Promise<ProcessRouting[]> => {
    const response = await apiClient.get<ProcessRouting[]>(`/routings/product/${productId}`);
    return response.data;
  },

  getById: async (routingId: number): Promise<ProcessRouting> => {
    const response = await apiClient.get<ProcessRouting>(`/routings/${routingId}`);
    return response.data;
  },

  create: async (request: RoutingCreateRequest): Promise<ProcessRouting> => {
    const response = await apiClient.post<ProcessRouting>('/routings', request);
    return response.data;
  },

  update: async (routingId: number, request: RoutingUpdateRequest): Promise<ProcessRouting> => {
    const response = await apiClient.put<ProcessRouting>(`/routings/${routingId}`, request);
    return response.data;
  },

  delete: async (routingId: number): Promise<void> => {
    await apiClient.delete(`/routings/${routingId}`);
  },

  toggleActive: async (routingId: number): Promise<ProcessRouting> => {
    const response = await apiClient.post<ProcessRouting>(`/routings/${routingId}/toggle-active`);
    return response.data;
  },

  copy: async (routingId: number, newVersion: string): Promise<ProcessRouting> => {
    const response = await apiClient.post<ProcessRouting>(`/routings/${routingId}/copy`, null, {
      params: { newVersion }
    });
    return response.data;
  },
};

export default processRoutingService;
