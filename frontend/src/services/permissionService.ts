/**
 * Permission Service
 * 권한 관리 API 서비스
 * @author Moon Myung-seop
 */

import apiClient from './api';

export interface Permission {
  permissionId: number;
  permissionCode: string;
  permissionName: string;
  module: string;
  description?: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface PermissionCreateRequest {
  permissionCode: string;
  permissionName: string;
  module: string;
  description?: string;
}

export interface PermissionUpdateRequest {
  permissionName?: string;
  module?: string;
  description?: string;
}

class PermissionService {
  private basePath = '/permissions';

  /**
   * 전체 권한 목록 조회
   */
  async getPermissions(): Promise<Permission[]> {
    const response = await apiClient.get<Permission[]>(this.basePath);
    return response.data;
  }

  /**
   * 활성 권한 목록 조회
   */
  async getActivePermissions(): Promise<Permission[]> {
    const response = await apiClient.get<Permission[]>(`${this.basePath}/active`);
    return response.data;
  }

  /**
   * 모듈별 권한 목록 조회
   */
  async getPermissionsByModule(module: string): Promise<Permission[]> {
    const response = await apiClient.get<Permission[]>(`${this.basePath}/module/${module}`);
    return response.data;
  }

  /**
   * 권한 상세 조회
   */
  async getPermission(id: number): Promise<Permission> {
    const response = await apiClient.get<Permission>(`${this.basePath}/${id}`);
    return response.data;
  }

  /**
   * 권한 생성
   */
  async createPermission(data: PermissionCreateRequest): Promise<Permission> {
    const response = await apiClient.post<Permission>(this.basePath, data);
    return response.data;
  }

  /**
   * 권한 수정
   */
  async updatePermission(id: number, data: PermissionUpdateRequest): Promise<Permission> {
    const response = await apiClient.put<Permission>(`${this.basePath}/${id}`, data);
    return response.data;
  }

  /**
   * 권한 활성화
   */
  async activatePermission(id: number): Promise<Permission> {
    const response = await apiClient.put<Permission>(`${this.basePath}/${id}/activate`);
    return response.data;
  }

  /**
   * 권한 비활성화
   */
  async deactivatePermission(id: number): Promise<Permission> {
    const response = await apiClient.put<Permission>(`${this.basePath}/${id}/deactivate`);
    return response.data;
  }

  /**
   * 권한 삭제
   */
  async deletePermission(id: number): Promise<void> {
    await apiClient.delete(`${this.basePath}/${id}`);
  }
}

export const permissionService = new PermissionService();
export default permissionService;
