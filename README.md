# 🏢 Building Management System (BMS) Backend

**A comprehensive microservices-based Building Management System for property managers, tenants, and building owners.**

This project provides a complete backend solution for managing residential and commercial properties, tenants, maintenance requests, and building operations through REST APIs and microservices architecture.

## 🌟 Features

### 👥 User Management
- **Role-based Authentication**: Property Managers, Tenants, Building Owners
- **JWT-based Security**: Secure token-based authentication
- **Profile Management**: Contact information updates for all user types
- **Case-insensitive Search**: Advanced user search capabilities

### 🏠 Property & Apartment Management
- **Multi-Property Support**: Managers can handle multiple buildings
- **Apartment Tracking**: Unit numbers, occupancy status, tenant assignments
- **Unique Unit Validation**: Prevents duplicate unit numbers within properties
- **Occupancy Management**: Real-time vacant/occupied status tracking
- **Advanced Search**: Search by tenant info, unit numbers, property details

### 🤝 Tenant Management
- **Tenant-Property Connections**: Link tenants to specific apartments with lease details
- **Enhanced Property Views**: Tenants see complete property information including manager contacts
- **Lease Management**: Start/end dates, rent amounts, security deposits
- **Global Tenant Search**: Find and connect existing tenants to new properties

### 🔧 Maintenance Management
- **Request Tracking**: Create, update, and manage maintenance requests
- **Status Management**: SUBMITTED, IN_PROGRESS, COMPLETED workflow
- **Tenant-Manager Communication**: Seamless request handling between parties

### 📱 API-First Design
- **RESTful APIs**: Complete CRUD operations for all entities
- **Standardized Responses**: Consistent ApiResponse format across all endpoints
- **Comprehensive Error Handling**: Detailed validation and error messages
- **JSON Serialization**: Proper date/time formatting for frontend consumption

## 🏗️ Architecture Overview

```
BMS Backend/
├── services/
│   ├── core-service/          # Main business logic (Port 8080)
│   │   ├── Authentication & Authorization
│   │   ├── Property & Apartment Management
│   │   ├── Tenant Management
│   │   ├── Maintenance Requests
│   │   └── User Profile Management
│   └── notification-service/  # Notifications handling (Port 8081)
│       ├── Push Notifications (FCM/APNs)
│       ├── Email Notifications
│       └── SMS Notifications
├── shared-services/           # Common utilities and DTOs
├── docker-compose.yml         # Container orchestration
└── pom.xml                   # Parent POM
```

## 🚀 Quick Start

### Using Docker (Recommended)

1. **Pull the latest image:**
   ```bash
   docker pull anoshorpaul/bms-core-service:latest
   ```

2. **Run with Docker Compose:**
   ```bash
   docker-compose up -d
   ```

### Local Development

#### Prerequisites
- Java 17
- Maven 3.6+
- MySQL 8.0 (optional - uses H2 in-memory for development)

#### Running Locally

1. **Clone and build:**
   ```bash
   git clone <repository-url>
   cd "BMS Backend"
   mvn clean install
   ```

2. **Run Core Service:**
   ```bash
   cd services/core-service
   mvn spring-boot:run
   ```

## 📋 API Documentation

### 🔐 Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register new user (Manager/Tenant) |
| POST | `/api/v1/auth/login` | User login with JWT token |
| POST | `/api/v1/auth/refresh` | Refresh JWT token |
| PUT | `/api/v1/auth/update-contact` | Update user contact information |

### 🏢 Property Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/properties` | Create new property |
| GET | `/api/v1/properties` | Get manager's properties |
| GET | `/api/v1/properties/search?q={query}` | Search properties |
| PUT | `/api/v1/properties/{id}` | Update property |
| DELETE | `/api/v1/properties/{id}` | Delete property |

### 🏠 Apartment Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/apartments` | Create apartment (with unique unit validation) |
| GET | `/api/v1/apartments` | Get all apartments for manager |
| GET | `/api/v1/apartments/search?searchText={query}` | Search apartments |
| GET | `/api/v1/apartments/property/{propertyId}` | Get apartments by property |
| GET | `/api/v1/apartments/occupied` | Get occupied apartments |
| GET | `/api/v1/apartments/unoccupied` | Get vacant apartments (case-insensitive) |
| GET | `/api/v1/apartments/tenant/search` | Search by tenant information |
| PUT | `/api/v1/apartments/{id}` | Update apartment |
| DELETE | `/api/v1/apartments/{id}` | Delete apartment |

### 👥 Tenant Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/tenants/connect` | Connect tenant to apartment with lease details |
| GET | `/api/v1/tenants/search?searchText={query}` | Search manager's tenant connections |
| GET | `/api/v1/tenants/search/global?searchText={query}` | Global tenant search |
| GET | `/api/v1/tenants/connections?searchText={query}` | Get tenant connections with enhanced details |
| GET | `/api/v1/tenants/my-properties` | **Tenant view**: Get properties with complete details |

#### Enhanced Tenant Properties Response
The `/api/v1/tenants/my-properties` endpoint returns comprehensive property information:
```json
{
  "success": true,
  "data": [
    {
      "connectionId": "uuid",
      "propertyName": "Sunset Apartments",
      "propertyAddress": "123 Main St, City, State",
      "propertyId": "uuid",
      "apartmentId": "uuid",
      "unitId": "A101",  // Unit number as string
      "startDate": "2024-01-01",
      "endDate": "2024-12-31",
      "monthlyRent": 1200.00,
      "securityDeposit": 2400.00,
      "managerName": "John Smith",
      "managerEmail": "manager@example.com",
      "managerPhone": "+1234567890"
    }
  ]
}
```

### 🔧 Maintenance Requests

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/maintenance-requests` | Create maintenance request |
| GET | `/api/v1/maintenance-requests` | Get requests (filtered by user role) |
| GET | `/api/v1/maintenance-requests/search?searchText={query}` | Search requests |
| PUT | `/api/v1/maintenance-requests/{id}` | Update request status |
| DELETE | `/api/v1/maintenance-requests/{id}` | Delete request |

## 🐳 Docker Deployment

### Available Images
- **Latest**: `anoshorpaul/bms-core-service:latest`
- **Stable**: `anoshorpaul/bms-core-service:v1.2`

### Multi-Platform Support
Images are built for:
- `linux/amd64` (Intel/AMD processors)
- `linux/arm64` (Apple Silicon, ARM processors)

### Docker Compose Configuration

```yaml
version: '3.8'
services:
  bms-core:
    image: anoshorpaul/bms-core-service:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/bms_core
      - JWT_SECRET=your-secret-key
    depends_on:
      - mysql
  
  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=rootpassword
      - MYSQL_DATABASE=bms_core
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```

## 🔧 Configuration

### Environment Variables

#### Core Service
| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Application port | 8080 |
| `SPRING_DATASOURCE_URL` | Database URL | H2 in-memory |
| `SPRING_DATASOURCE_USERNAME` | DB username | sa |
| `SPRING_DATASOURCE_PASSWORD` | DB password | password |
| `JWT_SECRET` | JWT signing secret | bms-secret-key |
| `JWT_EXPIRATION` | Token expiration (ms) | 86400000 (24h) |

### Database Configuration

#### Development (H2)
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

#### Production (MySQL)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bms_core
spring.datasource.username=bms_user
spring.datasource.password=secure_password
spring.jpa.hibernate.ddl-auto=update
```

## 🧪 Testing

### API Testing
Complete API test scripts are available in `services/core-service/testing/`:
- `api-tests.sh` - Comprehensive API testing
- `complete-flow-test.sh` - End-to-end workflow testing
- `postman-collection.json` - Postman collection for manual testing

### Running Tests
```bash
# Run all tests
mvn test

# Run specific service tests
cd services/core-service
mvn test

# Run integration tests with Docker
docker-compose -f docker-compose.test.yml up --abort-on-container-exit
```

## 🔍 Key Features & Improvements

### Recent Enhancements (v1.2)
- ✅ **Unique Unit Validation**: Prevents duplicate unit numbers within properties
- ✅ **Enhanced Tenant APIs**: Complete property information with manager contacts
- ✅ **Case-Insensitive Queries**: Robust apartment occupancy status handling
- ✅ **JSON Serialization Fixes**: Proper date/time formatting for all endpoints
- ✅ **Improved Error Handling**: Detailed validation messages and proper HTTP status codes
- ✅ **Multi-Platform Docker**: Support for AMD64 and ARM64 architectures

### Advanced Features
- **Smart Search**: Case-insensitive search across multiple fields
- **Relationship Management**: Automatic handling of tenant-apartment connections
- **Data Integrity**: Comprehensive validation and constraint enforcement
- **Security**: JWT-based authentication with role-based authorization
- **Scalability**: Microservices architecture ready for horizontal scaling

## 🚀 Production Deployment

### Health Checks
- Core Service: `http://localhost:8080/api/v1/actuator/health`
- Notification Service: `http://localhost:8081/api/v1/actuator/health`

### Monitoring
- Application logs via Spring Boot Actuator
- Database connection monitoring
- JWT token validation and refresh handling
- Error tracking and reporting

### Security Considerations
- JWT tokens with configurable expiration
- Role-based access control (RBAC)
- Input validation and sanitization
- SQL injection prevention via JPA/Hibernate
- CORS configuration for frontend integration

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Support

For support and questions:
- Create an issue in the repository
- Contact: [Your contact information]

---

**Built with ❤️ using Spring Boot 3.2, Java 17, and Docker**