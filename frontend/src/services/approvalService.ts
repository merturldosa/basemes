/**
 * Approval Service
 * 결재 관리 API 서비스
 */

import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

// ==================== Interfaces ====================

export interface ApprovalLineTemplate {
  templateId: number;
  tenantId: string;
  templateName: string;
  templateCode: string;
  documentType: string;
  description?: string;
  approvalType: 'SEQUENTIAL' | 'PARALLEL' | 'HYBRID';
  autoApproveAmount?: number;
  skipIfSamePerson: boolean;
  isDefault: boolean;
  isActive: boolean;
  steps?: ApprovalLineStep[];
  createdAt?: string;
  updatedAt?: string;
}

export interface ApprovalLineStep {
  stepId: number;
  stepOrder: number;
  stepName: string;
  stepType: 'APPROVAL' | 'REVIEW' | 'NOTIFICATION';
  approverType: 'ROLE' | 'POSITION' | 'DEPARTMENT' | 'USER';
  approverRole?: string;
  approverPosition?: string;
  approverDepartment?: string;
  approverUserId?: number;
  isMandatory: boolean;
  approvalMethod: 'SINGLE' | 'ALL' | 'MAJORITY';
  parallelGroup?: number;
  autoApproveOnTimeout: boolean;
  timeoutHours?: number;
  allowDelegation: boolean;
  allowSkip: boolean;
}

export interface ApprovalInstance {
  instanceId: number;
  tenantId: string;
  templateId?: number;
  documentType: string;
  documentId: number;
  documentNo?: string;
  documentTitle?: string;
  documentAmount?: number;
  approvalStatus: 'PENDING' | 'IN_PROGRESS' | 'APPROVED' | 'REJECTED' | 'CANCELLED';
  currentStepOrder?: number;
  requesterId: number;
  requesterName?: string;
  requesterDepartment?: string;
  requestDate: string;
  requestComment?: string;
  completedDate?: string;
  finalApproverId?: number;
  finalApproverName?: string;
  stepInstances?: ApprovalStepInstance[];
}

export interface ApprovalStepInstance {
  stepInstanceId: number;
  stepOrder: number;
  stepName: string;
  stepType: string;
  approverId: number;
  approverName?: string;
  approverDepartment?: string;
  approverPosition?: string;
  delegatedToId?: number;
  delegatedToName?: string;
  delegationReason?: string;
  stepStatus: 'PENDING' | 'IN_PROGRESS' | 'APPROVED' | 'REJECTED' | 'SKIPPED' | 'TIMEOUT';
  approvalDate?: string;
  approvalComment?: string;
  rejectionReason?: string;
  assignedDate: string;
  dueDate?: string;
}

export interface ApprovalDelegation {
  delegationId: number;
  tenantId: string;
  delegatorId: number;
  delegatorName?: string;
  delegateId: number;
  delegateName?: string;
  delegationType: 'FULL' | 'PARTIAL';
  documentTypes?: string;
  startDate: string;
  endDate: string;
  isActive: boolean;
  delegationReason?: string;
}

export interface CreateApprovalRequest {
  tenantId: string;
  documentType: string;
  documentId: number;
  documentNo?: string;
  documentTitle?: string;
  documentAmount?: number;
  requesterId: number;
  requesterName?: string;
  requesterDepartment?: string;
  requestComment?: string;
}

export interface ApprovalActionRequest {
  approverId: number;
  comment?: string;
  reason?: string;
}

export interface ApprovalStatistics {
  pending: number;
  inProgress: number;
  approved: number;
  rejected: number;
  total: number;
  active: number;
  approvalRate: number;
}

// ==================== Approval Service ====================

class ApprovalServiceClass {
  private baseURL = `${API_BASE_URL}/approvals`;

  // ==================== Template APIs ====================

  /**
   * Get all templates
   */
  async getAllTemplates(tenantId: string): Promise<ApprovalLineTemplate[]> {
    const response = await axios.get(`${this.baseURL}/templates`, {
      params: { tenantId }
    });
    return response.data.data;
  }

  /**
   * Get templates by document type
   */
  async getTemplatesByDocumentType(tenantId: string, documentType: string): Promise<ApprovalLineTemplate[]> {
    const response = await axios.get(`${this.baseURL}/templates/document-type/${documentType}`, {
      params: { tenantId }
    });
    return response.data.data;
  }

  /**
   * Create template
   */
  async createTemplate(template: Partial<ApprovalLineTemplate>): Promise<ApprovalLineTemplate> {
    const response = await axios.post(`${this.baseURL}/templates`, template);
    return response.data.data;
  }

  /**
   * Update template
   */
  async updateTemplate(templateId: number, template: Partial<ApprovalLineTemplate>): Promise<ApprovalLineTemplate> {
    const response = await axios.put(`${this.baseURL}/templates/${templateId}`, template);
    return response.data.data;
  }

  // ==================== Instance APIs ====================

  /**
   * Create approval instance
   */
  async createApprovalInstance(request: CreateApprovalRequest): Promise<ApprovalInstance> {
    const response = await axios.post(`${this.baseURL}/instances`, request);
    return response.data.data;
  }

  /**
   * Approve step
   */
  async approveStep(instanceId: number, stepId: number, action: ApprovalActionRequest): Promise<void> {
    await axios.post(`${this.baseURL}/instances/${instanceId}/steps/${stepId}/approve`, action);
  }

  /**
   * Reject step
   */
  async rejectStep(instanceId: number, stepId: number, action: ApprovalActionRequest): Promise<void> {
    await axios.post(`${this.baseURL}/instances/${instanceId}/steps/${stepId}/reject`, action);
  }

  /**
   * Get pending approvals for user
   */
  async getPendingApprovals(tenantId: string, userId: number): Promise<ApprovalInstance[]> {
    const response = await axios.get(`${this.baseURL}/pending`, {
      params: { tenantId, userId }
    });
    return response.data.data;
  }

  /**
   * Get approval statistics
   */
  async getStatistics(tenantId: string): Promise<ApprovalStatistics> {
    const response = await axios.get(`${this.baseURL}/statistics`, {
      params: { tenantId }
    });
    return response.data.data;
  }

  // ==================== Delegation APIs ====================

  /**
   * Create delegation
   */
  async createDelegation(delegation: Partial<ApprovalDelegation>): Promise<ApprovalDelegation> {
    const response = await axios.post(`${this.baseURL}/delegations`, delegation);
    return response.data.data;
  }

  /**
   * Get current delegations
   */
  async getCurrentDelegations(tenantId: string): Promise<ApprovalDelegation[]> {
    const response = await axios.get(`${this.baseURL}/delegations/current`, {
      params: { tenantId }
    });
    return response.data.data;
  }
}

// ==================== Helper Functions ====================

/**
 * Get approval status label
 */
export function getApprovalStatusLabel(status: string): string {
  const labels: Record<string, string> = {
    'PENDING': '대기',
    'IN_PROGRESS': '진행중',
    'APPROVED': '승인',
    'REJECTED': '반려',
    'CANCELLED': '취소',
    'SKIPPED': '건너뜀',
    'TIMEOUT': '시간초과'
  };
  return labels[status] || status;
}

/**
 * Get approval status color
 */
export function getApprovalStatusColor(status: string): 'default' | 'primary' | 'success' | 'error' | 'warning' {
  const colors: Record<string, 'default' | 'primary' | 'success' | 'error' | 'warning'> = {
    'PENDING': 'default',
    'IN_PROGRESS': 'primary',
    'APPROVED': 'success',
    'REJECTED': 'error',
    'CANCELLED': 'default',
    'SKIPPED': 'warning',
    'TIMEOUT': 'error'
  };
  return colors[status] || 'default';
}

/**
 * Get step type label
 */
export function getStepTypeLabel(type: string): string {
  const labels: Record<string, string> = {
    'APPROVAL': '승인',
    'REVIEW': '검토',
    'NOTIFICATION': '통보'
  };
  return labels[type] || type;
}

/**
 * Get approver type label
 */
export function getApproverTypeLabel(type: string): string {
  const labels: Record<string, string> = {
    'ROLE': '역할',
    'POSITION': '직급',
    'DEPARTMENT': '부서',
    'USER': '특정 사용자'
  };
  return labels[type] || type;
}

/**
 * Get approval type label
 */
export function getApprovalTypeLabel(type: string): string {
  const labels: Record<string, string> = {
    'SEQUENTIAL': '순차',
    'PARALLEL': '병렬',
    'HYBRID': '혼합'
  };
  return labels[type] || type;
}

/**
 * Get document type label
 */
export function getDocumentTypeLabel(type: string): string {
  const labels: Record<string, string> = {
    'PURCHASE_ORDER': '구매 주문',
    'WORK_ORDER': '작업 지시',
    'SALES_ORDER': '판매 주문',
    'HOLIDAY_REQUEST': '휴가 신청',
    'QUALITY_INSPECTION': '품질 검사'
  };
  return labels[type] || type;
}

/**
 * Format date time
 */
export function formatDateTime(dateStr?: string): string {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
}

/**
 * Calculate remaining hours
 */
export function calculateRemainingHours(dueDate?: string): number {
  if (!dueDate) return 0;
  const now = new Date();
  const due = new Date(dueDate);
  const diff = due.getTime() - now.getTime();
  return Math.max(0, Math.floor(diff / (1000 * 60 * 60)));
}

// Export service instance
export const approvalService = new ApprovalServiceClass();
