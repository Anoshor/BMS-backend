#!/bin/bash

# Payment Flow Integration Test
# This script validates the complete flow: Signup → Login → Get Lease Details → Create Payment Intent

set -e  # Exit on error

CORE_SERVICE="http://localhost:8080"
PAYMENT_SERVICE="http://localhost:8082"

echo "=========================================="
echo "Payment Integration Flow Test"
echo "=========================================="
echo ""

# Ask user if they want to signup or login
echo "Do you want to:"
echo "1. Signup new user (creates test tenant)"
echo "2. Login with existing user"
read -p "Enter choice (1 or 2): " CHOICE
echo ""

if [ "$CHOICE" == "1" ]; then
    # Step 1a: Signup
    echo "Step 1a: Creating new test user..."
    echo "----------------------------------------"

    # Generate random test data
    TIMESTAMP=$(date +%s)
    TEST_EMAIL="test-tenant-${TIMESTAMP}@example.com"
    TEST_PASSWORD="Test@123456"
    TEST_NAME="Test Tenant ${TIMESTAMP}"
    TEST_PHONE="97540${TIMESTAMP:(-5)}"

    echo "Creating test user:"
    echo "  Email: $TEST_EMAIL"
    echo "  Password: $TEST_PASSWORD"
    echo "  Name: $TEST_NAME"
    echo "  Phone: $TEST_PHONE"
    echo ""

    SIGNUP_RESPONSE=$(curl -s -X POST "${CORE_SERVICE}/api/v1/auth/signup" \
      -H "Content-Type: application/json" \
      -d "{
        \"email\": \"${TEST_EMAIL}\",
        \"contactNum\": \"${TEST_PHONE}\",
        \"password\": \"${TEST_PASSWORD}\",
        \"firstName\": \"Test\",
        \"lastName\": \"Tenant${TIMESTAMP}\",
        \"dob\": \"1995-01-01T00:00:00.000Z\",
        \"gender\": \"male\",
        \"role\": \"TENANT\"
      }")

    echo "Signup response:"
    echo "$SIGNUP_RESPONSE" | jq .

    # Check if signup was successful
    SIGNUP_SUCCESS=$(echo "$SIGNUP_RESPONSE" | jq -r '.success // false')

    if [ "$SIGNUP_SUCCESS" != "true" ]; then
        echo ""
        echo "❌ Signup failed!"
        echo "Trying to login with existing credentials instead..."
        echo ""
    else
        echo ""
        echo "✅ Signup successful!"
        echo ""
        echo "⚠️  NOTE: This is a new user with no leases. You'll need to:"
        echo "   1. Create a property (as manager)"
        echo "   2. Create a lease for this tenant"
        echo "   3. Then test payment flow"
        echo ""
        echo "For this test, we'll try to login with an existing user instead..."
        echo ""
    fi

    # For testing, we'll use existing credentials
    read -p "Enter existing email/identifier: " IDENTIFIER
    read -sp "Enter password: " PASSWORD
    echo ""

else
    # Step 1b: Login with existing user
    echo "Step 1: Logging in with existing credentials..."
    echo "----------------------------------------"

    read -p "Enter email/identifier: " IDENTIFIER
    read -sp "Enter password: " PASSWORD
    echo ""
fi

# Login
echo ""
echo "Logging in..."
LOGIN_RESPONSE=$(curl -s -X POST "${CORE_SERVICE}/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"identifier\": \"${IDENTIFIER}\",
    \"password\": \"${PASSWORD}\",
    \"role\": \"TENANT\"
  }")

echo "Login response:"
echo "$LOGIN_RESPONSE" | jq .

# Extract access token
ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.accessToken // empty')

if [ -z "$ACCESS_TOKEN" ] || [ "$ACCESS_TOKEN" == "null" ]; then
    echo ""
    echo "❌ Login failed! Could not get access token."
    echo "Response was: $LOGIN_RESPONSE"
    exit 1
fi

echo ""
echo "✅ Login successful!"
echo "Token (first 50 chars): ${ACCESS_TOKEN:0:50}..."
echo ""

# Step 2: Get user's leases
echo "Step 2: Fetching user's leases..."
echo "----------------------------------------"

# Check user role from token
USER_ROLE=$(echo "$LOGIN_RESPONSE" | jq -r '.data.role // empty')
echo "User role: $USER_ROLE"
echo ""

if [ "$USER_ROLE" == "TENANT" ]; then
    # For tenant, get their leases
    LEASES_RESPONSE=$(curl -s -X GET "${CORE_SERVICE}/api/v1/tenants/me/properties" \
      -H "Authorization: Bearer ${ACCESS_TOKEN}")

    echo "Tenant properties response:"
    echo "$LEASES_RESPONSE" | jq .

    # Try to extract first lease ID
    LEASE_UUID=$(echo "$LEASES_RESPONSE" | jq -r '.data[0].leaseId // empty')

    if [ -z "$LEASE_UUID" ] || [ "$LEASE_UUID" == "null" ]; then
        echo ""
        echo "⚠️  No leases found for this tenant."
        read -p "Enter lease UUID manually (or press Ctrl+C to exit): " LEASE_UUID
    else
        echo ""
        echo "Found lease: $LEASE_UUID"
        read -p "Use this lease? (y/n, or enter different UUID): " USE_LEASE
        if [ "$USE_LEASE" != "y" ] && [ "$USE_LEASE" != "Y" ]; then
            read -p "Enter lease UUID: " LEASE_UUID
        fi
    fi
else
    # For manager, ask for lease UUID
    echo "As a manager, please provide a lease UUID to test:"
    read -p "Enter lease UUID (connectionId): " LEASE_UUID
fi

echo ""
echo "Using lease UUID: $LEASE_UUID"
echo ""

# Step 3: Get lease payment details from core-service
echo "Step 3: Fetching lease payment details..."
echo "----------------------------------------"

LEASE_RESPONSE=$(curl -s -X GET "${CORE_SERVICE}/api/v1/leases/${LEASE_UUID}/payment-details" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}")

echo "Lease payment details response:"
echo "$LEASE_RESPONSE" | jq .

# Extract payment details
TENANT_ID=$(echo "$LEASE_RESPONSE" | jq -r '.data.tenantId // empty')
TENANT_NAME=$(echo "$LEASE_RESPONSE" | jq -r '.data.tenantName // empty')
TENANT_EMAIL=$(echo "$LEASE_RESPONSE" | jq -r '.data.tenantEmail // empty')
TENANT_PHONE=$(echo "$LEASE_RESPONSE" | jq -r '.data.tenantPhone // empty')
PROPERTY_NAME=$(echo "$LEASE_RESPONSE" | jq -r '.data.propertyName // empty')
TOTAL_AMOUNT=$(echo "$LEASE_RESPONSE" | jq -r '.data.totalPayableAmount // empty')
CONNECTION_ID=$(echo "$LEASE_RESPONSE" | jq -r '.data.connectionId // empty')

if [ -z "$TENANT_ID" ] || [ "$TENANT_ID" == "null" ]; then
    echo ""
    echo "❌ Failed to fetch lease payment details!"
    echo "Error: $(echo "$LEASE_RESPONSE" | jq -r '.message // empty')"
    exit 1
fi

echo ""
echo "✅ Lease payment details fetched successfully!"
echo "   Connection ID: $CONNECTION_ID"
echo "   Property: $PROPERTY_NAME"
echo "   Tenant: $TENANT_NAME ($TENANT_EMAIL)"
echo "   Phone: $TENANT_PHONE"
echo "   Total Amount: \$$TOTAL_AMOUNT"
echo ""

# Step 4: Create payment intent via payment-service
echo "Step 4: Creating payment intent via payment-service..."
echo "----------------------------------------"
echo "This simulates what the UI does when user clicks 'Pay Now'"
echo ""

PAYMENT_INTENT_RESPONSE=$(curl -s -X POST "${PAYMENT_SERVICE}/api/payments/create-card-intent" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -d "{
    \"leaseId\": \"${CONNECTION_ID}\",
    \"tenantId\": \"${TENANT_ID}\",
    \"tenantName\": \"${TENANT_NAME}\",
    \"tenantEmail\": \"${TENANT_EMAIL}\",
    \"tenantPhone\": \"${TENANT_PHONE}\",
    \"description\": \"Rent payment for ${PROPERTY_NAME}\"
  }")

echo "Payment intent response:"
echo "$PAYMENT_INTENT_RESPONSE" | jq .

# Check if payment intent was created successfully
CLIENT_SECRET=$(echo "$PAYMENT_INTENT_RESPONSE" | jq -r '.clientSecret // empty')
PAYMENT_INTENT_ID=$(echo "$PAYMENT_INTENT_RESPONSE" | jq -r '.paymentIntentId // empty')
PAYMENT_AMOUNT=$(echo "$PAYMENT_INTENT_RESPONSE" | jq -r '.amount // empty')
ERROR=$(echo "$PAYMENT_INTENT_RESPONSE" | jq -r '.error // empty')

echo ""
echo "=========================================="
echo "Test Results"
echo "=========================================="
echo ""

if [ -n "$ERROR" ] && [ "$ERROR" != "null" ]; then
    echo "❌ Payment intent creation FAILED!"
    echo "   Error: $ERROR"
    ERROR_MSG=$(echo "$PAYMENT_INTENT_RESPONSE" | jq -r '.errorMessage // empty')
    echo "   Message: $ERROR_MSG"
    echo ""
    echo "🔍 Troubleshooting:"
    echo "   1. Check if payment-service is running on port 8082"
    echo "   2. Check if core-service is running on port 8080"
    echo "   3. Verify the lease UUID is correct"
    echo "   4. Check payment-service logs for details"
    echo "   5. Verify Stripe API keys are configured"
    exit 1
fi

if [ -z "$CLIENT_SECRET" ] || [ "$CLIENT_SECRET" == "null" ]; then
    echo "❌ Payment intent creation FAILED!"
    echo "   No client secret returned"
    exit 1
fi

echo "✅ Payment intent created successfully!"
echo ""
echo "Payment Intent Details:"
echo "   Payment Intent ID: $PAYMENT_INTENT_ID"
echo "   Client Secret: ${CLIENT_SECRET:0:50}..."
echo "   Amount: \$$(echo "scale=2; $PAYMENT_AMOUNT / 100" | bc)"
echo ""
echo "=========================================="
echo "✅ ALL TESTS PASSED!"
echo "=========================================="
echo ""
echo "The complete flow works:"
echo "  1. ✅ Login and get valid JWT token"
echo "  2. ✅ Fetch lease payment details from core-service"
echo "  3. ✅ Payment-service receives correct leaseId (connectionId/UUID)"
echo "  4. ✅ Payment-service calls core-service with forwarded token"
echo "  5. ✅ Core-service validates token and returns payment details"
echo "  6. ✅ Payment intent created with Stripe"
echo ""
echo "💡 This proves the backend integration is working correctly!"
echo ""
echo "📱 For the UI:"
echo "   - Make sure to send 'connectionId' as the 'leaseId' parameter ✅ (FIXED)"
echo "   - The token must be valid and not expired"
echo "   - The flow should work exactly as tested here"
echo ""
