import apiClient from './api';

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
    const response = await apiClient.get('/departments');
    return response.data.data;
  },

  getPage: async (page: number, size: number) => {
    const response = await apiClient.get('/departments/page', {
      params: { page, size },
    });
    return response.data.data;
  },

  create: async (department: DepartmentRequest): Promise<Department> => {
    const response = await apiClient.post('/departments', department);
    return response.data.data;
  },

  update: async (id: number, department: DepartmentRequest): Promise<Department> => {
    const response = await apiClient.put(`/departments/${id}`, department);
    return response.data.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/departments/${id}`);
  },
};

export default departmentService;
