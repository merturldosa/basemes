/**
 * Audit Log Service
 * 감사 로그 API 서비스
 * @author Moon Myung-seop
 */

import apiClient from './api';
import { AuditLog, AuditLogSearchRequest, PageResponse } from '@/types';

class AuditLogService {
  private basePath = '/audit-logs';

  /**
   * 감사 로그 조회 (페이징 + 검색)
   */
  async getAuditLogs(params: AuditLogSearchRequest): Promise<PageResponse<AuditLog>> {
    const response = await apiClient.get<PageResponse<AuditLog>>(this.basePath, params);
    return response.data;
  }

  /**
   * 감사 로그 단건 조회
   */
  async getAuditLogById(auditId: number): Promise<AuditLog> {
    const response = await apiClient.get<AuditLog>(`${this.basePath}/${auditId}`);
    return response.data;
  }
}

export const auditLogService = new AuditLogService();
export default auditLogService;
