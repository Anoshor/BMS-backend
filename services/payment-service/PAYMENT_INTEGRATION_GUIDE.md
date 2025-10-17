# BMS Payment Service - Integration Guide

## Overview
The BMS Payment Service handles tenant rent payments using Stripe. It integrates seamlessly with the core-service to link payments to specific tenants and their properties.

---

## Table of Contents
- [Architecture](#architecture)
- [API Endpoints](#api-endpoints)
- [Frontend Integration](#frontend-integration)
- [Payment Flow](#payment-flow)
- [Testing](#testing)
- [Error Handling](#error-handling)
- [Webhooks](#webhooks)

---

## Architecture

### Service Communication
```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│  Frontend   │ ───────>│ Core Service│         │   Payment   │
│             │ <───────│  (Tenant    │         │   Service   │
│             │         │   Info)     │         │  (Stripe)   │
└─────────────┘         └─────────────┘         └─────────────┘
       │                                                │
       └────────────────────────────────────────────────┘
              Payment Intent Creation (with tenantId)
```

### Data Flow
1. **Frontend** fetches tenant details from **Core Service** (tenant ID, name, email)
2. **Frontend** creates payment intent via **Payment Service** with tenant details
3. **Payment Service** auto-creates/retrieves Stripe customer linked to tenant
4. **Frontend** confirms payment using Stripe.js and `clientSecret`

### Database Schema

**Payment Service:**
- `customers` - Maps tenant IDs to Stripe customer IDs
- Future: `payment_transactions` - Payment history

**Core Service:**
- `tenant_property_connections` - Contains rent details (`monthlyRent`, `paymentFrequency`)
- `tenant_profiles` - Tenant contact information

---

## API Endpoints

### Base URLs
- **Core Service:** `http://localhost:8080/api`
- **Payment Service:** `http://localhost:8082/api/payments`

### 1. Get Stripe Publishable Key
```http
GET /api/payments/stripe/publishable-key
```

**Response:**
```json
{
  "publishableKey": "pk_test_51SF6a1LQeG8GWSCS..."
}
```

**Use Case:** Initialize Stripe.js on frontend

---

### 2. Create Card Payment Intent
```http
POST /api/payments/create-card-intent
```

**Request Body:**
```json
{
  "amount": 150000,
  "currency": "usd",
  "tenantId": 1,
  "tenantName": "John Doe",
  "tenantEmail": "john.doe@example.com",
  "tenantPhone": "+1234567890",
  "description": "Monthly rent payment - November 2025"
}
```

**Field Details:**
| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `amount` | Long | ✅ | Amount in **cents** | `150000` ($1,500.00) |
| `currency` | String | ✅ | Currency code | `"usd"` |
| `tenantId` | Long | ✅ | Tenant ID from core-service | `1` |
| `tenantName` | String | ✅* | Tenant full name | `"John Doe"` |
| `tenantEmail` | String | ✅* | Tenant email | `"john@example.com"` |
| `tenantPhone` | String | ❌ | Tenant phone | `"+1234567890"` |
| `description` | String | ❌ | Payment description | `"Monthly rent"` |
| `receiptEmail` | String | ❌ | Custom receipt email | `"custom@example.com"` |

**\*Required for first payment only (customer creation)**

**Response:**
```json
{
  "clientSecret": "pi_3AbCdEfGhIjKlMnO_secret_123",
  "paymentIntentId": "pi_3AbCdEfGhIjKlMnO",
  "status": "requires_payment_method",
  "amount": 150000,
  "currency": "usd",
  "error": null,
  "errorMessage": null
}
```

**Status Values:**
- `requires_payment_method` - Ready for payment confirmation
- `requires_confirmation` - Payment method attached, needs confirmation
- `requires_action` - Additional authentication required (3D Secure)
- `processing` - Payment is processing
- `succeeded` - Payment completed
- `canceled` - Payment canceled

---

### 3. Create ACH/Bank Payment Intent
```http
POST /api/payments/create-ach-intent
```

**Request Body:** Same as card payment

**Response:** Same as card payment

**Note:** ACH payments require bank account verification

---

### 4. Get Payment Intent
```http
GET /api/payments/{paymentIntentId}
```

**Response:**
```json
{
  "clientSecret": "pi_3AbCdEfGhIjKlMnO_secret_123",
  "paymentIntentId": "pi_3AbCdEfGhIjKlMnO",
  "status": "succeeded",
  "amount": 150000,
  "currency": "usd",
  "error": null,
  "errorMessage": null
}
```

---

### 5. Cancel Payment Intent
```http
POST /api/payments/{paymentIntentId}/cancel
```

**Response:** Payment intent with `status: "canceled"`

---

## Frontend Integration

### Step 1: Install Stripe.js
```bash
npm install @stripe/stripe-js
```

### Step 2: Initialize Stripe
```javascript
import { loadStripe } from '@stripe/stripe-js';

// Fetch publishable key from backend
const response = await fetch('http://localhost:8082/api/payments/stripe/publishable-key');
const { publishableKey } = await response.json();

// Initialize Stripe
const stripe = await loadStripe(publishableKey);
```

### Step 3: Get Tenant Details from Core Service
```javascript
// Assuming tenant is logged in
const tenantResponse = await fetch('http://localhost:8080/api/tenant/profile', {
  headers: {
    'Authorization': `Bearer ${accessToken}`
  }
});
const tenantData = await tenantResponse.json();

// Get tenant's property/rent details
const connectionResponse = await fetch('http://localhost:8080/api/tenant/connections', {
  headers: {
    'Authorization': `Bearer ${accessToken}`
  }
});
const connections = await connectionResponse.json();

// Extract rent amount
const monthlyRent = connections.data[0].monthlyRent; // e.g., 1500.00
```

### Step 4: Create Payment Intent
```javascript
const createPaymentIntent = async (rentAmount, tenantInfo) => {
  const response = await fetch('http://localhost:8082/api/payments/create-card-intent', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      amount: Math.round(rentAmount * 100), // Convert to cents
      currency: 'usd',
      tenantId: tenantInfo.userId,
      tenantName: `${tenantInfo.firstName} ${tenantInfo.lastName}`,
      tenantEmail: tenantInfo.email,
      tenantPhone: tenantInfo.phone,
      description: `Rent payment - ${new Date().toLocaleDateString()}`
    })
  });

  const data = await response.json();

  if (data.error) {
    throw new Error(data.errorMessage);
  }

  return data.clientSecret;
};
```

### Step 5: Confirm Payment with Card Details
```javascript
const confirmPayment = async (clientSecret, cardElement) => {
  const result = await stripe.confirmCardPayment(clientSecret, {
    payment_method: {
      card: cardElement,
      billing_details: {
        name: tenantInfo.name,
        email: tenantInfo.email
      }
    }
  });

  if (result.error) {
    // Show error to customer
    console.error(result.error.message);
  } else if (result.paymentIntent.status === 'succeeded') {
    // Payment successful!
    console.log('Payment succeeded:', result.paymentIntent.id);
  }
};
```

### Complete React Component Example
```jsx
import React, { useState, useEffect } from 'react';
import { loadStripe } from '@stripe/stripe-js';
import { CardElement, Elements, useStripe, useElements } from '@stripe/react-stripe-js';

// Initialize Stripe
const stripePromise = loadStripe('pk_test_...');

function PaymentForm({ tenantInfo, rentAmount }) {
  const stripe = useStripe();
  const elements = useElements();
  const [error, setError] = useState(null);
  const [processing, setProcessing] = useState(false);
  const [succeeded, setSucceeded] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setProcessing(true);

    // 1. Create payment intent
    const clientSecret = await createPaymentIntent(rentAmount, tenantInfo);

    // 2. Confirm payment
    const cardElement = elements.getElement(CardElement);
    const result = await stripe.confirmCardPayment(clientSecret, {
      payment_method: {
        card: cardElement,
        billing_details: {
          name: `${tenantInfo.firstName} ${tenantInfo.lastName}`,
          email: tenantInfo.email
        }
      }
    });

    if (result.error) {
      setError(result.error.message);
      setProcessing(false);
    } else {
      setError(null);
      setSucceeded(true);
      setProcessing(false);
      // Show success message
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <h2>Pay Rent: ${rentAmount}</h2>
      <CardElement />
      <button disabled={!stripe || processing || succeeded}>
        {processing ? 'Processing...' : 'Pay Now'}
      </button>
      {error && <div className="error">{error}</div>}
      {succeeded && <div className="success">Payment successful!</div>}
    </form>
  );
}

function RentPayment() {
  const [tenantInfo, setTenantInfo] = useState(null);
  const [rentAmount, setRentAmount] = useState(null);

  useEffect(() => {
    // Fetch tenant info and rent details from core service
    fetchTenantData();
  }, []);

  return (
    <Elements stripe={stripePromise}>
      {tenantInfo && rentAmount && (
        <PaymentForm tenantInfo={tenantInfo} rentAmount={rentAmount} />
      )}
    </Elements>
  );
}

export default RentPayment;
```

---

## Payment Flow

### Typical Rent Payment Flow

```
1. Tenant logs in → Frontend
2. Fetch tenant profile → GET /api/tenant/profile (Core Service)
3. Fetch tenant connections → GET /api/tenant/connections (Core Service)
4. Extract monthlyRent, tenantId, etc.
5. User clicks "Pay Rent" → Frontend
6. Create payment intent → POST /api/payments/create-card-intent (Payment Service)
7. Get clientSecret
8. Show Stripe card form → Frontend (Stripe.js)
9. User enters card details
10. Confirm payment → stripe.confirmCardPayment(clientSecret)
11. Payment processed → Stripe
12. Show success/failure → Frontend
13. (Optional) Store transaction → Payment Service webhook
```

### First-time vs Returning Customer

**First Payment:**
```json
{
  "tenantId": 1,
  "tenantName": "John Doe",
  "tenantEmail": "john@example.com",
  ...
}
```
→ Creates new Stripe customer and saves to database

**Subsequent Payments:**
```json
{
  "tenantId": 1,
  "tenantName": "John Doe",  // Optional now
  "tenantEmail": "john@example.com",  // Optional now
  ...
}
```
→ Retrieves existing Stripe customer from database

---

## Testing

### Test Mode
All API keys use **test mode** (keys start with `sk_test_` and `pk_test_`). No real money is charged.

### Test Card Numbers

| Card Number | Scenario |
|-------------|----------|
| 4242 4242 4242 4242 | ✅ Success |
| 4000 0000 0000 0002 | ❌ Card declined |
| 4000 0025 0000 3155 | 🔐 Requires 3D Secure authentication |
| 4000 0000 0000 9995 | ❌ Insufficient funds |
| 4000 0000 0000 9987 | ❌ Lost card |

**Card Details:**
- **Expiry:** Any future date (e.g., 12/34)
- **CVC:** Any 3 digits (e.g., 123)
- **ZIP:** Any 5 digits (e.g., 12345)

### Test Amount Examples

| Rent | Amount in Cents |
|------|-----------------|
| $500.00 | 50000 |
| $1,500.00 | 150000 |
| $2,000.00 | 200000 |

### Testing with cURL

```bash
# 1. Get publishable key
curl http://localhost:8082/api/payments/stripe/publishable-key

# 2. Create payment intent for $1,500 rent
curl -X POST http://localhost:8082/api/payments/create-card-intent \
  -H 'Content-Type: application/json' \
  -d '{
    "amount": 150000,
    "currency": "usd",
    "tenantId": 1,
    "tenantName": "John Doe",
    "tenantEmail": "john.doe@example.com",
    "description": "November 2025 rent"
  }'

# Response:
# {
#   "clientSecret": "pi_..._secret_...",
#   "paymentIntentId": "pi_...",
#   "status": "requires_payment_method",
#   "amount": 150000,
#   "currency": "usd"
# }
```

### Swagger UI Testing
Visit `http://localhost:8082/swagger-ui.html` for interactive API testing.

---

## Error Handling

### Common Errors

| Error Code | Message | Cause | Solution |
|------------|---------|-------|----------|
| `PAYMENT_INTENT_CREATION_FAILED` | Invalid API Key | Stripe keys not configured | Check environment variables |
| `PAYMENT_INTENT_CREATION_FAILED` | No such customer | Customer doesn't exist | Ensure tenantId is valid |
| `400` | Amount too small | Amount < 50 cents | Use minimum $0.50 |
| `card_declined` | Card declined | Card issues | Try different card |
| `insufficient_funds` | Insufficient funds | Not enough balance | Add funds or try different card |

### Frontend Error Handling
```javascript
try {
  const clientSecret = await createPaymentIntent(amount, tenant);
  const result = await stripe.confirmCardPayment(clientSecret, {...});

  if (result.error) {
    // Handle Stripe errors
    switch(result.error.code) {
      case 'card_declined':
        alert('Your card was declined. Please try another card.');
        break;
      case 'insufficient_funds':
        alert('Insufficient funds. Please use another card.');
        break;
      default:
        alert(`Payment failed: ${result.error.message}`);
    }
  } else {
    // Success!
  }
} catch (error) {
  // Handle API errors
  console.error('Payment error:', error);
}
```

---

## Webhooks

### Setup (Future Enhancement)
Stripe webhooks notify your backend when payment events occur.

**Common Events:**
- `payment_intent.succeeded` - Payment completed
- `payment_intent.payment_failed` - Payment failed
- `charge.refunded` - Refund processed

**Endpoint (to be implemented):**
```http
POST /api/payments/webhook
```

---

## Production Checklist

### Before Going Live:

- [ ] Replace test Stripe keys with **live keys** (`sk_live_...`, `pk_live_...`)
- [ ] Set up Stripe webhooks for production
- [ ] Implement payment transaction logging
- [ ] Add payment history UI for tenants
- [ ] Set up recurring payments (optional)
- [ ] Configure proper error logging
- [ ] Add payment receipt emails
- [ ] Test with real bank accounts (small amounts)
- [ ] Implement refund functionality
- [ ] Add payment analytics dashboard

---

## API Reference Summary

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/stripe/publishable-key` | GET | Get Stripe public key |
| `/create-card-intent` | POST | Create card payment |
| `/create-ach-intent` | POST | Create ACH payment |
| `/{paymentIntentId}` | GET | Get payment details |
| `/{paymentIntentId}/cancel` | POST | Cancel payment |

---

## Support & Resources

- **Swagger UI:** http://localhost:8082/swagger-ui.html
- **Stripe Docs:** https://stripe.com/docs
- **Stripe Test Cards:** https://stripe.com/docs/testing#cards
- **Stripe Dashboard:** https://dashboard.stripe.com

---

## Questions?

Contact the backend team or check logs:
```bash
docker-compose -f docker-compose.prod.yml logs -f bms-payment-service
```
