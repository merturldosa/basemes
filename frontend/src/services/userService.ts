/**
 * User Service
 * 사용자 관리 API 서비스
 * @author Moon Myung-seop
 */

import apiClient from './api';
import {
  User,
  UserCreateRequest,
  UserUpdateRequest,
  ChangePasswordRequest,
  PageResponse,
} from '@/types';

class UserService {
  private basePath = '/users';

  /**
   * 사용자 목록 조회 (페이징)
   */
  async getUsers(params?: {
    page?: number;
    size?: number;
    sortBy?: string;
    sortDirection?: 'ASC' | 'DESC';
    status?: string;
    search?: string;
  }): Promise<PageResponse<User>> {
    const response = await apiClient.get<PageResponse<User>>(this.basePath, params);
    return response.data;
  }

  /**
   * 사용자 단건 조회
   */
  async getUserById(userId: number): Promise<User> {
    const response = await apiClient.get<User>(`${this.basePath}/${userId}`);
    return response.data;
  }

  /**
   * 사용자 생성
   */
  async createUser(data: UserCreateRequest): Promise<User> {
    const response = await apiClient.post<User>(this.basePath, data);
    return response.data;
  }

  /**
   * 사용자 수정
   */
  async updateUser(userId: number, data: UserUpdateRequest): Promise<User> {
    const response = await apiClient.put<User>(`${this.basePath}/${userId}`, data);
    return response.data;
  }

  /**
   * 사용자 삭제
   */
  async deleteUser(userId: number): Promise<void> {
    await apiClient.delete(`${this.basePath}/${userId}`);
  }

  /**
   * 사용자 활성화
   */
  async activateUser(userId: number): Promise<User> {
    const response = await apiClient.patch<User>(`${this.basePath}/${userId}/activate`);
    return response.data;
  }

  /**
   * 사용자 비활성화
   */
  async deactivateUser(userId: number): Promise<User> {
    const response = await apiClient.patch<User>(`${this.basePath}/${userId}/deactivate`);
    return response.data;
  }

  /**
   * 비밀번호 변경
   */
  async changePassword(userId: number, data: ChangePasswordRequest): Promise<void> {
    await apiClient.patch(`${this.basePath}/${userId}/password`, data);
  }

  /**
   * 사용자 역할 조회
   */
  async getUserRoles(userId: number): Promise<any[]> {
    const response = await apiClient.get<any[]>(`${this.basePath}/${userId}/roles`);
    return response.data;
  }

  /**
   * 사용자 역할 할당
   */
  async assignRole(userId: number, roleId: number): Promise<void> {
    await apiClient.post(`${this.basePath}/${userId}/roles/${roleId}`);
  }

  /**
   * 사용자 역할 제거
   */
  async unassignRole(userId: number, roleId: number): Promise<void> {
    await apiClient.delete(`${this.basePath}/${userId}/roles/${roleId}`);
  }
}

export type { User };
export const userService = new UserService();
export default userService;
