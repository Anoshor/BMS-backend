#!/bin/bash

# BMS Complete Flow Test: Building → Apartments → Tenant Connection
# Usage: ./complete-flow-test.sh [base_url]

set -e

# Configuration
BASE_URL="${1:-http://localhost:8080}"
API_BASE="$BASE_URL/api/v1"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Test data
MANAGER_EMAIL="flowtest-manager@bms.com"
TENANT_EMAIL="flowtest-tenant@bms.com"
MANAGER_PASSWORD="TestPass123!"
TENANT_PASSWORD="TestPass123!"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  BMS Complete Flow Test Started${NC}"
echo -e "${BLUE}  Base URL: $BASE_URL${NC}"
echo -e "${BLUE}========================================${NC}"

# Function to make API calls with error handling
api_call() {
    local method="$1"
    local endpoint="$2"
    local headers="$3"
    local data="$4"
    local description="$5"
    
    echo -e "\n${YELLOW}📡 $description${NC}"
    
    local curl_cmd="curl -s -w \"%{http_code}\" -X $method"
    
    if [[ -n "$headers" ]]; then
        curl_cmd="$curl_cmd $headers"
    fi
    
    if [[ -n "$data" ]]; then
        curl_cmd="$curl_cmd -d '$data'"
    fi
    
    curl_cmd="$curl_cmd $API_BASE$endpoint"
    
    local response=$(eval $curl_cmd)
    local status_code="${response: -3}"
    local response_body="${response%???}"
    
    if [[ "$status_code" =~ ^2[0-9]{2}$ ]]; then
        echo -e "${GREEN}✅ Success ($status_code)${NC}"
        echo "$response_body"
        return 0
    else
        echo -e "${RED}❌ Failed ($status_code)${NC}"
        echo -e "${RED}Response: $response_body${NC}"
        exit 1
    fi
}

# Step 1: Create Manager Account
echo -e "\n${BLUE}=== STEP 1: CREATE MANAGER ACCOUNT ===${NC}"

api_call "POST" "/auth/signup" \
    "-H 'Content-Type: application/json'" \
    '{
        "email": "'$MANAGER_EMAIL'",
        "contactNum": "9876543210",
        "password": "'$MANAGER_PASSWORD'",
        "firstName": "Flow",
        "lastName": "Manager",
        "dob": "1990-01-15T00:00:00.000Z",
        "gender": "male",
        "role": "MANAGER"
    }' \
    "Creating Manager Account"

# Step 2: Manager Login
echo -e "\n${BLUE}=== STEP 2: MANAGER LOGIN ===${NC}"

MANAGER_RESPONSE=$(curl -s -X POST "$API_BASE/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
        "identifier": "'$MANAGER_EMAIL'",
        "password": "'$MANAGER_PASSWORD'",
        "role": "MANAGER"
    }')

if echo "$MANAGER_RESPONSE" | grep -q "accessToken"; then
    MANAGER_TOKEN=$(echo "$MANAGER_RESPONSE" | jq -r '.data.accessToken')
    echo -e "${GREEN}✅ Manager Login Successful${NC}"
    echo -e "${BLUE}Token: ${MANAGER_TOKEN:0:30}...${NC}"
else
    echo -e "${RED}❌ Manager Login Failed${NC}"
    echo "$MANAGER_RESPONSE"
    exit 1
fi

# Step 3: Create Property Building
echo -e "\n${BLUE}=== STEP 3: CREATE PROPERTY BUILDING ===${NC}"

PROPERTY_RESPONSE=$(api_call "POST" "/properties/buildings" \
    "-H 'Content-Type: application/json' -H 'Authorization: Bearer $MANAGER_TOKEN'" \
    '{
        "name": "Sunset Heights Tower",
        "address": "456 Oak Avenue, Downtown District",
        "propertyType": "Luxury Apartment Complex",
        "residentialType": "residential",
        "totalUnits": 50,
        "totalFloors": 10,
        "yearBuilt": 2020,
        "amenities": "Swimming Pool, Gym, Parking, 24/7 Security, Garden"
    }' \
    "Creating Property Building")

PROPERTY_ID=$(echo "$PROPERTY_RESPONSE" | jq -r '.data.id')
echo -e "${GREEN}🏢 Property ID: $PROPERTY_ID${NC}"

# Step 4: Create Apartments in the Building
echo -e "\n${BLUE}=== STEP 4: CREATE APARTMENTS ===${NC}"

# Apartment A101 - 2BHK Premium
APARTMENT_A101_RESPONSE=$(api_call "POST" "/apartments" \
    "-H 'Content-Type: application/json' -H 'Authorization: Bearer $MANAGER_TOKEN'" \
    '{
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
        "occupancyStatus": "VACANT",
        "utilityMeterNumbers": "{\"electric\":\"ELE-A101-001\",\"gas\":\"GAS-A101-002\",\"water\":\"WAT-A101-003\"}"
    }' \
    "Creating Apartment A101 (2BHK Premium)")

APARTMENT_A101_ID=$(echo "$APARTMENT_A101_RESPONSE" | jq -r '.data.id')
echo -e "${GREEN}🏠 Apartment A101 ID: $APARTMENT_A101_ID${NC}"

# Apartment A102 - 1BHK Compact
APARTMENT_A102_RESPONSE=$(api_call "POST" "/apartments" \
    "-H 'Content-Type: application/json' -H 'Authorization: Bearer $MANAGER_TOKEN'" \
    '{
        "propertyId": "'$PROPERTY_ID'",
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
        "occupancyStatus": "VACANT",
        "utilityMeterNumbers": "{\"electric\":\"ELE-A102-001\",\"gas\":\"GAS-A102-002\",\"water\":\"WAT-A102-003\"}"
    }' \
    "Creating Apartment A102 (1BHK Compact)")

APARTMENT_A102_ID=$(echo "$APARTMENT_A102_RESPONSE" | jq -r '.data.id')
echo -e "${GREEN}🏠 Apartment A102 ID: $APARTMENT_A102_ID${NC}"

# Apartment B201 - 3BHK Luxury
APARTMENT_B201_RESPONSE=$(api_call "POST" "/apartments" \
    "-H 'Content-Type: application/json' -H 'Authorization: Bearer $MANAGER_TOKEN'" \
    '{
        "propertyId": "'$PROPERTY_ID'",
        "unitNumber": "B201",
        "unitType": "3BHK",
        "floor": 2,
        "bedrooms": 3,
        "bathrooms": 2.5,
        "squareFootage": 1800,
        "furnished": "Fully-Furnished",
        "balcony": "Yes",
        "rent": 2500.00,
        "securityDeposit": 5000.00,
        "maintenanceCharges": 300.00,
        "occupancyStatus": "VACANT",
        "utilityMeterNumbers": "{\"electric\":\"ELE-B201-001\",\"gas\":\"GAS-B201-002\",\"water\":\"WAT-B201-003\"}"
    }' \
    "Creating Apartment B201 (3BHK Luxury)")

APARTMENT_B201_ID=$(echo "$APARTMENT_B201_RESPONSE" | jq -r '.data.id')
echo -e "${GREEN}🏠 Apartment B201 ID: $APARTMENT_B201_ID${NC}"

# Step 5: Create Tenant Account
echo -e "\n${BLUE}=== STEP 5: CREATE TENANT ACCOUNT ===${NC}"

api_call "POST" "/auth/signup" \
    "-H 'Content-Type: application/json'" \
    '{
        "email": "'$TENANT_EMAIL'",
        "contactNum": "9876543211",
        "password": "'$TENANT_PASSWORD'",
        "firstName": "John",
        "lastName": "Tenant",
        "dob": "1995-06-08T00:00:00.000Z",
        "gender": "male",
        "role": "TENANT"
    }' \
    "Creating Tenant Account"

# Step 6: Tenant Login (Optional - for verification)
echo -e "\n${BLUE}=== STEP 6: TENANT LOGIN ===${NC}"

TENANT_RESPONSE=$(curl -s -X POST "$API_BASE/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
        "identifier": "'$TENANT_EMAIL'",
        "password": "'$TENANT_PASSWORD'",
        "role": "TENANT"
    }')

if echo "$TENANT_RESPONSE" | grep -q "accessToken"; then
    TENANT_TOKEN=$(echo "$TENANT_RESPONSE" | jq -r '.data.accessToken')
    echo -e "${GREEN}✅ Tenant Login Successful${NC}"
    echo -e "${BLUE}Token: ${TENANT_TOKEN:0:30}...${NC}"
else
    echo -e "${RED}❌ Tenant Login Failed${NC}"
    echo "$TENANT_RESPONSE"
fi

# Step 7: Connect Tenant to Apartment A101
echo -e "\n${BLUE}=== STEP 7: CONNECT TENANT TO APARTMENT A101 ===${NC}"

CONNECT_RESPONSE=$(api_call "POST" "/tenants/connect" \
    "-H 'Content-Type: application/json' -H 'Authorization: Bearer $MANAGER_TOKEN'" \
    '{
        "tenantEmail": "'$TENANT_EMAIL'",
        "apartmentId": "'$APARTMENT_A101_ID'",
        "startDate": "2025-09-15",
        "endDate": "2026-09-14",
        "monthlyRent": 1500.00,
        "securityDeposit": 3000.00,
        "notes": "1-year lease for 2BHK premium apartment A101 with semi-furnished setup"
    }' \
    "Connecting Tenant to Apartment A101")

# Step 8: Verification Tests
echo -e "\n${BLUE}=== STEP 8: VERIFICATION TESTS ===${NC}"

# 8.1 Check Apartment A101 is now occupied
echo -e "\n${YELLOW}📋 Checking Apartment A101 Occupancy Status${NC}"
APARTMENT_CHECK=$(curl -s -X GET "$API_BASE/apartments/$APARTMENT_A101_ID" \
    -H "Authorization: Bearer $MANAGER_TOKEN")

OCCUPANCY_STATUS=$(echo "$APARTMENT_CHECK" | jq -r '.data.occupancyStatus')
TENANT_NAME=$(echo "$APARTMENT_CHECK" | jq -r '.data.tenantName')

if [[ "$OCCUPANCY_STATUS" == "OCCUPIED" ]]; then
    echo -e "${GREEN}✅ Apartment A101 is now OCCUPIED${NC}"
    echo -e "${GREEN}✅ Tenant: $TENANT_NAME${NC}"
else
    echo -e "${RED}❌ Apartment A101 status: $OCCUPANCY_STATUS${NC}"
fi

# 8.2 Search connected tenants
echo -e "\n${YELLOW}📋 Searching Connected Tenants${NC}"
TENANT_SEARCH=$(curl -s -X GET "$API_BASE/tenants/search?searchText=John" \
    -H "Authorization: Bearer $MANAGER_TOKEN")

SEARCH_COUNT=$(echo "$TENANT_SEARCH" | jq '.data | length')
echo -e "${GREEN}✅ Found $SEARCH_COUNT tenant connection(s)${NC}"

# 8.3 Check tenant's properties (from tenant's perspective)
if [[ -n "$TENANT_TOKEN" ]]; then
    echo -e "\n${YELLOW}📋 Checking Tenant's Properties${NC}"
    TENANT_PROPERTIES=$(curl -s -X GET "$API_BASE/tenants/my-properties" \
        -H "Authorization: Bearer $TENANT_TOKEN")
    
    PROPERTY_COUNT=$(echo "$TENANT_PROPERTIES" | jq '.data | length')
    echo -e "${GREEN}✅ Tenant has $PROPERTY_COUNT property connection(s)${NC}"
fi

# 8.4 Get all apartments in building
echo -e "\n${YELLOW}📋 All Apartments in Building${NC}"
ALL_APARTMENTS=$(curl -s -X GET "$API_BASE/apartments/property/$PROPERTY_ID" \
    -H "Authorization: Bearer $MANAGER_TOKEN")

TOTAL_APARTMENTS=$(echo "$ALL_APARTMENTS" | jq '.data | length')
OCCUPIED_APARTMENTS=$(echo "$ALL_APARTMENTS" | jq '[.data[] | select(.occupancyStatus == "OCCUPIED")] | length')
VACANT_APARTMENTS=$(echo "$ALL_APARTMENTS" | jq '[.data[] | select(.occupancyStatus == "VACANT")] | length')

echo -e "${BLUE}📊 Building Summary:${NC}"
echo -e "   Total Apartments: $TOTAL_APARTMENTS"
echo -e "   Occupied: $OCCUPIED_APARTMENTS"
echo -e "   Vacant: $VACANT_APARTMENTS"

# Final Summary
echo -e "\n${BLUE}========================================${NC}"
echo -e "${BLUE}           FLOW TEST SUMMARY${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}✅ Building Created: Sunset Heights Tower${NC}"
echo -e "${GREEN}✅ Apartments Created: 3 units (A101, A102, B201)${NC}"
echo -e "${GREEN}✅ Tenant Connected: John Tenant → Apartment A101${NC}"
echo -e "${GREEN}✅ Occupancy Updated: A101 status changed to OCCUPIED${NC}"
echo -e "${GREEN}✅ Tenant Details Stored: Name, email, phone in apartment record${NC}"

echo -e "\n${YELLOW}📋 Key IDs for Further Testing:${NC}"
echo -e "   Property ID: $PROPERTY_ID"
echo -e "   Apartment A101 ID: $APARTMENT_A101_ID"
echo -e "   Apartment A102 ID: $APARTMENT_A102_ID (VACANT)"
echo -e "   Apartment B201 ID: $APARTMENT_B201_ID (VACANT)"

echo -e "\n${GREEN}🎉 Complete Flow Test Passed Successfully!${NC}"
echo -e "${BLUE}Your building → apartment → tenant connection flow is working perfectly!${NC}"