#!/bin/bash

# Individual API Test Scripts
# Run specific tests individually for debugging

BASE_URL="http://localhost:8080/api/v1"

# Test 1: Manager Registration
test_manager_registration() {
    echo "Testing Manager Registration..."
    curl -X POST "$BASE_URL/auth/signup" \
      -H "Content-Type: application/json" \
      -d '{
        "email": "manager.individual@test.com",
        "contactNum": "9876543210",
        "password": "password123",
        "firstName": "Individual",
        "lastName": "Manager",
        "dob": "1990-01-15T00:00:00.000Z",
        "gender": "male",
        "role": "MANAGER"
      }' | jq .
}

# Test 2: Manager Login
test_manager_login() {
    echo "Testing Manager Login..."
    curl -X POST "$BASE_URL/auth/login" \
      -H "Content-Type: application/json" \
      -d '{
        "identifier": "manager.individual@test.com",
        "password": "password123",
        "role": "MANAGER"
      }' | jq .
}

# Test 3: Add Property (requires token)
test_add_property() {
    local token="$1"
    if [ -z "$token" ]; then
        echo "Error: Token required for this test"
        return 1
    fi
    
    echo "Testing Add Property..."
    curl -X POST "$BASE_URL/properties/add" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $token" \
      -d '{
        "propertyName": "Individual Test Apartments",
        "propertyManagerName": "Individual Manager",
        "propertyAddress": "456 Individual St, Test City, TS",
        "propertyType": "residential",
        "squareFootage": 1500,
        "numberOfUnits": 20,
        "unitNumber": "202",
        "unitType": "3BHK",
        "floor": 2,
        "bedrooms": 3,
        "bathrooms": 2,
        "furnished": "semi-furnished",
        "balcony": "yes",
        "rent": 2000.00,
        "securityDeposit": 4000.00,
        "maintenanceCharges": 300.00,
        "occupancy": "vacant",
        "utilityMeterNumber": 67890
      }' | jq .
}

# Test 4: Get Unoccupied Properties
test_get_unoccupied_properties() {
    local token="$1"
    if [ -z "$token" ]; then
        echo "Error: Token required for this test"
        return 1
    fi
    
    echo "Testing Get Unoccupied Properties..."
    curl -X GET "$BASE_URL/properties/unoccupied" \
      -H "Authorization: Bearer $token" | jq .
}

# Test 5: Tenant Registration
test_tenant_registration() {
    echo "Testing Tenant Registration..."
    curl -X POST "$BASE_URL/auth/signup" \
      -H "Content-Type: application/json" \
      -d '{
        "email": "tenant.individual@test.com",
        "contactNum": "9876543211",
        "password": "password123",
        "firstName": "Individual",
        "lastName": "Tenant",
        "dob": "1995-05-20T00:00:00.000Z",
        "gender": "female",
        "role": "TENANT"
      }' | jq .
}

# Test 6: Tenant Login
test_tenant_login() {
    echo "Testing Tenant Login..."
    curl -X POST "$BASE_URL/auth/login" \
      -H "Content-Type: application/json" \
      -d '{
        "identifier": "tenant.individual@test.com",
        "password": "password123",
        "role": "TENANT"
      }' | jq .
}

# Test 7: Connect Tenant to Property
test_connect_tenant() {
    local manager_token="$1"
    if [ -z "$manager_token" ]; then
        echo "Error: Manager token required for this test"
        return 1
    fi
    
    echo "Testing Connect Tenant to Property..."
    curl -X POST "$BASE_URL/tenants/connect" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $manager_token" \
      -d '{
        "tenantEmail": "tenant.individual@test.com",
        "propertyName": "Individual Test Apartments",
        "startDate": "2025-02-01",
        "endDate": "2026-01-31",
        "monthlyRent": 2000.00,
        "securityDeposit": 4000.00,
        "notes": "Individual test lease for unit 202"
      }' | jq .
}

# Test 8: Search Tenants
test_search_tenants() {
    local manager_token="$1"
    if [ -z "$manager_token" ]; then
        echo "Error: Manager token required for this test"
        return 1
    fi
    
    echo "Testing Search Tenants..."
    curl -X GET "$BASE_URL/tenants/search?searchText=Individual" \
      -H "Authorization: Bearer $manager_token" | jq .
}

# Test 9: Get Tenant Properties
test_get_tenant_properties() {
    local tenant_token="$1"
    if [ -z "$tenant_token" ]; then
        echo "Error: Tenant token required for this test"
        return 1
    fi
    
    echo "Testing Get Tenant Properties..."
    curl -X GET "$BASE_URL/tenants/my-properties" \
      -H "Authorization: Bearer $tenant_token" | jq .
}

# Test 10: Refresh Token
test_refresh_token() {
    local token="$1"
    if [ -z "$token" ]; then
        echo "Error: Token required for this test"
        return 1
    fi
    
    echo "Testing Refresh Token..."
    curl -X POST "$BASE_URL/auth/refresh-token" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $token" | jq .
}

# Help function
show_help() {
    echo "Individual API Test Scripts"
    echo
    echo "Usage: $0 [test_name] [token]"
    echo
    echo "Available tests:"
    echo "  manager_registration    - Test manager signup"
    echo "  manager_login          - Test manager login"
    echo "  add_property          - Test add property (requires manager token)"
    echo "  unoccupied_properties - Test get unoccupied properties (requires manager token)"
    echo "  tenant_registration   - Test tenant signup"
    echo "  tenant_login         - Test tenant login"
    echo "  connect_tenant       - Test connect tenant to property (requires manager token)"
    echo "  search_tenants       - Test search tenants (requires manager token)"
    echo "  tenant_properties    - Test get tenant properties (requires tenant token)"
    echo "  refresh_token        - Test refresh token (requires any token)"
    echo "  help                 - Show this help"
    echo
    echo "Examples:"
    echo "  $0 manager_registration"
    echo "  $0 add_property YOUR_MANAGER_TOKEN"
    echo "  $0 tenant_properties YOUR_TENANT_TOKEN"
}

# Main execution
case "$1" in
    "manager_registration")
        test_manager_registration
        ;;
    "manager_login")
        test_manager_login
        ;;
    "add_property")
        test_add_property "$2"
        ;;
    "unoccupied_properties")
        test_get_unoccupied_properties "$2"
        ;;
    "tenant_registration")
        test_tenant_registration
        ;;
    "tenant_login")
        test_tenant_login
        ;;
    "connect_tenant")
        test_connect_tenant "$2"
        ;;
    "search_tenants")
        test_search_tenants "$2"
        ;;
    "tenant_properties")
        test_get_tenant_properties "$2"
        ;;
    "refresh_token")
        test_refresh_token "$2"
        ;;
    "help"|"")
        show_help
        ;;
    *)
        echo "Unknown test: $1"
        echo "Run '$0 help' for available tests"
        exit 1
        ;;
esac