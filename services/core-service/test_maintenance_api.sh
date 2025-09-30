#!/bin/bash

BASE_URL="http://localhost:8080"

echo "=== BMS Backend API Test - Maintenance Details with Landlord Info ==="
echo ""

# Function to make API calls with error handling
make_api_call() {
    local method=$1
    local endpoint=$2
    local data=$3
    local auth_header=$4

    if [ -n "$auth_header" ]; then
        response=$(curl -s -w "\n%{http_code}" -X $method \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $auth_header" \
            ${data:+-d "$data"} \
            "$BASE_URL$endpoint")
    else
        response=$(curl -s -w "\n%{http_code}" -X $method \
            -H "Content-Type: application/json" \
            ${data:+-d "$data"} \
            "$BASE_URL$endpoint")
    fi

    # Split response and status code
    status_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)

    echo "Status: $status_code"
    echo "Response: $body" | jq . 2>/dev/null || echo "Response: $body"
    echo ""

    # Return the body for further processing
    echo "$body"
}

# Test 1: Health check
echo "1. Testing Health Endpoint..."
make_api_call "GET" "/api/v1/health" > /dev/null

# Test 2: Register Manager (Property Manager)
echo "2. Creating Manager Account..."
manager_data='{
  "firstName": "John",
  "lastName": "Manager",
  "email": "manager@test.com",
  "phone": "1234567890",
  "password": "password123",
  "role": "PROPERTY_MANAGER",
  "deviceType": "web",
  "deviceId": "test-device-1"
}'

manager_response=$(make_api_call "POST" "/auth/signup" "$manager_data")

# Test 3: Register Tenant
echo "3. Creating Tenant Account..."
tenant_data='{
  "firstName": "Jane",
  "lastName": "Tenant",
  "email": "tenant@test.com",
  "phone": "0987654321",
  "password": "password123",
  "role": "TENANT",
  "deviceType": "web",
  "deviceId": "test-device-2"
}'

tenant_response=$(make_api_call "POST" "/auth/signup" "$tenant_data")

# Extract token from tenant response (tenants get tokens immediately)
tenant_token=$(echo "$tenant_response" | jq -r '.data.accessToken // empty' 2>/dev/null)

# Test 4: Login as Manager (since managers need admin approval, we'll try direct login with the created account)
echo "4. Attempting Manager Login..."
manager_login_data='{
  "identifier": "manager@test.com",
  "password": "password123",
  "role": "PROPERTY_MANAGER",
  "deviceType": "web",
  "deviceId": "test-device-1"
}'

manager_login_response=$(make_api_call "POST" "/auth/login" "$manager_login_data")
manager_token=$(echo "$manager_login_response" | jq -r '.data.accessToken // empty' 2>/dev/null)

if [ -z "$manager_token" ] || [ "$manager_token" = "null" ]; then
    echo "Manager login failed. Let's check what users exist in the database via API calls..."

    # If manager login fails, we need to activate the account manually
    # For now, let's just use the tenant token to see what data is available
    echo "5. Using Tenant Token to Explore Available Data..."

    if [ -n "$tenant_token" ] && [ "$tenant_token" != "null" ]; then
        echo "Tenant logged in successfully. Token: ${tenant_token:0:20}..."

        # Get all maintenance requests (this will likely be empty or access denied)
        echo "6. Testing Maintenance Requests Endpoint..."
        make_api_call "GET" "/maintenance/requests" "" "$tenant_token" > /dev/null

        echo "7. Since we need manager access, let's try to initialize some default categories first..."
        make_api_call "POST" "/maintenance/categories/init" "" "$tenant_token" > /dev/null

    else
        echo "Both manager and tenant login failed. Checking health again..."
        make_api_call "GET" "/api/v1/health" > /dev/null
    fi
else
    echo "Manager logged in successfully. Token: ${manager_token:0:20}..."

    # Test 5: Initialize default service categories
    echo "5. Initializing Default Service Categories..."
    make_api_call "POST" "/maintenance/categories/init" "" "$manager_token" > /dev/null

    # Test 6: Get all service categories
    echo "6. Getting Service Categories..."
    categories_response=$(make_api_call "GET" "/maintenance/categories" "" "$manager_token")

    # Extract first category ID for creating maintenance request
    category_id=$(echo "$categories_response" | jq -r '.data[0].id // empty' 2>/dev/null)

    if [ -n "$category_id" ] && [ "$category_id" != "null" ]; then
        echo "Found category ID: $category_id"

        # Test 7: Create a property first (assuming PropertyController exists)
        echo "7. Attempting to create a property..."
        property_data='{
          "name": "Test Apartment Building",
          "propertyType": "Apartment",
          "address": "123 Test Street, Test City, TC 12345",
          "totalUnits": 10,
          "constructionYear": 2020
        }'

        # This endpoint might not exist, but let's try common patterns
        property_response=$(make_api_call "POST" "/properties" "$property_data" "$manager_token")

        # Test 8: Try to get maintenance requests (should be empty initially)
        echo "8. Getting Current Maintenance Requests..."
        requests_response=$(make_api_call "GET" "/maintenance/requests" "" "$manager_token")

        # If there are existing requests, test the details endpoint
        request_id=$(echo "$requests_response" | jq -r '.data[0].id // empty' 2>/dev/null)

        if [ -n "$request_id" ] && [ "$request_id" != "null" ]; then
            echo "9. Found existing maintenance request: $request_id"
            echo "Testing Maintenance Details API..."
            details_response=$(make_api_call "GET" "/maintenance/requests/$request_id/details" "" "$manager_token")

            echo "=== FINAL RESULT: Maintenance Details Response ==="
            echo "$details_response" | jq . 2>/dev/null || echo "$details_response"
        else
            echo "9. No existing maintenance requests found. Test complete - need to create more test data manually."
        fi
    else
        echo "No service categories available. Check category initialization."
    fi
fi

echo ""
echo "=== Test Complete ==="