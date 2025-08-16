# BMS Notification Service - Authentication & Setup Guide

This guide explains how to set up authentication for all third-party services used in the notification service.

## 🔧 Service Configurations

### 1. **RabbitMQ Setup**

**Local Installation:**
```bash
# macOS
brew install rabbitmq
brew services start rabbitmq

# Ubuntu/Debian
sudo apt-get install rabbitmq-server
sudo systemctl start rabbitmq-server

# Access management UI at: http://localhost:15672 (guest/guest)
```

**Environment Variables:**
```bash
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
RABBITMQ_VHOST=/
```

**Docker:**
```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

---

### 2. **Firebase Cloud Messaging (FCM) Setup**

**Step 1: Create Firebase Project**
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" or select existing project
3. Enable Cloud Messaging

**Step 2: Generate Service Account Key**
1. Go to Project Settings → Service Accounts
2. Click "Generate new private key"
3. Download the JSON file (e.g., `firebase-service-account.json`)

**Step 3: Configuration**
```bash
# Environment Variables
FIREBASE_CREDENTIALS_PATH=classpath:firebase-service-account.json
FIREBASE_PROJECT_ID=your-firebase-project-id
```

**File Placement:**
```
notification-service/
├── src/main/resources/
│   ├── firebase-service-account.json  ← Place your downloaded JSON here
│   └── application.properties
```

**Android App Setup:**
1. Add your Android app to Firebase project
2. Download `google-services.json`
3. Get your app's FCM registration token
4. Use the token to register devices via `/devices/register` endpoint

---

### 3. **Twilio SMS Setup**

**Step 1: Create Twilio Account**
1. Sign up at [twilio.com](https://www.twilio.com/)
2. Verify your email and phone number

**Step 2: Get Credentials**
1. Go to Twilio Console Dashboard
2. Find your Account SID and Auth Token
3. Get a Twilio phone number (Buy a number or use trial number)

**Step 3: Configuration**
```bash
# Environment Variables
TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_AUTH_TOKEN=your_auth_token_here
TWILIO_PHONE_NUMBER=+1234567890
```

**Free Trial Limitations:**
- Can only send SMS to verified phone numbers
- Messages include "Sent from your Twilio trial account" prefix
- Upgrade to paid account for production use

---

### 4. **Gmail SMTP Setup**

**Step 1: Enable 2-Factor Authentication**
1. Go to [Google Account Security](https://myaccount.google.com/security)
2. Enable 2-Step Verification

**Step 2: Generate App Password**
1. Go to Security → 2-Step Verification → App passwords
2. Select "Mail" and "Other (Custom name)"
3. Enter "BMS Notification Service"
4. Copy the 16-character app password

**Step 3: Configuration**
```bash
# Environment Variables
GMAIL_USERNAME=your-email@gmail.com
GMAIL_APP_PASSWORD=abcd-efgh-ijkl-mnop  # 16-character app password
```

**Alternative SMTP Providers:**
- **SendGrid**: More reliable for production
- **Mailgun**: Good free tier
- **AWS SES**: Pay-per-use

---

## 🚀 Complete Environment Setup

### **Production Environment Variables**
```bash
# Server Configuration
SERVER_PORT=8081

# Database
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/bms_notifications
SPRING_DATASOURCE_USERNAME=bms_user
SPRING_DATASOURCE_PASSWORD=your_db_password

# RabbitMQ
RABBITMQ_HOST=your-rabbitmq-host
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=your_rabbitmq_user
RABBITMQ_PASSWORD=your_rabbitmq_password

# Firebase FCM
FIREBASE_CREDENTIALS_PATH=classpath:firebase-service-account.json
FIREBASE_PROJECT_ID=your-firebase-project-id

# Twilio SMS
TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_AUTH_TOKEN=your_twilio_auth_token
TWILIO_PHONE_NUMBER=+1234567890

# Gmail SMTP
GMAIL_USERNAME=your-email@gmail.com
GMAIL_APP_PASSWORD=your-16-char-app-password
```

### **Development Environment Variables**
```bash
# For testing without real credentials, use these defaults:
FIREBASE_CREDENTIALS_PATH=classpath:firebase-service-account.json
FIREBASE_PROJECT_ID=your-firebase-project-id
TWILIO_ACCOUNT_SID=your-twilio-account-sid
TWILIO_AUTH_TOKEN=your-twilio-auth-token
TWILIO_PHONE_NUMBER=+1234567890
GMAIL_USERNAME=your-email@gmail.com
GMAIL_APP_PASSWORD=your-app-password
```

---

## 📋 Testing the Setup

### **1. Start Required Services**
```bash
# Start RabbitMQ
rabbitmq-server

# Start the notification service
cd services/notification-service
mvn spring-boot:run
```

### **2. Test Health Check**
```bash
curl http://localhost:8081/api/v1/notifications/health
```

### **3. Register a Device (for push notifications)**
```bash
curl -X POST http://localhost:8081/api/v1/devices/register \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test-user-123",
    "deviceToken": "your-fcm-device-token",
    "platform": "ANDROID",
    "appVersion": "1.0.0"
  }'
```

### **4. Send Test Notification**
```bash
curl -X POST http://localhost:8081/api/v1/notifications/send \
  -H "Content-Type: application/json" \
  -d '{
    "userIds": ["test-user-123"],
    "title": "Test Notification",
    "body": "This is a test notification from BMS",
    "type": "PUSH_NOTIFICATION",
    "priority": "NORMAL"
  }'
```

---

## 🔍 Troubleshooting

### **Common Issues:**

**1. Firebase Not Working:**
- Check if `firebase-service-account.json` is in `src/main/resources/`
- Verify Firebase project ID is correct
- Ensure device tokens are valid and from the same Firebase project

**2. Twilio SMS Not Sending:**
- Verify Account SID and Auth Token are correct
- Check if phone numbers are in E.164 format (+1234567890)
- For trial accounts, verify recipient phone numbers

**3. Gmail SMTP Authentication Failed:**
- Ensure 2-Factor Authentication is enabled
- Use App Password, not your regular Gmail password
- Check if "Less secure app access" is disabled (it should be)

**4. RabbitMQ Connection Failed:**
- Ensure RabbitMQ server is running
- Check if ports 5672 (AMQP) and 15672 (Management) are accessible
- Verify credentials and virtual host

---

## 📊 Monitoring & Logs

### **Application Logs:**
```bash
# Check logs for errors
tail -f logs/notification-service.log

# Or view in console
mvn spring-boot:run
```

### **Key Log Messages:**
- `Firebase initialized successfully` - FCM is working
- `Twilio initialized successfully` - Twilio is configured
- `Gmail SMTP not configured properly` - Email needs setup
- `RabbitMQ connection established` - Queue is working

### **Database Monitoring:**
```sql
-- Check notification logs
SELECT * FROM notification_logs ORDER BY created_at DESC LIMIT 10;

-- Check active device tokens
SELECT * FROM device_tokens WHERE is_active = true;

-- Check notification success rate
SELECT status, COUNT(*) FROM notification_logs GROUP BY status;
```

---

## 🔐 Security Best Practices

1. **Never commit credentials** to version control
2. **Use environment variables** for all sensitive data
3. **Rotate credentials** regularly
4. **Use separate credentials** for dev/staging/production
5. **Monitor failed authentication** attempts
6. **Enable logging** for all notification attempts
7. **Implement rate limiting** for notification endpoints

---

## 📈 Production Deployment

### **Docker Environment Variables:**
```yaml
# docker-compose.yml
environment:
  - FIREBASE_CREDENTIALS_PATH=/app/config/firebase-service-account.json
  - FIREBASE_PROJECT_ID=${FIREBASE_PROJECT_ID}
  - TWILIO_ACCOUNT_SID=${TWILIO_ACCOUNT_SID}
  - TWILIO_AUTH_TOKEN=${TWILIO_AUTH_TOKEN}
  - TWILIO_PHONE_NUMBER=${TWILIO_PHONE_NUMBER}
  - GMAIL_USERNAME=${GMAIL_USERNAME}
  - GMAIL_APP_PASSWORD=${GMAIL_APP_PASSWORD}
volumes:
  - ./config/firebase-service-account.json:/app/config/firebase-service-account.json:ro
```

### **Kubernetes Secrets:**
```bash
# Create secrets
kubectl create secret generic notification-secrets \
  --from-literal=firebase-project-id="your-project-id" \
  --from-literal=twilio-account-sid="ACxxxxx" \
  --from-literal=twilio-auth-token="your-token" \
  --from-literal=gmail-username="your-email@gmail.com" \
  --from-literal=gmail-app-password="your-app-password"
```

This setup guide ensures your notification service is properly configured with all required third-party services for production use.