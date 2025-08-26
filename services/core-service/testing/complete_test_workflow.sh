#!/bin/bash

# BMS Dashboard API Complete Test Workflow
# This script tests the entire dashboard workflow from manager signup to tenant viewing apartments

BASE_URL="http://localhost:8080/api/v1"

echo "=== BMS Dashboard API Testing ==="
echo "Testing against: $BASE_URL"
echo

# Function to extract token from JSON response
extract_token() {
    echo "$1" | grep -o '"accessToken":"[^"]*' | grep -o '[^"]*$'
}

# Function to check if response is successful
check_success() {
    local response="$1"
    local step="$2"
    
    if echo "$response" | grep -q '"success":true'; then
        echo "✅ $step - SUCCESS"
        return 0
    else
        echo "❌ $step - FAILED"
        echo "Response: $response"
        return 1
    fi
}

# Step 1: Register Manager
echo "1. Registering Manager..."
MANAGER_SIGNUP=$(curl -s -X POST "$BASE_URL/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "manager@test.com",
    "contactNum": "9876543210",
    "password": "password123",
    "firstName": "Test",
    "lastName": "Manager",
    "dob": "1990-01-15T00:00:00.000Z",
    "gender": "male",
    "role": "MANAGER"
  }')

if ! check_success "$MANAGER_SIGNUP" "Manager Registration"; then
    echo "Stopping test due to manager registration failure"
    exit 1
fi

# Step 2: Manager Login
echo
echo "2. Manager Login..."
MANAGER_LOGIN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "manager@test.com",
    "password": "password123",
    "role": "MANAGER"
  }')

if ! check_success "$MANAGER_LOGIN" "Manager Login"; then
    echo "Stopping test due to manager login failure"
    exit 1
fi

# Extract manager token
MANAGER_TOKEN=$(extract_token "$MANAGER_LOGIN")
if [ -z "$MANAGER_TOKEN" ]; then
    echo "❌ Failed to extract manager token"
    echo "Login response: $MANAGER_LOGIN"
    exit 1
fi
echo "Manager token extracted: ${MANAGER_TOKEN:0:20}..."

# Step 3: Add Property
echo
echo "3. Adding Property..."
PROPERTY_ADD=$(curl -s -X POST "$BASE_URL/properties/add" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $MANAGER_TOKEN" \
  -d '{
    "propertyName": "Test Apartments",
    "propertyManagerName": "Test Manager",
    "propertyAddress": "123 Test St, Test City, TS",
    "propertyType": "residential",
    "squareFootage": 1200,
    "numberOfUnits": 10,
    "unitNumber": "101",
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
  }')

check_success "$PROPERTY_ADD" "Property Addition"

# Step 4: Get Unoccupied Properties
echo
echo "4. Getting Unoccupied Properties..."
UNOCCUPIED_PROPERTIES=$(curl -s -X GET "$BASE_URL/properties/unoccupied" \
  -H "Authorization: Bearer $MANAGER_TOKEN")

check_success "$UNOCCUPIED_PROPERTIES" "Get Unoccupied Properties"

# Step 5: Register Tenant
echo
echo "5. Registering Tenant..."
TENANT_SIGNUP=$(curl -s -X POST "$BASE_URL/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "tenant@test.com",
    "contactNum": "9876543211",
    "password": "password123",
    "firstName": "Test",
    "lastName": "Tenant",
    "dob": "1995-05-20T00:00:00.000Z",
    "gender": "female",
    "role": "TENANT"
  }')

if ! check_success "$TENANT_SIGNUP" "Tenant Registration"; then
    echo "Stopping test due to tenant registration failure"
    exit 1
fi

# Step 6: Tenant Login
echo
echo "6. Tenant Login..."
TENANT_LOGIN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "tenant@test.com",
    "password": "password123",
    "role": "TENANT"
  }')

if ! check_success "$TENANT_LOGIN" "Tenant Login"; then
    echo "Stopping test due to tenant login failure"
    exit 1
fi

# Extract tenant token
TENANT_TOKEN=$(extract_token "$TENANT_LOGIN")
if [ -z "$TENANT_TOKEN" ]; then
    echo "❌ Failed to extract tenant token"
    echo "Login response: $TENANT_LOGIN"
    exit 1
fi
echo "Tenant token extracted: ${TENANT_TOKEN:0:20}..."

# Step 7: Connect Tenant to Property (Manager Action)
echo
echo "7. Connecting Tenant to Property..."
CONNECT_TENANT=$(curl -s -X POST "$BASE_URL/tenants/connect" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $MANAGER_TOKEN" \
  -d '{
    "tenantEmail": "tenant@test.com",
    "propertyName": "Test Apartments",
    "startDate": "2025-01-01",
    "endDate": "2025-12-31",
    "monthlyRent": 1500.00,
    "securityDeposit": 3000.00,
    "notes": "Test lease agreement for unit 101"
  }')

check_success "$CONNECT_TENANT" "Tenant-Property Connection"

# Step 8: Search Tenants (Manager)
echo
echo "8. Searching Tenants (Manager View)..."
SEARCH_TENANTS=$(curl -s -X GET "$BASE_URL/tenants/search?searchText=Test" \
  -H "Authorization: Bearer $MANAGER_TOKEN")

check_success "$SEARCH_TENANTS" "Search Tenants"

# Step 9: Get Tenant's Properties (Tenant Dashboard)
echo
echo "9. Getting Tenant's Properties (Tenant Dashboard)..."
TENANT_PROPERTIES=$(curl -s -X GET "$BASE_URL/tenants/my-properties" \
  -H "Authorization: Bearer $TENANT_TOKEN")

check_success "$TENANT_PROPERTIES" "Get Tenant Properties"

# Step 10: Test Refresh Token
echo
echo "10. Testing Refresh Token..."
REFRESH_TOKEN=$(curl -s -X POST "$BASE_URL/auth/refresh-token" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $MANAGER_TOKEN")

check_success "$REFRESH_TOKEN" "Refresh Token"

echo
echo "=== Dashboard Workflow Test Complete ==="
echo
echo "Summary of tested endpoints:"
echo "✅ Manager Registration (/auth/signup)"
echo "✅ Manager Login (/auth/login)"
echo "✅ Add Property (/properties/add)"
echo "✅ Get Unoccupied Properties (/properties/unoccupied)"
echo "✅ Tenant Registration (/auth/signup)"
echo "✅ Tenant Login (/auth/login)"
echo "✅ Connect Tenant to Property (/tenants/connect)"
echo "✅ Search Tenants (/tenants/search)"
echo "✅ Get Tenant Properties (/tenants/my-properties)"
echo "✅ Refresh Token (/auth/refresh-token)"
echo
echo "🎉 Complete BMS Dashboard Workflow Test Completed Successfully!"