import apiClient from './api';

export interface WorkOrder {
  workOrderId: number;
  workOrderNo: string;
  status: string;
  productId: number;
  productCode: string;
  productName: string;
  processId: number;
  processCode: string;
  processName: string;
  assignedUserId?: number;
  assignedUserName?: string;
  plannedQuantity: number;
  actualQuantity: number;
  goodQuantity: number;
  defectQuantity: number;
  plannedStartDate: string;
  plannedEndDate: string;
  actualStartDate?: string;
  actualEndDate?: string;
  priority?: string;
  tenantId: string;
  tenantName: string;
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

export interface WorkOrderCreateRequest {
  workOrderNo: string;
  productId: number;
  processId: number;
  plannedQuantity: number;
  plannedStartDate: string;
  plannedEndDate: string;
  assignedUserId?: number;
  priority?: string;
  remarks?: string;
}

export interface WorkOrderUpdateRequest {
  productId?: number;
  processId?: number;
  plannedQuantity?: number;
  plannedStartDate?: string;
  plannedEndDate?: string;
  assignedUserId?: number;
  priority?: string;
  remarks?: string;
}

const workOrderService = {
  // Get all work orders
  getWorkOrders: async (): Promise<WorkOrder[]> => {
    const response = await apiClient.get<WorkOrder[]>('/work-orders');
    return response.data;
  },

  // Get work orders by status
  getWorkOrdersByStatus: async (status: string): Promise<WorkOrder[]> => {
    const response = await apiClient.get<WorkOrder[]>(`/work-orders/status/${status}`);
    return response.data;
  },

  // Get work orders by date range
  getWorkOrdersByDateRange: async (startDate: string, endDate: string): Promise<WorkOrder[]> => {
    const response = await apiClient.get<WorkOrder[]>('/work-orders/date-range', { startDate, endDate });
    return response.data;
  },

  // Get work order by ID
  getWorkOrder: async (id: number): Promise<WorkOrder> => {
    const response = await apiClient.get<WorkOrder>(`/work-orders/${id}`);
    return response.data;
  },

  // Get work order by number
  getWorkOrderByNo: async (workOrderNo: string): Promise<WorkOrder> => {
    const response = await apiClient.get<WorkOrder>(`/work-orders/no/${workOrderNo}`);
    return response.data;
  },

  // Create new work order
  createWorkOrder: async (workOrder: WorkOrderCreateRequest): Promise<WorkOrder> => {
    const response = await apiClient.post<WorkOrder>('/work-orders', workOrder);
    return response.data;
  },

  // Update work order
  updateWorkOrder: async (id: number, workOrder: WorkOrderUpdateRequest): Promise<WorkOrder> => {
    const response = await apiClient.put<WorkOrder>(`/work-orders/${id}`, workOrder);
    return response.data;
  },

  // Delete work order
  deleteWorkOrder: async (id: number): Promise<void> => {
    await apiClient.delete<void>(`/work-orders/${id}`);
  },

  // Start work order (status: READY/PENDING -> IN_PROGRESS)
  startWorkOrder: async (id: number): Promise<WorkOrder> => {
    const response = await apiClient.post<WorkOrder>(`/work-orders/${id}/start`);
    return response.data;
  },

  // Complete work order (status: IN_PROGRESS -> COMPLETED)
  completeWorkOrder: async (id: number): Promise<WorkOrder> => {
    const response = await apiClient.post<WorkOrder>(`/work-orders/${id}/complete`);
    return response.data;
  },

  // Cancel work order
  cancelWorkOrder: async (id: number): Promise<WorkOrder> => {
    const response = await apiClient.post<WorkOrder>(`/work-orders/${id}/cancel`);
    return response.data;
  },
};

export default workOrderService;
