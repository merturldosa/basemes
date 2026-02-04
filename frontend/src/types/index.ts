/**
 * Common Types
 * @author Moon Myung-seop
 */

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface ErrorResponse {
  success: false;
  errorCode: string;
  message: string;
  timestamp: string;
  path: string;
  fieldErrors?: FieldError[];
}

export interface FieldError {
  field: string;
  message: string;
  rejectedValue: any;
}

// User Types
export interface User {
  userId: number;
  username: string;
  email: string;
  fullName: string;
  status: string;
  preferredLanguage: string;
  tenantId: string;
  tenantName: string;
  lastLoginAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface LoginRequest {
  username: string;
  password: string;
  tenantId: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface UserCreateRequest {
  username: string;
  email: string;
  password: string;
  fullName: string;
  preferredLanguage?: string;
}

export interface UserUpdateRequest {
  email?: string;
  fullName?: string;
  preferredLanguage?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

// Role Types
export interface Role {
  roleId: number;
  roleCode: string;
  roleName: string;
  description?: string;
  status: string;
  tenantId: string;
  tenantName: string;
  config?: Record<string, any>;
  createdAt: string;
  updatedAt: string;
}

export interface RoleCreateRequest {
  roleCode: string;
  roleName: string;
  description?: string;
  config?: Record<string, any>;
}

export interface RoleUpdateRequest {
  roleName?: string;
  description?: string;
  config?: Record<string, any>;
}

// Permission Types
export interface Permission {
  permissionId: number;
  permissionCode: string;
  permissionName: string;
  module: string;
  description?: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface PermissionCreateRequest {
  permissionCode: string;
  permissionName: string;
  module: string;
  description?: string;
}

export interface PermissionUpdateRequest {
  permissionName?: string;
  module?: string;
  description?: string;
}

// Theme Types
export interface Theme {
  themeId: number;
  themeCode: string;
  themeName: string;
  industryType: string;
  description?: string;
  isDefault: boolean;
  status: string;
  colorScheme?: Record<string, any>;
  typography?: Record<string, any>;
  layout?: Record<string, any>;
  components?: Record<string, any>;
  enabledModules?: Record<string, any>;
  additionalConfig?: Record<string, any>;
  createdAt: string;
  updatedAt: string;
}

export interface ThemeCreateRequest {
  themeCode: string;
  themeName: string;
  industryType?: string;
  description?: string;
  isDefault?: boolean;
  colorScheme?: Record<string, any>;
  typography?: Record<string, any>;
  layout?: Record<string, any>;
  components?: Record<string, any>;
  enabledModules?: Record<string, any>;
  additionalConfig?: Record<string, any>;
}

// Audit Log Types
export interface AuditLog {
  auditId: number;
  tenantId: string;
  tenantName: string;
  userId?: number;
  username: string;
  action: string;
  entityType?: string;
  entityId?: string;
  description?: string;
  oldValue?: string;
  newValue?: string;
  ipAddress?: string;
  userAgent?: string;
  httpMethod?: string;
  endpoint?: string;
  success: boolean;
  errorMessage?: string;
  createdAt: string;
  metadata?: string;
}

export interface AuditLogSearchRequest {
  username?: string;
  action?: string;
  entityType?: string;
  entityId?: string;
  success?: boolean;
  ipAddress?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
}

// Industry Types
export type IndustryType =
  | 'CHEMICAL'
  | 'ELECTRONICS'
  | 'MEDICAL'
  | 'FOOD'
  | 'GENERAL';
