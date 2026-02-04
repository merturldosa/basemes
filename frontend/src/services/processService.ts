import apiClient from './api';

export interface Process {
  processId: number;
  processCode: string;
  processName: string;
  processType?: string;
  sequenceOrder: number;
  isActive: boolean;
  tenantId: string;
  tenantName: string;
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ProcessCreateRequest {
  processCode: string;
  processName: string;
  processType?: string;
  sequenceOrder?: number;
  remarks?: string;
}

export interface ProcessUpdateRequest {
  processName?: string;
  processType?: string;
  sequenceOrder?: number;
  remarks?: string;
}

const processService = {
  // Get all processes (ordered by sequence)
  getProcesses: async (): Promise<Process[]> => {
    const response = await apiClient.get<Process[]>('/processes');
    return response.data;
  },

  // Get active processes only
  getActiveProcesses: async (): Promise<Process[]> => {
    const response = await apiClient.get<Process[]>('/processes/active');
    return response.data;
  },

  // Get process by ID
  getProcess: async (id: number): Promise<Process> => {
    const response = await apiClient.get<Process>(`/processes/${id}`);
    return response.data;
  },

  // Get process by code
  getProcessByCode: async (processCode: string): Promise<Process> => {
    const response = await apiClient.get<Process>(`/processes/code/${processCode}`);
    return response.data;
  },

  // Create new process
  createProcess: async (process: ProcessCreateRequest): Promise<Process> => {
    const response = await apiClient.post<Process>('/processes', process);
    return response.data;
  },

  // Update process
  updateProcess: async (id: number, process: ProcessUpdateRequest): Promise<Process> => {
    const response = await apiClient.put<Process>(`/processes/${id}`, process);
    return response.data;
  },

  // Delete process
  deleteProcess: async (id: number): Promise<void> => {
    await apiClient.delete<void>(`/processes/${id}`);
  },

  // Activate process
  activateProcess: async (id: number): Promise<Process> => {
    const response = await apiClient.post<Process>(`/processes/${id}/activate`);
    return response.data;
  },

  // Deactivate process
  deactivateProcess: async (id: number): Promise<Process> => {
    const response = await apiClient.post<Process>(`/processes/${id}/deactivate`);
    return response.data;
  },
};

export default processService;
