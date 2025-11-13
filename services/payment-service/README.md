# BMS Payment Service

A standalone microservice for handling payment processing using Stripe. Supports both **Card Payments** and **ACH Bank Transfers**.

## Features

- ✅ **Card Payments** - Credit/Debit card processing via Stripe
- ✅ **ACH Payments** - US Bank Account transfers
- ✅ **Webhook Support** - Real-time payment status updates
- ✅ **Payment Intent Management** - Create, retrieve, and cancel payment intents
- ✅ **Secure Configuration** - Environment-based API key management
- ✅ **OpenAPI Documentation** - Interactive API documentation with Swagger UI
- ✅ **Docker Support** - Containerized deployment

## Prerequisites

- Java 17+
- Maven 3.6+
- PostgreSQL 14+ (or use Docker)
- Stripe Account ([Sign up here](https://dashboard.stripe.com/register))

## Quick Start

### 1. Clone and Navigate

```bash
cd services/payment-service
```

### 2. Configure Environment Variables

Copy the example environment file:

```bash
cp .env.example .env
```

Edit `.env` and add your Stripe API keys:

```env
STRIPE_SECRET_KEY=sk_test_your_actual_secret_key
STRIPE_PUBLISHABLE_KEY=pk_test_your_actual_publishable_key
STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret
```

**Get your Stripe keys:**
- Secret Key: https://dashboard.stripe.com/apikeys
- Webhook Secret: https://dashboard.stripe.com/webhooks

### 3. Run Locally

**Option A: Using Maven**

```bash
# Make sure PostgreSQL is running
mvn spring-boot:run
```

**Option B: Using Docker Compose**

```bash
# From the project root
docker-compose up payment-service
```

The service will start on **http://localhost:8082**

### 4. Access API Documentation

Open Swagger UI:
```
http://localhost:8082/swagger-ui.html
```

## API Endpoints

### Payment Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/payments/stripe/publishable-key` | Get Stripe publishable key |
| POST | `/api/payments/create-card-intent` | Create card payment intent |
| POST | `/api/payments/create-ach-intent` | Create ACH payment intent |
| GET | `/api/payments/{paymentIntentId}` | Get payment intent details |
| POST | `/api/payments/{paymentIntentId}/cancel` | Cancel payment intent |

### Webhook

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/webhooks/stripe` | Stripe webhook handler |

## Example Usage

### Create Card Payment Intent

```bash
curl -X POST http://localhost:8082/api/payments/create-card-intent \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 5000,
    "currency": "usd",
    "description": "Battery rental payment",
    "receiptEmail": "customer@example.com"
  }'
```

**Response:**
```json
{
  "clientSecret": "pi_xxx_secret_yyy",
  "paymentIntentId": "pi_xxxxxxxxx",
  "status": "requires_payment_method",
  "amount": 5000,
  "currency": "usd"
}
```

### Create ACH Payment Intent

```bash
curl -X POST http://localhost:8082/api/payments/create-ach-intent \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 10000,
    "currency": "usd",
    "description": "Monthly subscription"
  }'
```

## Testing

### Test Cards

Use Stripe's test cards for development:

| Card Number | Description |
|-------------|-------------|
| `4242 4242 4242 4242` | Successful payment |
| `4000 0000 0000 9995` | Declined payment |
| `4000 0025 0000 3155` | Requires authentication |

**CVV:** Any 3 digits
**Expiry:** Any future date

### Test ACH

Use Stripe's test bank accounts:

- **Routing Number:** `110000000`
- **Account Number:** `000123456789`

## Webhook Setup

### 1. Using Stripe CLI (Development)

Install Stripe CLI:
```bash
brew install stripe/stripe-cli/stripe
```

Login:
```bash
stripe login
```

Forward webhooks to local server:
```bash
stripe listen --forward-to localhost:8082/api/webhooks/stripe
```

Copy the webhook signing secret to your `.env` file.

### 2. Production Webhook

1. Go to https://dashboard.stripe.com/webhooks
2. Click "Add endpoint"
3. Enter your webhook URL: `https://your-domain.com/api/webhooks/stripe`
4. Select events to listen to:
   - `payment_intent.succeeded`
   - `payment_intent.payment_failed`
   - `payment_intent.canceled`
5. Copy the webhook signing secret to your environment variables

## Architecture

```
payment-service/
├── src/main/java/com/bms/payment/
│   ├── config/
│   │   └── StripeConfig.java          # Stripe initialization
│   ├── controller/
│   │   ├── PaymentController.java     # Payment endpoints
│   │   └── WebhookController.java     # Webhook handler
│   ├── dto/
│   │   ├── PaymentIntentRequest.java  # Request DTOs
│   │   └── PaymentIntentResponse.java # Response DTOs
│   ├── service/
│   │   └── PaymentService.java        # Business logic
│   └── PaymentServiceApplication.java # Main application
└── src/main/resources/
    ├── application.yml                # Default config
    └── application-dev.yml            # Dev config
```

## Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `STRIPE_SECRET_KEY` | Stripe secret API key | `sk_test_...` |
| `STRIPE_PUBLISHABLE_KEY` | Stripe publishable key | `pk_test_...` |
| `STRIPE_WEBHOOK_SECRET` | Webhook signing secret | `whsec_...` |
| `DATABASE_URL` | PostgreSQL connection URL | `jdbc:postgresql://...` |
| `DATABASE_USERNAME` | Database username | `bms_user` |
| `DATABASE_PASSWORD` | Database password | `bms_password` |
| `SERVER_PORT` | Service port | `8082` |

## Database Schema

The service uses PostgreSQL for storing payment transaction records and audit logs (to be implemented).

## Security Best Practices

1. ✅ Never expose secret keys to frontend
2. ✅ Always verify webhook signatures
3. ✅ Use HTTPS in production
4. ✅ Store API keys in environment variables
5. ✅ Implement proper error handling
6. ✅ Log all payment events for audit

## Integration with Core Service

To integrate with the BMS core service:

1. Call payment service from core service when user initiates payment
2. Store payment intent ID in your order/booking records
3. Listen to webhooks to update order status
4. Handle success/failure cases appropriately

## Monitoring & Logging

The service logs all payment operations at different levels:
- **INFO**: Successful operations
- **ERROR**: Payment failures and errors
- **DEBUG**: Detailed Stripe API interactions

## Troubleshooting

### Issue: "Webhook signature verification failed"
**Solution:** Ensure your webhook secret matches the one from Stripe dashboard

### Issue: "No API key provided"
**Solution:** Check that `STRIPE_SECRET_KEY` is set in your environment

### Issue: "PaymentIntent creation failed"
**Solution:** Verify amount is at least 50 cents and currency is valid

## Support

For issues or questions:
1. Check Stripe API documentation: https://stripe.com/docs/api
2. Review Stripe logs: https://dashboard.stripe.com/logs
3. Check application logs for detailed error messages

## License

MIT License - see LICENSE file for details
