# Payment Service - Integration Test Guide

## Prerequisites

1. **Start Core Service** (Port 8080)
```bash
cd services/core-service
mvn spring-boot:run
```

2. **Start Payment Service** (Port 8082)
```bash
cd services/payment-service
mvn spring-boot:run
```

3. **Start Stripe CLI** (For webhooks)
```bash
stripe listen --forward-to localhost:8082/api/webhooks/stripe
```

---

## Automated Tests

### Run All Integration Tests
```bash
cd services/payment-service
mvn test
```

### Run Specific Test
```bash
# Test basic payment functionality
mvn test -Dtest=PaymentIntegrationTest

# Test secure lease payment flow (CRITICAL SECURITY TEST)
mvn test -Dtest=SecureLeasePaymentTest
```

---

## Manual Testing with cURL

### Test 1: Get Stripe Publishable Key
```bash
curl -X GET http://localhost:8082/api/payments/stripe/publishable-key
```

**Expected Response:**
```json
{
  "publishableKey": "pk_test_..."
}
```

---

### Test 2: Create Manual Payment (No Lease)
```bash
curl -X POST http://localhost:8082/api/payments/create-card-intent \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 5000,
    "currency": "usd",
    "description": "Test payment"
  }'
```

**Expected Response:**
```json
{
  "clientSecret": "pi_xxx_secret_xxx",
  "paymentIntentId": "pi_xxx",
  "status": "requires_payment_method",
  "amount": 5000,
  "currency": "usd"
}
```

---

### Test 3: 🔒 SECURE Lease Payment (Server-Side Amount)

**Step 1: Get a Lease ID from Core Service**
```bash
curl -X GET http://localhost:8080/api/v1/leases \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Step 2: Get Payment Details for Lease**
```bash
curl -X GET http://localhost:8080/api/v1/leases/YOUR_LEASE_ID/payment-details \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "leaseId": "LEASE-2025-7B48",
    "tenantId": "04fc37d0-e819-4488-9849-4f237f9b45c1",
    "tenantName": "Sudarshana V Sharma",
    "rentAmount": 600.00,
    "latePaymentCharges": 60.00,
    "totalPayableAmount": 660.00
  }
}
```

**Step 3: Create Payment Intent (ONLY send leaseId)**
```bash
curl -X POST http://localhost:8082/api/payments/create-card-intent \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "leaseId": "YOUR_LEASE_ID"
  }'
```

**Expected Response:**
```json
{
  "clientSecret": "pi_xxx_secret_xxx",
  "paymentIntentId": "pi_xxx",
  "status": "requires_payment_method",
  "amount": 66000,
  "currency": "usd"
}
```

**✅ SECURITY CHECK:** Amount should be 66000 cents ($660) - fetched from server, NOT from client!

---

### Test 4: ACH Payment with Lease
```bash
curl -X POST http://localhost:8082/api/payments/create-ach-intent \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "leaseId": "YOUR_LEASE_ID"
  }'
```

---

### Test 5: Security Test - Try to Tamper with Amount (Should be IGNORED)

**❌ ATTACK ATTEMPT:**
```bash
curl -X POST http://localhost:8082/api/payments/create-card-intent \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "leaseId": "YOUR_LEASE_ID",
    "amount": 100
  }'
```

**✅ Expected Behavior:**
- Server should IGNORE the `amount: 100` from client
- Server should fetch amount from core-service
- Payment intent should be created with CORRECT amount from database
- **If client could pay $1 instead of $660, the security is BROKEN!**

---

## Verify in Stripe Dashboard

1. Go to: https://dashboard.stripe.com/test/payments
2. Find your payment intent ID (starts with `pi_`)
3. Check:
   - ✅ Amount matches server-side calculation
   - ✅ Customer is linked (if tenantId provided)
   - ✅ Payment methods include card and us_bank_account (if ACH enabled)

---

## Check Webhooks

After creating payment intent, check Stripe CLI output:
```
--> payment_intent.created [evt_xxx]
<-- [200] POST http://localhost:8082/api/webhooks/stripe
```

Check payment service logs:
```
2025-10-17 11:45:22 - Fetching lease payment details for lease: LEASE-2025-7B48
2025-10-17 11:45:22 - Verified amount from core-service: $660.00 for lease LEASE-2025-7B48
2025-10-17 11:45:22 - Created card PaymentIntent: pi_xxx for amount: $660.0
2025-10-17 11:45:22 - Received webhook event: payment_intent.created
```

---

## Expected Test Results

| Test | Expected Result |
|------|----------------|
| Get publishable key | ✅ Returns key starting with `pk_test_` |
| Create manual payment | ✅ Returns client_secret and correct amount |
| Create lease payment | ✅ Fetches amount from core-service |
| Client sends wrong amount | ✅ Server ignores it, uses DB amount |
| ACH payment | ✅ Works if ACH enabled in Stripe |
| Webhook | ✅ Receives payment_intent.created event |

---

## Troubleshooting

### Error: "Connection refused to localhost:8080"
**Solution:** Start core-service first
```bash
cd services/core-service
mvn spring-boot:run
```

### Error: "us_bank_account not supported"
**Solution:** Enable ACH in Stripe Dashboard
- Go to: https://dashboard.stripe.com/settings/payment_methods
- Enable: "ACH Direct Debit"

### Error: "Amount is required when leaseId is not provided"
**Solution:** Either provide `leaseId` OR `amount`, not neither

### Tests Fail with Stripe API Error
**Solution:** Set real Stripe API keys in `.env` or `application.yml`:
```bash
STRIPE_SECRET_KEY=sk_test_your_real_key
STRIPE_PUBLISHABLE_KEY=pk_test_your_real_key
```

---

## Success Criteria

All tests pass when:
1. ✅ Payment service starts on port 8082
2. ✅ Core service starts on port 8080
3. ✅ Can create payment intent with manual amount
4. ✅ Can create payment intent with leaseId (fetches amount from core-service)
5. ✅ Server-side amount verification works (client cannot tamper)
6. ✅ Webhooks receive events successfully
7. ✅ ACH payments work (if enabled)

---

## Next Steps After Tests Pass

1. **Frontend Integration:**
   - Use `clientSecret` with Stripe Elements
   - Display payment sheet
   - Handle payment confirmation

2. **Payment Status Tracking:**
   - Create `RentPayment` table in core-service
   - Record payment success/failure from webhooks
   - Link payments to leases

3. **Production Deployment:**
   - Use production Stripe keys
   - Set up webhook endpoints
   - Configure CORS properly
