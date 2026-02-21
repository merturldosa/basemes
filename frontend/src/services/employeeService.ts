import apiClient from './api';
import { PageResponse } from '@/types';

export interface Employee {
  id: number;
  employeeNo: string;
  employeeName: string;
  departmentId?: number;
  departmentName?: string;
  position?: string;
  jobGrade?: string;
  hireDate?: string;
  phoneNumber?: string;
  email?: string;
  userId?: number;
  username?: string;
  employmentStatus: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface EmployeeRequest {
  employeeNo: string;
  employeeName: string;
  departmentId?: number;
  position?: string;
  jobGrade?: string;
  hireDate?: string;
  phoneNumber?: string;
  email?: string;
  employmentStatus: string;
  isActive: boolean;
}

const employeeService = {
  getAll: async (): Promise<Employee[]> => {
    const response = await apiClient.get<Employee[]>('/employees');
    return response.data;
  },

  getPage: async (page: number, size: number) => {
    const response = await apiClient.get<PageResponse<Employee>>('/employees/page', {
      params: { page, size },
    });
    return response.data;
  },

  search: async (keyword: string, page: number, size: number) => {
    const response = await apiClient.get<PageResponse<Employee>>('/employees/search', {
      params: { keyword, page, size },
    });
    return response.data;
  },

  create: async (employee: EmployeeRequest): Promise<Employee> => {
    const response = await apiClient.post<Employee>('/employees', employee);
    return response.data;
  },

  update: async (id: number, employee: EmployeeRequest): Promise<Employee> => {
    const response = await apiClient.put<Employee>(`/employees/${id}`, employee);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/employees/${id}`);
  },
};

export default employeeService;
