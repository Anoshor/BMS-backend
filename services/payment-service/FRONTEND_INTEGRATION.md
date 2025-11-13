# Frontend Integration - Tenant Rent Payment

## Overview
This guide shows how to integrate rent payments into your BMS frontend application for tenants.

---

## Prerequisites

### 1. Install Dependencies
```bash
npm install @stripe/stripe-js @stripe/react-stripe-js axios
```

### 2. Environment Variables
Create `.env` file in your frontend:

```env
REACT_APP_CORE_API_URL=http://localhost:8080/api
REACT_APP_PAYMENT_API_URL=http://localhost:8082/api/payments
```

---

## Architecture Flow

```
┌─────────────────────────────────────────────────────────────┐
│                      FRONTEND APP                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. User Dashboard                                          │
│     ├─── Fetch tenant info (core-service)                  │
│     ├─── Display rent amount, due date                     │
│     └─── "Pay Rent" button                                 │
│                                                             │
│  2. Payment Page                                            │
│     ├─── Initialize Stripe (payment-service)               │
│     ├─── Show card form (Stripe.js)                        │
│     ├─── Create payment intent (payment-service)           │
│     └─── Confirm payment (Stripe.js)                       │
│                                                             │
│  3. Payment Success/Failure                                 │
│     └─── Show receipt / retry                              │
│                                                             │
└─────────────────────────────────────────────────────────────┘
         ↓                                ↓
   Core Service                    Payment Service
   (Tenant Data)                   (Stripe Payments)
```

---

## Step-by-Step Implementation

### Step 1: Create API Service Layer

**`src/services/api.js`**
```javascript
import axios from 'axios';

const coreAPI = axios.create({
  baseURL: process.env.REACT_APP_CORE_API_URL,
});

const paymentAPI = axios.create({
  baseURL: process.env.REACT_APP_PAYMENT_API_URL,
});

// Add auth token to requests
coreAPI.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export { coreAPI, paymentAPI };
```

---

### Step 2: Fetch Tenant Data

**`src/services/tenantService.js`**
```javascript
import { coreAPI } from './api';

export const tenantService = {
  // Get tenant profile
  getProfile: async () => {
    const response = await coreAPI.get('/tenant/profile');
    return response.data.data;
  },

  // Get tenant's property connections (rent info)
  getConnections: async () => {
    const response = await coreAPI.get('/tenant/connections');
    return response.data.data;
  },

  // Get specific connection details
  getConnectionDetails: async (connectionId) => {
    const response = await coreAPI.get(`/tenant/connections/${connectionId}`);
    return response.data.data;
  }
};
```

**Example Response from `/tenant/connections`:**
```json
{
  "success": true,
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "propertyName": "Sunset Apartments",
      "apartmentNumber": "A-101",
      "monthlyRent": 1500.00,
      "securityDeposit": 1500.00,
      "paymentFrequency": "monthly",
      "startDate": "2025-01-01",
      "endDate": "2025-12-31",
      "isActive": true,
      "managerName": "Jane Smith",
      "managerEmail": "jane.smith@example.com"
    }
  ]
}
```

---

### Step 3: Create Payment Service

**`src/services/paymentService.js`**
```javascript
import { paymentAPI } from './api';

export const paymentService = {
  // Get Stripe publishable key
  getPublishableKey: async () => {
    const response = await paymentAPI.get('/stripe/publishable-key');
    return response.data.publishableKey;
  },

  // Create payment intent
  createPaymentIntent: async (paymentData) => {
    const response = await paymentAPI.post('/create-card-intent', paymentData);
    return response.data;
  },

  // Get payment status
  getPaymentIntent: async (paymentIntentId) => {
    const response = await paymentAPI.get(`/${paymentIntentId}`);
    return response.data;
  },

  // Cancel payment
  cancelPayment: async (paymentIntentId) => {
    const response = await paymentAPI.post(`/${paymentIntentId}/cancel`);
    return response.data;
  }
};
```

---

### Step 4: Tenant Dashboard Component

**`src/components/TenantDashboard.jsx`**
```jsx
import React, { useState, useEffect } from 'react';
import { tenantService } from '../services/tenantService';
import { useNavigate } from 'react-router-dom';

function TenantDashboard() {
  const [profile, setProfile] = useState(null);
  const [connections, setConnections] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadTenantData();
  }, []);

  const loadTenantData = async () => {
    try {
      const [profileData, connectionsData] = await Promise.all([
        tenantService.getProfile(),
        tenantService.getConnections()
      ]);

      setProfile(profileData);
      setConnections(connectionsData);
    } catch (error) {
      console.error('Error loading tenant data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handlePayRent = (connection) => {
    // Navigate to payment page with connection data
    navigate('/pay-rent', {
      state: {
        connection,
        tenant: profile
      }
    });
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="tenant-dashboard">
      <h1>Welcome, {profile?.user?.firstName}!</h1>

      <div className="properties-section">
        <h2>Your Properties</h2>

        {connections.map((connection) => (
          <div key={connection.id} className="property-card">
            <h3>{connection.propertyName}</h3>
            <p>Apartment: {connection.apartmentNumber}</p>
            <p>Monthly Rent: ${connection.monthlyRent?.toFixed(2)}</p>
            <p>Lease Period: {connection.startDate} to {connection.endDate}</p>
            <p>Payment Frequency: {connection.paymentFrequency}</p>

            {connection.isActive && (
              <button
                onClick={() => handlePayRent(connection)}
                className="pay-rent-button"
              >
                Pay Rent - ${connection.monthlyRent?.toFixed(2)}
              </button>
            )}
          </div>
        ))}

        {connections.length === 0 && (
          <p>No active properties found.</p>
        )}
      </div>
    </div>
  );
}

export default TenantDashboard;
```

---

### Step 5: Payment Page Component

**`src/components/PaymentPage.jsx`**
```jsx
import React, { useState, useEffect } from 'react';
import { loadStripe } from '@stripe/stripe-js';
import { Elements } from '@stripe/react-stripe-js';
import { useLocation } from 'react-router-dom';
import { paymentService } from '../services/paymentService';
import PaymentForm from './PaymentForm';

function PaymentPage() {
  const [stripePromise, setStripePromise] = useState(null);
  const [loading, setLoading] = useState(true);
  const location = useLocation();

  const { connection, tenant } = location.state || {};

  useEffect(() => {
    initializeStripe();
  }, []);

  const initializeStripe = async () => {
    try {
      const publishableKey = await paymentService.getPublishableKey();
      setStripePromise(loadStripe(publishableKey));
    } catch (error) {
      console.error('Error initializing Stripe:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Loading payment system...</div>;

  if (!connection || !tenant) {
    return <div>Invalid payment request. Please go back to dashboard.</div>;
  }

  return (
    <div className="payment-page">
      <div className="payment-header">
        <h1>Pay Rent</h1>
        <div className="payment-details">
          <p><strong>Property:</strong> {connection.propertyName}</p>
          <p><strong>Apartment:</strong> {connection.apartmentNumber}</p>
          <p><strong>Amount:</strong> ${connection.monthlyRent?.toFixed(2)}</p>
          <p><strong>Payment for:</strong> {new Date().toLocaleDateString('en-US', { month: 'long', year: 'numeric' })}</p>
        </div>
      </div>

      {stripePromise && (
        <Elements stripe={stripePromise}>
          <PaymentForm
            amount={connection.monthlyRent}
            connection={connection}
            tenant={tenant}
          />
        </Elements>
      )}
    </div>
  );
}

export default PaymentPage;
```

---

### Step 6: Payment Form Component (Stripe Card Element)

**`src/components/PaymentForm.jsx`**
```jsx
import React, { useState } from 'react';
import { useStripe, useElements, CardElement } from '@stripe/react-stripe-js';
import { paymentService } from '../services/paymentService';
import { useNavigate } from 'react-router-dom';

const CARD_ELEMENT_OPTIONS = {
  style: {
    base: {
      color: '#32325d',
      fontFamily: '"Helvetica Neue", Helvetica, sans-serif',
      fontSmoothing: 'antialiased',
      fontSize: '16px',
      '::placeholder': {
        color: '#aab7c4'
      }
    },
    invalid: {
      color: '#fa755a',
      iconColor: '#fa755a'
    }
  }
};

function PaymentForm({ amount, connection, tenant }) {
  const stripe = useStripe();
  const elements = useElements();
  const navigate = useNavigate();

  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState(null);
  const [succeeded, setSucceeded] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!stripe || !elements) {
      return;
    }

    setProcessing(true);
    setError(null);

    try {
      // Step 1: Create payment intent on backend
      const paymentIntentData = {
        amount: Math.round(amount * 100), // Convert dollars to cents
        currency: 'usd',
        tenantId: tenant.user.id,
        tenantName: `${tenant.user.firstName} ${tenant.user.lastName}`,
        tenantEmail: tenant.user.email,
        tenantPhone: tenant.user.phone,
        description: `Rent payment - ${connection.propertyName} - ${connection.apartmentNumber} - ${new Date().toLocaleDateString('en-US', { month: 'long', year: 'numeric' })}`
      };

      console.log('Creating payment intent:', paymentIntentData);

      const paymentIntent = await paymentService.createPaymentIntent(paymentIntentData);

      if (paymentIntent.error) {
        throw new Error(paymentIntent.errorMessage);
      }

      // Step 2: Confirm payment with card details
      const cardElement = elements.getElement(CardElement);

      const result = await stripe.confirmCardPayment(paymentIntent.clientSecret, {
        payment_method: {
          card: cardElement,
          billing_details: {
            name: `${tenant.user.firstName} ${tenant.user.lastName}`,
            email: tenant.user.email,
            phone: tenant.user.phone
          }
        }
      });

      if (result.error) {
        // Payment failed
        setError(result.error.message);
        setProcessing(false);
      } else if (result.paymentIntent.status === 'succeeded') {
        // Payment succeeded
        setSucceeded(true);
        setProcessing(false);

        // Navigate to success page after 2 seconds
        setTimeout(() => {
          navigate('/payment-success', {
            state: {
              paymentIntentId: result.paymentIntent.id,
              amount: amount,
              connection: connection
            }
          });
        }, 2000);
      }
    } catch (err) {
      setError(err.message || 'An unexpected error occurred');
      setProcessing(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="payment-form">
      <div className="form-section">
        <h3>Payment Information</h3>

        <div className="card-element-container">
          <CardElement options={CARD_ELEMENT_OPTIONS} />
        </div>

        {error && (
          <div className="error-message">
            {error}
          </div>
        )}

        {succeeded && (
          <div className="success-message">
            Payment successful! Redirecting...
          </div>
        )}

        <div className="payment-summary">
          <p>Amount to pay: <strong>${amount?.toFixed(2)}</strong></p>
        </div>

        <button
          type="submit"
          disabled={!stripe || processing || succeeded}
          className="submit-button"
        >
          {processing ? 'Processing...' : `Pay $${amount?.toFixed(2)}`}
        </button>

        <p className="secure-note">
          🔒 Payments are secure and encrypted via Stripe
        </p>
      </div>
    </form>
  );
}

export default PaymentForm;
```

---

### Step 7: Payment Success Component

**`src/components/PaymentSuccess.jsx`**
```jsx
import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

function PaymentSuccess() {
  const location = useLocation();
  const navigate = useNavigate();

  const { paymentIntentId, amount, connection } = location.state || {};

  return (
    <div className="payment-success">
      <div className="success-icon">✅</div>

      <h1>Payment Successful!</h1>

      <div className="payment-details">
        <p><strong>Amount Paid:</strong> ${amount?.toFixed(2)}</p>
        <p><strong>Property:</strong> {connection?.propertyName}</p>
        <p><strong>Apartment:</strong> {connection?.apartmentNumber}</p>
        <p><strong>Transaction ID:</strong> {paymentIntentId}</p>
        <p><strong>Date:</strong> {new Date().toLocaleDateString()}</p>
      </div>

      <p>A receipt has been sent to your email.</p>

      <button onClick={() => navigate('/dashboard')}>
        Back to Dashboard
      </button>
    </div>
  );
}

export default PaymentSuccess;
```

---

### Step 8: Routing Setup

**`src/App.jsx`**
```jsx
import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import TenantDashboard from './components/TenantDashboard';
import PaymentPage from './components/PaymentPage';
import PaymentSuccess from './components/PaymentSuccess';
import Login from './components/Login';
import ProtectedRoute from './components/ProtectedRoute';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />

        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <TenantDashboard />
            </ProtectedRoute>
          }
        />

        <Route
          path="/pay-rent"
          element={
            <ProtectedRoute>
              <PaymentPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/payment-success"
          element={
            <ProtectedRoute>
              <PaymentSuccess />
            </ProtectedRoute>
          }
        />

        <Route path="/" element={<Navigate to="/dashboard" />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
```

---

## Sample CSS Styles

**`src/styles/payment.css`**
```css
.payment-page {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.payment-header {
  background: #f7f9fc;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.payment-details {
  margin-top: 15px;
}

.payment-details p {
  margin: 8px 0;
}

.payment-form {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.card-element-container {
  border: 1px solid #ccc;
  padding: 15px;
  border-radius: 4px;
  margin: 20px 0;
}

.error-message {
  color: #fa755a;
  background: #fef5f3;
  padding: 10px;
  border-radius: 4px;
  margin: 15px 0;
}

.success-message {
  color: #28a745;
  background: #f0f9f4;
  padding: 10px;
  border-radius: 4px;
  margin: 15px 0;
}

.submit-button {
  width: 100%;
  padding: 15px;
  background: #5469d4;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 20px;
}

.submit-button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.submit-button:hover:not(:disabled) {
  background: #3d52b3;
}

.secure-note {
  text-align: center;
  color: #666;
  font-size: 14px;
  margin-top: 15px;
}

.payment-summary {
  margin: 20px 0;
  padding: 15px;
  background: #f7f9fc;
  border-radius: 4px;
  text-align: center;
}

.payment-success {
  max-width: 500px;
  margin: 50px auto;
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.success-icon {
  font-size: 72px;
  margin-bottom: 20px;
}

.tenant-dashboard {
  padding: 20px;
}

.property-card {
  background: white;
  padding: 20px;
  margin: 15px 0;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.pay-rent-button {
  background: #28a745;
  color: white;
  padding: 12px 24px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 15px;
}

.pay-rent-button:hover {
  background: #218838;
}
```

---

## Testing the Flow

### 1. Start Backend Services
```bash
cd "BMS App/BMS Backend"
docker-compose -f docker-compose.prod.yml up -d
```

### 2. Test Login & Get Token
```bash
# Login as tenant
curl -X POST http://localhost:8080/api/auth/tenant/login \
  -H 'Content-Type: application/json' \
  -d '{
    "identifier": "tenant@example.com",
    "password": "Password123!"
  }'

# Save the accessToken from response
```

### 3. Test Tenant Data Fetching
```bash
# Get tenant profile
curl http://localhost:8080/api/tenant/profile \
  -H 'Authorization: Bearer YOUR_ACCESS_TOKEN'

# Get tenant connections (rent info)
curl http://localhost:8080/api/tenant/connections \
  -H 'Authorization: Bearer YOUR_ACCESS_TOKEN'
```

### 4. Test Payment Flow
```bash
# Create payment intent
curl -X POST http://localhost:8082/api/payments/create-card-intent \
  -H 'Content-Type: application/json' \
  -d '{
    "amount": 150000,
    "currency": "usd",
    "tenantId": 1,
    "tenantName": "John Doe",
    "tenantEmail": "john@example.com",
    "description": "November rent"
  }'
```

### 5. Test with Stripe Test Cards
In your frontend payment form, use:
- **Card:** 4242 4242 4242 4242
- **Expiry:** 12/34
- **CVC:** 123
- **ZIP:** 12345

---

## Error Scenarios to Handle

### 1. No Active Connections
```jsx
{connections.length === 0 && (
  <div className="no-properties">
    <p>You don't have any active leases.</p>
    <button onClick={() => navigate('/browse-properties')}>
      Browse Available Properties
    </button>
  </div>
)}
```

### 2. Payment Declined
```jsx
if (result.error) {
  if (result.error.decline_code === 'insufficient_funds') {
    setError('Insufficient funds. Please use another card.');
  } else if (result.error.code === 'card_declined') {
    setError('Card declined. Please try another payment method.');
  } else {
    setError(result.error.message);
  }
}
```

### 3. Network Errors
```jsx
try {
  const paymentIntent = await paymentService.createPaymentIntent(data);
} catch (error) {
  if (error.response?.status === 401) {
    // Token expired
    navigate('/login');
  } else if (error.response?.status === 500) {
    setError('Server error. Please try again later.');
  } else {
    setError('Network error. Please check your connection.');
  }
}
```

---

## Security Best Practices

1. **Never store card details** - Let Stripe.js handle it
2. **Use HTTPS in production** - Always use secure connections
3. **Validate token expiry** - Check JWT expiration before API calls
4. **Don't log sensitive data** - Remove console.logs in production
5. **Implement CSRF protection** - Use tokens for form submissions

---

## Production Deployment

### Environment Variables for Production
```env
REACT_APP_CORE_API_URL=https://api.yourdomain.com/api
REACT_APP_PAYMENT_API_URL=https://api.yourdomain.com/payment-api
```

### Build for Production
```bash
npm run build
```

Deploy the `build` folder to your hosting service (Netlify, Vercel, etc.)

---

## Summary

This integration allows tenants to:
1. ✅ View their properties and rent amounts
2. ✅ Click "Pay Rent" button
3. ✅ Enter card details securely via Stripe
4. ✅ Complete payment
5. ✅ Receive confirmation

**No PCI compliance needed** - Stripe handles all card data!

---

## Need Help?

- Check browser console for errors
- Verify API URLs are correct
- Ensure backend services are running
- Check Stripe dashboard for payment logs
