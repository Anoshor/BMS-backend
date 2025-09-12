# BMS Backend - Docker Setup

The BMS (Building Management System) backend is now fully containerized and ready for sharing!

## 🚀 Quick Start

### Prerequisites
- Docker installed on your machine
- Docker Compose installed

### Running the Service

1. **Start the service:**
   ```bash
   docker-compose up -d
   ```

2. **Check if it's running:**
   ```bash
   curl http://localhost:8082/api/v1/health
   ```

3. **Stop the service:**
   ```bash
   docker-compose down
   ```

## 📋 Service Details

- **Service URL:** http://localhost:8082
- **Health Check:** http://localhost:8082/api/v1/health
- **Database:** H2 (file-based, persisted in Docker volume)
- **Data Volume:** Persistent data storage for H2 database

## 🔗 API Testing

All existing APIs are available on port 8082. Update your API testing URLs from port 8080 to 8082:

### Example API Calls
```bash
# Manager Registration
curl -X POST "http://localhost:8082/api/v1/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "manager@example.com",
    "contactNum": "9876543210",
    "password": "password123",
    "firstName": "John",
    "lastName": "Manager",
    "dob": "1990-01-15T00:00:00.000Z",
    "gender": "male",
    "role": "MANAGER"
  }'

# Manager Login
curl -X POST "http://localhost:8082/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "manager@example.com",
    "password": "password123",
    "role": "MANAGER"
  }'
```

## 📚 Complete API Documentation

See `testing/API_TESTING_GUIDE.md` for complete API documentation. Simply replace `localhost:8080` with `localhost:8082` in all API calls.

## 🐳 Docker Configuration

### Built Images
- **Base Images:** OpenJDK 17 (builder) + Eclipse Temurin 17 JRE (runtime)
- **Optimizations:** Multi-stage build, non-root user, health checks
- **Security:** Runs as non-privileged `bmsapp` user

### Persistent Data
- Database files are stored in Docker volume `core-service_bms-data`
- Data persists across container restarts

### Environment Variables
- `SPRING_PROFILES_ACTIVE=docker`
- `JWT_SECRET=mySecretKey123456789012345678901234567890`
- H2 database configured for file persistence

## 🔧 Development

### Rebuild After Code Changes
```bash
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

### View Logs
```bash
docker-compose logs -f bms-core
```

### Access H2 Console (if enabled)
- URL: http://localhost:8082/api/v1/h2-console
- JDBC URL: `jdbc:h2:file:/app/data/bmsdb`
- Username: `sa`
- Password: (empty)

## 📦 Sharing the Application

To share the complete application:

1. **Share the entire project folder** containing:
   - `docker-compose.yml`
   - `Dockerfile`
   - Source code
   - API documentation

2. **Recipients just need to run:**
   ```bash
   cd project-folder
   docker-compose up -d
   ```

That's it! No need for Java installation, Maven setup, or complex configurations.

## ✅ Features Included

- ✅ Complete Building Management System APIs
- ✅ Property/Apartment separation architecture
- ✅ Maintenance management system
- ✅ Tenant dashboard APIs for notification service integration
- ✅ Enhanced schema (half bathrooms, multiple utility meters, document storage)
- ✅ JWT authentication with role-based authorization
- ✅ Persistent H2 database
- ✅ Docker containerization
- ✅ Health monitoring
- ✅ Security best practices

## 🛑 Troubleshooting

### Port Already in Use
If port 8082 is busy, edit `docker-compose.yml`:
```yaml
ports:
  - "8083:8080"  # Change 8082 to any available port
```

### Container Won't Start
```bash
docker-compose logs bms-core
```

### Reset Database
```bash
docker-compose down -v  # Removes volumes
docker-compose up -d
```