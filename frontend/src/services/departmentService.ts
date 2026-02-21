import apiClient from './api';
import { PageResponse } from '@/types';

export interface Department {
  id: number;
  departmentCode: string;
  departmentName: string;
  parentDepartmentId?: number;
  parentDepartmentName?: string;
  description?: string;
  sortOrder?: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface DepartmentRequest {
  departmentCode: string;
  departmentName: string;
  parentDepartmentId?: number;
  description?: string;
  sortOrder?: number;
  isActive: boolean;
}

const departmentService = {
  getAll: async (): Promise<Department[]> => {
    const response = await apiClient.get<Department[]>('/departments');
    return response.data;
  },

  getPage: async (page: number, size: number) => {
    const response = await apiClient.get<PageResponse<Department>>('/departments/page', {
      params: { page, size },
    });
    return response.data;
  },

  create: async (department: DepartmentRequest): Promise<Department> => {
    const response = await apiClient.post<Department>('/departments', department);
    return response.data;
  },

  update: async (id: number, department: DepartmentRequest): Promise<Department> => {
    const response = await apiClient.put<Department>(`/departments/${id}`, department);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/departments/${id}`);
  },
};

export default departmentService;
