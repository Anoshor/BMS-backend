did # BMS Notification Service

A comprehensive notification microservice for the Building Management System (BMS) that handles multi-channel notifications including push notifications, email, and SMS.

## 🏗️ Architecture Overview

The notification service follows a modular, queue-driven architecture designed for scalability and reliability:

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   API Gateway   │───▶│ Notification API │───▶│ Message Queue   │
│  (Core Service) │    │   Controller     │    │    (Redis)      │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │                        │
                                ▼                        ▼
                       ┌──────────────────┐    ┌─────────────────┐
                       │ Notification     │───▶│ Background      │
                       │   Service        │    │  Workers        │
                       └──────────────────┘    └─────────────────┘
                                │                        │
                                ▼                        ▼
                       ┌──────────────────┐    ┌─────────────────┐
                       │  Channel         │    │ External APIs   │
                       │  Services        │    │ FCM/APNs/SMTP   │
                       └──────────────────┘    └─────────────────┘
```

## ✅ **What's Actually Working**

The notification service is **fully functional** with real integrations:

### **🔥 Real Implementations**
- **Firebase FCM**: Sends actual push notifications to Android/Web devices
- **Twilio SMS**: Sends real SMS messages via Twilio API
- **Gmail SMTP**: Sends HTML emails via Gmail's SMTP server
- **RabbitMQ**: Processes notifications asynchronously with proper queuing
- **Database Logging**: Tracks all notification attempts with success/failure status
- **Device Management**: Register/unregister devices with complete CRUD operations

### **📱 Development Mode**
- Works without credentials (logs notifications instead of sending)
- Perfect for testing API endpoints and business logic
- Automatically detects when credentials are not configured

## 🚀 Technology Stack

### **Core Framework**
- **Spring Boot 3.2.0** - Main application framework
- **Java 17** - Programming language
- **Maven** - Build tool and dependency management

### **Messaging & Queuing**
- **Redis** - Message queuing and caching
- **Spring Boot AMQP** - RabbitMQ integration (alternative to Redis)
- **Spring Boot Data Redis** - Redis integration

### **Push Notifications**
- **Firebase Admin SDK 9.2.0** - Firebase Cloud Messaging (FCM) for Android/Web
- **Spring WebFlux** - Reactive HTTP client for APNs integration

### **Communication Channels**
- **Spring Boot Mail** - Email notifications via SMTP
- **External SMS APIs** - Ready for Twilio/AWS SNS integration

### **Database & Persistence**
- **Spring Boot Data JPA** - Database abstraction
- **MySQL 8.0** - Production database
- **H2 Database** - Development/testing database

### **Additional Libraries**
- **Lombok** - Reduces boilerplate code
- **Jackson** - JSON serialization/deserialization
- **Spring Boot Validation** - Request validation
- **Spring Boot DevTools** - Development utilities

## 📁 Project Structure

```
notification-service/
├── src/main/java/com/bms/notification/
│   ├── NotificationServiceApplication.java    # Main Spring Boot application
│   ├── controller/
│   │   └── NotificationController.java        # REST API endpoints
│   ├── service/
│   │   ├── NotificationService.java           # Main orchestration service
│   │   ├── PushNotificationService.java       # FCM/APNs handling
│   │   ├── EmailNotificationService.java      # Email notifications
│   │   └── SmsNotificationService.java        # SMS notifications
│   ├── dto/
│   │   ├── ApiResponse.java                   # Standard API response wrapper
│   │   └── NotificationRequest.java           # Notification request payload
│   ├── entity/                                # JPA entities (to be added)
│   └── config/                                # Configuration classes
├── src/main/resources/
│   └── application.properties                 # Service configuration
└── pom.xml                                   # Maven dependencies
```

## 🔧 Configuration

### **Server Configuration**
```properties
server.port=8081
server.servlet.context-path=/api/v1
spring.application.name=notification-service
```

### **Database Configuration**
```properties
# H2 (Development)
spring.datasource.url=jdbc:h2:mem:notificationdb
spring.h2.console.enabled=true

# MySQL (Production)
spring.datasource.url=jdbc:mysql://localhost:3306/bms_notifications
```

### **Redis Configuration**
```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.timeout=2000ms
```

### **Firebase Configuration**
```properties
firebase.credentials.path=${FIREBASE_CREDENTIALS_PATH}
firebase.project.id=${FIREBASE_PROJECT_ID}
```

### **APNs Configuration**
```properties
apns.key.path=${APNS_KEY_PATH}
apns.key.id=${APNS_KEY_ID}
apns.team.id=${APNS_TEAM_ID}
apns.bundle.id=${APNS_BUNDLE_ID}
apns.production=${APNS_PRODUCTION:false}
```

## 📡 API Endpoints

### **Send Single Notification**
```http
POST /api/v1/notifications/send
Content-Type: application/json

{
  "userIds": ["user-123"],
  "tenantId": "tenant-456",
  "title": "Rent Reminder",
  "body": "Your rent is due tomorrow",
  "type": "PUSH_NOTIFICATION",
  "priority": "HIGH",
  "channels": ["FCM", "EMAIL"],
  "category": "rent_reminder",
  "data": {
    "amount": "$1200",
    "dueDate": "2024-01-15"
  }
}
```

### **Send Bulk Notification**
```http
POST /api/v1/notifications/send-bulk
Content-Type: application/json

{
  "userIds": ["user-123", "user-456", "user-789"],
  "title": "Building Maintenance",
  "body": "Scheduled maintenance on January 20th",
  "type": "PUSH_NOTIFICATION",
  "priority": "NORMAL",
  "topic": "building_123_announcements"
}
```

### **Health Check**
```http
GET /api/v1/notifications/health
```

## 📨 Notification Types & Channels

### **Notification Types**
- `PUSH_NOTIFICATION` - Mobile/web push notifications
- `EMAIL` - Email notifications
- `SMS` - Text message notifications
- `IN_APP` - In-application notifications

### **Notification Priorities**
- `LOW` - Non-urgent notifications
- `NORMAL` - Standard notifications
- `HIGH` - Important notifications
- `URGENT` - Critical notifications

### **Delivery Channels**
- `FCM` - Firebase Cloud Messaging (Android/Web)
- `APNS` - Apple Push Notification Service (iOS)
- `EMAIL` - SMTP email delivery
- `SMS` - SMS providers (Twilio/AWS SNS)

## 🔄 Message Flow

1. **API Request** - Core service sends notification request
2. **Validation** - Request is validated and sanitized
3. **Queuing** - Message is queued in Redis for async processing
4. **Processing** - Background workers pick up messages
5. **Channel Selection** - Appropriate delivery channels are selected
6. **Device Resolution** - User devices and preferences are resolved
7. **Delivery** - Notifications are sent via FCM/APNs/SMTP
8. **Tracking** - Delivery status and receipts are tracked

## 🚦 Implementation Status

### ✅ **Completed**
- [x] Basic Spring Boot application structure
- [x] REST API endpoints for notifications
- [x] Service layer architecture
- [x] Request/Response DTOs
- [x] Configuration setup for all channels
- [x] Maven dependencies for FCM, Redis, Email
- [x] Async processing capability
- [x] Multi-channel support structure

### ✅ **Implemented & Working**
- [x] **Firebase FCM integration** - Full implementation with Android/Web support
- [x] **Twilio SMS integration** - Complete SMS sending with logging
- [x] **Gmail SMTP integration** - HTML email sending with templates
- [x] **RabbitMQ message queuing** - Async processing with separate queues
- [x] **Device registry** - Full CRUD operations for device tokens
- [x] **Notification logging** - Complete audit trail with status tracking
- [x] **Error handling & retries** - Robust error handling with database logging
- [x] **Development mode** - Works without credentials for testing
- [x] **REST API endpoints** - Complete notification and device management APIs
- [x] **Database entities** - Full JPA entities for device tokens and logs

### 🚧 **TODO (Future Enhancements)**
- [ ] APNs integration for iOS (currently FCM only)
- [ ] User preferences and quiet hours
- [ ] Message templates and localization
- [ ] Advanced retry logic with exponential backoff
- [ ] Rate limiting and batching optimizations
- [ ] Metrics and monitoring dashboard
- [ ] Integration with Core Service for user data fetching

## 🛠️ Development Setup

### **Prerequisites**
- Java 17+
- Maven 3.6+
- Redis server (for message queuing)
- MySQL 8.0 (for production)

### **Running the Service**

1. **Start dependencies:**
   ```bash
   # Start Redis
   redis-server
   
   # Start MySQL (if using production config)
   mysql.server start
   ```

2. **Run the application:**
   ```bash
   cd services/notification-service
   mvn spring-boot:run
   ```

3. **Access endpoints:**
   - API Base: `http://localhost:8081/api/v1/notifications`
   - Health Check: `http://localhost:8081/api/v1/notifications/health`
   - H2 Console: `http://localhost:8081/h2-console`

## 🔐 Security Considerations

### **Environment Variables**
Store sensitive credentials as environment variables:
- `FIREBASE_CREDENTIALS_PATH` - Firebase service account key file
- `APNS_KEY_PATH` - APNs private key file
- `EMAIL_USERNAME` - SMTP username
- `EMAIL_PASSWORD` - SMTP password/app password

### **Authentication**
The service should validate requests from the core service using:
- JWT tokens
- API keys
- Service-to-service authentication

## 📊 Monitoring & Observability

### **Health Checks**
- Application health endpoint
- Database connectivity check
- Redis connectivity check
- External service health (FCM/APNs)

### **Metrics** (To be implemented)
- Notification delivery rates
- Channel-specific success rates
- Queue depth and processing times
- Error rates and retry attempts

### **Logging**
Structured logging for:
- Notification requests and responses
- Delivery attempts and results
- Error conditions and retries
- Performance metrics

## 🔄 Integration with Core Service

The notification service is designed to be called by the core service for:

### **Use Cases**
- **Rent Reminders** - Scheduled notifications for rent due dates
- **Maintenance Alerts** - Building maintenance announcements
- **Chat Messages** - Real-time messaging notifications
- **Account Updates** - Profile changes, approvals, etc.
- **Emergency Alerts** - Critical building notifications

### **Integration Pattern**
```java
// Core Service calls Notification Service
@Autowired
private NotificationService notificationService;

public void sendRentReminder(String tenantId) {
    NotificationRequest request = NotificationRequest.builder()
        .userIds(List.of(tenantId))
        .title("Rent Reminder")
        .body("Your rent is due in 3 days")
        .type(NotificationType.PUSH_NOTIFICATION)
        .priority(NotificationPriority.HIGH)
        .build();
    
    notificationService.sendNotification(request);
}
```

## 🎯 Future Enhancements

1. **Advanced Scheduling** - Cron-based scheduled notifications
2. **Template Engine** - Dynamic content with variables
3. **A/B Testing** - Different notification variants
4. **Analytics Dashboard** - Notification performance insights
5. **Webhook Support** - Third-party integrations
6. **Multi-language Support** - Localized notifications
7. **Rich Media** - Images and attachments in notifications

This notification service provides a solid foundation for scalable, multi-channel notifications in the BMS application while maintaining flexibility for future enhancements.