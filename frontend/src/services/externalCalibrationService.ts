import apiClient from './api';

export interface ExternalCalibration {
  calibrationId: number;
  tenantId: string;
  tenantName: string;
  calibrationNo: string;
  gaugeId: number;
  gaugeCode?: string;
  gaugeName?: string;
  calibrationVendor?: string;
  requestedDate: string;
  sentDate?: string;
  completedDate?: string;
  certificateNo?: string;
  certificateUrl?: string;
  calibrationResult?: string;
  cost?: number;
  nextCalibrationDate?: string;
  status: string;
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ExternalCalibrationCreateRequest {
  calibrationNo: string;
  gaugeId: number;
  requestedDate: string;
  calibrationVendor?: string;
  cost?: number;
  remarks?: string;
}

export interface ExternalCalibrationUpdateRequest {
  calibrationVendor?: string;
  sentDate?: string;
  cost?: number;
  remarks?: string;
  status?: string;
}

const externalCalibrationService = {
  /**
   * Get all external calibrations
   */
  getAll: async (): Promise<ExternalCalibration[]> => {
    const response = await apiClient.get<ExternalCalibration[]>('/external-calibrations');
    return response.data;
  },

  /**
   * Get external calibration by ID
   */
  getById: async (calibrationId: number): Promise<ExternalCalibration> => {
    const response = await apiClient.get<ExternalCalibration>(`/external-calibrations/${calibrationId}`);
    return response.data;
  },

  /**
   * Get external calibrations by gauge
   */
  getByGauge: async (gaugeId: number): Promise<ExternalCalibration[]> => {
    const response = await apiClient.get<ExternalCalibration[]>(`/external-calibrations/gauge/${gaugeId}`);
    return response.data;
  },

  /**
   * Get external calibrations by status
   */
  getByStatus: async (status: string): Promise<ExternalCalibration[]> => {
    const response = await apiClient.get<ExternalCalibration[]>(`/external-calibrations/status/${status}`);
    return response.data;
  },

  /**
   * Create external calibration
   */
  create: async (data: ExternalCalibrationCreateRequest): Promise<ExternalCalibration> => {
    const response = await apiClient.post<ExternalCalibration>('/external-calibrations', data);
    return response.data;
  },

  /**
   * Update external calibration
   */
  update: async (calibrationId: number, data: ExternalCalibrationUpdateRequest): Promise<ExternalCalibration> => {
    const response = await apiClient.put<ExternalCalibration>(`/external-calibrations/${calibrationId}`, data);
    return response.data;
  },

  /**
   * Complete external calibration
   */
  complete: async (calibrationId: number, result: string, certificateNo?: string, nextCalibrationDate?: string): Promise<ExternalCalibration> => {
    const params = new URLSearchParams({ result });
    if (certificateNo) params.append('certificateNo', certificateNo);
    if (nextCalibrationDate) params.append('nextCalibrationDate', nextCalibrationDate);
    const response = await apiClient.post<ExternalCalibration>(`/external-calibrations/${calibrationId}/complete?${params.toString()}`);
    return response.data;
  },

  /**
   * Delete external calibration
   */
  delete: async (calibrationId: number): Promise<void> => {
    await apiClient.delete(`/external-calibrations/${calibrationId}`);
  }
};

export default externalCalibrationService;
