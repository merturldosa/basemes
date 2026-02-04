import apiClient from './api';

export interface Site {
  siteId: number;
  tenantId: string;
  siteCode: string;
  siteName: string;
  address?: string;
  postalCode?: string;
  country?: string;
  region?: string;
  phone?: string;
  fax?: string;
  email?: string;
  managerName?: string;
  managerPhone?: string;
  managerEmail?: string;
  siteType?: string;
  isActive: boolean;
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

export interface SiteRequest {
  siteCode: string;
  siteName: string;
  address?: string;
  postalCode?: string;
  country?: string;
  region?: string;
  phone?: string;
  fax?: string;
  email?: string;
  managerName?: string;
  managerPhone?: string;
  managerEmail?: string;
  siteType?: string;
  remarks?: string;
}

class SiteService {
  async getAll(): Promise<Site[]> {
    const response = await apiClient.get<Site[]>('/sites');
    return response.data;
  }

  async getById(id: number): Promise<Site> {
    const response = await apiClient.get<Site>(`/sites/${id}`);
    return response.data;
  }

  async getActive(): Promise<Site[]> {
    const response = await apiClient.get<Site[]>('/sites/active');
    return response.data;
  }

  async getByType(siteType: string): Promise<Site[]> {
    const response = await apiClient.get<Site[]>(`/sites/type/${siteType}`);
    return response.data;
  }

  async create(request: SiteRequest): Promise<Site> {
    const response = await apiClient.post<Site>('/sites', request);
    return response.data;
  }

  async update(id: number, request: SiteRequest): Promise<Site> {
    const response = await apiClient.put<Site>(`/sites/${id}`, request);
    return response.data;
  }

  async delete(id: number): Promise<void> {
    await apiClient.delete(`/sites/${id}`);
  }

  async toggleActive(id: number): Promise<Site> {
    const response = await apiClient.post<Site>(`/sites/${id}/toggle-active`);
    return response.data;
  }
}

export default new SiteService();
