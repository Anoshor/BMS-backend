# Building Management System - Authentication API Documentation

## Overview
This document outlines the authentication endpoints for the Building Management System (BMS) backend API.

## Base URL
```
http://localhost:8080/api/v1
```

## Authentication Flow

### Two User Types:
1. **Tenants** - Property renters who search and apply for properties
2. **Property Managers** - Property owners/managers who list and manage properties

## API Endpoints

### Health Check
```http
GET /health
```
Returns service health status.

---

## Tenant Authentication

### 1. Tenant Registration
```http
POST /auth/tenant/register
```

**Request Body:**
```json
{
  "email": "tenant@example.com",
  "phone": "+1234567890",
  "password": "Password123!",
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1990-01-01",
  "deviceId": "device_123",
  "deviceType": "android"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": "uuid",
      "email": "tenant@example.com",
      "phone": "+1234567890",
      "firstName": "John",
      "lastName": "Doe",
      "role": "tenant",
      "accountStatus": "pending",
      "emailVerified": false,
      "phoneVerified": false
    },
    "requiresVerification": true,
    "requiresDocuments": false,
    "expiresIn": 900,
    "tokenType": "Bearer"
  },
  "message": "Tenant registration successful. Please verify your email and phone."
}
```

### 2. Email Verification (Tenant)
```http
POST /auth/tenant/verify-email
```

**Request Body:**
```json
{
  "identifier": "tenant@example.com",
  "otpCode": "123456",
  "otpType": "email_verification"
}
```

### 3. Phone Verification (Tenant)
```http
POST /auth/tenant/verify-phone
```

**Request Body:**
```json
{
  "identifier": "+1234567890",
  "otpCode": "123456",
  "otpType": "phone_verification"
}
```

### 4. Resend Email Verification (Tenant)
```http
POST /auth/tenant/resend-email-verification?email=tenant@example.com
```

### 5. Resend Phone Verification (Tenant)
```http
POST /auth/tenant/resend-phone-verification?phone=+1234567890
```

---

## Manager Authentication

### 1. Manager Registration
```http
POST /auth/manager/register
```

**Request Body:**
```json
{
  "email": "manager@example.com",
  "phone": "+1234567890",
  "password": "Password123!",
  "firstName": "Jane",
  "lastName": "Smith",
  "dateOfBirth": "1985-05-15",
  "companyName": "Property Management Co.",
  "businessLicenseNumber": "BL123456",
  "taxId": "TAX789012",
  "businessAddress": "123 Business St, City, State",
  "businessPhone": "+1234567891",
  "businessEmail": "business@example.com",
  "deviceId": "device_456",
  "deviceType": "android"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": "uuid",
      "email": "manager@example.com",
      "phone": "+1234567890",
      "firstName": "Jane",
      "lastName": "Smith",
      "role": "property_manager",
      "accountStatus": "pending",
      "emailVerified": false,
      "phoneVerified": false
    },
    "requiresVerification": true,
    "requiresDocuments": true,
    "expiresIn": 900,
    "tokenType": "Bearer"
  },
  "message": "Manager registration successful. Please verify your email and phone, then upload required documents."
}
```

### 2. Email/Phone Verification (Manager)
Same as tenant verification endpoints:
- `POST /auth/manager/verify-email`
- `POST /auth/manager/verify-phone`
- `POST /auth/manager/resend-email-verification`
- `POST /auth/manager/resend-phone-verification`

---

## Common Authentication

### 1. Login (Both Tenant & Manager)
```http
POST /auth/login
```

**Request Body:**
```json
{
  "identifier": "user@example.com",
  "password": "Password123!",
  "deviceId": "device_123",
  "deviceType": "android"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "role": "tenant",
      "accountStatus": "active",
      "emailVerified": true,
      "phoneVerified": true
    },
    "requiresVerification": false,
    "requiresDocuments": false,
    "expiresIn": 900,
    "tokenType": "Bearer"
  },
  "message": "Login successful"
}
```

### 2. Refresh Token
```http
POST /auth/refresh-token?refreshToken=eyJhbGciOiJIUzI1NiJ9...
```

### 3. Logout
```http
POST /auth/logout?refreshToken=eyJhbGciOiJIUzI1NiJ9...
```

### 4. Logout All Devices
```http
POST /auth/logout-all-devices?refreshToken=eyJhbGciOiJIUzI1NiJ9...
```

---

## Error Responses

### Validation Error
```json
{
  "success": false,
  "data": null,
  "message": "Validation failed",
  "errors": [
    "Email is required",
    "Password must be at least 8 characters"
  ],
  "timestamp": "2024-01-01T12:00:00.000Z"
}
```

### Authentication Error
```json
{
  "success": false,
  "data": null,
  "message": "Invalid credentials",
  "timestamp": "2024-01-01T12:00:00.000Z"
}
```

### Account Locked Error
```json
{
  "success": false,
  "data": null,
  "message": "Account is temporarily locked due to multiple failed login attempts",
  "timestamp": "2024-01-01T12:00:00.000Z"
}
```

---

## Authentication Headers

For protected endpoints, include the JWT token in the Authorization header:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## Database Schema

The authentication system uses the following main tables:
- `users` - Core user information
- `tenant_profiles` - Tenant-specific profile data
- `manager_profiles` - Manager-specific profile data
- `refresh_tokens` - JWT refresh token management
- `otp_verifications` - OTP codes for verification
- `login_logs` - Audit trail for login attempts

---

## Security Features

1. **Password Encryption** - BCrypt with strength 12
2. **JWT Tokens** - Separate access (15 min) and refresh (30 days) tokens
3. **Account Locking** - Temporary lock after 5 failed attempts
4. **OTP Verification** - Email and SMS verification for registration
5. **Rate Limiting** - Protection against brute force attacks
6. **CORS Configuration** - Secure cross-origin resource sharing
7. **Input Validation** - Comprehensive request validation

---

## Mobile App Integration

### Token Management
- Store access token for API requests
- Store refresh token securely for token renewal
- Implement automatic token refresh before expiration
- Handle token expiration gracefully

### Device Tracking
- Send unique device ID with registration and login
- Track device type (iOS/Android) for analytics
- Support multi-device login with device-specific token management

---

## Environment Configuration

Update `application.properties` for your environment:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/bms_db
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT
jwt.secret=your_secure_secret_key

# Email
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password

# SMS (when enabled)
sms.enabled=true
```

---

## Testing

Use tools like Postman or curl to test the API endpoints. The service includes comprehensive error handling and validation for all authentication flows.