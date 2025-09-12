#!/bin/bash

# BMS API Test Suite
# Usage: ./api-tests.sh [base_url]
# Example: ./api-tests.sh http://localhost:8080

set -e

# Configuration
BASE_URL="${1:-http://localhost:8080}"
API_BASE="$BASE_URL/api/v1"
TEST_RESULTS=()

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Utility functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[PASS]${NC} $1"
    ((PASSED_TESTS++))
}

log_error() {
    echo -e "${RED}[FAIL]${NC} $1"
    ((FAILED_TESTS++))
}

log_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# Test function
run_test() {
    local test_name="$1"
    local expected_status="$2"
    local method="$3"
    local endpoint="$4"
    local headers="$5"
    local data="$6"
    
    ((TOTAL_TESTS++))
    log_info "Running test: $test_name"
    
    # Build curl command
    local curl_cmd="curl -s -w \"%{http_code}\" -X $method"
    
    if [[ -n "$headers" ]]; then
        curl_cmd="$curl_cmd $headers"
    fi
    
    if [[ -n "$data" ]]; then
        curl_cmd="$curl_cmd -d '$data'"
    fi
    
    curl_cmd="$curl_cmd $API_BASE$endpoint"
    
    # Execute request
    local response=$(eval $curl_cmd)
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    # Check result
    if [[ "$status_code" == "$expected_status" ]]; then
        log_success "$test_name (Status: $status_code)"
        TEST_RESULTS+=("✅ $test_name")
        return 0
    else
        log_error "$test_name (Expected: $expected_status, Got: $status_code)"
        log_error "Response: $response_body"
        TEST_RESULTS+=("❌ $test_name")
        return 1
    fi
}

# Test variables
MANAGER_TOKEN=""
TENANT_TOKEN=""
MANAGER_EMAIL="testmanager@bms.com"
TENANT_EMAIL="testtenant@bms.com"
PROPERTY_NAME="Test Property Heights"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}    BMS API Test Suite Started${NC}"
echo -e "${BLUE}    Base URL: $BASE_URL${NC}"
echo -e "${BLUE}========================================${NC}"

# ===== AUTHENTICATION TESTS =====
echo -e "\n${YELLOW}=== AUTHENTICATION TESTS ===${NC}"

# Test 1: Manager Registration
run_test "Manager Registration" "200" "POST" "/auth/signup" \
    "-H 'Content-Type: application/json'" \
    '{
        "email": "'$MANAGER_EMAIL'",
        "contactNum": "9876543210",
        "password": "TestPass123!",
        "firstName": "Test",
        "lastName": "Manager",
        "dob": "1990-01-15T00:00:00.000Z",
        "gender": "male",
        "role": "MANAGER"
    }'

# Test 2: Manager Email Verification (mock)
run_test "Manager Email Verification" "200" "POST" "/auth/verify-email?email=${MANAGER_EMAIL}&otp=123456" \
    "-H 'Content-Type: application/json'" \
    '{
        "email": "'$MANAGER_EMAIL'",
        "otpCode": "123456"
    }'

# Test 3: Manager Login
log_info "Getting Manager Access Token..."
MANAGER_RESPONSE=$(curl -s -X POST "$API_BASE/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
        "identifier": "'$MANAGER_EMAIL'",
        "password": "TestPass123!",
        "role": "MANAGER"
    }')

if echo "$MANAGER_RESPONSE" | grep -q "accessToken"; then
    MANAGER_TOKEN=$(echo "$MANAGER_RESPONSE" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
    log_success "Manager Login - Token acquired"
    ((PASSED_TESTS++))
else
    log_error "Manager Login - Failed to get token"
    log_error "Response: $MANAGER_RESPONSE"
    ((FAILED_TESTS++))
fi
((TOTAL_TESTS++))

# Test 4: Tenant Registration
run_test "Tenant Registration" "200" "POST" "/auth/signup" \
    "-H 'Content-Type: application/json'" \
    '{
        "email": "'$TENANT_EMAIL'",
        "contactNum": "9876543211",
        "password": "TestPass123!",
        "firstName": "Test",
        "lastName": "Tenant",
        "dob": "1995-06-08T00:00:00.000Z",
        "gender": "female",
        "role": "TENANT"
    }'

# Test 5: Tenant Login
log_info "Getting Tenant Access Token..."
TENANT_RESPONSE=$(curl -s -X POST "$API_BASE/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
        "identifier": "'$TENANT_EMAIL'",
        "password": "TestPass123!",
        "role": "TENANT"
    }')

if echo "$TENANT_RESPONSE" | grep -q "accessToken"; then
    TENANT_TOKEN=$(echo "$TENANT_RESPONSE" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
    log_success "Tenant Login - Token acquired"
    ((PASSED_TESTS++))
else
    log_error "Tenant Login - Failed to get token"
    ((FAILED_TESTS++))
fi
((TOTAL_TESTS++))

# ===== PROPERTY MANAGEMENT TESTS =====
echo -e "\n${YELLOW}=== PROPERTY MANAGEMENT TESTS ===${NC}"

if [[ -n "$MANAGER_TOKEN" ]]; then
    # Test 6: Create Property
    run_test "Create Property Building" "200" "POST" "/properties/buildings" \
        "-H 'Content-Type: application/json' -H 'Authorization: Bearer $MANAGER_TOKEN'" \
        '{
            "name": "'$PROPERTY_NAME'",
            "address": "123 Test Street, Test City",
            "propertyType": "Apartment Complex",
            "residentialType": "residential",
            "totalUnits": 50,
            "totalFloors": 5,
            "yearBuilt": 2020,
            "amenities": "Pool, Gym, Parking"
        }'

    # Test 7: Get My Properties
    run_test "Get My Properties" "200" "GET" "/properties/buildings" \
        "-H 'Authorization: Bearer $MANAGER_TOKEN'" ""

    # Test 8: Search Properties
    run_test "Search Properties" "200" "GET" "/properties/buildings/search?searchText=Test" \
        "-H 'Authorization: Bearer $MANAGER_TOKEN'" ""
        
    # Test 9: Create Apartment
    run_test "Create Apartment" "200" "POST" "/apartments" \
        "-H 'Content-Type: application/json' -H 'Authorization: Bearer $MANAGER_TOKEN'" \
        '{
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

    # Test 10: Get My Apartments
    run_test "Get My Apartments" "200" "GET" "/apartments" \
        "-H 'Authorization: Bearer $MANAGER_TOKEN'" ""
else
    log_warning "Skipping Property Management tests - No manager token"
fi

# ===== TENANT CONNECTION TESTS =====
echo -e "\n${YELLOW}=== TENANT CONNECTION TESTS ===${NC}"

if [[ -n "$MANAGER_TOKEN" ]]; then
    # Test 11: Connect Tenant to Property
    run_test "Connect Tenant to Property" "200" "POST" "/tenants/connect" \
        "-H 'Content-Type: application/json' -H 'Authorization: Bearer $MANAGER_TOKEN'" \
        '{
            "tenantEmail": "'$TENANT_EMAIL'",
            "propertyName": "'$PROPERTY_NAME'",
            "startDate": "2025-01-01",
            "endDate": "2025-12-31",
            "monthlyRent": 1500.00,
            "securityDeposit": 3000.00,
            "notes": "Test lease agreement"
        }'

    # Test 12: Search Connected Tenants
    run_test "Search Connected Tenants" "200" "GET" "/tenants/search?searchText=Test" \
        "-H 'Authorization: Bearer $MANAGER_TOKEN'" ""

    # Test 13: Global Tenant Search
    run_test "Global Tenant Search" "200" "GET" "/tenants/search/global?searchText=Test" \
        "-H 'Authorization: Bearer $MANAGER_TOKEN'" ""
else
    log_warning "Skipping Tenant Connection tests - No manager token"
fi

# ===== TENANT DASHBOARD TESTS =====
echo -e "\n${YELLOW}=== TENANT DASHBOARD TESTS ===${NC}"

if [[ -n "$TENANT_TOKEN" ]]; then
    # Test 14: Get Tenant Properties
    run_test "Get Tenant Properties" "200" "GET" "/tenants/my-properties" \
        "-H 'Authorization: Bearer $TENANT_TOKEN'" ""

    # Test 15: Get Service Categories
    run_test "Get Service Categories" "200" "GET" "/maintenance/categories" \
        "-H 'Authorization: Bearer $TENANT_TOKEN'" ""

    # Test 16: Tenant Maintenance Summary
    run_test "Tenant Maintenance Summary" "200" "GET" "/tenant/dashboard/maintenance/summary" \
        "-H 'Authorization: Bearer $TENANT_TOKEN'" ""
else
    log_warning "Skipping Tenant Dashboard tests - No tenant token"
fi

# ===== SECURITY TESTS =====
echo -e "\n${YELLOW}=== SECURITY TESTS ===${NC}"

# Test 17: Unauthorized Access
run_test "Unauthorized Access Test" "401" "GET" "/properties/buildings" "" ""

# Test 18: Invalid Token
run_test "Invalid Token Test" "401" "GET" "/properties/buildings" \
    "-H 'Authorization: Bearer invalid_token_here'" ""

# Test 19: Contact Info Update
if [[ -n "$MANAGER_TOKEN" ]]; then
    run_test "Update Contact Info" "200" "PUT" "/auth/update-contact" \
        "-H 'Content-Type: application/json' -H 'Authorization: Bearer $MANAGER_TOKEN'" \
        '{
            "firstName": "Updated",
            "lastName": "Manager"
        }'
fi

# ===== VALIDATION TESTS =====
echo -e "\n${YELLOW}=== VALIDATION TESTS ===${NC}"

if [[ -n "$MANAGER_TOKEN" ]]; then
    # Test 20: Invalid Email Format
    run_test "Invalid Email Validation" "400" "POST" "/tenants/connect" \
        "-H 'Content-Type: application/json' -H 'Authorization: Bearer $MANAGER_TOKEN'" \
        '{
            "tenantEmail": "invalid-email",
            "propertyName": "'$PROPERTY_NAME'",
            "startDate": "2025-01-01",
            "endDate": "2025-12-31",
            "monthlyRent": 1500.00,
            "securityDeposit": 3000.00
        }'

    # Test 21: Missing Required Fields
    run_test "Missing Required Fields" "400" "POST" "/tenants/connect" \
        "-H 'Content-Type: application/json' -H 'Authorization: Bearer $MANAGER_TOKEN'" \
        '{
            "tenantEmail": "'$TENANT_EMAIL'"
        }'
fi

# ===== TEST SUMMARY =====
echo -e "\n${BLUE}========================================${NC}"
echo -e "${BLUE}           TEST RESULTS SUMMARY${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "Total Tests: $TOTAL_TESTS"
echo -e "${GREEN}Passed: $PASSED_TESTS${NC}"
echo -e "${RED}Failed: $FAILED_TESTS${NC}"

if [[ $FAILED_TESTS -eq 0 ]]; then
    echo -e "\n${GREEN}🎉 All tests passed! API is working correctly.${NC}"
    exit 0
else
    echo -e "\n${RED}❌ Some tests failed. Check the logs above.${NC}"
    
    echo -e "\n${YELLOW}Failed Tests:${NC}"
    for result in "${TEST_RESULTS[@]}"; do
        if [[ $result == ❌* ]]; then
            echo -e "  $result"
        fi
    done
    exit 1
fi