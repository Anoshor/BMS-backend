# 🧪 **BMS API Test Suite - Usage Guide**

## **Available Test Methods**

### **1. 🚀 Bash Script (Automated CLI Testing)**

**Quick Start:**
```bash
# Make script executable
chmod +x testing/api-tests.sh

# Run tests against localhost
./testing/api-tests.sh

# Run tests against different server
./testing/api-tests.sh http://192.168.19.155:8082

# Run with detailed output
./testing/api-tests.sh http://localhost:8080 | tee test-results.log
```

**Features:**
- ✅ Full API coverage (21+ tests)
- ✅ Automatic token management
- ✅ Colored output with pass/fail indicators  
- ✅ Security validation tests
- ✅ Error response validation
- ✅ Final summary report

---

### **2. 📬 Postman Collection (GUI Testing)**

**Setup:**
1. Import `testing/postman-collection.json` into Postman
2. Create environment with variables:
   ```json
   {
     "base_url": "http://localhost:8080",
     "manager_email": "testmanager@bms.com",
     "tenant_email": "testtenant@bms.com", 
     "property_name": "Test Property Heights"
   }
   ```
3. Run collection with Newman or Postman UI

**Newman CLI:**
```bash
# Install Newman
npm install -g newman

# Run collection
newman run testing/postman-collection.json \
  --env-var "base_url=http://localhost:8080"

# Generate HTML report
newman run testing/postman-collection.json \
  --env-var "base_url=http://localhost:8080" \
  --reporters cli,html \
  --reporter-html-export test-report.html
```

---

### **3. ☕ JUnit Integration Tests (Spring Boot)**

**Run Tests:**
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TenantConnectionIntegrationTest

# Run with coverage
mvn test jacoco:report

# Run tests with detailed output
mvn test -Dtest=TenantConnectionIntegrationTest -X
```

**Features:**
- ✅ Spring Boot Test Context
- ✅ Database rollback after each test
- ✅ Mock security context
- ✅ Validation testing
- ✅ Response content verification

---

## **🎯 Test Categories**

### **Authentication Tests**
- ✅ Manager registration/login
- ✅ Tenant registration/login
- ✅ Token generation and validation
- ✅ Unauthorized access protection

### **Property Management Tests**
- ✅ Create property buildings
- ✅ Get manager properties
- ✅ Search properties
- ✅ Create apartments

### **Tenant Connection Tests** ⭐ **MAIN FOCUS**
- ✅ Connect tenant to property
- ✅ Search connected tenants
- ✅ Global tenant search
- ✅ Date format validation
- ✅ Field validation

### **Security Tests** 🔐
- ✅ Password hash exposure prevention
- ✅ Sensitive field filtering
- ✅ Authorization checks
- ✅ Contact info updates

### **Validation Tests**
- ✅ Email format validation
- ✅ Required field validation  
- ✅ Date range validation
- ✅ Business rule validation

---

## **🔧 Customization**

### **Bash Script Configuration**
Edit variables in `api-tests.sh`:
```bash
MANAGER_EMAIL="your-test-manager@example.com"
TENANT_EMAIL="your-test-tenant@example.com" 
PROPERTY_NAME="Your Test Property"
```

### **Add New Tests**
```bash
# Add to api-tests.sh
run_test "Your Test Name" "200" "POST" "/your/endpoint" \
    "-H 'Authorization: Bearer $TOKEN'" \
    '{"your": "data"}'
```

### **Environment-Specific Testing**
```bash
# Development
./api-tests.sh http://localhost:8080

# Staging  
./api-tests.sh https://staging-api.yourdomain.com

# Docker
./api-tests.sh http://localhost:8082
```

---

## **📊 Expected Results**

### **✅ Successful Run Output:**
```
========================================
    BMS API Test Suite Started
    Base URL: http://localhost:8080
========================================

=== AUTHENTICATION TESTS ===
[INFO] Running test: Manager Registration
[PASS] Manager Registration (Status: 200)
[PASS] Manager Login - Token acquired
...

========================================
           TEST RESULTS SUMMARY
========================================
Total Tests: 21
Passed: 21
Failed: 0

🎉 All tests passed! API is working correctly.
```

### **❌ Failure Example:**
```
[FAIL] Connect Tenant to Property (Expected: 200, Got: 400)
[FAIL] Response: {"success":false,"message":"Validation failed: {startDate=Start date is required}"}
```

---

## **🐛 Troubleshooting**

### **Common Issues:**

1. **"Connection refused"**
   ```bash
   # Check if backend is running
   curl -s http://localhost:8080/api/v1/health || echo "Backend not running"
   ```

2. **"401 Unauthorized"**
   - Check token generation in authentication tests
   - Verify JWT configuration

3. **"400 Bad Request"**  
   - Review request payload format
   - Check validation error details in response

4. **Tests pass individually but fail in sequence**
   - Database state conflicts
   - Add cleanup between tests

---

## **🚀 Continuous Integration**

### **GitHub Actions Example:**
```yaml
- name: Run API Tests
  run: |
    # Start application in background
    java -jar target/bms-backend-*.jar &
    
    # Wait for startup
    sleep 30
    
    # Run tests
    chmod +x testing/api-tests.sh
    ./testing/api-tests.sh http://localhost:8080
```

### **Docker Testing:**
```bash
# Start services
docker-compose up -d

# Run tests against Docker
./testing/api-tests.sh http://localhost:8082

# Cleanup
docker-compose down
```

---

This comprehensive test suite ensures your **Tenant Connect API** and all related endpoints work correctly! 🎯