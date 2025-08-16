# BMS Backend - Microservices Architecture

This project contains the backend microservices for the Building Management System (BMS) application.

## Architecture Overview

```
BMS Backend/
├── services/
│   ├── core-service/          # Main business logic (Port 8080)
│   └── notification-service/  # Notifications handling (Port 8081)
├── shared-services/           # Common utilities and DTOs
├── docker-compose.yml         # Container orchestration
└── pom.xml                   # Parent POM
```

## Services

### Core Service (Port 8080)
- User authentication and authorization
- Property management
- Tenant and manager operations
- Main business logic APIs

**Endpoints:** `http://localhost:8080/api/v1/`

### Notification Service (Port 8081)
- Push notifications (FCM/APNs)
- Email notifications
- SMS notifications
- Notification queuing and retry logic

**Endpoints:** `http://localhost:8081/api/v1/notifications/`

### Shared Services
- Common DTOs (ApiResponse, etc.)
- Utility classes (DateUtils, ValidationUtils)
- Shared configurations

## Getting Started

### Prerequisites
- Java 17
- Maven 3.6+
- Docker & Docker Compose (optional)

### Running Locally

1. **Build all services:**
   ```bash
   mvn clean install
   ```

2. **Run Core Service:**
   ```bash
   cd services/core-service
   mvn spring-boot:run
   ```

3. **Run Notification Service:**
   ```bash
   cd services/notification-service
   mvn spring-boot:run
   ```

### Running with Docker

1. **Start all services:**
   ```bash
   docker-compose up -d
   ```

2. **Stop all services:**
   ```bash
   docker-compose down
   ```

## Service Ports

| Service | Port | Context Path |
|---------|------|--------------|
| Core Service | 8080 | /api/v1 |
| Notification Service | 8081 | /api/v1 |
| MySQL | 3306 | - |
| Redis | 6379 | - |

## Database

- **Core Service:** Uses `bms_core` database
- **Notification Service:** Uses `bms_notifications` database
- **Development:** H2 in-memory database
- **Production:** MySQL 8.0

## Environment Configuration

Each service has its own `application.properties` file with environment-specific configurations.

### Core Service Environment Variables
- `SERVER_PORT`: Server port (default: 8080)
- `SPRING_DATASOURCE_URL`: Database URL
- `JWT_SECRET`: JWT signing secret

### Notification Service Environment Variables
- `SERVER_PORT`: Server port (default: 8081)
- `FIREBASE_CREDENTIALS_PATH`: Firebase service account key
- `SPRING_DATA_REDIS_HOST`: Redis host for queuing

## Development

### Adding a New Microservice

1. Create a new module under `services/`
2. Add it to the parent `pom.xml` modules section
3. Configure unique port and database
4. Add to `docker-compose.yml`

### Inter-Service Communication

Services communicate via:
- REST APIs (synchronous)
- Message queues (asynchronous via Redis/RabbitMQ)

## Testing

Run tests for all services:
```bash
mvn test
```

Run tests for specific service:
```bash
cd services/core-service
mvn test
```

## Monitoring

Health check endpoints:
- Core Service: `http://localhost:8080/api/v1/actuator/health`
- Notification Service: `http://localhost:8081/api/v1/actuator/health`