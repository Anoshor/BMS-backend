# BMS Backend

Building Management System - Microservices Backend with Stripe Payment Integration

## Architecture

```
BMS Backend/
├── services/
│   ├── core-service/       # Main API (Port 8080)
│   │   ├── Auth, Users, Properties, Apartments
│   │   ├── Tenants, Leases, Maintenance
│   │   └── AWS S3 + CloudFront Integration
│   └── payment-service/    # Payments (Port 8082)
│       └── Stripe Integration (Cards, ACH, Webhooks)
├── docker-compose.yml      # Full stack
├── docker-compose.prod.yml # Databases only
└── .env                    # Environment variables
```

## Quick Start

### 1. Prerequisites

- Docker Desktop
- Stripe CLI (`brew install stripe/stripe-cli/stripe`)
- jq (`brew install jq`) - for test scripts

### 2. Environment Setup

Create `.env` in project root (already exists with your config):

```bash
# JWT
JWT_SECRET=your_jwt_secret_min_32_chars
JWT_ACCESS_TOKEN_EXPIRATION=900
JWT_REFRESH_TOKEN_EXPIRATION=2592000

# AWS S3
AWS_S3_BUCKET_NAME=bms-app-storage
AWS_S3_REGION=us-east-2
AWS_S3_ACCESS_KEY=your_access_key
AWS_S3_SECRET_KEY=your_secret_key
AWS_S3_BASE_URL=https://s3.us-east-2.amazonaws.com
AWS_CLOUDFRONT_DOMAIN=your_domain.cloudfront.net
AWS_CLOUDFRONT_ENABLED=true

# Stripe
STRIPE_SECRET_KEY=sk_test_xxx
STRIPE_PUBLISHABLE_KEY=pk_test_xxx
STRIPE_WEBHOOK_SECRET=whsec_xxx
```

### 3. Start All Services

```bash
# Start everything (databases + services)
docker-compose up -d

# Check status
docker-compose ps
```

### 4. Start Stripe Webhook Listener

```bash
# Login to Stripe (if session expired)
stripe login

# Forward webhooks to payment service
stripe listen --forward-to localhost:8082/api/webhooks/stripe
```

### 5. Verify Services

```bash
# Core service
curl http://localhost:8080/api/v1/properties
# Expected: 401 (auth required) - service is running

# Payment service
curl http://localhost:8082/api/payments/stripe/publishable-key
# Expected: {"publishableKey":"pk_test_..."}
```

## Services Overview

| Service | Port | Database | Description |
|---------|------|----------|-------------|
| core-service | 8080 | PostgreSQL:5432 | Main business logic |
| payment-service | 8082 | PostgreSQL:5434 | Stripe payments |
| postgres-core | 5432 | - | Core DB |
| postgres-payment | 5434 | - | Payment DB |
| redis | 6379 | - | Cache |

## Common Commands

```bash
# Start all
docker-compose up -d

# Stop all
docker-compose down

# View logs
docker-compose logs -f bms-core-service
docker-compose logs -f bms-payment-service

# Restart a service
docker-compose restart bms-payment-service

# Reset databases (delete volumes)
docker-compose down -v
```

## Running E2E Tests

```bash
# Full integration test
./complete-setup-and-test.sh

# Simple payment test
./simple-payment-test.sh

# Automated payment test
./automated-payment-test.sh
```

## API Endpoints

### Auth
- `POST /api/v1/auth/signup` - Register
- `POST /api/v1/auth/login` - Login
- `POST /api/v1/auth/verify-email` - Verify email
- `POST /api/v1/auth/verify-phone` - Verify phone

### Properties & Apartments
- `GET/POST /api/v1/properties/buildings` - Properties
- `GET/POST /api/v1/apartments` - Apartments

### Tenants & Leases
- `POST /api/v1/tenants/connect` - Create lease
- `GET /api/v1/tenants/connections` - List connections
- `GET /api/v1/leases/{id}/payment-details` - Payment info
- `GET /api/v1/leases/{id}/payment-summary` - Summary
- `GET /api/v1/leases/{id}/payment-schedule` - Schedule

### Payments
- `POST /api/payments/create-card-intent` - Create payment
- `GET /api/payments/stripe/publishable-key` - Get Stripe key
- `POST /api/webhooks/stripe` - Webhook endpoint

### Maintenance
- `GET/POST /api/v1/maintenance/requests` - Requests
- `GET /api/v1/maintenance/categories` - Categories

## Stripe Test Cards

| Card | Result |
|------|--------|
| 4242 4242 4242 4242 | Success |
| 4000 0000 0000 0002 | Declined |
| 4000 0025 0000 3155 | 3D Secure |

## Build & Push Docker Images

```bash
# Build and push to Docker Hub
./build-and-push.sh
```

## Troubleshooting

**Port already in use:**
```bash
lsof -i :8080  # Find process
kill -9 <PID>  # Kill it
```

**Payment service can't reach core service:**
- Ensure `CORE_SERVICE_URL=http://bms-core-service:8080` is in docker-compose.yml

**Stripe webhook not working:**
```bash
stripe login  # Re-authenticate
stripe listen --forward-to localhost:8082/api/webhooks/stripe
```

**Reset everything:**
```bash
docker-compose down -v
docker-compose up -d
```

## Docker Hub

- Core: `anoshorpaul/bms-core-service:latest`
- Payment: `anoshorpaul/bms-payment-service:latest`
