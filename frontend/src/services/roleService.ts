/**
 * Role Service
 * 역할 관리 API 서비스
 * @author Moon Myung-seop
 */

import apiClient from './api';
import {
  Role,
  RoleCreateRequest,
  RoleUpdateRequest,
  Permission,
} from '@/types';

class RoleService {
  private basePath = '/roles';

  /**
   * 역할 목록 조회
   */
  async getRoles(): Promise<Role[]> {
    const response = await apiClient.get<Role[]>(this.basePath);
    return response.data;
  }

  /**
   * 활성 역할 목록 조회
   */
  async getActiveRoles(): Promise<Role[]> {
    const response = await apiClient.get<Role[]>(`${this.basePath}/active`);
    return response.data;
  }

  /**
   * 역할 상세 조회
   */
  async getRole(roleId: number): Promise<Role> {
    const response = await apiClient.get<Role>(`${this.basePath}/${roleId}`);
    return response.data;
  }

  /**
   * 역할 생성
   */
  async createRole(data: RoleCreateRequest): Promise<Role> {
    const response = await apiClient.post<Role>(this.basePath, data);
    return response.data;
  }

  /**
   * 역할 수정
   */
  async updateRole(roleId: number, data: RoleUpdateRequest): Promise<Role> {
    const response = await apiClient.put<Role>(`${this.basePath}/${roleId}`, data);
    return response.data;
  }

  /**
   * 역할 삭제
   */
  async deleteRole(roleId: number): Promise<void> {
    await apiClient.delete(`${this.basePath}/${roleId}`);
  }

  /**
   * 역할의 권한 목록 조회
   */
  async getRolePermissions(roleId: number): Promise<Permission[]> {
    const response = await apiClient.get<Permission[]>(`${this.basePath}/${roleId}/permissions`);
    return response.data;
  }

  /**
   * 역할에 권한 할당
   */
  async assignPermission(roleId: number, permissionId: number): Promise<void> {
    await apiClient.post(`${this.basePath}/${roleId}/permissions`, { permissionId });
  }

  /**
   * 역할에서 권한 제거
   */
  async removePermission(roleId: number, permissionId: number): Promise<void> {
    await apiClient.delete(`${this.basePath}/${roleId}/permissions/${permissionId}`);
  }

  /**
   * 역할 활성화
   */
  async activateRole(roleId: number): Promise<Role> {
    const response = await apiClient.patch<Role>(`${this.basePath}/${roleId}/activate`);
    return response.data;
  }

  /**
   * 역할 비활성화
   */
  async deactivateRole(roleId: number): Promise<Role> {
    const response = await apiClient.patch<Role>(`${this.basePath}/${roleId}/deactivate`);
    return response.data;
  }
}

export const roleService = new RoleService();
export default roleService;
