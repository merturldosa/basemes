import apiClient from './api';

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
    const response = await apiClient.get('/employees');
    return response.data.data;
  },

  getPage: async (page: number, size: number) => {
    const response = await apiClient.get('/employees/page', {
      params: { page, size },
    });
    return response.data.data;
  },

  search: async (keyword: string, page: number, size: number) => {
    const response = await apiClient.get('/employees/search', {
      params: { keyword, page, size },
    });
    return response.data.data;
  },

  create: async (employee: EmployeeRequest): Promise<Employee> => {
    const response = await apiClient.post('/employees', employee);
    return response.data.data;
  },

  update: async (id: number, employee: EmployeeRequest): Promise<Employee> => {
    const response = await apiClient.put(`/employees/${id}`, employee);
    return response.data.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/employees/${id}`);
  },
};

export default employeeService;
