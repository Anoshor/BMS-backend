#!/bin/bash

# Complete E2E Setup and Payment Integration Test
# Creates everything from scratch: Manager → Property → Apartment → Tenant → Lease → Payment

set -e

CORE_SERVICE="http://localhost:8080"
PAYMENT_SERVICE="http://localhost:8082"

echo "=========================================="
echo "🚀 COMPLETE E2E PAYMENT INTEGRATION TEST"
echo "=========================================="
echo ""

TIMESTAMP=$(date +%s)

# Step 1: Create Manager Account
echo "Step 1: Creating Manager Account..."
echo "----------------------------------------"

MANAGER_EMAIL="manager-${TIMESTAMP}@test.com"
MANAGER_PASSWORD="Test@123456"

MANAGER_SIGNUP=$(curl -s -X POST "${CORE_SERVICE}/api/v1/auth/signup" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"${MANAGER_EMAIL}\",
    \"contactNum\": \"98765${TIMESTAMP:(-5)}\",
    \"password\": \"${MANAGER_PASSWORD}\",
    \"firstName\": \"Manager\",
    \"lastName\": \"Test${TIMESTAMP}\",
    \"dob\": \"1990-01-01T00:00:00.000Z\",
    \"gender\": \"male\",
    \"role\": \"MANAGER\"
  }")

echo "Manager signup: $(echo $MANAGER_SIGNUP | jq -r '.message // .error')"

# Verify Manager Email
echo "Verifying manager email..."
VERIFY_EMAIL=$(curl -s -X POST "${CORE_SERVICE}/api/v1/auth/verify-email?email=${MANAGER_EMAIL}&otp=123456" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"${MANAGER_EMAIL}\",
    \"otpCode\": \"123456\"
  }")
echo "Email verification: $(echo $VERIFY_EMAIL | jq -r '.message // .error // "Done"')"

# Verify Manager Phone
MANAGER_PHONE="98765${TIMESTAMP:(-5)}"
echo "Verifying manager phone..."
VERIFY_PHONE=$(curl -s -X POST "${CORE_SERVICE}/api/v1/auth/verify-phone?phone=${MANAGER_PHONE}&otp=654321")
echo "Phone verification: $(echo $VERIFY_PHONE | jq -r '.message // .error // "Done"')"

# Approve Manager (using admin endpoint)
echo "Approving manager account..."
APPROVE=$(curl -s -X POST "${CORE_SERVICE}/api/v1/admin/managers/approve" \
  -H "Content-Type: application/json" \
  -d "{
    \"managerEmail\": \"${MANAGER_EMAIL}\",
    \"action\": \"APPROVE\",
    \"adminEmail\": \"admin@example.com\"
  }")
echo "Approval: $(echo $APPROVE | jq -r '.message // .error // "Done"')"

# Login as Manager
MANAGER_LOGIN=$(curl -s -X POST "${CORE_SERVICE}/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"identifier\": \"${MANAGER_EMAIL}\",
    \"password\": \"${MANAGER_PASSWORD}\",
    \"role\": \"MANAGER\"
  }")

MANAGER_TOKEN=$(echo "$MANAGER_LOGIN" | jq -r '.data.accessToken')

if [ -z "$MANAGER_TOKEN" ] || [ "$MANAGER_TOKEN" == "null" ]; then
    echo "❌ Manager login failed!"
    echo "$MANAGER_LOGIN" | jq .
    exit 1
fi

echo "✅ Manager created, verified, approved and logged in"
echo ""

# Step 2: Create Property
echo "Step 2: Creating Property..."
echo "----------------------------------------"

PROPERTY_RESPONSE=$(curl -s -X POST "${CORE_SERVICE}/api/v1/properties/buildings" \
  -H "Authorization: Bearer ${MANAGER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Test Building ${TIMESTAMP}\",
    \"address\": \"123 Test St, Test City, TS 12345\",
    \"propertyType\": \"RESIDENTIAL\",
    \"residentialType\": \"APARTMENT\",
    \"totalUnits\": 10,
    \"totalFloors\": 5,
    \"yearBuilt\": 2020
  }")

PROPERTY_ID=$(echo "$PROPERTY_RESPONSE" | jq -r '.data.id // empty')

if [ -z "$PROPERTY_ID" ] || [ "$PROPERTY_ID" == "null" ]; then
    echo "❌ Property creation failed!"
    echo "$PROPERTY_RESPONSE" | jq .
    exit 1
fi

echo "✅ Property created: $PROPERTY_ID"
echo ""

# Step 3: Create Apartment/Unit
echo "Step 3: Creating Apartment..."
echo "----------------------------------------"

APARTMENT_RESPONSE=$(curl -s -X POST "${CORE_SERVICE}/api/v1/apartments" \
  -H "Authorization: Bearer ${MANAGER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"propertyId\": \"${PROPERTY_ID}\",
    \"unitNumber\": \"101\",
    \"floor\": 1,
    \"bedrooms\": 2,
    \"bathrooms\": 1.5,
    \"squareFootage\": 850,
    \"baseRent\": 1500,
    \"baseSecurityDeposit\": 3000,
    \"occupancyStatus\": \"VACANT\"
  }")

APARTMENT_ID=$(echo "$APARTMENT_RESPONSE" | jq -r '.data.id // empty')

if [ -z "$APARTMENT_ID" ] || [ "$APARTMENT_ID" == "null" ]; then
    echo "❌ Apartment creation failed!"
    echo "$APARTMENT_RESPONSE" | jq .
    exit 1
fi

echo "✅ Apartment created: $APARTMENT_ID"
echo ""

# Step 4: Create Tenant Account
echo "Step 4: Creating Tenant Account..."
echo "----------------------------------------"

TENANT_EMAIL="tenant-${TIMESTAMP}@test.com"
TENANT_PASSWORD="Test@123456"

TENANT_SIGNUP=$(curl -s -X POST "${CORE_SERVICE}/api/v1/auth/signup" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"${TENANT_EMAIL}\",
    \"contactNum\": \"97540${TIMESTAMP:(-5)}\",
    \"password\": \"${TENANT_PASSWORD}\",
    \"firstName\": \"Tenant\",
    \"lastName\": \"Test${TIMESTAMP}\",
    \"dob\": \"1995-05-15T00:00:00.000Z\",
    \"gender\": \"female\",
    \"role\": \"TENANT\"
  }")

echo "Tenant signup: $(echo $TENANT_SIGNUP | jq -r '.message // .error')"

# Verify Tenant Email
echo "Verifying tenant email..."
TENANT_VERIFY_EMAIL=$(curl -s -X POST "${CORE_SERVICE}/api/v1/auth/verify-email?email=${TENANT_EMAIL}&otp=123456" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"${TENANT_EMAIL}\",
    \"otpCode\": \"123456\"
  }")
echo "Email verification: $(echo $TENANT_VERIFY_EMAIL | jq -r '.message // .error // "Done"')"

# Verify Tenant Phone
TENANT_PHONE_NUM="97540${TIMESTAMP:(-5)}"
echo "Verifying tenant phone..."
TENANT_VERIFY_PHONE=$(curl -s -X POST "${CORE_SERVICE}/api/v1/auth/verify-phone?phone=${TENANT_PHONE_NUM}&otp=654321")
echo "Phone verification: $(echo $TENANT_VERIFY_PHONE | jq -r '.message // .error // "Done"')"

# Login as Tenant to get their ID
TENANT_LOGIN=$(curl -s -X POST "${CORE_SERVICE}/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"identifier\": \"${TENANT_EMAIL}\",
    \"password\": \"${TENANT_PASSWORD}\",
    \"role\": \"TENANT\"
  }")

TENANT_TOKEN=$(echo "$TENANT_LOGIN" | jq -r '.data.accessToken')
TENANT_ID=$(echo "$TENANT_LOGIN" | jq -r '.data.accessToken' | cut -d'.' -f2 | base64 -d 2>/dev/null | jq -r '.sub // empty')

if [ -z "$TENANT_TOKEN" ] || [ "$TENANT_TOKEN" == "null" ]; then
    echo "❌ Tenant login failed!"
    echo "$TENANT_LOGIN" | jq .
    exit 1
fi

echo "✅ Tenant created and verified: $TENANT_ID"
echo ""

# Step 5: Connect Tenant to Property (This creates the lease/connection)
echo "Step 5: Creating Tenant-Property Connection (Lease)..."
echo "----------------------------------------"

CONNECT_RESPONSE=$(curl -s -X POST "${CORE_SERVICE}/api/v1/tenants/connect" \
  -H "Authorization: Bearer ${MANAGER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"tenantEmail\": \"${TENANT_EMAIL}\",
    \"apartmentId\": \"${APARTMENT_ID}\",
    \"startDate\": \"2025-01-01\",
    \"endDate\": \"2026-01-01\",
    \"monthlyRent\": 1500.00,
    \"securityDeposit\": 3000.00,
    \"paymentFrequency\": \"MONTHLY\"
  }")

echo "$CONNECT_RESPONSE" | jq .

LEASE_SUCCESS=$(echo "$CONNECT_RESPONSE" | jq -r '.success // false')

if [ "$LEASE_SUCCESS" != "true" ]; then
    echo "❌ Connection creation failed!"
    exit 1
fi

# Extract connection ID from the database using manager's token
echo "✅ Tenant connected to property! Fetching connection ID..."

# Wait a moment for database to update
sleep 2

# Get all connections using manager token
CONNECTIONS=$(curl -s -X GET "${CORE_SERVICE}/api/v1/tenants/connections" \
  -H "Authorization: Bearer ${MANAGER_TOKEN}")

echo "Connections response:"
echo "$CONNECTIONS" | jq .

# Extract the connection ID for our tenant (should be the last one created)
CONNECTION_ID=$(echo "$CONNECTIONS" | jq -r '.data[-1].id // empty')

if [ -z "$CONNECTION_ID" ] || [ "$CONNECTION_ID" == "null" ]; then
    # Try alternative: Get tenant details
    echo "Trying alternative: fetching tenant details..."
    TENANT_DETAILS=$(curl -s -X GET "${CORE_SERVICE}/api/v1/tenants/details/${TENANT_ID}" \
      -H "Authorization: Bearer ${MANAGER_TOKEN}")

    echo "$TENANT_DETAILS" | jq .

    CONNECTION_ID=$(echo "$TENANT_DETAILS" | jq -r '.data.properties[0].connectionId // empty')
fi

if [ -z "$CONNECTION_ID" ] || [ "$CONNECTION_ID" == "null" ]; then
    echo "❌ Could not get connection ID!"
    exit 1
fi

LEASE_ID=$CONNECTION_ID
echo ""
echo "✅ Connection/Lease ID: $LEASE_ID"
echo ""

# Step 6: Get Payment Details (as tenant)
echo "Step 6: Fetching Payment Details..."
echo "----------------------------------------"

PAYMENT_DETAILS=$(curl -s -X GET "${CORE_SERVICE}/api/v1/leases/${LEASE_ID}/payment-details" \
  -H "Authorization: Bearer ${TENANT_TOKEN}")

echo "$PAYMENT_DETAILS" | jq .

CONNECTION_ID=$(echo "$PAYMENT_DETAILS" | jq -r '.data.connectionId // empty')
TENANT_NAME=$(echo "$PAYMENT_DETAILS" | jq -r '.data.tenantName // empty')
TENANT_EMAIL_DATA=$(echo "$PAYMENT_DETAILS" | jq -r '.data.tenantEmail // empty')
TENANT_PHONE=$(echo "$PAYMENT_DETAILS" | jq -r '.data.tenantPhone // empty')
PROPERTY_NAME=$(echo "$PAYMENT_DETAILS" | jq -r '.data.propertyName // empty')
TOTAL_AMOUNT=$(echo "$PAYMENT_DETAILS" | jq -r '.data.totalPayableAmount // empty')

SUCCESS=$(echo "$PAYMENT_DETAILS" | jq -r '.success // false')
if [ "$SUCCESS" != "true" ]; then
    echo "❌ Failed to get payment details!"
    exit 1
fi

echo ""
echo "✅ Payment details retrieved!"
echo "   Connection ID (UUID): $CONNECTION_ID"
echo "   Property: $PROPERTY_NAME"
echo "   Tenant: $TENANT_NAME"
echo "   Total Amount: \$$TOTAL_AMOUNT"
echo ""

# Step 7: THE CRITICAL TEST - Create Payment Intent
echo "Step 7: Creating Payment Intent (CRITICAL TEST)..."
echo "----------------------------------------"
echo "Testing: UI → Payment Service → Core Service flow"
echo ""

PAYMENT_INTENT=$(curl -s -X POST "${PAYMENT_SERVICE}/api/payments/create-card-intent" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TENANT_TOKEN}" \
  -d "{
    \"leaseId\": \"${CONNECTION_ID}\",
    \"tenantId\": \"${TENANT_ID}\",
    \"tenantName\": \"${TENANT_NAME}\",
    \"tenantEmail\": \"${TENANT_EMAIL_DATA}\",
    \"tenantPhone\": \"${TENANT_PHONE}\",
    \"description\": \"Rent payment for ${PROPERTY_NAME}\"
  }")

echo "$PAYMENT_INTENT" | jq .

CLIENT_SECRET=$(echo "$PAYMENT_INTENT" | jq -r '.clientSecret // empty')
PAYMENT_INTENT_ID=$(echo "$PAYMENT_INTENT" | jq -r '.paymentIntentId // empty')
ERROR=$(echo "$PAYMENT_INTENT" | jq -r '.error // empty')

echo ""
echo "=========================================="
echo "📊 FINAL TEST RESULTS"
echo "=========================================="
echo ""

if [ -n "$ERROR" ] && [ "$ERROR" != "null" ]; then
    echo "❌ PAYMENT INTEGRATION FAILED!"
    echo ""
    echo "Error: $ERROR"
    ERROR_MSG=$(echo "$PAYMENT_INTENT" | jq -r '.errorMessage // empty')
    echo "Message: $ERROR_MSG"
    echo ""
    exit 1
fi

if [ -z "$CLIENT_SECRET" ] || [ "$CLIENT_SECRET" == "null" ]; then
    echo "❌ PAYMENT INTEGRATION FAILED!"
    echo "No client secret returned"
    exit 1
fi

echo "✅✅✅ ALL TESTS PASSED! ✅✅✅"
echo ""
echo "Payment Intent Created Successfully!"
echo "  Payment Intent ID: $PAYMENT_INTENT_ID"
echo "  Client Secret: ${CLIENT_SECRET:0:50}..."
echo ""
echo "=========================================="
echo "🎉 COMPLETE END-TO-END SUCCESS!"
echo "=========================================="
echo ""
echo "What was tested (COMPLETE FLOW):"
echo "  ✅ Manager account creation"
echo "  ✅ Property creation"
echo "  ✅ Apartment/Unit creation"
echo "  ✅ Tenant account creation"
echo "  ✅ Lease creation"
echo "  ✅ Payment details fetch from core-service"
echo "  ✅ UI sends connectionId (UUID) as leaseId ✅"
echo "  ✅ Payment-service → Core-service communication"
echo "  ✅ JWT token forwarding and validation"
echo "  ✅ Stripe payment intent creation"
echo ""
echo "🎯 THE INTEGRATION IS 100% WORKING!"
echo ""
echo "Test Data Created:"
echo "  Manager: $MANAGER_EMAIL / $MANAGER_PASSWORD"
echo "  Tenant: $TENANT_EMAIL / $TENANT_PASSWORD"
echo "  Property ID: $PROPERTY_ID"
echo "  Apartment ID: $APARTMENT_ID"
echo "  Lease ID (UUID): $LEASE_ID"
echo "  Connection ID: $CONNECTION_ID"
echo ""
