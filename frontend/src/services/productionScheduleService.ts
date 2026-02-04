import apiClient from './api';

export interface ScheduleStep {
  routingStepId?: number;
  sequenceOrder?: number;
  processId: number;
  processCode?: string;
  processName?: string;
  plannedStartTime: string;
  plannedEndTime: string;
  plannedDuration: number;
  actualStartTime?: string;
  actualEndTime?: string;
  actualDuration?: number;
  assignedEquipmentId?: number;
  assignedEquipmentCode?: string;
  assignedEquipmentName?: string;
  assignedWorkers?: number;
  assignedUserId?: number;
  assignedUserName?: string;
  remarks?: string;
}

export interface ProductionSchedule {
  scheduleId: number;
  tenantId: string;
  tenantName: string;
  workOrderId: number;
  workOrderNo: string;
  productId: number;
  productCode: string;
  productName: string;
  routingStepId: number;
  sequenceOrder: number;
  processId: number;
  processCode: string;
  processName: string;
  plannedStartTime: string;
  plannedEndTime: string;
  plannedDuration: number;
  actualStartTime?: string;
  actualEndTime?: string;
  actualDuration?: number;
  assignedEquipmentId?: number;
  assignedEquipmentCode?: string;
  assignedEquipmentName?: string;
  assignedWorkers?: number;
  assignedUserId?: number;
  assignedUserName?: string;
  status: string;
  progressRate?: number;
  isDelayed: boolean;
  delayMinutes?: number;
  delayReason?: string;
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ScheduleCreateRequest {
  workOrderId: number;
  routingStepId: number;
  sequenceOrder: number;
  plannedStartTime: string;
  plannedEndTime: string;
  plannedDuration: number;
  assignedEquipmentId?: number;
  assignedWorkers?: number;
  assignedUserId?: number;
  remarks?: string;
}

export interface ScheduleUpdateRequest {
  scheduleId: number;
  plannedStartTime?: string;
  plannedEndTime?: string;
  plannedDuration?: number;
  actualStartTime?: string;
  actualEndTime?: string;
  assignedEquipmentId?: number;
  assignedWorkers?: number;
  assignedUserId?: number;
  status?: string;
  progressRate?: number;
  delayReason?: string;
  remarks?: string;
}

export interface GanttTask {
  id: string;
  name: string;
  startTime: string;
  endTime: string;
  duration: number;
  progress: number;
  status: string;
  color: string;
  parentId?: string;
  dependencies?: string[];
  resource?: {
    equipmentCode?: string;
    equipmentName?: string;
    workers?: number;
    assignedUserName?: string;
  };
}

export interface GanttChartData {
  startDate: string;
  endDate: string;
  tasks: GanttTask[];
  resources?: any[];
}

const productionScheduleService = {
  // 전체 일정 조회
  getAll: async (status?: string): Promise<ProductionSchedule[]> => {
    const params = status ? { status } : {};
    const response = await apiClient.get<ProductionSchedule[]>('/schedules', { params });
    return response.data;
  },

  // 기간별 일정 조회
  getByPeriod: async (startDate: string, endDate: string): Promise<ProductionSchedule[]> => {
    const response = await apiClient.get<ProductionSchedule[]>('/schedules/period', {
      params: { startDate, endDate },
    });
    return response.data;
  },

  // WorkOrder별 일정 조회
  getByWorkOrder: async (workOrderId: number): Promise<ProductionSchedule[]> => {
    const response = await apiClient.get<ProductionSchedule[]>(`/schedules/work-order/${workOrderId}`);
    return response.data;
  },

  // 지연 일정 조회
  getDelayed: async (): Promise<ProductionSchedule[]> => {
    const response = await apiClient.get<ProductionSchedule[]>('/schedules/delayed');
    return response.data;
  },

  // 상세 조회
  getById: async (scheduleId: number): Promise<ProductionSchedule> => {
    const response = await apiClient.get<ProductionSchedule>(`/schedules/${scheduleId}`);
    return response.data;
  },

  // WorkOrder에서 자동 일정 생성
  generateFromWorkOrder: async (workOrderId: number): Promise<ProductionSchedule[]> => {
    const response = await apiClient.post<ProductionSchedule[]>(`/schedules/generate/${workOrderId}`);
    return response.data;
  },

  // 일정 생성
  create: async (request: ScheduleCreateRequest): Promise<ProductionSchedule> => {
    const response = await apiClient.post<ProductionSchedule>('/schedules', request);
    return response.data;
  },

  // 일정 수정
  update: async (scheduleId: number, request: ScheduleUpdateRequest): Promise<ProductionSchedule> => {
    const response = await apiClient.put<ProductionSchedule>(`/schedules/${scheduleId}`, request);
    return response.data;
  },

  // 일정 삭제
  delete: async (scheduleId: number): Promise<void> => {
    await apiClient.delete(`/schedules/${scheduleId}`);
  },

  // 상태 변경
  updateStatus: async (scheduleId: number, status: string): Promise<ProductionSchedule> => {
    const response = await apiClient.post<ProductionSchedule>(`/schedules/${scheduleId}/status`, null, {
      params: { status },
    });
    return response.data;
  },

  // Gantt Chart 데이터 조회
  getGanttChart: async (startDate: string, endDate: string): Promise<GanttChartData> => {
    const response = await apiClient.get<GanttChartData>('/schedules/gantt', {
      params: { startDate, endDate },
    });
    return response.data;
  },

  // 리소스 충돌 체크
  checkConflicts: async (scheduleId: number): Promise<ProductionSchedule[]> => {
    const response = await apiClient.get<ProductionSchedule[]>(`/schedules/${scheduleId}/conflicts`);
    return response.data;
  },
};

export default productionScheduleService;
