# 🏢 BMS Backend - Multi-Platform Docker Distribution

**Complete Building Management System Backend** ready for instant deployment!

## 🚀 Quick Start

1. **Download** this docker-compose.yml file
2. **Run:**
   ```bash
   docker-compose up -d
   ```
3. **Access:**
   - **API:** http://localhost:8082/api/v1/health
   - **Database:** http://localhost:8082/api/v1/h2-console

That's it! 🎉

## 🎯 What You Get

### ✅ Complete BMS APIs
- 🏗️ **Property/Building Management** - Create, manage properties and apartments
- 🔧 **Maintenance System** - Complete request tracking and management
- 👥 **User Management** - Managers, tenants, role-based authentication
- 🏠 **Tenant Dashboard** - Ready for notification service integration
- 🔐 **JWT Security** - Production-ready authentication

### ✅ Advanced Features
- **Multi-platform Support** - Works on Windows, Mac, Linux (Intel/ARM)
- **Persistent Database** - H2 file-based, data survives restarts
- **Web Database Access** - Built-in H2 console for data inspection
- **Enhanced Schema** - Half bathrooms, multiple utility meters, documents
- **Health Monitoring** - Built-in health checks and monitoring

## 📊 Database Access

### H2 Web Console
- **URL:** http://localhost:8082/api/v1/h2-console
- **JDBC URL:** `jdbc:h2:file:/app/data/bmsdb`
- **Username:** `sa`
- **Password:** (empty)

View all your data in real-time:
- Users, Properties, Apartments
- Maintenance requests and updates
- Property images and documents
- Service categories and tenant connections

## 📋 API Testing

### Quick Test Examples

```bash
# Health Check
curl http://localhost:8082/api/v1/health

# Register Manager
curl -X POST "http://localhost:8082/api/v1/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "manager@test.com",
    "contactNum": "9876543210",
    "password": "password123",
    "firstName": "John",
    "lastName": "Manager",
    "dob": "1990-01-15T00:00:00.000Z",
    "gender": "male",
    "role": "MANAGER"
  }'

# Login
curl -X POST "http://localhost:8082/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "manager@test.com",
    "password": "password123",
    "role": "MANAGER"
  }'
```

## 📚 Complete Documentation

For full API documentation, see the included files:
- **DEPLOYMENT_GUIDE.md** - Cloud deployment options
- **DATABASE_ACCESS.md** - Database management guide  
- **DOCKER_USAGE.md** - Local development guide

## 🛠️ Management Commands

```bash
# Start services
docker-compose up -d

# View logs
docker-compose logs -f bms-backend

# Stop services
docker-compose down

# Reset database (removes all data)
docker-compose down -v

# Update to latest version
docker-compose pull && docker-compose up -d
```

## 🌐 Production Ready

This Docker setup includes:
- ✅ **Security** - Non-root user, proper secrets management
- ✅ **Performance** - Multi-stage build, optimized layers
- ✅ **Monitoring** - Health checks and logging
- ✅ **Persistence** - Proper data volume management
- ✅ **Scalability** - Environment variable configuration

## 🎭 Multi-Platform

Automatically works on:
- 🪟 **Windows** (Intel/AMD)
- 🍎 **macOS** (Intel/Apple Silicon)  
- 🐧 **Linux** (AMD64/ARM64)
- ☁️ **Cloud** (AWS, GCP, Azure)

## 🔗 Integration Ready

Perfect for:
- 📱 **Mobile Apps** - Complete REST API
- 🌐 **Web Frontend** - React, Vue, Angular
- 🔔 **Notification Services** - Built-in tenant dashboard APIs
- 📊 **Analytics** - Direct database access for reporting
- 🏗️ **Microservices** - Docker-native deployment

## 💡 Need Help?

1. **API Issues** → Check http://localhost:8082/api/v1/health
2. **Database Issues** → Access http://localhost:8082/api/v1/h2-console  
3. **Container Issues** → Run `docker-compose logs bms-backend`
4. **Port Conflicts** → Change `"8082:8080"` to `"8083:8080"` in docker-compose.yml

---

**🎉 Ready to scale your Building Management System!**

Built with ❤️ for the modern property management industry.