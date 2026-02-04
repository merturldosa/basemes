import apiClient from './api';

export interface Product {
  productId: number;
  productCode: string;
  productName: string;
  productType?: string;
  specification?: string;
  unit: string;
  standardCycleTime?: number;
  isActive: boolean;
  tenantId: string;
  tenantName: string;
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ProductCreateRequest {
  productCode: string;
  productName: string;
  productType?: string;
  specification?: string;
  unit: string;
  standardCycleTime?: number;
  remarks?: string;
}

export interface ProductUpdateRequest {
  productName?: string;
  productType?: string;
  specification?: string;
  unit?: string;
  standardCycleTime?: number;
  remarks?: string;
}

const productService = {
  // Get all products
  getProducts: async (): Promise<Product[]> => {
    const response = await apiClient.get<Product[]>('/products');
    return response.data;
  },

  // Get active products only
  getActiveProducts: async (): Promise<Product[]> => {
    const response = await apiClient.get<Product[]>('/products/active');
    return response.data;
  },

  // Get product by ID
  getProduct: async (id: number): Promise<Product> => {
    const response = await apiClient.get<Product>(`/products/${id}`);
    return response.data;
  },

  // Get product by code
  getProductByCode: async (productCode: string): Promise<Product> => {
    const response = await apiClient.get<Product>(`/products/code/${productCode}`);
    return response.data;
  },

  // Create new product
  createProduct: async (product: ProductCreateRequest): Promise<Product> => {
    const response = await apiClient.post<Product>('/products', product);
    return response.data;
  },

  // Update product
  updateProduct: async (id: number, product: ProductUpdateRequest): Promise<Product> => {
    const response = await apiClient.put<Product>(`/products/${id}`, product);
    return response.data;
  },

  // Delete product
  deleteProduct: async (id: number): Promise<void> => {
    await apiClient.delete<void>(`/products/${id}`);
  },

  // Activate product
  activateProduct: async (id: number): Promise<Product> => {
    const response = await apiClient.post<Product>(`/products/${id}/activate`);
    return response.data;
  },

  // Deactivate product
  deactivateProduct: async (id: number): Promise<Product> => {
    const response = await apiClient.post<Product>(`/products/${id}/deactivate`);
    return response.data;
  },
};

export default productService;
