import apiClient from './api';

export interface Gauge {
  gaugeId: number;
  tenantId: string;
  tenantName: string;
  gaugeCode: string;
  gaugeName: string;
  gaugeType?: string;
  manufacturer?: string;
  modelName?: string;
  serialNo?: string;
  equipmentId?: number;
  equipmentCode?: string;
  equipmentName?: string;
  departmentId?: number;
  departmentCode?: string;
  departmentName?: string;
  location?: string;
  measurementRange?: string;
  accuracy?: string;
  calibrationCycleDays?: number;
  lastCalibrationDate?: string;
  nextCalibrationDate?: string;
  calibrationStatus?: string;
  status: string;
  remarks?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface GaugeCreateRequest {
  gaugeCode: string;
  gaugeName: string;
  gaugeType?: string;
  manufacturer?: string;
  modelName?: string;
  serialNo?: string;
  equipmentId?: number;
  departmentId?: number;
  location?: string;
  measurementRange?: string;
  accuracy?: string;
  calibrationCycleDays?: number;
  lastCalibrationDate?: string;
  nextCalibrationDate?: string;
  calibrationStatus?: string;
  status?: string;
  remarks?: string;
}

export interface GaugeUpdateRequest {
  gaugeName?: string;
  gaugeType?: string;
  manufacturer?: string;
  modelName?: string;
  serialNo?: string;
  equipmentId?: number;
  departmentId?: number;
  location?: string;
  measurementRange?: string;
  accuracy?: string;
  calibrationCycleDays?: number;
  lastCalibrationDate?: string;
  nextCalibrationDate?: string;
  calibrationStatus?: string;
  status?: string;
  remarks?: string;
}

const gaugeService = {
  /**
   * Get all gauges
   */
  getAll: async (): Promise<Gauge[]> => {
    const response = await apiClient.get<Gauge[]>('/gauges');
    return response.data;
  },

  /**
   * Get gauge by ID
   */
  getById: async (gaugeId: number): Promise<Gauge> => {
    const response = await apiClient.get<Gauge>(`/gauges/${gaugeId}`);
    return response.data;
  },

  /**
   * Get gauges with calibration due
   */
  getCalibrationDue: async (dueDate: string): Promise<Gauge[]> => {
    const response = await apiClient.get<Gauge[]>('/gauges/calibration-due', {
      params: { dueDate }
    });
    return response.data;
  },

  /**
   * Create gauge
   */
  create: async (data: GaugeCreateRequest): Promise<Gauge> => {
    const response = await apiClient.post<Gauge>('/gauges', data);
    return response.data;
  },

  /**
   * Update gauge
   */
  update: async (gaugeId: number, data: GaugeUpdateRequest): Promise<Gauge> => {
    const response = await apiClient.put<Gauge>(`/gauges/${gaugeId}`, data);
    return response.data;
  },

  /**
   * Delete gauge
   */
  delete: async (gaugeId: number): Promise<void> => {
    await apiClient.delete(`/gauges/${gaugeId}`);
  }
};

export default gaugeService;
