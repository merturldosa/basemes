/**
 * Alarm Service
 * 알람 관리 API 서비스
 */

import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

// ==================== Interfaces ====================

export interface AlarmTemplate {
  templateId: number;
  tenantId: string;
  templateCode: string;
  templateName: string;
  alarmType: string;
  eventType: string;
  titleTemplate: string;
  messageTemplate: string;
  enableEmail: boolean;
  enableSms: boolean;
  enablePush: boolean;
  enableSystem: boolean;
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
  isActive: boolean;
}

export interface AlarmHistory {
  alarmId: number;
  tenantId: string;
  recipientUserId: number;
  recipientName?: string;
  alarmType: string;
  eventType: string;
  priority: string;
  title: string;
  message: string;
  referenceType?: string;
  referenceId?: number;
  referenceNo?: string;
  sentViaEmail: boolean;
  sentViaSms: boolean;
  sentViaPush: boolean;
  sentViaSystem: boolean;
  isRead: boolean;
  readAt?: string;
  sendStatus: 'PENDING' | 'SENT' | 'FAILED';
  sentAt?: string;
  createdAt: string;
}

export interface AlarmStatistics {
  unreadCount: number;
  totalCount: number;
  approvalCount: number;
  qualityCount: number;
  productionCount: number;
  inventoryCount: number;
  readCount: number;
  readRate: number;
}

// ==================== Alarm Service ====================

class AlarmServiceClass {
  private baseURL = `${API_BASE_URL}/alarms`;

  /**
   * Get all templates
   */
  async getAllTemplates(tenantId: string): Promise<AlarmTemplate[]> {
    const response = await axios.get(`${this.baseURL}/templates`, {
      params: { tenantId }
    });
    return response.data.data;
  }

  /**
   * Get alarms for user
   */
  async getAlarms(tenantId: string, userId: number): Promise<AlarmHistory[]> {
    const response = await axios.get(this.baseURL, {
      params: { tenantId, userId }
    });
    return response.data.data;
  }

  /**
   * Get unread alarms
   */
  async getUnreadAlarms(tenantId: string, userId: number): Promise<AlarmHistory[]> {
    const response = await axios.get(`${this.baseURL}/unread`, {
      params: { tenantId, userId }
    });
    return response.data.data;
  }

  /**
   * Get recent alarms (last 7 days)
   */
  async getRecentAlarms(tenantId: string, userId: number): Promise<AlarmHistory[]> {
    const response = await axios.get(`${this.baseURL}/recent`, {
      params: { tenantId, userId }
    });
    return response.data.data;
  }

  /**
   * Count unread alarms
   */
  async countUnreadAlarms(tenantId: string, userId: number): Promise<number> {
    const response = await axios.get(`${this.baseURL}/unread/count`, {
      params: { tenantId, userId }
    });
    return response.data.data;
  }

  /**
   * Mark alarm as read
   */
  async markAsRead(alarmId: number): Promise<void> {
    await axios.put(`${this.baseURL}/${alarmId}/read`);
  }

  /**
   * Mark all alarms as read
   */
  async markAllAsRead(tenantId: string, userId: number): Promise<void> {
    await axios.put(`${this.baseURL}/read-all`, null, {
      params: { tenantId, userId }
    });
  }

  /**
   * Get alarm statistics
   */
  async getStatistics(tenantId: string, userId: number): Promise<AlarmStatistics> {
    const response = await axios.get(`${this.baseURL}/statistics`, {
      params: { tenantId, userId }
    });
    return response.data.data;
  }
}

// ==================== Helper Functions ====================

/**
 * Get alarm type label
 */
export function getAlarmTypeLabel(type: string): string {
  const labels: Record<string, string> = {
    'SYSTEM': '시스템',
    'APPROVAL': '결재',
    'QUALITY': '품질',
    'PRODUCTION': '생산',
    'INVENTORY': '재고',
    'DELIVERY': '출하'
  };
  return labels[type] || type;
}

/**
 * Get alarm type color
 */
export function getAlarmTypeColor(type: string): 'default' | 'primary' | 'success' | 'warning' | 'error' {
  const colors: Record<string, 'default' | 'primary' | 'success' | 'warning' | 'error'> = {
    'SYSTEM': 'default',
    'APPROVAL': 'primary',
    'QUALITY': 'warning',
    'PRODUCTION': 'success',
    'INVENTORY': 'error',
    'DELIVERY': 'primary'
  };
  return colors[type] || 'default';
}

/**
 * Get priority label
 */
export function getPriorityLabel(priority: string): string {
  const labels: Record<string, string> = {
    'LOW': '낮음',
    'NORMAL': '보통',
    'HIGH': '높음',
    'URGENT': '긴급'
  };
  return labels[priority] || priority;
}

/**
 * Get priority color
 */
export function getPriorityColor(priority: string): 'default' | 'primary' | 'warning' | 'error' {
  const colors: Record<string, 'default' | 'primary' | 'warning' | 'error'> = {
    'LOW': 'default',
    'NORMAL': 'primary',
    'HIGH': 'warning',
    'URGENT': 'error'
  };
  return colors[priority] || 'default';
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
 * Get relative time
 */
export function getRelativeTime(dateStr: string): string {
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();

  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);

  if (minutes < 1) return '방금 전';
  if (minutes < 60) return `${minutes}분 전`;
  if (hours < 24) return `${hours}시간 전`;
  if (days < 7) return `${days}일 전`;
  return formatDateTime(dateStr);
}

// Export service instance
export const alarmService = new AlarmServiceClass();
