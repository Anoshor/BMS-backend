# 🐋 BMS Docker Setup Guide

## Overview
This guide explains how to build and deploy the BMS Backend microservices with PostgreSQL databases.

---

## 📁 Architecture

```
BMS Backend (Microservices)
├── Core Service (Port 8080)
│   └── PostgreSQL DB: bms_core (Port 5432)
├── Notification Service (Port 8081)
│   └── PostgreSQL DB: bms_notifications (Port 5433)
└── Redis (Port 6379)
```

---

## 🚀 Quick Start

### 1. Local Development (Docker PostgreSQL + Local Spring Boot)

**Start databases:**
```bash
docker-compose -f docker-compose-dev.yml up -d
```

**Run services locally:**
```bash
# Core service
cd services/core-service
mvn spring-boot:run

# Notification service
cd services/notification-service
mvn spring-boot:run
```

**Stop databases:**
```bash
docker-compose -f docker-compose-dev.yml down
```

---

### 2. Full Docker Deployment (All Services in Containers)

**Build and run everything:**
```bash
docker-compose up --build
```

This will:
- Build both service images
- Start PostgreSQL databases (core + notification)
- Start Redis
- Launch both microservices

**Access:**
- Core Service: http://localhost:8080
- Notification Service: http://localhost:8081
- Redis: localhost:6379

---

### 3. Build & Push to Docker Hub (Production)

**Using the fast build script:**
```bash
cd services/core-service
./docker-build-fast.sh --multi --push
```

This will:
- Build multi-platform images (amd64 + arm64)
- Push to Docker Hub: `anoshorpaul/bms-backend:latest`
- Use registry caching for faster builds

**Single-platform build (faster for dev):**
```bash
./docker-build-fast.sh --fast
```

---

## 🔐 Environment Variables

**Before deploying, set up your environment variables:**

1. Copy the example file:
```bash
cp .env.example .env
```

2. Edit `.env` with your actual credentials:
```bash
nano .env
```

Required variables:
- `JWT_SECRET` - Secure random string for JWT signing
- `AWS_S3_*` - AWS S3 credentials
- `AWS_CLOUDFRONT_*` - CloudFront configuration

---

## 🗄️ Database Configuration

### Local Development
- **Core DB**: `localhost:5432/bms_core`
- **Notification DB**: `localhost:5433/bms_notifications`
- **User**: `bms_user`
- **Password**: `bms_password`

### Production (AWS RDS)
Update `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://your-rds-endpoint.rds.amazonaws.com:5432/bms_core
```

---

## 📦 Docker Compose Files

| File | Purpose | Use Case |
|------|---------|----------|
| `docker-compose-dev.yml` | DBs only | Local Spring Boot development |
| `docker-compose.yml` | Full stack | Production deployment |
| `services/core-service/docker-compose.yml` | Single service | Deploy core service standalone |

---

## 🔧 Useful Commands

**View logs:**
```bash
docker-compose logs -f bms-core-service
docker-compose logs -f postgres-core
```

**Rebuild a single service:**
```bash
docker-compose up -d --build bms-core-service
```

**Access PostgreSQL CLI:**
```bash
docker exec -it bms-postgres-core psql -U bms_user -d bms_core
```

**Clean up everything (including data):**
```bash
docker-compose down -v
```

---

## 🚨 Security Notes

1. ✅ `.env` file is gitignored
2. ✅ Exposed AWS credentials removed from docker-compose files
3. ✅ All secrets should be in `.env` file
4. ✅ Never commit `.env` to git

---

## 📊 Health Checks

**Core Service:**
```bash
curl http://localhost:8080/api/v1/health
```

**PostgreSQL:**
```bash
docker exec bms-postgres-core pg_isready -U bms_user -d bms_core
```

---

## 🆘 Troubleshooting

**PostgreSQL not starting:**
```bash
docker-compose down -v
docker-compose up -d
```

**Service can't connect to DB:**
- Check if PostgreSQL is healthy: `docker ps`
- Verify connection string in environment variables

**Build fails:**
```bash
# Clear build cache
docker builder prune -a

# Rebuild without cache
docker-compose build --no-cache
```

---

## 🎯 Next Steps

1. Set up AWS RDS PostgreSQL for production
2. Configure environment-specific profiles (dev/staging/prod)
3. Set up CI/CD pipeline for automatic builds
4. Configure monitoring and logging
