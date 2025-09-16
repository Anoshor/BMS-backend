# 🔧 Maintenance Management API Guide

## Overview

This guide covers the maintenance request system for both tenant and manager dashboards, including response models, endpoints, and dashboard structures.

## 📋 Response Models

### MaintenanceRequestResponse Structure

**File:** `/src/main/java/com/bms/backend/dto/response/MaintenanceRequestResponse.java`

```json
{
  "id": "uuid",
  "title": "Leaking Faucet in Kitchen",
  "description": "The kitchen faucet has been leaking for 2 days. Water drips continuously.",
  "priority": "MEDIUM",
  "status": "OPEN",
  "apartmentUnitNumber": "A101",
  "serviceCategoryName": "Plumbing",
  "requesterEmail": "tenant@example.com",
  "tenantEmail": "tenant@example.com",
  "assignedToEmail": "maintenance@example.com",
  "scheduledAt": "2024-01-15T10:00:00Z",
  "submittedAt": "2024-01-10T14:30:00Z",
  "resolvedAt": null,
  "createdAt": "2024-01-10T14:30:00Z",
  "updatedAt": "2024-01-12T09:15:00Z"
}
```

### Field Descriptions

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Unique request identifier |
| `title` | String | Brief title (optional - can be null) |
| `description` | String | Detailed description of the issue |
| `priority` | String | `LOW`, `MEDIUM`, `HIGH` |
| `status` | String | `OPEN`, `IN_PROGRESS`, `SUBMITTED`, `RESOLVED`, `CANCELLED` |
| `apartmentUnitNumber` | String | Unit number (e.g., "A101") |
| `serviceCategoryName` | String | Category (e.g., "Plumbing", "Electrical") |
| `requesterEmail` | String | Email of person who created request |
| `tenantEmail` | String | Email of tenant (if different from requester) |
| `assignedToEmail` | String | Email of assigned maintenance worker |
| `scheduledAt` | Instant | When maintenance is scheduled |
| `submittedAt` | Instant | When request was submitted |
| `resolvedAt` | Instant | When request was resolved (null if ongoing) |
| `createdAt` | Instant | Record creation timestamp |
| `updatedAt` | Instant | Last update timestamp |

---

## 🏠 Tenant Maintenance Dashboard

### Key Endpoints for Tenants

#### 1. Get My Requests
```http
GET /api/v1/maintenance/requests/tenant?tenantEmail={email}
Authorization: Bearer {tenant-jwt-token}
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "req-uuid-1",
      "title": "Leaking Faucet",
      "description": "Kitchen faucet dripping continuously",
      "priority": "MEDIUM",
      "status": "IN_PROGRESS",
      "apartmentUnitNumber": "A101",
      "serviceCategoryName": "Plumbing",
      "requesterEmail": "tenant@example.com",
      "scheduledAt": "2024-01-15T10:00:00Z",
      "submittedAt": "2024-01-10T14:30:00Z",
      "createdAt": "2024-01-10T14:30:00Z",
      "updatedAt": "2024-01-12T09:15:00Z"
    }
  ],
  "message": "Tenant maintenance requests retrieved successfully"
}
```

#### 2. Create New Request
```http
POST /api/v1/maintenance/requests
Authorization: Bearer {tenant-jwt-token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "apartmentId": "apt-uuid",
  "serviceCategoryId": "cat-uuid",
  "title": "Leaking Faucet in Kitchen",
  "description": "The kitchen faucet has been leaking for 2 days. Water drips continuously.",
  "priority": "MEDIUM",
  "photos": ["base64-image-data-1", "base64-image-data-2"]
}
```

#### 3. Get Request Updates
```http
GET /api/v1/maintenance/requests/{requestId}/updates
Authorization: Bearer {tenant-jwt-token}
```

### Tenant Dashboard Structure

**Components for Tenant View:**

1. **Request List Card**
   - Status badge (color-coded)
   - Priority indicator
   - Unit number
   - Category icon
   - Submitted date
   - Quick action buttons

2. **Request Details Modal**
   - Full description
   - Photo gallery
   - Status timeline
   - Updates/comments
   - Scheduled date (if any)

3. **Create Request Form**
   - Apartment selection (if multiple)
   - Category dropdown
   - Title field (optional)
   - Description textarea
   - Priority selection
   - Photo upload

4. **Status Filters**
   - All Requests
   - Open
   - In Progress
   - Resolved

---

## 👨‍💼 Manager Maintenance Dashboard

### Key Endpoints for Managers

#### 1. Get All Requests (Manager's Properties)
```http
GET /api/v1/maintenance/requests
Authorization: Bearer {manager-jwt-token}
```

#### 2. Filter by Status
```http
GET /api/v1/maintenance/requests/status/{status}
Authorization: Bearer {manager-jwt-token}
```

**Available Statuses:** `OPEN`, `IN_PROGRESS`, `SUBMITTED`, `RESOLVED`, `CANCELLED`

#### 3. Filter by Priority
```http
GET /api/v1/maintenance/requests/priority/{priority}
Authorization: Bearer {manager-jwt-token}
```

**Available Priorities:** `LOW`, `MEDIUM`, `HIGH`

#### 4. Search Requests
```http
GET /api/v1/maintenance/requests/search?searchText=faucet
Authorization: Bearer {manager-jwt-token}
```

#### 5. Update Request Status
```http
PUT /api/v1/maintenance/requests/{requestId}
Authorization: Bearer {manager-jwt-token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "status": "IN_PROGRESS",
  "description": "Maintenance worker assigned and will visit tomorrow"
}
```

#### 6. Add Progress Update
```http
POST /api/v1/maintenance/requests/{requestId}/updates
Authorization: Bearer {manager-jwt-token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "message": "Plumber will arrive tomorrow at 10 AM",
  "updateType": "STATUS_UPDATE"
}
```

### Manager Dashboard Structure

**Components for Manager View:**

1. **Overview Cards**
   - Total open requests
   - High priority count
   - Requests this week
   - Average resolution time

2. **Request Management Table**
   - Sortable columns
   - Status dropdowns
   - Priority indicators
   - Tenant info
   - Action buttons

3. **Filters & Search**
   - Status filter tabs
   - Priority filter
   - Property filter
   - Search bar
   - Date range picker

4. **Request Details Panel**
   - Full request info
   - Tenant contact details
   - Photo gallery
   - Update timeline
   - Status change buttons
   - Assignment options

---

## 🎨 UI/UX Recommendations

### Status Color Coding
- **OPEN**: 🔵 Blue (#3B82F6)
- **IN_PROGRESS**: 🟡 Yellow (#F59E0B)
- **SUBMITTED**: 🟠 Orange (#EA580C)
- **RESOLVED**: 🟢 Green (#10B981)
- **CANCELLED**: 🔴 Red (#EF4444)

### Priority Indicators
- **HIGH**: 🔺 Red triangle
- **MEDIUM**: 🟡 Yellow circle
- **LOW**: 🔵 Blue square

### Category Icons
- **Plumbing**: 🚿
- **Electrical**: ⚡
- **HVAC**: ❄️
- **Appliances**: 📺
- **General**: 🔧

---

## 📊 Sample Dashboard Responses

### Manager Dashboard Data
```http
GET /api/v1/maintenance/requests
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "req-001",
      "title": null,
      "description": "AC not cooling properly",
      "priority": "HIGH",
      "status": "OPEN",
      "apartmentUnitNumber": "B204",
      "serviceCategoryName": "HVAC",
      "requesterEmail": "tenant1@example.com",
      "tenantEmail": "tenant1@example.com",
      "assignedToEmail": null,
      "submittedAt": "2024-01-10T14:30:00Z",
      "createdAt": "2024-01-10T14:30:00Z"
    },
    {
      "id": "req-002",
      "title": "Kitchen Sink Issue",
      "description": "Kitchen sink is clogged and water won't drain",
      "priority": "MEDIUM",
      "status": "IN_PROGRESS",
      "apartmentUnitNumber": "A101",
      "serviceCategoryName": "Plumbing",
      "requesterEmail": "tenant2@example.com",
      "assignedToEmail": "plumber@maintenance.com",
      "scheduledAt": "2024-01-15T09:00:00Z",
      "submittedAt": "2024-01-08T11:20:00Z"
    }
  ],
  "message": "Maintenance requests retrieved successfully"
}
```

### Tenant Dashboard Data
```http
GET /api/v1/maintenance/requests/tenant?tenantEmail=tenant1@example.com
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "req-001",
      "title": null,
      "description": "AC not cooling properly, temperature stays around 78°F even on max cool",
      "priority": "HIGH",
      "status": "OPEN",
      "apartmentUnitNumber": "B204",
      "serviceCategoryName": "HVAC",
      "requesterEmail": "tenant1@example.com",
      "submittedAt": "2024-01-10T14:30:00Z",
      "createdAt": "2024-01-10T14:30:00Z",
      "updatedAt": "2024-01-10T14:30:00Z"
    }
  ],
  "message": "Tenant maintenance requests retrieved successfully"
}
```

---

## 🔍 Error Handling

### Common Error Responses

**Validation Error (400):**
```json
{
  "success": false,
  "data": null,
  "message": "Validation failed: {field: 'error message'}",
  "timestamp": "2024-01-10T14:30:00Z"
}
```

**Not Found (404):**
```json
{
  "success": false,
  "data": null,
  "message": "Maintenance request not found",
  "timestamp": "2024-01-10T14:30:00Z"
}
```

**Server Error (500):**
```json
{
  "success": false,
  "data": null,
  "message": "Failed to create maintenance request: Database connection error",
  "timestamp": "2024-01-10T14:30:00Z"
}
```

---

## 📱 Mobile Considerations

### Responsive Design
- Card-based layouts for mobile
- Swipe actions for status changes
- Touch-friendly buttons
- Optimized photo upload

### Offline Support
- Cache recent requests
- Queue updates when offline
- Sync when connection restored

---

**Built with ❤️ for efficient building management**