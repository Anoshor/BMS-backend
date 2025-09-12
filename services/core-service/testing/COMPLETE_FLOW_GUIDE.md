# 🏗️ **Complete BMS Tenant-Apartment Connection Flow**

## **✅ Updated Architecture:**
```
Property Building → Apartments → Tenant Assignment to Specific Apartment
```

---

## **🚀 Step-by-Step API Testing Flow**

### **Step 1: Manager Setup**

#### **1.1 Manager Login**
```bash
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "manager@example.com",
    "password": "password123",
    "role": "MANAGER"
  }'
```
**📋 Save the `accessToken` from response as `MANAGER_TOKEN`**

---

### **Step 2: Create Property Building**

#### **2.1 Create Property Building**
```bash
curl -X POST "http://localhost:8080/api/v1/properties/buildings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer MANAGER_TOKEN" \
  -d '{
    "name": "Sunset Heights",
    "address": "456 Oak Avenue, Downtown",
    "propertyType": "Apartment Complex",
    "residentialType": "residential",
    "totalUnits": 100,
    "totalFloors": 10,
    "yearBuilt": 2015,
    "amenities": "Pool, Gym, Parking, Security"
  }'
```
**📋 Save the `id` from response as `PROPERTY_ID`**

---

### **Step 3: Create Apartments in Property**

#### **3.1 Create Apartment A101**
```bash
curl -X POST "http://localhost:8080/api/v1/apartments" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer MANAGER_TOKEN" \
  -d '{
    "propertyId": "PROPERTY_ID",
    "unitNumber": "A101",
    "unitType": "2BHK",
    "floor": 1,
    "bedrooms": 2,
    "bathrooms": 2.0,
    "squareFootage": 1200,
    "furnished": "Semi-Furnished",
    "balcony": "Yes",
    "rent": 1500.00,
    "securityDeposit": 3000.00,
    "maintenanceCharges": 200.00,
    "occupancyStatus": "VACANT",
    "utilityMeterNumbers": "{\"electric\":\"ELE123\",\"gas\":\"GAS456\",\"water\":\"WAT789\"}"
  }'
```
**📋 Save the `id` from response as `APARTMENT_ID`**

#### **3.2 Create More Apartments (Optional)**
```bash
# Apartment A102
curl -X POST "http://localhost:8080/api/v1/apartments" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer MANAGER_TOKEN" \
  -d '{
    "propertyId": "PROPERTY_ID",
    "unitNumber": "A102",
    "unitType": "1BHK",
    "floor": 1,
    "bedrooms": 1,
    "bathrooms": 1.0,
    "squareFootage": 800,
    "furnished": "Unfurnished",
    "balcony": "No",
    "rent": 1200.00,
    "securityDeposit": 2400.00,
    "maintenanceCharges": 150.00,
    "occupancyStatus": "VACANT"
  }'
```

---

### **Step 4: Create Tenant Account**

#### **4.1 Tenant Registration**
```bash
curl -X POST "http://localhost:8080/api/v1/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "tenant@example.com",
    "contactNum": "9876543210",
    "password": "password123",
    "firstName": "Jane",
    "lastName": "Tenant",
    "dob": "1995-06-08T00:00:00.000Z",
    "gender": "female",
    "role": "TENANT"
  }'
```

#### **4.2 Tenant Login (Optional - for verification)**
```bash
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "tenant@example.com",
    "password": "password123",
    "role": "TENANT"
  }'
```

---

### **Step 5: 🎯 Connect Tenant to Specific Apartment**

#### **5.1 Connect Tenant to Apartment A101**
```bash
curl -X POST "http://localhost:8080/api/v1/tenants/connect" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer MANAGER_TOKEN" \
  -d '{
    "tenantEmail": "tenant@example.com",
    "apartmentId": "APARTMENT_ID",
    "startDate": "2025-09-10",
    "endDate": "2026-09-10",
    "monthlyRent": 1500.00,
    "securityDeposit": 3000.00,
    "notes": "Standard lease agreement for unit A101"
  }'
```

**✅ Expected Success Response:**
```json
{
  "success": true,
  "data": null,
  "message": "Tenant connected to property successfully",
  "timestamp": "2025-09-10T..."
}
```

---

### **Step 6: Verify Connection**

#### **6.1 Check Apartment is Now Occupied**
```bash
curl -X GET "http://localhost:8080/api/v1/apartments/APARTMENT_ID" \
  -H "Authorization: Bearer MANAGER_TOKEN"
```
**Should show `occupancyStatus: "OCCUPIED"` and tenant details**

#### **6.2 Search Connected Tenants**
```bash
curl -X GET "http://localhost:8080/api/v1/tenants/search?searchText=Jane" \
  -H "Authorization: Bearer MANAGER_TOKEN"
```

#### **6.3 Get Tenant's Properties (Login as Tenant)**
```bash
curl -X GET "http://localhost:8080/api/v1/tenants/my-properties" \
  -H "Authorization: Bearer TENANT_TOKEN"
```

---

## **🔄 Alternative: Direct Apartment Assignment**

If you prefer the direct apartment assignment method:

```bash
curl -X POST "http://localhost:8080/api/v1/apartments/APARTMENT_ID/tenant" \
  -H "Authorization: Bearer MANAGER_TOKEN" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  --data-urlencode 'tenantName=Jane Tenant' \
  --data-urlencode 'tenantEmail=tenant@example.com' \
  --data-urlencode 'tenantPhone=9876543210'
```

---

## **📋 Complete Test Script**

```bash
#!/bin/bash

# Configuration
BASE_URL="http://localhost:8080"
MANAGER_EMAIL="manager@example.com"
TENANT_EMAIL="tenant@example.com"

echo "🏗️ Starting BMS Tenant-Apartment Connection Flow Test"

# 1. Manager Login
echo "Step 1: Manager Login"
MANAGER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "'$MANAGER_EMAIL'",
    "password": "password123",
    "role": "MANAGER"
  }')

MANAGER_TOKEN=$(echo "$MANAGER_RESPONSE" | jq -r '.data.accessToken')
echo "✅ Manager Token: ${MANAGER_TOKEN:0:20}..."

# 2. Create Property Building
echo "Step 2: Create Property Building"
PROPERTY_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/properties/buildings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $MANAGER_TOKEN" \
  -d '{
    "name": "Sunset Heights",
    "address": "456 Oak Avenue, Downtown",
    "propertyType": "Apartment Complex",
    "residentialType": "residential",
    "totalUnits": 100,
    "totalFloors": 10,
    "yearBuilt": 2015,
    "amenities": "Pool, Gym, Parking, Security"
  }')

PROPERTY_ID=$(echo "$PROPERTY_RESPONSE" | jq -r '.data.id')
echo "✅ Property ID: $PROPERTY_ID"

# 3. Create Apartment
echo "Step 3: Create Apartment A101"
APARTMENT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/apartments" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $MANAGER_TOKEN" \
  -d '{
    "propertyId": "'$PROPERTY_ID'",
    "unitNumber": "A101",
    "unitType": "2BHK",
    "floor": 1,
    "bedrooms": 2,
    "bathrooms": 2.0,
    "squareFootage": 1200,
    "furnished": "Semi-Furnished",
    "balcony": "Yes",
    "rent": 1500.00,
    "securityDeposit": 3000.00,
    "maintenanceCharges": 200.00,
    "occupancyStatus": "VACANT"
  }')

APARTMENT_ID=$(echo "$APARTMENT_RESPONSE" | jq -r '.data.id')
echo "✅ Apartment ID: $APARTMENT_ID"

# 4. Connect Tenant to Apartment
echo "Step 4: Connect Tenant to Apartment"
CONNECT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/tenants/connect" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $MANAGER_TOKEN" \
  -d '{
    "tenantEmail": "'$TENANT_EMAIL'",
    "apartmentId": "'$APARTMENT_ID'",
    "startDate": "2025-09-10",
    "endDate": "2026-09-10",
    "monthlyRent": 1500.00,
    "securityDeposit": 3000.00,
    "notes": "Test lease agreement"
  }')

echo "✅ Connection Result:"
echo "$CONNECT_RESPONSE" | jq '.'

echo "🎉 Flow completed successfully!"
```

---

## **🚨 What Changed:**

### **Before (Incorrect):**
```json
{
  "tenantEmail": "tenant@example.com",
  "propertyName": "Sunset Heights",  // ❌ Too vague
  "startDate": "2025-09-10",
  "endDate": "2026-09-10",
  "monthlyRent": 1500.00,
  "securityDeposit": 3000.00
}
```

### **After (Correct):**
```json
{
  "tenantEmail": "tenant@example.com",
  "apartmentId": "550e8400-e29b-41d4-a716-446655440000",  // ✅ Specific apartment
  "startDate": "2025-09-10",
  "endDate": "2026-09-10",
  "monthlyRent": 1500.00,
  "securityDeposit": 3000.00,
  "notes": "Lease for specific unit with all details"
}
```

---

## **🎯 Benefits of New Flow:**

1. **🏠 Precise Assignment** - Tenants connected to specific apartments, not just buildings
2. **📊 Better Tracking** - Exact unit details (room numbers, specifications) linked
3. **🔒 Occupancy Control** - Prevents double-booking of apartments  
4. **📈 Data Integrity** - Apartment occupancy status automatically updated
5. **🏗️ Scalability** - Supports multiple buildings with hundreds of units

**Your frontend can now show exactly which unit (A101, A102, etc.) with all specifications the tenant is assigned to!** 🎉