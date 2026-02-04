import api from './api';

// ==================== Interfaces ====================

export interface DocumentTemplate {
  templateId: number;
  templateCode: string;
  templateName: string;
  description?: string;
  templateType: string; // SOP, CHECKLIST, INSPECTION_SHEET, REPORT
  category?: string; // PRODUCTION, WAREHOUSE, QUALITY, FACILITY, COMMON
  fileName?: string;
  filePath?: string;
  fileType?: string; // EXCEL, WORD, PDF, HTML
  fileSize?: number;
  templateContent?: string;
  version: string;
  isLatest: boolean;
  displayOrder: number;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface SOP {
  sopId: number;
  sopCode: string;
  sopName: string;
  description?: string;
  sopType: string; // PRODUCTION, WAREHOUSE, QUALITY, FACILITY, SAFETY, MAINTENANCE
  category?: string;
  targetProcess?: string;
  template?: DocumentTemplate;
  version: string;
  revisionDate?: string;
  effectiveDate?: string;
  reviewDate?: string;
  nextReviewDate?: string;
  approvalStatus: string; // DRAFT, PENDING, APPROVED, REJECTED, OBSOLETE
  approvedBy?: number;
  approvedAt?: string;
  documentUrl?: string;
  attachments?: string; // JSON
  requiredRole?: string;
  restricted: boolean;
  displayOrder: number;
  isActive: boolean;
  steps?: SOPStep[];
  createdAt?: string;
  updatedAt?: string;
}

export interface SOPStep {
  sopStepId: number;
  stepNumber: number;
  stepTitle: string;
  stepDescription?: string;
  stepType?: string; // PREPARATION, EXECUTION, INSPECTION, DOCUMENTATION, SAFETY
  estimatedDuration?: number; // Minutes
  detailedInstruction?: string;
  cautionNotes?: string;
  qualityPoints?: string;
  imageUrls?: string; // JSON array
  videoUrl?: string;
  checklistItems?: string; // JSON array
  prerequisiteStepId?: number;
  isCritical: boolean;
  isMandatory: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface SOPExecution {
  executionId: number;
  executionNo: string;
  executionDate: string;
  executorId: number;
  executorName: string;
  referenceType?: string; // WORK_ORDER, INSPECTION, MAINTENANCE
  referenceId?: number;
  referenceNo?: string;
  executionStatus: string; // IN_PROGRESS, COMPLETED, FAILED, CANCELLED
  startTime?: string;
  endTime?: string;
  duration?: number; // Minutes
  completionRate: number; // Percentage
  stepsCompleted: number;
  stepsTotal: number;
  reviewerId?: number;
  reviewStatus?: string; // PENDING, APPROVED, REJECTED
  reviewComments?: string;
  reviewedAt?: string;
  remarks?: string;
  executionSteps?: SOPExecutionStep[];
  createdAt?: string;
  updatedAt?: string;
}

export interface SOPExecutionStep {
  executionStepId: number;
  stepNumber: number;
  stepStatus: string; // PENDING, IN_PROGRESS, COMPLETED, SKIPPED, FAILED
  startedAt?: string;
  completedAt?: string;
  duration?: number; // Minutes
  resultValue?: string;
  checklistResults?: string; // JSON
  photos?: string; // JSON array
  signature?: string;
  remarks?: string;
  sopStep?: SOPStep;
}

// ==================== Request DTOs ====================

export interface DocumentTemplateCreateRequest {
  templateCode: string;
  templateName: string;
  description?: string;
  templateType: string;
  category?: string;
  fileName?: string;
  filePath?: string;
  fileType?: string;
  fileSize?: number;
  templateContent?: string;
  version?: string;
  displayOrder?: number;
  isActive?: boolean;
}

export interface SOPCreateRequest {
  sopCode: string;
  sopName: string;
  description?: string;
  sopType: string;
  category?: string;
  targetProcess?: string;
  templateId?: number;
  version?: string;
  documentUrl?: string;
  attachments?: string;
  requiredRole?: string;
  restricted?: boolean;
  displayOrder?: number;
}

export interface SOPStepCreateRequest {
  stepTitle: string;
  stepDescription?: string;
  stepType?: string;
  estimatedDuration?: number;
  detailedInstruction?: string;
  cautionNotes?: string;
  qualityPoints?: string;
  imageUrls?: string;
  videoUrl?: string;
  checklistItems?: string;
  prerequisiteStepId?: number;
  isCritical?: boolean;
  isMandatory?: boolean;
}

export interface ExecutionStartRequest {
  executorId: number;
  referenceType?: string;
  referenceId?: number;
  referenceNo?: string;
}

export interface StepCompleteRequest {
  resultValue?: string;
  checklistResults?: string;
}

// ==================== Document Template Service ====================

class DocumentTemplateService {
  async getTemplates(): Promise<DocumentTemplate[]> {
    const response = await api.get('/document-templates');
    return response.data.data;
  }

  async getActiveTemplates(): Promise<DocumentTemplate[]> {
    const response = await api.get('/document-templates/active');
    return response.data.data;
  }

  async getTemplateById(id: number): Promise<DocumentTemplate> {
    const response = await api.get(`/document-templates/${id}`);
    return response.data.data;
  }

  async getTemplatesByType(templateType: string): Promise<DocumentTemplate[]> {
    const response = await api.get(`/document-templates/type/${templateType}`);
    return response.data.data;
  }

  async getTemplatesByCategory(category: string): Promise<DocumentTemplate[]> {
    const response = await api.get(`/document-templates/category/${category}`);
    return response.data.data;
  }

  async getLatestTemplateByCode(templateCode: string): Promise<DocumentTemplate> {
    const response = await api.get(`/document-templates/by-code/${templateCode}`);
    return response.data.data;
  }

  async getAllVersions(templateCode: string): Promise<DocumentTemplate[]> {
    const response = await api.get(`/document-templates/versions/${templateCode}`);
    return response.data.data;
  }

  async createTemplate(data: DocumentTemplateCreateRequest): Promise<DocumentTemplate> {
    const response = await api.post('/document-templates', data);
    return response.data.data;
  }

  async updateTemplate(id: number, data: Partial<DocumentTemplateCreateRequest>): Promise<DocumentTemplate> {
    const response = await api.put(`/document-templates/${id}`, data);
    return response.data.data;
  }

  async createNewVersion(templateCode: string, newVersion: string): Promise<DocumentTemplate> {
    const response = await api.post(`/document-templates/${templateCode}/new-version`, null, {
      params: { newVersion }
    });
    return response.data.data;
  }

  async deleteTemplate(id: number): Promise<void> {
    await api.delete(`/document-templates/${id}`);
  }

  async activateTemplate(id: number): Promise<DocumentTemplate> {
    const response = await api.post(`/document-templates/${id}/activate`);
    return response.data.data;
  }

  async deactivateTemplate(id: number): Promise<DocumentTemplate> {
    const response = await api.post(`/document-templates/${id}/deactivate`);
    return response.data.data;
  }
}

// ==================== SOP Service ====================

class SOPService {
  // SOP CRUD
  async getSOPs(): Promise<SOP[]> {
    const response = await api.get('/sops');
    return response.data.data;
  }

  async getActiveSOPs(): Promise<SOP[]> {
    const response = await api.get('/sops/active');
    return response.data.data;
  }

  async getApprovedSOPs(): Promise<SOP[]> {
    const response = await api.get('/sops/approved');
    return response.data.data;
  }

  async getSOPById(id: number): Promise<SOP> {
    const response = await api.get(`/sops/${id}`);
    return response.data.data;
  }

  async getSOPsByType(sopType: string): Promise<SOP[]> {
    const response = await api.get(`/sops/type/${sopType}`);
    return response.data.data;
  }

  async getSOPsByCategory(category: string): Promise<SOP[]> {
    const response = await api.get(`/sops/category/${category}`);
    return response.data.data;
  }

  async getSOPsByProcess(targetProcess: string): Promise<SOP[]> {
    const response = await api.get(`/sops/process/${targetProcess}`);
    return response.data.data;
  }

  async getSOPsRequiringReview(): Promise<SOP[]> {
    const response = await api.get('/sops/requiring-review');
    return response.data.data;
  }

  async getSOPsPendingApproval(): Promise<SOP[]> {
    const response = await api.get('/sops/pending-approval');
    return response.data.data;
  }

  async createSOP(data: SOPCreateRequest): Promise<SOP> {
    const response = await api.post('/sops', data);
    return response.data.data;
  }

  async updateSOP(id: number, data: Partial<SOPCreateRequest>): Promise<SOP> {
    const response = await api.put(`/sops/${id}`, data);
    return response.data.data;
  }

  async deleteSOP(id: number): Promise<void> {
    await api.delete(`/sops/${id}`);
  }

  // SOP Approval Workflow
  async submitForApproval(sopId: number): Promise<SOP> {
    const response = await api.post(`/sops/${sopId}/submit`);
    return response.data.data;
  }

  async approveSOP(sopId: number, approverId: number): Promise<SOP> {
    const response = await api.post(`/sops/${sopId}/approve`, null, {
      params: { approverId }
    });
    return response.data.data;
  }

  async rejectSOP(sopId: number): Promise<SOP> {
    const response = await api.post(`/sops/${sopId}/reject`);
    return response.data.data;
  }

  async markObsolete(sopId: number): Promise<SOP> {
    const response = await api.post(`/sops/${sopId}/obsolete`);
    return response.data.data;
  }

  // SOP Steps
  async addStep(sopId: number, data: SOPStepCreateRequest): Promise<SOPStep> {
    const response = await api.post(`/sops/${sopId}/steps`, data);
    return response.data.data;
  }

  async updateStep(stepId: number, data: Partial<SOPStepCreateRequest>): Promise<SOPStep> {
    const response = await api.put(`/sops/steps/${stepId}`, data);
    return response.data.data;
  }

  async deleteStep(stepId: number): Promise<void> {
    await api.delete(`/sops/steps/${stepId}`);
  }

  // SOP Execution
  async startExecution(sopId: number, data: ExecutionStartRequest): Promise<SOPExecution> {
    const response = await api.post(`/sops/${sopId}/executions`, data);
    return response.data.data;
  }

  async startExecutionStep(executionId: number, stepId: number): Promise<SOPExecutionStep> {
    const response = await api.post(`/sops/executions/${executionId}/steps/${stepId}/start`);
    return response.data.data;
  }

  async completeExecutionStep(executionStepId: number, data: StepCompleteRequest): Promise<SOPExecutionStep> {
    const response = await api.post(`/sops/executions/steps/${executionStepId}/complete`, data);
    return response.data.data;
  }

  async completeExecution(executionId: number): Promise<SOPExecution> {
    const response = await api.post(`/sops/executions/${executionId}/complete`);
    return response.data.data;
  }

  async cancelExecution(executionId: number, reason: string): Promise<SOPExecution> {
    const response = await api.post(`/sops/executions/${executionId}/cancel`, null, {
      params: { reason }
    });
    return response.data.data;
  }

  // Helper methods
  getStatusColor(status: string): string {
    const colors: Record<string, string> = {
      DRAFT: '#9E9E9E',
      PENDING: '#FFC107',
      APPROVED: '#4CAF50',
      REJECTED: '#F44336',
      OBSOLETE: '#607D8B',
      IN_PROGRESS: '#2196F3',
      COMPLETED: '#4CAF50',
      FAILED: '#F44336',
      CANCELLED: '#9E9E9E'
    };
    return colors[status] || '#9E9E9E';
  }

  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      DRAFT: '임시저장',
      PENDING: '승인대기',
      APPROVED: '승인완료',
      REJECTED: '반려',
      OBSOLETE: '폐기',
      IN_PROGRESS: '진행중',
      COMPLETED: '완료',
      FAILED: '실패',
      CANCELLED: '취소'
    };
    return labels[status] || status;
  }

  getTypeLabel(type: string): string {
    const labels: Record<string, string> = {
      PRODUCTION: '생산',
      WAREHOUSE: '창고',
      QUALITY: '품질',
      FACILITY: '설비',
      SAFETY: '안전',
      MAINTENANCE: '유지보수',
      SOP: 'SOP',
      CHECKLIST: '체크리스트',
      INSPECTION_SHEET: '검사표',
      REPORT: '보고서',
      PREPARATION: '준비',
      EXECUTION: '실행',
      INSPECTION: '검사',
      DOCUMENTATION: '문서화'
    };
    return labels[type] || type;
  }
}

export const documentTemplateService = new DocumentTemplateService();
export const sopService = new SOPService();

export default sopService;
