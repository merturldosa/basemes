import api from './api';

export interface CommonCodeGroup {
  codeGroupId: number;
  codeGroup: string;
  codeGroupName: string;
  description?: string;
  isSystem: boolean;
  displayOrder: number;
  isActive: boolean;
  details?: CommonCodeDetail[];
  createdAt?: string;
  updatedAt?: string;
}

export interface CommonCodeDetail {
  codeDetailId: number;
  code: string;
  codeName: string;
  description?: string;
  displayOrder: number;
  isDefault: boolean;
  isActive: boolean;
  value1?: string;
  value2?: string;
  value3?: string;
  value4?: string;
  value5?: string;
  colorCode?: string;
  iconName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CommonCodeGroupCreateRequest {
  codeGroup: string;
  codeGroupName: string;
  description?: string;
  isSystem?: boolean;
  displayOrder?: number;
  isActive?: boolean;
}

export interface CommonCodeDetailCreateRequest {
  code: string;
  codeName: string;
  description?: string;
  displayOrder?: number;
  isDefault?: boolean;
  isActive?: boolean;
  value1?: string;
  value2?: string;
  value3?: string;
  value4?: string;
  value5?: string;
  colorCode?: string;
  iconName?: string;
}

class CommonCodeService {
  // ==================== Code Group APIs ====================

  async getCodeGroups(): Promise<CommonCodeGroup[]> {
    const response = await api.get('/common-codes/groups');
    return response.data.data;
  }

  async getActiveCodeGroups(): Promise<CommonCodeGroup[]> {
    const response = await api.get('/common-codes/groups/active');
    return response.data.data;
  }

  async getCodeGroupById(id: number): Promise<CommonCodeGroup> {
    const response = await api.get(`/common-codes/groups/${id}`);
    return response.data.data;
  }

  async getCodeGroupByCode(codeGroup: string): Promise<CommonCodeGroup> {
    const response = await api.get(`/common-codes/groups/by-code/${codeGroup}`);
    return response.data.data;
  }

  async createCodeGroup(data: CommonCodeGroupCreateRequest): Promise<CommonCodeGroup> {
    const response = await api.post('/common-codes/groups', data);
    return response.data.data;
  }

  async updateCodeGroup(id: number, data: Partial<CommonCodeGroupCreateRequest>): Promise<CommonCodeGroup> {
    const response = await api.put(`/common-codes/groups/${id}`, data);
    return response.data.data;
  }

  async deleteCodeGroup(id: number): Promise<void> {
    await api.delete(`/common-codes/groups/${id}`);
  }

  // ==================== Code Detail APIs ====================

  async getCodeDetails(groupId: number): Promise<CommonCodeDetail[]> {
    const response = await api.get(`/common-codes/groups/${groupId}/details`);
    return response.data.data;
  }

  async getActiveCodeDetails(groupId: number): Promise<CommonCodeDetail[]> {
    const response = await api.get(`/common-codes/groups/${groupId}/details/active`);
    return response.data.data;
  }

  async getCodesByGroupCode(codeGroup: string): Promise<CommonCodeDetail[]> {
    const response = await api.get(`/common-codes/${codeGroup}/codes`);
    return response.data.data;
  }

  async getCodeDetailById(id: number): Promise<CommonCodeDetail> {
    const response = await api.get(`/common-codes/details/${id}`);
    return response.data.data;
  }

  async createCodeDetail(groupId: number, data: CommonCodeDetailCreateRequest): Promise<CommonCodeDetail> {
    const response = await api.post(`/common-codes/groups/${groupId}/details`, data);
    return response.data.data;
  }

  async updateCodeDetail(id: number, data: Partial<CommonCodeDetailCreateRequest>): Promise<CommonCodeDetail> {
    const response = await api.put(`/common-codes/details/${id}`, data);
    return response.data.data;
  }

  async deleteCodeDetail(id: number): Promise<void> {
    await api.delete(`/common-codes/details/${id}`);
  }

  // ==================== Utility APIs ====================

  async getAllCodesAsMap(): Promise<Record<string, CommonCodeDetail[]>> {
    const response = await api.get('/common-codes/all');
    return response.data.data;
  }

  async getCodeName(codeGroup: string, code: string): Promise<string> {
    const response = await api.get(`/common-codes/${codeGroup}/${code}/name`);
    return response.data.data;
  }

  async validateCode(codeGroup: string, code: string): Promise<boolean> {
    const response = await api.get(`/common-codes/${codeGroup}/${code}/validate`);
    return response.data.data;
  }

  // ==================== Helper Methods ====================

  /**
   * Get code options for select/dropdown (client-side helper)
   */
  async getCodeOptions(codeGroup: string): Promise<{ value: string; label: string; color?: string }[]> {
    const codes = await this.getCodesByGroupCode(codeGroup);
    return codes.map(code => ({
      value: code.code,
      label: code.codeName,
      color: code.colorCode,
    }));
  }

  /**
   * Get code name by code (client-side cache)
   */
  private codeNameCache: Record<string, string> = {};

  async getCodeNameCached(codeGroup: string, code: string): Promise<string> {
    const key = `${codeGroup}.${code}`;
    if (this.codeNameCache[key]) {
      return this.codeNameCache[key];
    }

    const name = await this.getCodeName(codeGroup, code);
    this.codeNameCache[key] = name;
    return name;
  }

  /**
   * Clear cache
   */
  clearCache(): void {
    this.codeNameCache = {};
  }
}

export default new CommonCodeService();
