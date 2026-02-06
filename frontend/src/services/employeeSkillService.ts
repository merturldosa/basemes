import apiClient from './api';

/**
 * Employee Skill Entity Interface
 */
export interface EmployeeSkill {
  employeeSkillId: number;
  tenantId: string;
  tenantName: string;
  employeeId: number;
  employeeNo: string;
  employeeName: string;
  skillId: number;
  skillCode: string;
  skillName: string;
  skillCategory: string;
  skillLevel?: string; // BEGINNER, INTERMEDIATE, ADVANCED, EXPERT, MASTER
  skillLevelNumeric?: number; // 1~5
  acquisitionDate?: string;
  expiryDate?: string;
  lastAssessmentDate?: string;
  nextAssessmentDate?: string;
  certificationNo?: string;
  issuingAuthority?: string;
  assessorName?: string;
  assessmentScore?: number;
  assessmentResult?: string; // PASS, FAIL, CONDITIONAL
  remarks?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

/**
 * Employee Skill Create Request Interface
 */
export interface EmployeeSkillCreateRequest {
  employeeId: number;
  skillId: number;
  skillLevel?: string;
  skillLevelNumeric?: number;
  acquisitionDate?: string;
  expiryDate?: string;
  lastAssessmentDate?: string;
  nextAssessmentDate?: string;
  certificationNo?: string;
  issuingAuthority?: string;
  assessorName?: string;
  assessmentScore?: number;
  assessmentResult?: string;
  remarks?: string;
}

/**
 * Employee Skill Update Request Interface
 */
export interface EmployeeSkillUpdateRequest {
  skillLevel?: string;
  skillLevelNumeric?: number;
  acquisitionDate?: string;
  expiryDate?: string;
  lastAssessmentDate?: string;
  nextAssessmentDate?: string;
  certificationNo?: string;
  issuingAuthority?: string;
  assessorName?: string;
  assessmentScore?: number;
  assessmentResult?: string;
  remarks?: string;
}

/**
 * Employee Skill Service
 */
const employeeSkillService = {
  /**
   * Get all employee skills
   */
  getAll: async (): Promise<EmployeeSkill[]> => {
    const response = await apiClient.get<EmployeeSkill[]>('/employee-skills');
    return response.data;
  },

  /**
   * Get employee skill by ID
   */
  getById: async (employeeSkillId: number): Promise<EmployeeSkill> => {
    const response = await apiClient.get<EmployeeSkill>(`/api/employee-skills/${employeeSkillId}`);
    return response.data;
  },

  /**
   * Get skills by employee
   */
  getByEmployee: async (employeeId: number): Promise<EmployeeSkill[]> => {
    const response = await apiClient.get<EmployeeSkill[]>(`/api/employee-skills/employee/${employeeId}`);
    return response.data;
  },

  /**
   * Get employees by skill
   */
  getBySkill: async (skillId: number): Promise<EmployeeSkill[]> => {
    const response = await apiClient.get<EmployeeSkill[]>(`/api/employee-skills/skill/${skillId}`);
    return response.data;
  },

  /**
   * Get employees by skill and minimum level
   */
  getBySkillAndLevel: async (skillId: number, minLevel: number): Promise<EmployeeSkill[]> => {
    const response = await apiClient.get<EmployeeSkill[]>(
      `/api/employee-skills/skill/${skillId}/level/${minLevel}`
    );
    return response.data;
  },

  /**
   * Get expiring certifications
   */
  getExpiringCertifications: async (expiryDate: string): Promise<EmployeeSkill[]> => {
    const response = await apiClient.get<EmployeeSkill[]>('/employee-skills/expiring-certifications', {
      params: { expiryDate },
    });
    return response.data;
  },

  /**
   * Get pending assessments
   */
  getPendingAssessments: async (assessmentDate: string): Promise<EmployeeSkill[]> => {
    const response = await apiClient.get<EmployeeSkill[]>('/employee-skills/pending-assessments', {
      params: { assessmentDate },
    });
    return response.data;
  },

  /**
   * Create employee skill
   */
  create: async (data: EmployeeSkillCreateRequest): Promise<EmployeeSkill> => {
    const response = await apiClient.post<EmployeeSkill>('/employee-skills', data);
    return response.data;
  },

  /**
   * Update employee skill
   */
  update: async (employeeSkillId: number, data: EmployeeSkillUpdateRequest): Promise<EmployeeSkill> => {
    const response = await apiClient.put<EmployeeSkill>(`/api/employee-skills/${employeeSkillId}`, data);
    return response.data;
  },

  /**
   * Delete employee skill
   */
  delete: async (employeeSkillId: number): Promise<void> => {
    await apiClient.delete(`/api/employee-skills/${employeeSkillId}`);
  },
};

export default employeeSkillService;
