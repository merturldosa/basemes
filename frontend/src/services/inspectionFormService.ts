import apiClient from './api';

export interface InspectionFormField {
  fieldId?: number;
  fieldName: string;
  fieldType: string;
  fieldOrder: number;
  isRequired?: boolean;
  options?: string;
  unit?: string;
  minValue?: number;
  maxValue?: number;
}

export interface InspectionForm {
  formId: number;
  tenantId: string;
  tenantName: string;
  formCode: string;
  formName: string;
  description?: string;
  equipmentType?: string;
  inspectionType?: string;
  fields?: InspectionFormField[];
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface InspectionFormCreateRequest {
  formCode: string;
  formName: string;
  description?: string;
  equipmentType?: string;
  inspectionType?: string;
  fields?: InspectionFormField[];
}

export interface InspectionFormUpdateRequest {
  formName?: string;
  description?: string;
  equipmentType?: string;
  inspectionType?: string;
  fields?: InspectionFormField[];
}

const inspectionFormService = {
  /**
   * Get all inspection forms
   */
  getAll: async (): Promise<InspectionForm[]> => {
    const response = await apiClient.get<InspectionForm[]>('/inspection-forms');
    return response.data;
  },

  /**
   * Get active inspection forms
   */
  getActive: async (): Promise<InspectionForm[]> => {
    const response = await apiClient.get<InspectionForm[]>('/inspection-forms/active');
    return response.data;
  },

  /**
   * Get inspection form by ID
   */
  getById: async (formId: number): Promise<InspectionForm> => {
    const response = await apiClient.get<InspectionForm>(`/inspection-forms/${formId}`);
    return response.data;
  },

  /**
   * Create inspection form
   */
  create: async (data: InspectionFormCreateRequest): Promise<InspectionForm> => {
    const response = await apiClient.post<InspectionForm>('/inspection-forms', data);
    return response.data;
  },

  /**
   * Update inspection form
   */
  update: async (formId: number, data: InspectionFormUpdateRequest): Promise<InspectionForm> => {
    const response = await apiClient.put<InspectionForm>(`/inspection-forms/${formId}`, data);
    return response.data;
  },

  /**
   * Delete inspection form
   */
  delete: async (formId: number): Promise<void> => {
    await apiClient.delete(`/inspection-forms/${formId}`);
  }
};

export default inspectionFormService;
