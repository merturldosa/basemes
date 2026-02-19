import apiClient from './api';

export interface InspectionPlan {
  planId: number;
  tenantId: string;
  tenantName: string;
  planCode: string;
  planName: string;
  equipmentId: number;
  equipmentCode?: string;
  equipmentName?: string;
  formId?: number;
  formName?: string;
  inspectionType: string;
  cycleDays: number;
  assignedUserId?: number;
  assignedUserName?: string;
  lastExecutionDate?: string;
  nextDueDate?: string;
  status: string;
  remarks?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface InspectionPlanCreateRequest {
  planCode: string;
  planName: string;
  equipmentId: number;
  inspectionType: string;
  cycleDays: number;
  formId?: number;
  assignedUserId?: number;
  nextDueDate?: string;
  remarks?: string;
}

export interface InspectionPlanUpdateRequest {
  planName?: string;
  equipmentId?: number;
  inspectionType?: string;
  cycleDays?: number;
  formId?: number;
  assignedUserId?: number;
  nextDueDate?: string;
  status?: string;
  remarks?: string;
}

export interface InspectionAction {
  actionId: number;
  tenantId: string;
  tenantName: string;
  inspectionId: number;
  inspectionNo?: string;
  actionType: string;
  description: string;
  assignedUserId?: number;
  assignedUserName?: string;
  dueDate?: string;
  completedDate?: string;
  status: string;
  result?: string;
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

export interface InspectionActionCreateRequest {
  inspectionId: number;
  actionType: string;
  description: string;
  assignedUserId?: number;
  dueDate?: string;
  remarks?: string;
}

export interface InspectionActionUpdateRequest {
  description?: string;
  assignedUserId?: number;
  dueDate?: string;
  completedDate?: string;
  status?: string;
  result?: string;
  remarks?: string;
}

const inspectionPlanService = {
  /**
   * Get all inspection plans
   */
  getAll: async (): Promise<InspectionPlan[]> => {
    const response = await apiClient.get<InspectionPlan[]>('/inspection-plans');
    return response.data;
  },

  /**
   * Get inspection plan by ID
   */
  getById: async (planId: number): Promise<InspectionPlan> => {
    const response = await apiClient.get<InspectionPlan>(`/inspection-plans/${planId}`);
    return response.data;
  },

  /**
   * Get due inspection plans
   */
  getDue: async (dueDate: string): Promise<InspectionPlan[]> => {
    const response = await apiClient.get<InspectionPlan[]>('/inspection-plans/due', {
      params: { dueDate }
    });
    return response.data;
  },

  /**
   * Create inspection plan
   */
  create: async (data: InspectionPlanCreateRequest): Promise<InspectionPlan> => {
    const response = await apiClient.post<InspectionPlan>('/inspection-plans', data);
    return response.data;
  },

  /**
   * Update inspection plan
   */
  update: async (planId: number, data: InspectionPlanUpdateRequest): Promise<InspectionPlan> => {
    const response = await apiClient.put<InspectionPlan>(`/inspection-plans/${planId}`, data);
    return response.data;
  },

  /**
   * Execute inspection plan
   */
  execute: async (planId: number, executionDate: string): Promise<InspectionPlan> => {
    const response = await apiClient.post<InspectionPlan>(`/inspection-plans/${planId}/execute?executionDate=${encodeURIComponent(executionDate)}`);
    return response.data;
  },

  /**
   * Delete inspection plan
   */
  delete: async (planId: number): Promise<void> => {
    await apiClient.delete(`/inspection-plans/${planId}`);
  },

  /**
   * Get all inspection actions
   */
  getAllActions: async (): Promise<InspectionAction[]> => {
    const response = await apiClient.get<InspectionAction[]>('/inspection-actions');
    return response.data;
  },

  /**
   * Get inspection action by ID
   */
  getActionById: async (actionId: number): Promise<InspectionAction> => {
    const response = await apiClient.get<InspectionAction>(`/inspection-actions/${actionId}`);
    return response.data;
  },

  /**
   * Get inspection actions by inspection ID
   */
  getActionsByInspection: async (inspectionId: number): Promise<InspectionAction[]> => {
    const response = await apiClient.get<InspectionAction[]>(`/inspection-actions/inspection/${inspectionId}`);
    return response.data;
  },

  /**
   * Create inspection action
   */
  createAction: async (data: InspectionActionCreateRequest): Promise<InspectionAction> => {
    const response = await apiClient.post<InspectionAction>('/inspection-actions', data);
    return response.data;
  },

  /**
   * Update inspection action
   */
  updateAction: async (actionId: number, data: InspectionActionUpdateRequest): Promise<InspectionAction> => {
    const response = await apiClient.put<InspectionAction>(`/inspection-actions/${actionId}`, data);
    return response.data;
  },

  /**
   * Delete inspection action
   */
  deleteAction: async (actionId: number): Promise<void> => {
    await apiClient.delete(`/inspection-actions/${actionId}`);
  }
};

export default inspectionPlanService;
