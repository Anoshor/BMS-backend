# 🚀 BMS Backend - Deployment Guide

This guide is for deploying the pre-built BMS Backend Docker image.

---

## 📋 Prerequisites

- Docker & Docker Compose installed
- AWS S3 bucket and credentials (for file storage)
- At least 2GB RAM available

---

## 🔧 Quick Start

### 1. Download the deployment files

You need these files:
- `docker-compose-deployment.yml`
- `.env.deployment.example`

### 2. Create your environment file

```bash
cp .env.deployment.example .env
```

### 3. Edit `.env` with your credentials

**Required variables:**
```bash
# Generate a secure JWT secret (minimum 256 bits)
JWT_SECRET=your-super-secure-random-string-here

# Your AWS S3 credentials
AWS_S3_BUCKET_NAME=your-bucket-name
AWS_S3_ACCESS_KEY=AKIA...
AWS_S3_SECRET_KEY=...
AWS_S3_REGION=us-east-2

# CloudFront (if using)
AWS_CLOUDFRONT_DOMAIN=your-domain.cloudfront.net
```

**Optional but recommended:**
```bash
# Change database password
POSTGRES_PASSWORD=your-secure-database-password
```

### 4. Deploy

```bash
docker-compose -f docker-compose-deployment.yml up -d
```

### 5. Verify deployment

**Check services are running:**
```bash
docker-compose -f docker-compose-deployment.yml ps
```

**Check logs:**
```bash
docker-compose -f docker-compose-deployment.yml logs -f bms-core
```

**Test API:**
```bash
curl http://localhost:8080/api/v1/health
```

---

## 🗄️ What Gets Deployed

### Services:
1. **PostgreSQL 15** (Port 5432)
   - Database: `bms_core`
   - User: `bms_user`
   - Persistent volume for data

2. **BMS Core Service** (Port 8080)
   - Pre-built image from Docker Hub
   - Connected to PostgreSQL
   - Health checks enabled
   - Auto-restart on failure

### Data Persistence:
- PostgreSQL data stored in Docker volume `postgres_data`
- Data survives container restarts
- To backup: `docker exec bms-postgres pg_dump -U bms_user bms_core > backup.sql`

---

## 🔐 Security Best Practices

1. ✅ **Never commit `.env` file**
2. ✅ **Change default JWT_SECRET**
3. ✅ **Use strong database passwords**
4. ✅ **Rotate AWS credentials regularly**
5. ✅ **Use CloudFront for S3 (optional but recommended)**
6. ✅ **Enable firewall (only expose port 8080)**

---

## 📊 Environment Variables Reference

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `JWT_SECRET` | ✅ Yes | - | Secret key for JWT tokens (min 256 bits) |
| `JWT_ACCESS_TOKEN_EXPIRATION` | No | 900 | Access token expiry (seconds) |
| `JWT_REFRESH_TOKEN_EXPIRATION` | No | 2592000 | Refresh token expiry (seconds) |
| `AWS_S3_BUCKET_NAME` | ✅ Yes | - | S3 bucket name |
| `AWS_S3_ACCESS_KEY` | ✅ Yes | - | AWS access key |
| `AWS_S3_SECRET_KEY` | ✅ Yes | - | AWS secret key |
| `AWS_S3_REGION` | No | us-east-2 | AWS region |
| `AWS_CLOUDFRONT_DOMAIN` | No | - | CloudFront domain |
| `POSTGRES_PASSWORD` | No | bms_password | Database password |

---

## 🔄 Updates & Maintenance

### Pull latest image:
```bash
docker-compose -f docker-compose-deployment.yml pull
docker-compose -f docker-compose-deployment.yml up -d
```

### View logs:
```bash
# All services
docker-compose -f docker-compose-deployment.yml logs -f

# Specific service
docker-compose -f docker-compose-deployment.yml logs -f bms-core
docker-compose -f docker-compose-deployment.yml logs -f postgres
```

### Restart services:
```bash
# All services
docker-compose -f docker-compose-deployment.yml restart

# Specific service
docker-compose -f docker-compose-deployment.yml restart bms-core
```

### Stop services:
```bash
docker-compose -f docker-compose-deployment.yml down
```

### Stop and remove data (⚠️ DESTRUCTIVE):
```bash
docker-compose -f docker-compose-deployment.yml down -v
```

---

## 🐛 Troubleshooting

### Service won't start:
```bash
# Check logs
docker-compose -f docker-compose-deployment.yml logs bms-core

# Common issues:
# - Missing .env file
# - Invalid AWS credentials
# - Port 8080 already in use
# - PostgreSQL not ready
```

### Database connection issues:
```bash
# Check PostgreSQL is healthy
docker-compose -f docker-compose-deployment.yml ps

# Test PostgreSQL connection
docker exec bms-postgres psql -U bms_user -d bms_core -c "SELECT 1"
```

### AWS S3 issues:
- Verify bucket exists and region is correct
- Check IAM permissions for access key
- Test credentials: `aws s3 ls s3://your-bucket --profile your-profile`

---

## 📈 Monitoring

### Health Check Endpoints:
- **API Health**: `http://localhost:8080/api/v1/health`
- **Database**: PostgreSQL health check runs automatically

### Resource Usage:
```bash
docker stats bms-core-service bms-postgres
```

---

## 🆘 Support

If you encounter issues:
1. Check logs: `docker-compose -f docker-compose-deployment.yml logs -f`
2. Verify environment variables in `.env`
3. Ensure AWS credentials are valid
4. Check port availability: `lsof -i :8080`

---

## 📝 Notes

- **First startup** takes 30-60 seconds for database initialization
- **API Documentation**: Available at `http://localhost:8080/swagger-ui.html` (if enabled)
- **Default API path**: `http://localhost:8080/api/v1/`
- **PostgreSQL port**: Only exposed to localhost (5432)

---

## 🔄 Migration from H2 to PostgreSQL

If you previously deployed with H2:
- **Data will NOT migrate automatically**
- This is a fresh PostgreSQL installation
- Old H2 data is not compatible
- Contact developer for data migration tools if needed
