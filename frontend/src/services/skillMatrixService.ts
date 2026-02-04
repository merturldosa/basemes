import apiClient from './api';

/**
 * Skill Matrix Entity Interface
 */
export interface SkillMatrix {
  skillId: number;
  tenantId: string;
  tenantName: string;
  skillCode: string;
  skillName: string;
  skillCategory: string; // TECHNICAL, OPERATIONAL, QUALITY, SAFETY, MANAGEMENT
  skillLevelDefinition?: string;
  description?: string;
  certificationRequired: boolean;
  certificationName?: string;
  validityPeriodMonths?: number;
  remarks?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

/**
 * Skill Matrix Create Request Interface
 */
export interface SkillMatrixCreateRequest {
  skillCode: string;
  skillName: string;
  skillCategory: string;
  skillLevelDefinition?: string;
  description?: string;
  certificationRequired: boolean;
  certificationName?: string;
  validityPeriodMonths?: number;
  remarks?: string;
}

/**
 * Skill Matrix Update Request Interface
 */
export interface SkillMatrixUpdateRequest {
  skillName?: string;
  skillCategory?: string;
  skillLevelDefinition?: string;
  description?: string;
  certificationRequired?: boolean;
  certificationName?: string;
  validityPeriodMonths?: number;
  remarks?: string;
}

/**
 * Skill Matrix Service
 */
const skillMatrixService = {
  /**
   * Get all skills
   */
  getAll: async (): Promise<SkillMatrix[]> => {
    const response = await apiClient.get<SkillMatrix[]>('/api/skill-matrix');
    return response.data;
  },

  /**
   * Get active skills only
   */
  getActive: async (): Promise<SkillMatrix[]> => {
    const response = await apiClient.get<SkillMatrix[]>('/api/skill-matrix/active');
    return response.data;
  },

  /**
   * Get skill by ID
   */
  getById: async (skillId: number): Promise<SkillMatrix> => {
    const response = await apiClient.get<SkillMatrix>(`/api/skill-matrix/${skillId}`);
    return response.data;
  },

  /**
   * Get skills by category
   */
  getByCategory: async (category: string): Promise<SkillMatrix[]> => {
    const response = await apiClient.get<SkillMatrix[]>(`/api/skill-matrix/category/${category}`);
    return response.data;
  },

  /**
   * Get skills requiring certification
   */
  getRequiringCertification: async (): Promise<SkillMatrix[]> => {
    const response = await apiClient.get<SkillMatrix[]>('/api/skill-matrix/certification-required');
    return response.data;
  },

  /**
   * Create new skill
   */
  create: async (data: SkillMatrixCreateRequest): Promise<SkillMatrix> => {
    const response = await apiClient.post<SkillMatrix>('/api/skill-matrix', data);
    return response.data;
  },

  /**
   * Update skill
   */
  update: async (skillId: number, data: SkillMatrixUpdateRequest): Promise<SkillMatrix> => {
    const response = await apiClient.put<SkillMatrix>(`/api/skill-matrix/${skillId}`, data);
    return response.data;
  },

  /**
   * Activate skill
   */
  activate: async (skillId: number): Promise<SkillMatrix> => {
    const response = await apiClient.patch<SkillMatrix>(`/api/skill-matrix/${skillId}/activate`);
    return response.data;
  },

  /**
   * Deactivate skill
   */
  deactivate: async (skillId: number): Promise<SkillMatrix> => {
    const response = await apiClient.patch<SkillMatrix>(`/api/skill-matrix/${skillId}/deactivate`);
    return response.data;
  },

  /**
   * Delete skill
   */
  delete: async (skillId: number): Promise<void> => {
    await apiClient.delete(`/api/skill-matrix/${skillId}`);
  },
};

export default skillMatrixService;
