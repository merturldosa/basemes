import apiClient from './api';

export interface WorkResult {
  workResultId: number;
  workOrderId: number;
  workOrderNo: string;
  resultDate: string;
  quantity: number;
  goodQuantity: number;
  defectQuantity: number;
  workStartTime: string;
  workEndTime: string;
  workDuration?: number;  // in minutes
  workerId?: number;
  workerName?: string;
  defectReason?: string;
  tenantId: string;
  tenantName: string;
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

export interface WorkResultCreateRequest {
  workOrderId: number;
  resultDate: string;
  quantity: number;
  goodQuantity: number;
  defectQuantity: number;
  workStartTime: string;
  workEndTime: string;
  workDuration?: number;
  workerId?: number;
  workerName?: string;
  defectReason?: string;
  remarks?: string;
}

export interface WorkResultUpdateRequest {
  resultDate?: string;
  quantity?: number;
  goodQuantity?: number;
  defectQuantity?: number;
  workStartTime?: string;
  workEndTime?: string;
  workDuration?: number;
  workerId?: number;
  workerName?: string;
  defectReason?: string;
  remarks?: string;
}

const workResultService = {
  // Get all work results
  getWorkResults: async (): Promise<WorkResult[]> => {
    const response = await apiClient.get<WorkResult[]>('/work-results');
    return response.data;
  },

  // Get work results by work order
  getWorkResultsByWorkOrder: async (workOrderId: number): Promise<WorkResult[]> => {
    const response = await apiClient.get<WorkResult[]>(`/work-results/work-order/${workOrderId}`);
    return response.data;
  },

  // Get work results by date range
  getWorkResultsByDateRange: async (startDate: string, endDate: string): Promise<WorkResult[]> => {
    const response = await apiClient.get<WorkResult[]>('/work-results/date-range', { startDate, endDate });
    return response.data;
  },

  // Get work result by ID
  getWorkResult: async (id: number): Promise<WorkResult> => {
    const response = await apiClient.get<WorkResult>(`/work-results/${id}`);
    return response.data;
  },

  // Create new work result (auto-updates work order aggregates)
  createWorkResult: async (workResult: WorkResultCreateRequest): Promise<WorkResult> => {
    const response = await apiClient.post<WorkResult>('/work-results', workResult);
    return response.data;
  },

  // Update work result (auto-updates work order aggregates)
  updateWorkResult: async (id: number, workResult: WorkResultUpdateRequest): Promise<WorkResult> => {
    const response = await apiClient.put<WorkResult>(`/work-results/${id}`, workResult);
    return response.data;
  },

  // Delete work result (auto-updates work order aggregates)
  deleteWorkResult: async (id: number): Promise<void> => {
    await apiClient.delete<void>(`/work-results/${id}`);
  },

  // Count work results for a work order
  countWorkResults: async (workOrderId: number): Promise<number> => {
    const response = await apiClient.get<number>(`/work-results/work-order/${workOrderId}/count`);
    return response.data;
  },
};

export default workResultService;
