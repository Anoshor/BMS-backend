# 🧪 **Manual Test Steps: Building → Apartments → Tenant Connection**

## **Prerequisites**
- Backend running on `http://localhost:8080`
- Use tools like Postman, curl, or API testing tool
- Replace `YOUR_ACCESS_TOKEN` with actual tokens from responses

---

## **Step 1: 👤 Create & Login Manager**

### **1.1 Register Manager**
```bash
curl -X POST "http://localhost:8080/api/v1/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testmanager@example.com",
    "contactNum": "9876543210", 
    "password": "TestPass123!",
    "firstName": "Test",
    "lastName": "Manager",
    "dob": "1990-01-15T00:00:00.000Z",
    "gender": "male",
    "role": "MANAGER"
  }'
```

### **1.2 Login Manager**
```bash
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "testmanager@example.com",
    "password": "TestPass123!",
    "role": "MANAGER"
  }'
```
**📋 Copy `accessToken` from response → Use as `MANAGER_TOKEN`**

---

## **Step 2: 🏢 Create Property Building**

```bash
curl -X POST "http://localhost:8080/api/v1/properties/buildings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer MANAGER_TOKEN" \
  -d '{
    "name": "Sunset Heights",
    "address": "456 Oak Avenue, Downtown",
    "propertyType": "Apartment Complex",
    "residentialType": "residential", 
    "totalUnits": 50,
    "totalFloors": 10,
    "yearBuilt": 2020,
    "amenities": "Pool, Gym, Parking, Security"
  }'
```
**📋 Copy `id` from response → Use as `PROPERTY_ID`**

---

## **Step 3: 🏠 Create Apartments in Building**

### **3.1 Create Apartment A101 (2BHK)**
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
    "occupancyStatus": "VACANT"
  }'
```
**📋 Copy `id` from response → Use as `APARTMENT_A101_ID`**

### **3.2 Create Apartment A102 (1BHK)**
```bash
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
**📋 Copy `id` from response → Use as `APARTMENT_A102_ID`**

---

## **Step 4: 👥 Create Tenant Account**

### **4.1 Register Tenant**
```bash
curl -X POST "http://localhost:8080/api/v1/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testtenant@example.com",
    "contactNum": "9876543211",
    "password": "TestPass123!",
    "firstName": "John", 
    "lastName": "Tenant",
    "dob": "1995-06-08T00:00:00.000Z",
    "gender": "male",
    "role": "TENANT"
  }'
```

---

## **Step 5: 🔗 Connect Tenant to Apartment**

### **5.1 Connect Tenant to Apartment A101**
```bash
curl -X POST "http://localhost:8080/api/v1/tenants/connect" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer MANAGER_TOKEN" \
  -d '{
    "tenantEmail": "testtenant@example.com",
    "apartmentId": "APARTMENT_A101_ID",
    "startDate": "2025-09-15",
    "endDate": "2026-09-14",
    "monthlyRent": 1500.00,
    "securityDeposit": 3000.00,
    "notes": "1-year lease for apartment A101"
  }'
```

**✅ Expected Response:**
```json
{
  "success": true,
  "data": null,
  "message": "Tenant connected to property successfully",
  "timestamp": "2025-09-10T..."
}
```

---

## **Step 6: ✅ Verification Tests**

### **6.1 Check Apartment A101 is Occupied**
```bash
curl -X GET "http://localhost:8080/api/v1/apartments/APARTMENT_A101_ID" \
  -H "Authorization: Bearer MANAGER_TOKEN"
```
**Should show:** `"occupancyStatus": "OCCUPIED"` and tenant details

### **6.2 Check All Apartments in Building**
```bash
curl -X GET "http://localhost:8080/api/v1/apartments/property/PROPERTY_ID" \
  -H "Authorization: Bearer MANAGER_TOKEN"  
```
**Should show:** A101 = OCCUPIED, A102 = VACANT

### **6.3 Search Connected Tenants**
```bash
curl -X GET "http://localhost:8080/api/v1/tenants/search?searchText=John" \
  -H "Authorization: Bearer MANAGER_TOKEN"
```
**Should return:** Tenant connection details

### **6.4 Get Tenant's Properties (Optional)**
```bash
# First login as tenant to get token
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "testtenant@example.com",
    "password": "TestPass123!",
    "role": "TENANT"
  }'

# Then check tenant's properties
curl -X GET "http://localhost:8080/api/v1/tenants/my-properties" \
  -H "Authorization: Bearer TENANT_TOKEN"
```

---

## **📊 Expected Results Summary:**

| Step | Expected Result |
|------|-----------------|
| **Building Created** | ✅ Property ID returned |
| **Apartments Created** | ✅ A101 & A102 with VACANT status |
| **Tenant Connected** | ✅ Connection successful message |
| **A101 Status** | ✅ Changed to OCCUPIED with tenant details |
| **A102 Status** | ✅ Still VACANT |
| **Search Results** | ✅ Returns tenant connection |

---

## **🔧 Troubleshooting:**

### **❌ "Apartment not found"**
- Check `PROPERTY_ID` is correct
- Ensure apartment was created successfully

### **❌ "Tenant not found"**  
- Check tenant email is exact match
- Ensure tenant account was created

### **❌ "Apartment already occupied"**
- Apartment already has a tenant
- Use a different vacant apartment

### **❌ "You don't have permission"**
- Apartment doesn't belong to this manager
- Check `PROPERTY_ID` ownership

---

## **🚀 Quick Test Script:**

Save this as `quick-test.sh`:
```bash
#!/bin/bash
BASE_URL="http://localhost:8080/api/v1"

# Replace with your actual tokens and IDs
MANAGER_TOKEN="your-manager-token-here"
PROPERTY_ID="your-property-id-here"  
APARTMENT_ID="your-apartment-id-here"

# Test connection
curl -X POST "$BASE_URL/tenants/connect" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $MANAGER_TOKEN" \
  -d '{
    "tenantEmail": "testtenant@example.com",
    "apartmentId": "'$APARTMENT_ID'",
    "startDate": "2025-09-15",
    "endDate": "2026-09-14", 
    "monthlyRent": 1500.00,
    "securityDeposit": 3000.00,
    "notes": "Test connection"
  }'
```

**Perfect flow: Building → Apartments → Tenant Assignment! 🎉**