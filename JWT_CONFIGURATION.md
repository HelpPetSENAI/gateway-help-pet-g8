# JWT Configuration Guide

## Problem Fixed ✅

The application was failing with:
```
Could not resolve placeholder 'JWT_SECRET' in value "${JWT_SECRET}"
```

This has been resolved by adding a default value to the `application.yml` configuration.

## Current Configuration

**File**: `src/main/resources/application.yml`

```yaml
jwt:
  secret: ${JWT_SECRET:your-secret-key-change-in-production}
  expiration: ${JWT_EXPIRATION:86400000}
```

### What Changed:
- Added a default value for `JWT_SECRET`: `your-secret-key-change-in-production`
- The application now starts without requiring the environment variable to be explicitly set
- Environment variables will still override the default if provided

## Production Configuration

### Option 1: Environment Variables (Recommended)

Set the following environment variables before running the application:

```bash
export JWT_SECRET="your-secure-secret-key-here-with-min-32-chars"
export JWT_EXPIRATION=86400000  # 24 hours in milliseconds (optional)

java -jar gateway-1.0.0.jar
```

### Option 2: Docker Environment

In your Dockerfile or docker-compose.yml:

```yaml
environment:
  JWT_SECRET: "your-secure-secret-key-here-with-min-32-chars"
  JWT_EXPIRATION: "86400000"
```

### Option 3: Custom Properties File

Create `application-prod.yml`:

```yaml
jwt:
  secret: "your-secure-secret-key-here-with-min-32-chars"
  expiration: 86400000
```

Then run with:
```bash
java -jar gateway-1.0.0.jar --spring.profiles.active=prod
```

## Security Best Practices

1. **Use a strong secret key**: Min 32 characters, use random alphanumeric + special characters
2. **Never commit secrets**: Keep production secrets in environment variables or secret management tools
3. **Rotate secrets periodically**: Implement key rotation policy
4. **Use HTTPS only**: Always transmit tokens over HTTPS in production

### Example Strong Secret Generation:

```bash
# Linux/macOS
openssl rand -base64 32

# Result example:
# L7k9mR2xN4pQ1vW5sY8aB3cD6eF7gH0i+jK1lM2nO3p4=
```

## JWT Token Details

- **Algorithm**: HS256 (HMAC SHA-256)
- **Claims**:
  - `sub` (subject): User email
  - `userId`: User ID
  - `iat`: Issued at timestamp
  - `exp`: Expiration timestamp
- **Default Expiration**: 86400000ms = 24 hours

## Testing JWT Authentication

### 1. Login Endpoint (Generate Token)
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}'

# Response example:
# {
#   "token": "eyJhbGciOiJIUzI1NiJ9...",
#   "userId": "user-123"
# }
```

### 2. Use Token in Requests
```bash
curl -X GET http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer <token-from-login>"
```

## Troubleshooting

### Error: "Could not resolve placeholder 'JWT_SECRET'"
- **Cause**: Environment variable not set and no default provided
- **Solution**: Set `JWT_SECRET` environment variable before running

### Error: "Token invalid or expired"
- **Cause**: Token signature doesn't match or token has expired
- **Solution**: Ensure all services use the same `JWT_SECRET`

### CircuitBreaker Filter Error
- **Cause**: Missing spring-cloud-starter-circuitbreaker dependency
- **Solution**: Add dependency to pom.xml or remove CircuitBreaker filter if not needed

## Additional Endpoints

- **Auth Login**: `POST /auth/login` - Public
- **Auth Validate**: `POST /auth/validate` - Public
- **Actuator Health**: `GET /actuator/health` - Public
- **Protected Routes**: `GET /api/v1/**` - Requires valid JWT token

