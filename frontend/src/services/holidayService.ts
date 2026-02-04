/**
 * Holiday Service
 * 휴일 관리 API 서비스
 */

import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

// ==================== Interfaces ====================

export interface Holiday {
  holidayId: number;
  tenantId: string;
  holidayName: string;
  holidayDate: string; // ISO date format: YYYY-MM-DD
  holidayType: 'NATIONAL' | 'COMPANY' | 'SPECIAL';
  isRecurring: boolean;
  recurrenceRule?: string; // YEARLY, MONTHLY, LUNAR
  isWorkingDay: boolean;
  description?: string;
  remarks?: string;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface HolidayCreateRequest {
  tenantId: string;
  holidayName: string;
  holidayDate: string;
  holidayType: 'NATIONAL' | 'COMPANY' | 'SPECIAL';
  isRecurring?: boolean;
  recurrenceRule?: string;
  isWorkingDay?: boolean;
  description?: string;
  remarks?: string;
}

export interface HolidayUpdateRequest {
  holidayName: string;
  holidayDate: string;
  holidayType: 'NATIONAL' | 'COMPANY' | 'SPECIAL';
  isRecurring?: boolean;
  recurrenceRule?: string;
  isWorkingDay?: boolean;
  description?: string;
  remarks?: string;
  isActive?: boolean;
}

export interface WorkingHours {
  workingHoursId: number;
  tenantId: string;
  scheduleName: string;
  description?: string;
  mondayStart?: string;
  mondayEnd?: string;
  tuesdayStart?: string;
  tuesdayEnd?: string;
  wednesdayStart?: string;
  wednesdayEnd?: string;
  thursdayStart?: string;
  thursdayEnd?: string;
  fridayStart?: string;
  fridayEnd?: string;
  saturdayStart?: string;
  saturdayEnd?: string;
  sundayStart?: string;
  sundayEnd?: string;
  breakStart1?: string;
  breakEnd1?: string;
  breakStart2?: string;
  breakEnd2?: string;
  effectiveFrom?: string;
  effectiveTo?: string;
  isDefault: boolean;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface WorkingHoursCreateRequest {
  tenantId: string;
  scheduleName: string;
  description?: string;
  mondayStart?: string;
  mondayEnd?: string;
  tuesdayStart?: string;
  tuesdayEnd?: string;
  wednesdayStart?: string;
  wednesdayEnd?: string;
  thursdayStart?: string;
  thursdayEnd?: string;
  fridayStart?: string;
  fridayEnd?: string;
  saturdayStart?: string;
  saturdayEnd?: string;
  sundayStart?: string;
  sundayEnd?: string;
  breakStart1?: string;
  breakEnd1?: string;
  breakStart2?: string;
  breakEnd2?: string;
  effectiveFrom?: string;
  effectiveTo?: string;
  isDefault?: boolean;
}

export interface BusinessDayCheckResult {
  date: string;
  isBusinessDay: boolean;
  dayOfWeek: string;
}

export interface BusinessDayCalculationResult {
  startDate: string;
  endDate: string;
  businessDays: number;
  totalDays: number;
}

export interface BusinessDayAddResult {
  startDate: string;
  businessDaysAdded: number;
  resultDate: string;
  dayOfWeek: string;
}

export interface NextBusinessDayResult {
  referenceDate: string;
  nextBusinessDay: string;
  dayOfWeek: string;
}

export interface PreviousBusinessDayResult {
  referenceDate: string;
  previousBusinessDay: string;
  dayOfWeek: string;
}

export interface HolidayCountResult {
  startDate: string;
  endDate: string;
  holidayCount: number;
  totalDays: number;
}

// ==================== Holiday Service ====================

class HolidayService {
  private baseURL = `${API_BASE_URL}/holidays`;

  /**
   * Get all holidays by tenant
   */
  async getAllHolidays(tenantId: string): Promise<Holiday[]> {
    const response = await axios.get(this.baseURL, {
      params: { tenantId }
    });
    return response.data.data;
  }

  /**
   * Get active holidays by tenant
   */
  async getActiveHolidays(tenantId: string): Promise<Holiday[]> {
    const response = await axios.get(`${this.baseURL}/active`, {
      params: { tenantId }
    });
    return response.data.data;
  }

  /**
   * Get holidays by year
   */
  async getHolidaysByYear(tenantId: string, year: number): Promise<Holiday[]> {
    const response = await axios.get(`${this.baseURL}/year/${year}`, {
      params: { tenantId }
    });
    return response.data.data;
  }

  /**
   * Get holidays by date range
   */
  async getHolidaysByDateRange(
    tenantId: string,
    startDate: string,
    endDate: string
  ): Promise<Holiday[]> {
    const response = await axios.get(`${this.baseURL}/range`, {
      params: { tenantId, startDate, endDate }
    });
    return response.data.data;
  }

  /**
   * Get holiday by ID
   */
  async getHolidayById(id: number): Promise<Holiday> {
    const response = await axios.get(`${this.baseURL}/${id}`);
    return response.data.data;
  }

  /**
   * Get holidays by type
   */
  async getHolidaysByType(tenantId: string, holidayType: string): Promise<Holiday[]> {
    const response = await axios.get(`${this.baseURL}/type/${holidayType}`, {
      params: { tenantId }
    });
    return response.data.data;
  }

  /**
   * Get national holidays
   */
  async getNationalHolidays(tenantId: string): Promise<Holiday[]> {
    const response = await axios.get(`${this.baseURL}/national`, {
      params: { tenantId }
    });
    return response.data.data;
  }

  /**
   * Create holiday
   */
  async createHoliday(request: HolidayCreateRequest): Promise<Holiday> {
    const response = await axios.post(this.baseURL, {
      tenant: { tenantId: request.tenantId },
      holidayName: request.holidayName,
      holidayDate: request.holidayDate,
      holidayType: request.holidayType,
      isRecurring: request.isRecurring || false,
      recurrenceRule: request.recurrenceRule,
      isWorkingDay: request.isWorkingDay || false,
      description: request.description,
      remarks: request.remarks,
      isActive: true
    });
    return response.data.data;
  }

  /**
   * Update holiday
   */
  async updateHoliday(id: number, request: HolidayUpdateRequest): Promise<Holiday> {
    const response = await axios.put(`${this.baseURL}/${id}`, request);
    return response.data.data;
  }

  /**
   * Delete holiday
   */
  async deleteHoliday(id: number): Promise<void> {
    await axios.delete(`${this.baseURL}/${id}`);
  }

  // ==================== Business Day Calculation ====================

  /**
   * Check if date is a business day
   */
  async checkBusinessDay(tenantId: string, date: string): Promise<BusinessDayCheckResult> {
    const response = await axios.get(`${this.baseURL}/business-day/check`, {
      params: { tenantId, date }
    });
    return response.data.data;
  }

  /**
   * Calculate business days between two dates
   */
  async calculateBusinessDays(
    tenantId: string,
    startDate: string,
    endDate: string
  ): Promise<BusinessDayCalculationResult> {
    const response = await axios.get(`${this.baseURL}/business-day/calculate`, {
      params: { tenantId, startDate, endDate }
    });
    return response.data.data;
  }

  /**
   * Add business days to a date
   */
  async addBusinessDays(
    tenantId: string,
    startDate: string,
    businessDaysToAdd: number
  ): Promise<BusinessDayAddResult> {
    const response = await axios.get(`${this.baseURL}/business-day/add`, {
      params: { tenantId, startDate, businessDaysToAdd }
    });
    return response.data.data;
  }

  /**
   * Get next business day
   */
  async getNextBusinessDay(tenantId: string, date: string): Promise<NextBusinessDayResult> {
    const response = await axios.get(`${this.baseURL}/business-day/next`, {
      params: { tenantId, date }
    });
    return response.data.data;
  }

  /**
   * Get previous business day
   */
  async getPreviousBusinessDay(tenantId: string, date: string): Promise<PreviousBusinessDayResult> {
    const response = await axios.get(`${this.baseURL}/business-day/previous`, {
      params: { tenantId, date }
    });
    return response.data.data;
  }

  /**
   * Count holidays in date range
   */
  async countHolidaysInRange(
    tenantId: string,
    startDate: string,
    endDate: string
  ): Promise<HolidayCountResult> {
    const response = await axios.get(`${this.baseURL}/count`, {
      params: { tenantId, startDate, endDate }
    });
    return response.data.data;
  }
}

// ==================== Working Hours Service ====================

class WorkingHoursService {
  private baseURL = `${API_BASE_URL}/working-hours`;

  /**
   * Get all working hours by tenant
   */
  async getAllWorkingHours(tenantId: string): Promise<WorkingHours[]> {
    const response = await axios.get(this.baseURL, {
      params: { tenantId }
    });
    return response.data.data;
  }

  /**
   * Get active working hours by tenant
   */
  async getActiveWorkingHours(tenantId: string): Promise<WorkingHours[]> {
    const response = await axios.get(`${this.baseURL}/active`, {
      params: { tenantId }
    });
    return response.data.data;
  }

  /**
   * Get default working hours
   */
  async getDefaultWorkingHours(tenantId: string): Promise<WorkingHours> {
    const response = await axios.get(`${this.baseURL}/default`, {
      params: { tenantId }
    });
    return response.data.data;
  }

  /**
   * Get working hours effective on given date
   */
  async getEffectiveWorkingHours(tenantId: string, date: string): Promise<WorkingHours[]> {
    const response = await axios.get(`${this.baseURL}/effective`, {
      params: { tenantId, date }
    });
    return response.data.data;
  }

  /**
   * Get working hours by ID
   */
  async getWorkingHoursById(id: number): Promise<WorkingHours> {
    const response = await axios.get(`${this.baseURL}/${id}`);
    return response.data.data;
  }

  /**
   * Get working hours by schedule name
   */
  async getWorkingHoursByScheduleName(tenantId: string, scheduleName: string): Promise<WorkingHours> {
    const response = await axios.get(`${this.baseURL}/schedule/${scheduleName}`, {
      params: { tenantId }
    });
    return response.data.data;
  }

  /**
   * Create working hours
   */
  async createWorkingHours(request: WorkingHoursCreateRequest): Promise<WorkingHours> {
    const response = await axios.post(this.baseURL, {
      tenant: { tenantId: request.tenantId },
      scheduleName: request.scheduleName,
      description: request.description,
      mondayStart: request.mondayStart,
      mondayEnd: request.mondayEnd,
      tuesdayStart: request.tuesdayStart,
      tuesdayEnd: request.tuesdayEnd,
      wednesdayStart: request.wednesdayStart,
      wednesdayEnd: request.wednesdayEnd,
      thursdayStart: request.thursdayStart,
      thursdayEnd: request.thursdayEnd,
      fridayStart: request.fridayStart,
      fridayEnd: request.fridayEnd,
      saturdayStart: request.saturdayStart,
      saturdayEnd: request.saturdayEnd,
      sundayStart: request.sundayStart,
      sundayEnd: request.sundayEnd,
      breakStart1: request.breakStart1,
      breakEnd1: request.breakEnd1,
      breakStart2: request.breakStart2,
      breakEnd2: request.breakEnd2,
      effectiveFrom: request.effectiveFrom,
      effectiveTo: request.effectiveTo,
      isDefault: request.isDefault || false,
      isActive: true
    });
    return response.data.data;
  }

  /**
   * Update working hours
   */
  async updateWorkingHours(id: number, request: Partial<WorkingHoursCreateRequest>): Promise<WorkingHours> {
    const response = await axios.put(`${this.baseURL}/${id}`, request);
    return response.data.data;
  }

  /**
   * Delete working hours
   */
  async deleteWorkingHours(id: number): Promise<void> {
    await axios.delete(`${this.baseURL}/${id}`);
  }

  /**
   * Set as default working hours
   */
  async setAsDefault(id: number, tenantId: string): Promise<WorkingHours> {
    const response = await axios.put(`${this.baseURL}/${id}/set-default`, null, {
      params: { tenantId }
    });
    return response.data.data;
  }
}

// ==================== Helper Functions ====================

/**
 * Get holiday type label in Korean
 */
export function getHolidayTypeLabel(type: string): string {
  const labels: Record<string, string> = {
    'NATIONAL': '국경일',
    'COMPANY': '회사 휴일',
    'SPECIAL': '특별 휴일'
  };
  return labels[type] || type;
}

/**
 * Get holiday type color
 */
export function getHolidayTypeColor(type: string): 'primary' | 'success' | 'warning' {
  const colors: Record<string, 'primary' | 'success' | 'warning'> = {
    'NATIONAL': 'primary',
    'COMPANY': 'success',
    'SPECIAL': 'warning'
  };
  return colors[type] || 'primary';
}

/**
 * Get recurrence rule label
 */
export function getRecurrenceRuleLabel(rule?: string): string {
  if (!rule) return '-';
  const labels: Record<string, string> = {
    'YEARLY': '매년',
    'MONTHLY': '매월',
    'LUNAR': '음력'
  };
  return labels[rule] || rule;
}

/**
 * Get day of week label in Korean
 */
export function getDayOfWeekLabel(dayOfWeek: string): string {
  const labels: Record<string, string> = {
    'MONDAY': '월요일',
    'TUESDAY': '화요일',
    'WEDNESDAY': '수요일',
    'THURSDAY': '목요일',
    'FRIDAY': '금요일',
    'SATURDAY': '토요일',
    'SUNDAY': '일요일'
  };
  return labels[dayOfWeek] || dayOfWeek;
}

/**
 * Format time (HH:mm:ss to HH:mm)
 */
export function formatTime(time?: string): string {
  if (!time) return '-';
  return time.substring(0, 5);
}

// Export service instances
export const holidayService = new HolidayService();
export const workingHoursService = new WorkingHoursService();
