# BMS Dashboard API Testing Guide

## Prerequisites
- Application running on `http://localhost:8080`
- All endpoints are prefixed with `/api/v1`

## Step 1: Manager Registration and Setup

### 1.1 Register a Manager
```bash
curl -X POST "http://localhost:8080/api/v1/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "manager@example.com",
    "contactNum": "9876543210",
    "password": "password123",
    "firstName": "John",
    "lastName": "Manager",
    "dob": "1990-01-15T00:00:00.000Z",
    "gender": "male",
    "role": "MANAGER"
  }'
```

### 1.2 Verify Manager Email (if OTP verification is enabled)
```bash
curl --location 'http://localhost:8080/api/v1/auth/verify-email?email=manager%40example.com&otp=123456' \
--header 'Content-Type: application/json' \
--data-raw '{
    "email": "manager@example.com",
    "otpCode": "123456"
  }'
```

### 1.3 Manager Login
```bash
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "manager@example.com",
    "password": "password123",
    "role": "MANAGER"
  }'
```

**Save the access token from the response for subsequent requests.**

### 1.4 Add Property Details (Manager)
```bash
curl -X POST "http://localhost:8080/api/v1/properties/add" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "propertyName": "Sunset Apartments",
    "propertyManagerName": "John Manager",
    "propertyAddress": "123 Main St, City, State",
    "propertyType": "residential",
    "squareFootage": 1200,
    "numberOfUnits": 50,
    "unitNumber": "A101",
    "unitType": "2BHK",
    "floor": 1,
    "bedrooms": 2,
    "bathrooms": 2,
    "furnished": "furnished",
    "balcony": "yes",
    "rent": 1500.00,
    "securityDeposit": 3000.00,
    "maintenanceCharges": 200.00,
    "occupancy": "vacant",
    "utilityMeterNumber": 12345
  }'
```

### 1.5 Get Unoccupied Properties (Manager)
```bash
curl -X GET "http://localhost:8080/api/v1/properties/unoccupied" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## Step 2: Tenant Registration and Setup

### 2.1 Register a Tenant
```bash
curl -X POST "http://localhost:8080/api/v1/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "tenant@example.com",
    "contactNum": "9876543211",
    "password": "password123",
    "firstName": "Jane",
    "lastName": "Tenant",
    "dob": "1995-05-20T00:00:00.000Z",
    "gender": "female",
    "role": "TENANT"
  }'
```

### 2.2 Tenant Login
```bash
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "tenant@example.com",
    "password": "password123",
    "role": "TENANT"
  }'
```

**Save the tenant's access token.**

## Step 3: Manager-Tenant Connection

### 3.1 Connect Tenant to Property (Manager Action)
```bash
curl -X POST "http://localhost:8080/api/v1/tenants/connect" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN" \
  -d '{
    "tenantEmail": "tenant@example.com",
    "propertyName": "Sunset Apartments",
    "startDate": "2025-01-01",
    "endDate": "2025-12-31",
    "monthlyRent": 1500.00,
    "securityDeposit": 3000.00,
    "notes": "Standard lease agreement for 2BHK unit A101"
  }'
```

### 3.2 Search Tenants (Manager)
```bash
curl -X GET "http://localhost:8080/api/v1/tenants/search?searchText=Jane" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"
```

## Step 4: Tenant Dashboard

### 4.1 Get Tenant's Properties/Apartments
```bash
curl -X GET "http://localhost:8080/api/v1/tenants/my-properties" \
  -H "Authorization: Bearer TENANT_ACCESS_TOKEN"
```

## Step 5: Authentication & Token Management

### 5.1 Refresh Access Token
```bash
curl -X POST "http://localhost:8080/api/v1/auth/refresh-token" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 5.2 Logout
```bash
curl -X POST "http://localhost:8080/api/v1/auth/logout" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## Step 6: Admin Operations (if needed)

### 6.1 Get Pending Manager Approvals
```bash
curl -X GET "http://localhost:8080/api/v1/admin/managers/pending" \
  -H "Authorization: Bearer ADMIN_ACCESS_TOKEN"
```

### 6.2 Approve Manager
```bash
curl -X PUT "http://localhost:8080/api/v1/admin/managers/approve" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ADMIN_ACCESS_TOKEN" \
  -d '{
    "managerEmail": "manager@example.com",
    "action": "approve",
    "adminEmail": "admin@example.com"
  }'
```

## Expected Response Format

All successful API responses follow this format:
```json
{
  "success": true,
  "data": { /* response data */ },
  "message": "Operation successful",
  "timestamp": "2025-08-21T11:45:00"
}
```

Error responses:
```json
{
  "success": false,
  "data": null,
  "message": "Error description",
  "timestamp": "2025-08-21T11:45:00"
}
```

## New Enhanced APIs (Property/Apartment Separation & Maintenance Management)

### Building Management APIs

#### Create Building
```bash
curl -X POST "http://localhost:8080/api/v1/properties/buildings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN" \
  -d '{
    "name": "Sunset Heights",
    "address": "456 Oak Avenue, Downtown",
    "propertyType": "Apartment Complex",
    "totalUnits": 100,
    "totalFloors": 10,
    "yearBuilt": 2015,
    "amenities": "Pool, Gym, Parking, Security"
  }'
```

#### Get My Buildings
```bash
curl -X GET "http://localhost:8080/api/v1/properties/buildings" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"
```

#### Search My Buildings
```bash
curl -X GET "http://localhost:8080/api/v1/properties/buildings/search?searchText=Sunset" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"
```

#### Global Building Search
```bash
curl -X GET "http://localhost:8080/api/v1/properties/buildings/search/global?searchText=Heights" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"
```

#### Update Building
```bash
curl -X PUT "http://localhost:8080/api/v1/properties/buildings/{buildingId}" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN" \
  -d '{
    "name": "Sunset Heights Updated",
    "address": "456 Oak Avenue, Downtown",
    "propertyType": "Luxury Apartment Complex",
    "totalUnits": 120,
    "totalFloors": 12,
    "yearBuilt": 2015,
    "amenities": "Pool, Gym, Parking, Security, Rooftop Garden"
  }'
```

### Apartment/Unit Management APIs

#### Create Apartment
```bash
curl -X POST "http://localhost:8080/api/v1/apartments" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN" \
  -d '{
    "propertyId": "building-uuid-here",
    "unitNumber": "A101",
    "unitType": "2BHK",
    "floor": 1,
    "bedrooms": 2,
    "bathrooms": 2,
    "squareFootage": 1200,
    "furnished": "Semi-Furnished",
    "balcony": "Yes",
    "rent": 2500.00,
    "securityDeposit": 5000.00,
    "maintenanceCharges": 300.00,
    "occupancyStatus": "VACANT",
    "utilityMeterNumber": "MTR12345"
  }'
```

#### Get My Apartments
```bash
curl -X GET "http://localhost:8080/api/v1/apartments" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"
```

#### Get Apartments by Building
```bash
curl -X GET "http://localhost:8080/api/v1/apartments/property/{propertyId}" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"
```

#### Search My Apartments
```bash
curl -X GET "http://localhost:8080/api/v1/apartments/search?searchText=A101" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"
```

#### Get Occupied Apartments
```bash
curl -X GET "http://localhost:8080/api/v1/apartments/occupied" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"
```

#### Get Vacant Apartments
```bash
curl -X GET "http://localhost:8080/api/v1/apartments/unoccupied" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"
```

#### Search Apartments by Tenant
```bash
curl -X GET "http://localhost:8080/api/v1/apartments/tenant/search?tenantEmail=jane@example.com" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"

curl -X GET "http://localhost:8080/api/v1/apartments/tenant/search?tenantName=Jane&tenantPhone=9876543211" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"
```

#### Assign Tenant to Apartment
```bash
curl -X POST "http://localhost:8080/api/v1/apartments/{apartmentId}/tenant" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN" \
  -d "tenantName=Jane Doe&tenantEmail=jane@example.com&tenantPhone=9876543211"
```

#### Remove Tenant from Apartment
```bash
curl -X DELETE "http://localhost:8080/api/v1/apartments/{apartmentId}/tenant" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"
```

### Maintenance Management APIs

#### Initialize Service Categories
```bash
curl -X POST "http://localhost:8080/api/v1/maintenance/categories/init" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"
```

#### Get Service Categories
```bash
curl -X GET "http://localhost:8080/api/v1/maintenance/categories" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"
```

#### Create Custom Service Category
```bash
curl -X POST "http://localhost:8080/api/v1/maintenance/categories" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN" \
  -d "name=Air Conditioning&description=AC repair and maintenance services"
```

#### Create Maintenance Request (Simple)
```bash
curl -X POST "http://localhost:8080/api/v1/maintenance/requests" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TENANT_ACCESS_TOKEN" \
  -d '{
    "apartmentId": "56e34038-17c8-4813-ab84-7e7241d6c3fd",
    "serviceCategoryId": "4c7308d1-41ca-4719-94b0-9be50377be8e",
    "title": "Leaking Faucet in Kitchen",
    "description": "The kitchen faucet has been leaking for 2 days. Water drips continuously.",
    "priority": "MEDIUM"
  }'
```

#### Create Maintenance Request (With Photos)
```bash
curl -X POST "http://localhost:8080/api/v1/maintenance/requests" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TENANT_ACCESS_TOKEN" \
  -d '{
    "apartmentId": "56e34038-17c8-4813-ab84-7e7241d6c3fd",
    "serviceCategoryId": "4c7308d1-41ca-4719-94b0-9be50377be8e",
    "title": "Leaking Faucet in Kitchen",
    "description": "The kitchen faucet has been leaking for 2 days. Water drips continuously.",
    "priority": "MEDIUM",
    "photos": ["data:image/jpeg;base64,/9j/4AAQSkZJRgABA..."]
  }'
```

**⚠️ Important Notes:**
- `apartmentId` and `serviceCategoryId` must be valid UUIDs (36-character format)
- Use actual UUIDs from your database, not placeholder values like "7"
- Photos should be proper base64 data URLs (optional field)
- The requester must have access to the specified apartment

#### Get Maintenance Requests (Manager View)
```bash
curl -X GET "http://localhost:8080/api/v1/maintenance/requests" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"
```

#### Get Maintenance Requests by Tenant
```bash
curl -X GET "http://localhost:8080/api/v1/maintenance/requests/tenant?tenantEmail=jane@example.com" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"
```

#### Get Maintenance Requests by Status
```bash
curl -X GET "http://localhost:8080/api/v1/maintenance/requests/status/OPEN" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"

curl -X GET "http://localhost:8080/api/v1/maintenance/requests/status/IN_PROGRESS" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"
```

#### Get Maintenance Requests by Priority
```bash
curl -X GET "http://localhost:8080/api/v1/maintenance/requests/priority/HIGH" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"

curl -X GET "http://localhost:8080/api/v1/maintenance/requests/priority/LOW" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"
```

#### Get Maintenance Requests by Category
```bash
curl -X GET "http://localhost:8080/api/v1/maintenance/requests/category/{categoryId}" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"
```

#### Get Assigned Maintenance Requests
```bash
curl -X GET "http://localhost:8080/api/v1/maintenance/requests/assigned" \
  -H "Authorization: Bearer ASSIGNED_USER_ACCESS_TOKEN"
```

#### Search Maintenance Requests
```bash
curl -X GET "http://localhost:8080/api/v1/maintenance/requests/search?searchText=leaking" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"
```

#### Update Maintenance Request
```bash
curl -X PUT "http://localhost:8080/api/v1/maintenance/requests/{requestId}" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN" \
  -d '{
    "status": "IN_PROGRESS",
    "assignedTo": "technician-user-uuid",
    "priority": "HIGH",
    "scheduledAt": "2025-08-25T10:00:00.000Z"
  }'
```

#### Add Update to Maintenance Request
```bash
curl -X POST "http://localhost:8080/api/v1/maintenance/requests/{requestId}/updates" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN" \
  -d '{
    "message": "Technician dispatched to apartment. Parts ordered for repair.",
    "updateType": "STATUS_UPDATE"
  }'
```

#### Get Updates for Maintenance Request
```bash
curl -X GET "http://localhost:8080/api/v1/maintenance/requests/{requestId}/updates" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"
```

#### Get Photos for Maintenance Request
```bash
curl -X GET "http://localhost:8080/api/v1/maintenance/requests/{requestId}/photos" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"
```

### Enhanced Search & Filter Examples

#### Complex Apartment Search
```bash
# Search by unit number
curl -X GET "http://localhost:8080/api/v1/apartments/search?searchText=A1" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"

# Search by tenant name
curl -X GET "http://localhost:8080/api/v1/apartments/search?searchText=Jane" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"
```

#### Maintenance Request Filtering
```bash
# Get all open requests
curl -X GET "http://localhost:8080/api/v1/maintenance/requests/status/OPEN" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"

# Get high priority requests
curl -X GET "http://localhost:8080/api/v1/maintenance/requests/priority/HIGH" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"

# Search by keyword
curl -X GET "http://localhost:8080/api/v1/maintenance/requests/search?searchText=plumbing" \
  -H "Authorization: Bearer MANAGER_ACCESS_TOKEN"
```

## API Response Formats

### Successful Building Creation Response
```json
{
  "success": true,
  "message": "Property created successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Sunset Heights",
    "address": "456 Oak Avenue, Downtown",
    "propertyType": "Apartment Complex",
    "totalUnits": 100,
    "totalFloors": 10,
    "yearBuilt": 2015,
    "amenities": "Pool, Gym, Parking, Security",
    "createdAt": "2025-08-22T10:30:00.000Z",
    "updatedAt": "2025-08-22T10:30:00.000Z"
  }
}
```

### Successful Maintenance Request Response
```json
{
  "success": true,
  "message": "Maintenance request created successfully",
  "data": {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "title": "Leaking Faucet in Kitchen",
    "description": "The kitchen faucet has been leaking for 2 days",
    "status": "OPEN",
    "priority": "MEDIUM",
    "createdAt": "2025-08-22T11:00:00.000Z",
    "updatedAt": "2025-08-22T11:00:00.000Z"
  }
}
```

## Available Enums

### Maintenance Request Status
- `OPEN` - New request
- `IN_PROGRESS` - Being worked on
- `SUBMITTED` - Work completed, pending verification
- `RESOLVED` - Completed and verified
- `CANCELLED` - Request cancelled

### Maintenance Request Priority
- `LOW` - Non-urgent issues
- `MEDIUM` - Standard priority
- `HIGH` - Urgent issues
- `CRITICAL` - Emergency repairs

### Maintenance Update Types
- `NOTE` - General note/comment
- `STATUS_UPDATE` - Status change notification
- `ASSIGNMENT` - Assignment notification
- `COMPLETION` - Work completion update

### Occupancy Status
- `VACANT` - Empty apartment
- `OCCUPIED` - Tenant assigned
- `MAINTENANCE` - Under maintenance

## Tenant Dashboard APIs

### Authentication Required
All tenant dashboard endpoints require TENANT role authentication. First login as a tenant:

```bash
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "tenant@example.com",
    "password": "password123",
    "role": "TENANT"
  }'
```

**Use the access token in Authorization header: `Authorization: Bearer YOUR_TOKEN`**

### Get My Maintenance Requests
```bash
curl --location 'http://localhost:8080/api/v1/tenant/dashboard/maintenance/my-requests' \
--header 'Authorization: Bearer YOUR_TENANT_TOKEN' \
--header 'Content-Type: application/json'
```

### Get My Maintenance Requests by Status
```bash
curl --location 'http://localhost:8080/api/v1/tenant/dashboard/maintenance/my-requests/status/OPEN' \
--header 'Authorization: Bearer YOUR_TENANT_TOKEN' \
--header 'Content-Type: application/json'
```

### Get My Maintenance Requests by Priority
```bash
curl --location 'http://localhost:8080/api/v1/tenant/dashboard/maintenance/my-requests/priority/HIGH' \
--header 'Authorization: Bearer YOUR_TENANT_TOKEN' \
--header 'Content-Type: application/json'
```

### Get Specific Maintenance Request
```bash
curl --location 'http://localhost:8080/api/v1/tenant/dashboard/maintenance/my-requests/{requestId}' \
--header 'Authorization: Bearer YOUR_TENANT_TOKEN' \
--header 'Content-Type: application/json'
```

### Get Updates for My Maintenance Request
```bash
curl --location 'http://localhost:8080/api/v1/tenant/dashboard/maintenance/my-requests/{requestId}/updates' \
--header 'Authorization: Bearer YOUR_TENANT_TOKEN' \
--header 'Content-Type: application/json'
```

### Get Photos for My Maintenance Request
```bash
curl --location 'http://localhost:8080/api/v1/tenant/dashboard/maintenance/my-requests/{requestId}/photos' \
--header 'Authorization: Bearer YOUR_TENANT_TOKEN' \
--header 'Content-Type: application/json'
```

### Get Maintenance Summary Dashboard
```bash
curl --location 'http://localhost:8080/api/v1/tenant/dashboard/maintenance/summary' \
--header 'Authorization: Bearer YOUR_TENANT_TOKEN' \
--header 'Content-Type: application/json'
```
**Returns:**
- Total requests count
- Breakdown by status
- Breakdown by priority  
- Recent requests (last 5)

### Get Recent Updates (for Notifications)
```bash
curl --location 'http://localhost:8080/api/v1/tenant/dashboard/maintenance/recent-updates?limit=10' \
--header 'Authorization: Bearer YOUR_TENANT_TOKEN' \
--header 'Content-Type: application/json'
```

### Get Service Categories
```bash
curl --location 'http://localhost:8080/api/v1/tenant/dashboard/maintenance/categories' \
--header 'Authorization: Bearer YOUR_TENANT_TOKEN' \
--header 'Content-Type: application/json'
```

### Get Unread Notification Count
```bash
curl --location 'http://localhost:8080/api/v1/tenant/dashboard/notifications/unread-count' \
--header 'Authorization: Bearer YOUR_TENANT_TOKEN' \
--header 'Content-Type: application/json'
```

**Perfect for notification service integration!**

### Example Tenant Dashboard Response
```json
{
  "success": true,
  "message": "Maintenance summary retrieved successfully",
  "data": {
    "totalRequests": 8,
    "statusBreakdown": {
      "OPEN": 2,
      "IN_PROGRESS": 3,
      "RESOLVED": 2,
      "CANCELLED": 1
    },
    "priorityBreakdown": {
      "LOW": 2,
      "MEDIUM": 4,
      "HIGH": 2
    },
    "recentRequests": [
      {
        "id": "abc123",
        "title": "Leaking Faucet",
        "status": "IN_PROGRESS",
        "priority": "MEDIUM",
        "apartmentUnitNumber": "A101",
        "serviceCategoryName": "Plumbing",
        "createdAt": "2025-08-26T10:00:00Z"
      }
    ]
  }
}
```

## Notes
- Replace `YOUR_ACCESS_TOKEN` with actual tokens from login responses
- Replace `{buildingId}`, `{apartmentId}`, `{requestId}` with actual UUIDs
- The `dob` field uses ISO 8601 format with time
- All monetary values use decimal format
- Date fields (startDate, endDate) use YYYY-MM-DD format
- Photos should be base64 encoded strings
- Ensure the application is running before testing
- All new APIs follow the same authentication pattern as existing APIs
- Tenant dashboard APIs return `MaintenanceRequestResponse` DTOs to avoid Hibernate serialization issues
- All tenant APIs automatically filter data to show only requests belonging to the authenticated tenant
- Notification endpoints return counts and recent updates for integration with notification service