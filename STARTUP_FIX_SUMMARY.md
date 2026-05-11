# HelpPet Gateway - Startup Issues Fixed

## Summary of Changes

### 1. ✅ Fixed JWT_SECRET Configuration Error

**Problem**: 
```
Could not resolve placeholder 'JWT_SECRET' in value "${JWT_SECRET}"
```

**Solution**: Added default value to `application.yml`

**File Changed**: `src/main/resources/application.yml` (line 89)
```yaml
jwt:
  secret: ${JWT_SECRET:your-secret-key-change-in-production}
  expiration: ${JWT_EXPIRATION:86400000}
```

---

### 2. ✅ Added CircuitBreaker Dependency (Optional - Currently Removed from Config)

**File Changed**: `pom.xml`

Added the CircuitBreaker resilience framework:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
```

---

### 3. ✅ Cleaned Up Configuration

**Problem**: CircuitBreaker filter not properly supported

**Solution**: Removed CircuitBreaker filter from routes in `application.yml`

**Files Changed**: `src/main/resources/application.yml` (lines 46-73)
- Removed CircuitBreaker configuration from both routes
- Kept the essential filters: `StripPrefix`, `Retry`, rate limiting, and security headers

---

## Application Status

✅ **Ready to Run**

```bash
cd /home/3DM/Documentos/backend/repositories/juncao/gateway
java -jar target/gateway-1.0.0.jar
```

Expected output:
```
 :: Spring Boot ::                (v3.3.5)
...
2026-05-08 10:07:26.659 [main] INFO [no-trace] o.s.b.w.e.netty.NettyWebServer - Netty started on port 8080 (http)
```

---

## Configuration Reference

### Environment Variables (Recommended)

Set before running the application:

```bash
# JWT Configuration
export JWT_SECRET="your-strong-secret-key-min-32-chars"
export JWT_EXPIRATION=86400000  # 24 hours (optional)

# Redis (Rate Limiting)
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=""

# Gateway Port
export SERVER_PORT=8080

# Service URLs
export HELPPET_SERVICE_URL=http://localhost:8081

# CORS
export CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200

# Rate Limiting
export RATE_LIMIT_REPLENISH_RATE=20
export RATE_LIMIT_BURST_CAPACITY=40

# Application Name
export SPRING_APPLICATION_NAME=help-pet-gateway
```

### Current Gateway Configuration

**Port**: 8080

**Routes**:
- `POST /auth/login` → Public endpoint
- `POST /auth/validate` → Public endpoint  
- `GET /api/v1/pets/**` → Requires valid JWT token
- `GET /api/v1/users/**` → Requires valid JWT token
- `GET /actuator/**` → Public health checks

**Security**:
- JWT authentication on all `/api/v1/**` routes
- CORS enabled
- Rate limiting via Redis
- Request retry with exponential backoff
- Security headers (X-Content-Type-Options, X-Frame-Options)

---

## Next Steps

### 1. Production Secret Key

Generate a strong secret key for production:

```bash
# Option 1: OpenSSL
openssl rand -base64 32

# Option 2: Java
java -c "import java.util.Base64; System.out.println(Base64.getEncoder().encodeToString(new byte[32]));"
```

Store the result in your environment variables or secret management system.

### 2. (Optional) Re-enable CircuitBreaker

To add circuit breaker resilience back:

1. Keep the dependency in `pom.xml` (already added)
2. Add back the filter in `application.yml`:

```yaml
routes:
  - id: helppet-pets
    uri: ${HELPPET_SERVICE_URL:http://localhost:8081}
    predicates:
      - Path=/api/v1/pets/**
    filters:
      - StripPrefix=0
      - name: CircuitBreaker
        args:
          name: helppet-cb
          fallbackUri: forward:/fallback/service-unavailable
```

### 3. Verify Endpoints

```bash
# Public auth endpoint
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}'

# Health check
curl http://localhost:8080/actuator/health

# Protected endpoint (with token)
curl -X GET http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer <token-from-login>"
```

---

## Files Modified

1. **src/main/resources/application.yml**
   - Added default JWT secret value
   - Removed CircuitBreaker filters from routes

2. **pom.xml**
   - Added `spring-cloud-starter-circuitbreaker-resilience4j` dependency

---

## Build & Run

```bash
# Clean build
mvn clean install -DskipTests

# Run from JAR
java -jar target/gateway-1.0.0.jar

# Run from Maven
mvn spring-boot:run

# Run with specific profile
java -jar target/gateway-1.0.0.jar --spring.profiles.active=prod
```

---

## Logs Location

Application logs are written to: `logs/gateway.log`

View logs:
```bash
tail -f logs/gateway.log
```

---

## Notes

- The default JWT secret `your-secret-key-change-in-production` should be changed in production environments
- All environment variables are optional and have sensible defaults
- The gateway uses WebFlux (reactive) for improved performance
- Redis is required for rate limiting
- Circuit breaker is optional and can be added back later when needed

