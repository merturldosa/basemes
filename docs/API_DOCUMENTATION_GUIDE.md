# SoIce MES Platform - API Documentation Guide

> **Author**: Moon Myung-seop (문명섭)
> **Company**: SoftIce Co., Ltd. (주)소프트아이스
> **Version**: 0.8.0
> **Last Updated**: 2026-01-27

---

## 📋 Overview

SoIce MES Platform provides a comprehensive REST API for managing manufacturing execution systems. This guide covers authentication, endpoints, request/response formats, and best practices.

**Base URL**: `http://localhost:8080/api`
**API Documentation**: `http://localhost:8080/api/swagger-ui.html`

---

## 🔐 Authentication

### Overview

The API uses **JWT (JSON Web Token)** based authentication with Bearer token scheme.

### Authentication Flow

```
1. POST /api/auth/login          → Get JWT tokens
2. Include token in requests     → Authorization: Bearer <access_token>
3. POST /api/auth/refresh        → Renew access token (when expired)
4. POST /api/auth/logout         → Invalidate tokens
```

### 1. Login

**Endpoint**: `POST /api/auth/login`

**Request**:
```json
{
  "tenantId": "default",
  "username": "admin",
  "password": "admin123"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "username": "admin",
      "email": "admin@soice.co.kr",
      "name": "관리자",
      "roles": ["ADMIN"]
    }
  }
}
```

### 2. Using the Token

Include the access token in the `Authorization` header:

```http
GET /api/users HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 3. Refresh Token

**Endpoint**: `POST /api/auth/refresh`

**Request**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response**:
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  }
}
```

### 4. Logout

**Endpoint**: `POST /api/auth/logout`

**Request**: No body required

**Response**:
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

---

## 📦 API Modules

The API is organized into 17 functional modules:

| Module | Base Path | Description |
|--------|-----------|-------------|
| **1. Authentication** | `/auth` | Login, logout, token refresh |
| **2. User Management** | `/users`, `/roles`, `/permissions` | User, role, permission management |
| **3. Dashboard** | `/dashboard` | Analytics and statistics |
| **4. Production** | `/products`, `/processes`, `/work-orders` | Production management |
| **5. Quality** | `/quality-standards`, `/quality-inspections` | Quality management (QMS) |
| **6. Inventory** | `/warehouses`, `/lots`, `/inventory` | Warehouse management (WMS) |
| **7. BOM** | `/boms` | Bill of materials |
| **8. Business** | `/customers`, `/suppliers` | Customer/supplier management |
| **9. Material** | `/materials` | Material master data |
| **10. Purchase** | `/purchase-requests`, `/purchase-orders` | Purchase management |
| **11. Sales** | `/sales-orders`, `/deliveries` | Sales and delivery |
| **12. Equipment** | `/equipments`, `/equipment-operations` | Equipment management |
| **13. Downtime** | `/downtimes` | Downtime tracking |
| **14. Mold** | `/molds`, `/mold-maintenances` | Mold management |
| **15. HR** | `/skill-matrix`, `/employee-skills` | Human resources |
| **16. Defect** | `/defects`, `/after-sales`, `/claims` | Defect management |
| **17. Common** | `/sites`, `/departments`, `/common-codes` | Common management |

---

## 🌐 Common Request/Response Patterns

### Standard Response Format

All API responses follow this structure:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { /* Response data */ },
  "timestamp": "2026-01-27T10:30:00"
}
```

**Success Response**:
```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": 123,
    "username": "newuser",
    "email": "user@example.com"
  },
  "timestamp": "2026-01-27T10:30:00"
}
```

**Error Response**:
```json
{
  "success": false,
  "message": "User not found",
  "error": "NOT_FOUND",
  "timestamp": "2026-01-27T10:30:00"
}
```

### Pagination

List endpoints support pagination:

**Request**:
```http
GET /api/users?page=0&size=20&sort=createdAt,desc
```

**Response**:
```json
{
  "success": true,
  "data": {
    "content": [
      { "id": 1, "username": "user1" },
      { "id": 2, "username": "user2" }
    ],
    "totalElements": 100,
    "totalPages": 5,
    "size": 20,
    "number": 0,
    "first": true,
    "last": false
  }
}
```

**Query Parameters**:
- `page`: Page number (0-indexed)
- `size`: Items per page (default: 20, max: 100)
- `sort`: Sort field and direction (e.g., `createdAt,desc`)

### Filtering

Use query parameters for filtering:

```http
GET /api/users?status=ACTIVE&roles=ADMIN&search=john
```

---

## 📚 API Examples

### User Management

#### Create User
```http
POST /api/users
Content-Type: application/json
Authorization: Bearer <token>

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "name": "John Doe",
  "departmentId": 10,
  "roleIds": [2, 3]
}
```

#### Get User
```http
GET /api/users/123
Authorization: Bearer <token>
```

#### Update User
```http
PUT /api/users/123
Content-Type: application/json
Authorization: Bearer <token>

{
  "email": "newemail@example.com",
  "name": "John D. Doe",
  "status": "ACTIVE"
}
```

#### Delete User
```http
DELETE /api/users/123
Authorization: Bearer <token>
```

### Dashboard

#### Get Statistics
```http
GET /api/dashboard/stats
Authorization: Bearer <token>
```

**Response**:
```json
{
  "success": true,
  "data": {
    "totalUsers": 150,
    "activeUsers": 120,
    "totalRoles": 5,
    "totalPermissions": 50,
    "todayLogins": 35,
    "activeSessions": 20
  }
}
```

#### Get Login Trend
```http
GET /api/dashboard/login-trend?days=7
Authorization: Bearer <token>
```

### Production

#### Create Work Order
```http
POST /api/work-orders
Content-Type: application/json
Authorization: Bearer <token>

{
  "orderNumber": "WO-2026-001",
  "productId": 45,
  "quantity": 1000,
  "plannedStartDate": "2026-02-01T08:00:00",
  "plannedEndDate": "2026-02-05T17:00:00",
  "priority": "HIGH"
}
```

### Inventory

#### Check Inventory
```http
GET /api/inventory?warehouseId=1&productId=45
Authorization: Bearer <token>
```

#### Create Goods Receipt
```http
POST /api/goods-receipts
Content-Type: application/json
Authorization: Bearer <token>

{
  "receiptDate": "2026-01-27",
  "warehouseId": 1,
  "supplierId": 10,
  "items": [
    {
      "materialId": 100,
      "quantity": 500,
      "lotNumber": "LOT-2026-001"
    }
  ]
}
```

---

## 🚨 Error Codes

| HTTP Status | Error Code | Description |
|-------------|------------|-------------|
| 400 | BAD_REQUEST | Invalid request parameters |
| 401 | UNAUTHORIZED | Missing or invalid authentication |
| 403 | FORBIDDEN | Insufficient permissions |
| 404 | NOT_FOUND | Resource not found |
| 409 | CONFLICT | Resource conflict (e.g., duplicate) |
| 422 | VALIDATION_ERROR | Validation failed |
| 500 | INTERNAL_SERVER_ERROR | Server error |

**Error Response Format**:
```json
{
  "success": false,
  "message": "Username already exists",
  "error": "CONFLICT",
  "details": {
    "field": "username",
    "value": "johndoe"
  },
  "timestamp": "2026-01-27T10:30:00"
}
```

---

## 💡 Best Practices

### 1. Always Use HTTPS in Production
```
❌ http://api.example.com
✅ https://api.example.com
```

### 2. Store Tokens Securely
- Use `httpOnly` cookies or secure storage
- Never store in localStorage (XSS vulnerable)
- Implement token refresh before expiry

### 3. Handle Token Expiration
```javascript
// Refresh token before expiry
const TOKEN_REFRESH_BUFFER = 5 * 60 * 1000; // 5 minutes

if (tokenExpiryTime - Date.now() < TOKEN_REFRESH_BUFFER) {
  await refreshAccessToken();
}
```

### 4. Implement Retry Logic
```javascript
// Retry failed requests
async function apiCall(url, options, retries = 3) {
  try {
    return await fetch(url, options);
  } catch (error) {
    if (retries > 0) {
      await delay(1000);
      return apiCall(url, options, retries - 1);
    }
    throw error;
  }
}
```

### 5. Use Pagination for Large Data Sets
```http
# Don't fetch all at once
❌ GET /api/users

# Use pagination
✅ GET /api/users?page=0&size=50
```

### 6. Validate Input on Client Side
```javascript
// Validate before sending
const user = {
  username: username.trim(),
  email: validateEmail(email) ? email : null,
  password: validatePassword(password) ? password : null
};

if (!user.email || !user.password) {
  throw new Error('Invalid input');
}
```

---

## 🛠️ Code Examples

### cURL

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "default",
    "username": "admin",
    "password": "admin123"
  }'

# Get users
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Create user
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "user@example.com",
    "password": "SecurePass123!",
    "name": "New User"
  }'
```

### JavaScript (Fetch API)

```javascript
// Login
async function login(username, password) {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      tenantId: 'default',
      username,
      password
    })
  });

  const data = await response.json();

  if (data.success) {
    localStorage.setItem('accessToken', data.data.accessToken);
    localStorage.setItem('refreshToken', data.data.refreshToken);
    return data.data.user;
  } else {
    throw new Error(data.message);
  }
}

// API call with authentication
async function getUsers() {
  const token = localStorage.getItem('accessToken');

  const response = await fetch('http://localhost:8080/api/users', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  const data = await response.json();
  return data.data.content;
}
```

### Python (Requests)

```python
import requests

# Base URL
BASE_URL = 'http://localhost:8080/api'

# Login
def login(username, password):
    response = requests.post(f'{BASE_URL}/auth/login', json={
        'tenantId': 'default',
        'username': username,
        'password': password
    })

    data = response.json()

    if data['success']:
        return data['data']['accessToken']
    else:
        raise Exception(data['message'])

# Get users
def get_users(token):
    headers = {
        'Authorization': f'Bearer {token}'
    }

    response = requests.get(f'{BASE_URL}/users', headers=headers)
    data = response.json()

    return data['data']['content']

# Create user
def create_user(token, user_data):
    headers = {
        'Authorization': f'Bearer {token}',
        'Content-Type': 'application/json'
    }

    response = requests.post(
        f'{BASE_URL}/users',
        headers=headers,
        json=user_data
    )

    return response.json()

# Usage
token = login('admin', 'admin123')
users = get_users(token)
print(f'Total users: {len(users)}')
```

---

## 🔍 Interactive API Documentation

### Swagger UI

Access interactive API documentation at:

**URL**: `http://localhost:8080/api/swagger-ui.html`

**Features**:
- Browse all endpoints by module
- View request/response schemas
- Try out API calls directly
- See example requests/responses
- Download OpenAPI spec (JSON/YAML)

### Using Swagger UI

1. **Authorize**:
   - Click "Authorize" button
   - Enter: `Bearer <your-access-token>`
   - Click "Authorize" and "Close"

2. **Explore Endpoints**:
   - Select a module (e.g., "User Management")
   - Expand an endpoint
   - Click "Try it out"

3. **Test API**:
   - Fill in parameters
   - Click "Execute"
   - View response

---

## 📞 Support

For API questions or issues:

- **Email**: msmoon@softice.co.kr
- **Phone**: 010-4882-2035
- **Documentation**: Check Swagger UI for latest API specs

---

## 📄 License

Proprietary - All rights reserved by SoftIce Co., Ltd.

---

**Made with ❤️ by SoftIce Co., Ltd.**
