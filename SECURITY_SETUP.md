# 🔒 **Security Setup Guide**

## **⚠️ IMPORTANT: Before Pushing to GitHub**

### **🔐 Environment Configuration**

**1. Create Local Configuration:**
```bash
# Copy template for local development
cp services/core-service/src/main/resources/application-template.properties \
   services/core-service/src/main/resources/application-local.properties
```

**2. Update Local Configuration:**
Edit `application-local.properties` with your actual values:
```properties
# JWT Configuration - CHANGE FOR PRODUCTION
jwt.secret=YourActualSecretKeyMinimum256BitsLongAndVerySecure123456789

# Email Configuration - YOUR ACTUAL CREDENTIALS  
spring.mail.username=your-actual-email@gmail.com
spring.mail.password=your-actual-app-password
spring.mail.from=noreply@yourdomain.com
```

**3. Spring Profile Setup:**
```properties
# In application-local.properties
spring.profiles.active=local
```

### **🌍 Environment Variables (Production)**

**For Production Deployment:**
```bash
export JWT_SECRET="YourProductionSecretKey256BitsMinimum"
export MAIL_USERNAME="production@yourdomain.com"
export MAIL_PASSWORD="production-app-password"
export MAIL_FROM="noreply@yourdomain.com"
```

**For Docker:**
```yaml
# docker-compose.yml
environment:
  - JWT_SECRET=YourProductionSecretKey256BitsMinimum
  - MAIL_USERNAME=production@yourdomain.com
  - MAIL_PASSWORD=production-app-password
  - SPRING_PROFILES_ACTIVE=production
```

---

## **🚫 What's Excluded from Git:**

### **Database Files:**
```
data/
*.db
*.h2.db
*.mv.db
*.trace.db
```

### **Environment-Specific Configs:**
```
application-local.properties
application-prod.properties
application-staging.properties
application-dev.properties
.env*
```

### **Security Files:**
```
*.key
*.pem
*.p12
*.jks
secrets/
```

---

## **✅ Safe for GitHub:**

### **Application Properties:**
- ✅ `application.properties` - Uses environment variable placeholders
- ✅ `application-template.properties` - Template with placeholder values
- ✅ `application-test.properties` - Test configuration only

### **Docker Files:**
- ✅ `Dockerfile` - No hardcoded secrets
- ✅ `docker-compose.yml` - Uses environment variables

---

## **🔧 Development Setup:**

**1. Clone Repository:**
```bash
git clone https://github.com/yourusername/bms-backend.git
cd bms-backend
```

**2. Setup Local Configuration:**
```bash
# Copy template
cp services/core-service/src/main/resources/application-template.properties \
   services/core-service/src/main/resources/application-local.properties

# Edit with your values
nano services/core-service/src/main/resources/application-local.properties
```

**3. Run Application:**
```bash
cd services/core-service
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

---

## **🚀 Production Deployment:**

**1. Environment Variables Required:**
```bash
JWT_SECRET=your-256-bit-secret-key
MAIL_HOST=smtp.yourdomain.com
MAIL_USERNAME=production@yourdomain.com
MAIL_PASSWORD=production-app-password
MAIL_FROM=noreply@yourdomain.com
SPRING_PROFILES_ACTIVE=production
```

**2. Database Configuration:**
- For production, consider PostgreSQL/MySQL instead of H2
- Update `application-prod.properties` accordingly

**3. Security Best Practices:**
- ✅ Use strong JWT secrets (minimum 256 bits)
- ✅ Rotate secrets regularly
- ✅ Use secure email credentials
- ✅ Enable HTTPS in production
- ✅ Configure proper CORS settings

---

## **⚠️ CRITICAL REMINDERS:**

1. **NEVER commit `application-local.properties`**
2. **NEVER hardcode secrets in code**
3. **ALWAYS use environment variables for production**
4. **CHANGE default JWT secret before production**
5. **Use proper email credentials**

**Your repository is now secure for GitHub! 🔒✨**