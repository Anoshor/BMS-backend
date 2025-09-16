# Maintenance Management API Documentation

## Overview

The BMS (Building Management System) provides comprehensive maintenance request management with the following key features:

- **Single & Bulk Maintenance Request Creation**
- **Manager & Tenant Access Control**
- **Multi-Unit Maintenance Requests** (Manager can create requests for multiple apartments)
- **Real-time Progress Tracking**
- **Advanced Filtering & Search**
- **Photo Attachments**
- **Status Management with Notes**

---

## Base URL
```
http://localhost:8080/api/v1/maintenance
```

---

## Authentication
All endpoints require JWT authentication. Include the JWT token in the Authorization header:
```
Authorization: Bearer <jwt_token>
```

---

## Core Entities

### MaintenanceRequest
```json
{
  "id": "uuid",
  "title": "string (optional)",
  "description": "string (required)",
  "priority": "LOW | MEDIUM | HIGH",
  "status": "OPEN | IN_PROGRESS | SUBMITTED | RESOLVED | CANCELLED",
  "apartmentId": "uuid",
  "serviceCategoryId": "uuid",
  "requesterId": "uuid",
  "tenantId": "uuid (optional)",
  "assignedToId": "uuid (optional)",
  "managerInitiated": "boolean",
  "submittedAt": "timestamp",
  "scheduledAt": "timestamp",
  "resolvedAt": "timestamp",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

---

## API Endpoints

### 1. Create Single Maintenance Request

**Endpoint:** `POST /requests`

**Access:** Tenants & Managers

**Request Body:**
```json
{
  "apartmentId": "uuid",
  "serviceCategoryId": "uuid",
  "title": "Leaky Faucet",
  "description": "Kitchen faucet is dripping constantly",
  "priority": "MEDIUM",
  "photos": ["base64_encoded_image_1", "base64_encoded_image_2"]
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "title": "Leaky Faucet",
    "description": "Kitchen faucet is dripping constantly",
    "priority": "MEDIUM",
    "status": "OPEN",
    "managerInitiated": false,
    // ... full maintenance request object
  },
  "message": "Maintenance request created successfully"
}
```

---

### 2. Create Bulk Maintenance Requests (Manager Only)

**Endpoint:** `POST /requests/bulk`

**Access:** Property Managers Only

**Use Case:** Manager creating maintenance requests for multiple units (e.g., building-wide HVAC maintenance, elevator repairs, common area issues)

**Request Body:**
```json
{
  "apartmentIds": ["uuid1", "uuid2", "uuid3"],
  "serviceCategoryId": "uuid",
  "title": "Monthly HVAC Maintenance",
  "description": "Routine HVAC system check and filter replacement for all units",
  "priority": "MEDIUM",
  "photos": ["base64_encoded_image"]
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "createdRequestIds": ["uuid1", "uuid2", "uuid3"],
    "totalCreated": 3,
    "totalFailed": 0,
    "failureMessages": []
  },
  "message": "Bulk maintenance request completed. Created: 3, Failed: 0"
}
```

**Error Scenarios:**
```json
{
  "success": true,
  "data": {
    "createdRequestIds": ["uuid1", "uuid2"],
    "totalCreated": 2,
    "totalFailed": 1,
    "failureMessages": [
      "Apartment not found for ID: invalid-uuid",
      "You don't have permission to create maintenance requests for apartment: unauthorized-uuid"
    ]
  },
  "message": "Bulk maintenance request completed. Created: 2, Failed: 1"
}
```

---

### 3. Get Maintenance Requests

#### 3.1 Get All Requests (Manager View)
**Endpoint:** `GET /requests`

**Access:** Property Managers

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "title": "Leaky Faucet",
      "description": "Kitchen faucet is dripping constantly",
      "priority": "MEDIUM",
      "status": "OPEN",
      "managerInitiated": false,
      "apartment": {
        "id": "uuid",
        "unitNumber": "101"
      },
      "tenant": {
        "id": "uuid",
        "name": "John Doe",
        "email": "john@example.com"
      },
      "createdAt": "2025-09-16T10:30:00Z"
    }
  ],
  "message": "Maintenance requests retrieved successfully"
}
```

#### 3.2 Get Requests by Tenant
**Endpoint:** `GET /requests/tenant?tenantEmail=tenant@example.com`

**Access:** Property Managers

#### 3.3 Get Requests by Status
**Endpoint:** `GET /requests/status/{status}`

**Access:** Property Managers

**Parameters:**
- `status`: OPEN, IN_PROGRESS, SUBMITTED, RESOLVED, CANCELLED

#### 3.4 Get Requests by Priority
**Endpoint:** `GET /requests/priority/{priority}`

**Access:** Property Managers

**Parameters:**
- `priority`: LOW, MEDIUM, HIGH

#### 3.5 Get Requests by Service Category
**Endpoint:** `GET /requests/category/{categoryId}`

**Access:** Property Managers

#### 3.6 Get Requests by Apartment/Unit ⭐ NEW
**Endpoint:** `GET /requests/apartment/{apartmentId}`

**Access:** Property Managers

**Use Case:** View all maintenance requests for a specific apartment/unit

#### 3.7 Search Requests
**Endpoint:** `GET /requests/search?searchText=faucet`

**Access:** Property Managers

**Search Fields:** Title, Description, Apartment Unit Number, Service Category Name

---

### 4. Maintenance Request Details

#### 4.1 Get Detailed Request Information ⭐ ENHANCED
**Endpoint:** `GET /requests/{id}/details`

**Access:** Managers (for their properties) & Tenants (for their requests)

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "title": "Monthly HVAC Maintenance",
    "description": "Routine HVAC system check and filter replacement",
    "priority": "MEDIUM",
    "status": "IN_PROGRESS",
    "managerInitiated": true,
    "apartment": {
      "id": "uuid",
      "unitNumber": "101"
    },
    "serviceCategory": {
      "id": "uuid",
      "name": "HVAC"
    },
    "requester": {
      "id": "uuid",
      "name": "Property Manager",
      "email": "manager@property.com",
      "role": "PROPERTY_MANAGER"
    },
    "tenant": {
      "id": "uuid",
      "name": "John Doe",
      "email": "john@example.com"
    },
    "assignedTo": {
      "id": "uuid",
      "name": "HVAC Technician",
      "email": "tech@hvac.com"
    },
    "submittedAt": "2025-09-16T10:30:00Z",
    "createdAt": "2025-09-16T09:00:00Z",
    "updatedAt": "2025-09-16T10:30:00Z",
    "photos": [
      {
        "id": "uuid",
        "photoData": "base64_encoded_image",
        "fileName": null,
        "contentType": null
      }
    ],
    "progressHistory": [
      {
        "id": "uuid",
        "updateType": "STATUS_CHANGE",
        "currentStatus": "IN_PROGRESS",
        "message": "Status changed from OPEN to IN_PROGRESS",
        "notes": "Technician assigned and work scheduled",
        "updatedBy": {
          "id": "uuid",
          "name": "Property Manager",
          "email": "manager@property.com",
          "role": "PROPERTY_MANAGER"
        },
        "timestamp": "2025-09-16T10:30:00Z"
      }
    ]
  },
  "message": "Maintenance request details retrieved successfully"
}
```

#### 4.2 Get Progress History ⭐ ENHANCED
**Endpoint:** `GET /requests/{id}/progress`

**Access:** Managers (for their properties) & Tenants (for their requests)

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "updateType": "STATUS_CHANGE",
      "currentStatus": "RESOLVED",
      "message": "Status changed from IN_PROGRESS to RESOLVED",
      "notes": "HVAC filter replaced successfully. System operating normally.",
      "updatedBy": {
        "id": "uuid",
        "name": "Property Manager",
        "email": "manager@property.com",
        "role": "PROPERTY_MANAGER"
      },
      "timestamp": "2025-09-16T14:30:00Z"
    },
    {
      "id": "uuid",
      "updateType": "STATUS_CHANGE",
      "currentStatus": "IN_PROGRESS",
      "message": "Status changed from OPEN to IN_PROGRESS",
      "notes": "Technician assigned and work scheduled",
      "updatedBy": {
        "id": "uuid",
        "name": "Property Manager",
        "email": "manager@property.com",
        "role": "PROPERTY_MANAGER"
      },
      "timestamp": "2025-09-16T10:30:00Z"
    }
  ],
  "message": "Maintenance request progress retrieved successfully"
}
```

---

### 5. Status Management

#### 5.1 Update Request Status (Manager Only) ⭐ ENHANCED
**Endpoint:** `PUT /requests/{id}/status`

**Access:** Property Managers Only

**Request Body:**
```json
{
  "status": "RESOLVED",
  "notes": "HVAC filter replaced successfully. System operating normally. Next maintenance due in 3 months."
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "updateType": "STATUS_CHANGE",
    "currentStatus": "RESOLVED",
    "message": "Status changed from IN_PROGRESS to RESOLVED",
    "notes": "HVAC filter replaced successfully. System operating normally. Next maintenance due in 3 months.",
    "updatedBy": {
      "id": "uuid",
      "name": "Property Manager",
      "email": "manager@property.com",
      "role": "PROPERTY_MANAGER"
    },
    "timestamp": "2025-09-16T14:30:00Z"
  },
  "message": "Maintenance request status updated successfully"
}
```

---

### 6. Service Categories

#### 6.1 Get All Categories
**Endpoint:** `GET /categories`

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "name": "Plumbing",
      "description": "Water, pipes, faucets, and drainage issues"
    },
    {
      "id": "uuid",
      "name": "HVAC",
      "description": "Heating, ventilation, and air conditioning"
    },
    {
      "id": "uuid",
      "name": "Electrical",
      "description": "Lighting, outlets, and electrical systems"
    }
  ],
  "message": "Service categories retrieved successfully"
}
```

---

## Property & Apartment Management APIs

### Property Endpoints

#### Get All Properties ⭐ NEW
**Endpoint:** `GET /properties`
**Access:** Property Managers

#### Get Property by ID ⭐ NEW
**Endpoint:** `GET /properties/{id}`
**Access:** Property Managers

#### Get Unoccupied Properties
**Endpoint:** `GET /properties/unoccupied`
**Access:** Property Managers

### Apartment/Unit Endpoints

#### Get All Apartments
**Endpoint:** `GET /apartments`
**Access:** Property Managers

#### Get Apartments by Property ⭐ USEFUL FOR BULK REQUESTS
**Endpoint:** `GET /apartments/property/{propertyId}`
**Access:** Property Managers

**Use Case:** Get all apartment IDs for a property to create bulk maintenance requests

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid1",
      "unitNumber": "101",
      "tenantEmail": "tenant1@example.com"
    },
    {
      "id": "uuid2",
      "unitNumber": "102",
      "tenantEmail": "tenant2@example.com"
    }
  ],
  "message": "Apartments retrieved successfully"
}
```

---

## Tenant Experience

### How Manager-Initiated Requests Appear to Tenants

When a manager creates maintenance requests for multiple units, tenants will see these requests in their dashboard with the following indicators:

1. **`managerInitiated: true`** - Indicates the request was created by the property manager
2. **Requester Information** - Shows the property manager as the requester
3. **Automatic Tenant Assignment** - If the apartment has tenant information, the tenant is automatically linked to the request

### Tenant API Access

Tenants can access their maintenance requests through existing endpoints:
- All tenant queries automatically include both tenant-created and manager-initiated requests
- Tenant can view details and progress of manager-initiated requests
- Tenant cannot modify or update status of any requests (manager-only privilege)

---

## Use Case Examples

### Use Case 1: Building-Wide HVAC Maintenance

**Scenario:** Property manager needs to schedule quarterly HVAC maintenance for all units in a building.

**Steps:**
1. Get all apartments in the property:
   ```
   GET /apartments/property/{propertyId}
   ```

2. Create bulk maintenance request:
   ```
   POST /requests/bulk
   {
     "apartmentIds": ["unit1-uuid", "unit2-uuid", "unit3-uuid"],
     "serviceCategoryId": "hvac-category-uuid",
     "title": "Quarterly HVAC Maintenance",
     "description": "Filter replacement and system inspection",
     "priority": "MEDIUM"
   }
   ```

3. Tenants automatically see the request in their dashboard
4. Manager can track progress and update status with notes

### Use Case 2: Emergency Elevator Repair

**Scenario:** Elevator breaks down affecting multiple floors.

**Steps:**
1. Identify affected apartments (floors 2-5)
2. Create bulk maintenance request for all affected units
3. Set priority to HIGH
4. Add photos of the issue
5. Tenants are immediately notified of the maintenance request

### Use Case 3: Apartment-Specific Issue

**Scenario:** Tenant reports water leak affecting their unit.

**Steps:**
1. Tenant creates single maintenance request
2. Manager receives notification
3. Manager assigns to plumber and updates status
4. Progress tracked with notes and photos

---

## Error Handling

### Common Error Responses

#### 401 Unauthorized
```json
{
  "success": false,
  "data": null,
  "message": "Authentication required"
}
```

#### 403 Forbidden
```json
{
  "success": false,
  "data": null,
  "message": "Only property managers can create bulk maintenance requests"
}
```

#### 404 Not Found
```json
{
  "success": false,
  "data": null,
  "message": "Maintenance request not found"
}
```

#### 400 Bad Request
```json
{
  "success": false,
  "data": null,
  "message": "At least one apartment ID is required"
}
```

---

## Business Logic

### Access Control Rules

1. **Property Managers:**
   - Can create single and bulk maintenance requests
   - Can view all requests for their properties
   - Can update status and add notes
   - Can assign requests to technicians

2. **Tenants:**
   - Can create maintenance requests for their apartment
   - Can view their own requests (both created by them and manager-initiated)
   - Cannot update status or modify requests
   - Can add comments/updates to existing requests

3. **System Behavior:**
   - Manager-initiated requests automatically link to tenants if apartment has tenant information
   - Bulk creation is transactional - continues on individual failures and reports results
   - All status changes are logged with timestamps and user information
   - Photos are stored as base64 encoded strings

### Notification Logic (Future Enhancement)

When manager creates bulk maintenance requests:
1. System creates individual requests for each apartment
2. Tenants are linked automatically based on apartment tenant information
3. Email/SMS notifications can be sent to affected tenants
4. Manager receives summary of successful/failed creations

---

## Database Schema Updates

### New Fields Added:

#### MaintenanceRequest Table:
- `manager_initiated` (BOOLEAN, NOT NULL, DEFAULT FALSE)

This field enables:
- Distinguishing between tenant-created and manager-created requests
- Proper UI display logic (showing "Created by Property Manager" vs "Created by You")
- Analytics and reporting on request sources
- Different notification logic for different request types

---

## Performance Considerations

1. **Bulk Operations:** Bulk creation processes apartments individually to ensure partial success
2. **Database Indexing:** Queries are optimized with proper indexes on apartment_id, tenant_email, and manager relationships
3. **Photo Storage:** Base64 encoding used for simplicity, but could be enhanced with file storage service
4. **Caching:** Service category and apartment information could be cached for better performance

---

## Testing

### Manual Testing Scenarios

1. **Create bulk maintenance request for 3 apartments**
2. **Verify tenants see the request in their dashboard**
3. **Manager updates status with notes**
4. **Verify progress history shows updates**
5. **Test access controls (tenant cannot update status)**
6. **Test partial failures in bulk creation**

### Sample cURL Commands

```bash
# Create bulk maintenance request
curl -X POST http://localhost:8080/api/v1/maintenance/requests/bulk \
  -H "Authorization: Bearer <jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "apartmentIds": ["uuid1", "uuid2"],
    "serviceCategoryId": "category-uuid",
    "title": "Monthly Maintenance",
    "description": "Routine monthly check",
    "priority": "MEDIUM"
  }'

# Get maintenance request details
curl -X GET http://localhost:8080/api/v1/maintenance/requests/{id}/details \
  -H "Authorization: Bearer <jwt_token>"

# Update request status (Manager only)
curl -X PUT http://localhost:8080/api/v1/maintenance/requests/{id}/status \
  -H "Authorization: Bearer <jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "RESOLVED",
    "notes": "Work completed successfully"
  }'
```

---

## Summary of Recent Enhancements

### ✅ **New Features Implemented:**

1. **Bulk Maintenance Request Creation**
   - Manager can create requests for multiple apartments simultaneously
   - Robust error handling with partial success reporting
   - Automatic tenant linking based on apartment information

2. **Enhanced Request Details**
   - Added `managerInitiated` field to track request source
   - Comprehensive request details with full user information
   - Progress history with notes and timestamps

3. **Improved Filtering**
   - Filter maintenance requests by specific apartment/unit
   - Enhanced search capabilities across multiple fields

4. **Property Management**
   - Complete property CRUD operations
   - List all properties for managers
   - Get property details by ID

5. **Status Management with Notes**
   - Manager can add detailed notes when updating status
   - Full audit trail of all status changes
   - Rich progress history for transparency

### 🎯 **Business Value:**

- **Efficiency:** Managers can handle building-wide maintenance in single operations
- **Transparency:** Tenants see all relevant maintenance requests automatically
- **Accountability:** Full audit trail of all actions and status changes
- **Scalability:** System handles individual apartment requests and building-wide operations
- **User Experience:** Clear indicators of request source and comprehensive details

This comprehensive maintenance management system provides a robust foundation for property management operations while ensuring excellent user experience for both property managers and tenants.