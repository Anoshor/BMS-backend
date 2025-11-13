# BMS Backend Deployment Guide

## Quick Start (Using Docker Compose)

### Prerequisites
- Docker & Docker Compose installed
- Environment variables configured

### Steps

1. **Clone/Download the repository**
   ```bash
   git clone <your-repo-url>
   cd BMS\ App/BMS\ Backend
   ```

2. **Configure environment variables**
   ```bash
   cp .env.example .env
   # Edit .env with your actual values
   nano .env
   ```

3. **Start the services**
   ```bash
   docker-compose -f docker-compose.prod.yml up -d
   ```

4. **Verify services are running**
   ```bash
   docker-compose -f docker-compose.prod.yml ps
   ```

### Service Endpoints

| Service | Port | URL | Swagger UI |
|---------|------|-----|------------|
| Core Service | 8080 | http://localhost:8080 | http://localhost:8080/swagger-ui.html |
| Payment Service | 8082 | http://localhost:8082 | http://localhost:8082/swagger-ui.html |
| PostgreSQL (Core) | 5432 | localhost:5432 | - |
| PostgreSQL (Payment) | 5434 | localhost:5434 | - |
| Redis | 6379 | localhost:6379 | - |

### Useful Commands

```bash
# Start services
docker-compose -f docker-compose.prod.yml up -d

# Stop services
docker-compose -f docker-compose.prod.yml down

# View logs
docker-compose -f docker-compose.prod.yml logs -f

# View logs for specific service
docker-compose -f docker-compose.prod.yml logs -f bms-core-service

# Restart a service
docker-compose -f docker-compose.prod.yml restart bms-core-service

# Pull latest images
docker-compose -f docker-compose.prod.yml pull

# Update and restart (pull latest images and restart)
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d
```

## Frontend Integration

### API Base URLs

For local development:
```javascript
const API_BASE_URL = 'http://localhost:8080/api'
const PAYMENT_API_BASE_URL = 'http://localhost:8082/api'
```

For production:
```javascript
const API_BASE_URL = 'https://your-domain.com/api'
const PAYMENT_API_BASE_URL = 'https://your-domain.com/payment-api'
```

### Example API Calls

**Core Service - Get Tenants:**
```bash
curl http://localhost:8080/api/tenants
```

**Payment Service - Create Payment Intent:**
```bash
curl -X POST http://localhost:8082/api/payments/create-card-intent \
  -H 'Content-Type: application/json' \
  -d '{
    "amount": 5000,
    "currency": "usd",
    "tenantId": 1,
    "tenantName": "John Doe",
    "tenantEmail": "john@example.com",
    "description": "Monthly rent payment"
  }'
```

## Environment Variables

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `JWT_SECRET` | Secret key for JWT tokens | `your-secret-key-min-32-chars` |
| `STRIPE_SECRET_KEY` | Stripe API secret key | `sk_test_...` |
| `STRIPE_PUBLISHABLE_KEY` | Stripe publishable key | `pk_test_...` |
| `AWS_S3_BUCKET_NAME` | S3 bucket name | `my-bms-bucket` |
| `AWS_S3_ACCESS_KEY` | AWS access key | `AKIA...` |
| `AWS_S3_SECRET_KEY` | AWS secret key | `...` |

### Optional Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `JWT_ACCESS_TOKEN_EXPIRATION` | Access token expiration (seconds) | `900` (15 min) |
| `JWT_REFRESH_TOKEN_EXPIRATION` | Refresh token expiration (seconds) | `2592000` (30 days) |
| `AWS_CLOUDFRONT_ENABLED` | Enable CloudFront CDN | `true` |

## Troubleshooting

### Services not starting
```bash
# Check logs
docker-compose -f docker-compose.prod.yml logs

# Check if ports are already in use
lsof -i :8080
lsof -i :8082
```

### Database connection issues
```bash
# Restart database
docker-compose -f docker-compose.prod.yml restart postgres-core postgres-payment

# Check database logs
docker-compose -f docker-compose.prod.yml logs postgres-core
```

### Reset everything
```bash
# Stop and remove all containers, networks, volumes
docker-compose -f docker-compose.prod.yml down -v

# Start fresh
docker-compose -f docker-compose.prod.yml up -d
```

## Production Deployment

For production deployment (AWS, GCP, etc.), you'll need:

1. **Reverse Proxy** (Nginx/Traefik) for HTTPS and routing
2. **Environment-specific `.env` file** with production secrets
3. **External database** (RDS, Cloud SQL) instead of Docker PostgreSQL
4. **Persistent volumes** for data storage
5. **Load balancer** for high availability

Contact the backend team for production deployment configuration.

## Docker Images

The following Docker images are available on Docker Hub:

- `anoshorpaul/bms-core-service:latest`
- `anoshorpaul/bms-payment-service:latest`

Both images support **linux/amd64** and **linux/arm64** platforms.

## Support

For issues or questions:
- Check Swagger UI for API documentation
- Review logs: `docker-compose -f docker-compose.prod.yml logs -f`
- Contact backend team
