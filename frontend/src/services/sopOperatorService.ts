import apiClient from './api';

/**
 * SOP Operator Service
 * Simplified SOP interface for field operators
 * @author Moon Myung-seop
 */

// ==================== Types ====================

export interface SOPSimplified {
  sopId: number;
  sopCode: string;
  sopName: string;
  description?: string;
  sopType: string;
  version: string;
  executionId?: number;
  executionNo?: string;
  executionStatus?: 'IN_PROGRESS' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
  totalSteps: number;
  completedSteps: number;
  completionRate: number;
  steps: SOPSimplifiedStep[];
}

export interface SOPSimplifiedStep {
  stepId: number;
  stepNumber: number;
  stepTitle: string;
  stepDescription?: string;
  isRequired: boolean;
  isCritical: boolean;
  executionStepId?: number;
  executionStatus?: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'SKIPPED' | 'FAILED';
  checkResult?: boolean;
  notes?: string;
}

export interface StartSOPExecutionRequest {
  sopId: number;
  workOrderId: number;
  operatorId: number;
}

export interface CompleteStepRequest {
  passed: boolean;
  notes?: string;
}

export interface CompleteSOPExecutionRequest {
  remarks?: string;
}

// ==================== Service ====================

const sopOperatorService = {
  /**
   * Get SOPs for a work order
   * GET /api/sop/operator/work-order/{workOrderId}
   */
  getWorkOrderSOPs: async (workOrderId: number): Promise<SOPSimplified[]> => {
    const response = await apiClient.get<SOPSimplified[]>(`/sop/operator/work-order/${workOrderId}`);
    return response.data;
  },

  /**
   * Start SOP execution
   * POST /api/sop/operator/execution/start
   */
  startExecution: async (request: StartSOPExecutionRequest): Promise<SOPSimplified> => {
    const response = await apiClient.post<SOPSimplified>('/sop/operator/execution/start', request);
    return response.data;
  },

  /**
   * Complete a step
   * PUT /api/sop/operator/execution/{executionId}/step/{stepId}/complete
   */
  completeStep: async (
    executionId: number,
    stepId: number,
    request: CompleteStepRequest
  ): Promise<void> => {
    await apiClient.put<void>(
      `/sop/operator/execution/${executionId}/step/${stepId}/complete`,
      request
    );
  },

  /**
   * Complete SOP execution
   * POST /api/sop/operator/execution/{executionId}/complete
   */
  completeExecution: async (
    executionId: number,
    request?: CompleteSOPExecutionRequest
  ): Promise<SOPSimplified> => {
    const response = await apiClient.post<SOPSimplified>(
      `/sop/operator/execution/${executionId}/complete`,
      request || {}
    );
    return response.data;
  },

  /**
   * Get execution progress
   */
  getExecutionProgress: async (executionId: number): Promise<SOPSimplified> => {
    // This would need to be added to the backend controller if needed
    // For now, we can get it from the steps within the SOP object
    const response = await apiClient.get<SOPSimplified>(`/sop/operator/execution/${executionId}/progress`);
    return response.data;
  },
};

export default sopOperatorService;
