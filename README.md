# Gateway API - Autenticação e Roteamento

API Gateway responsável por autenticação centralizada, validação de tokens JWT e roteamento de requisições para o microserviço de domínio.

## 🎯 Responsabilidades

- ✅ Autenticação com JWT (HS512)
- ✅ Geração e validação de tokens
- ✅ Roteamento de requisições para microserviço
- ✅ Adição de headers de contexto (X-User-Id, X-User-Email, X-Request-Id)
- ✅ CORS e políticas de segurança
- ✅ Logging estruturado com Correlation ID

## 📡 Endpoints

### Autenticação

```
POST   /auth/login         → Autenticar e gerar JWT
POST   /auth/refresh       → Renovar token expirado
GET    /auth/validate      → Validar token atual
```

### Health & Métricas

```
GET    /actuator/health    → Status geral do serviço
GET    /actuator/info      → Informações da aplicação
GET    /actuator/metrics   → Métricas de performance
```

## 🔐 Fluxo de Autenticação

### 1. Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "senha123"
  }'
```

**Resposta (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "email": "user@example.com",
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 2. Usar Token em Requisições Autenticadas

```bash
curl http://localhost:8080/api/v1/pets \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

**O que acontece internamente:**

1. Gateway valida a assinatura do JWT
2. Gateway extrai `userId` e `email` do token
3. Gateway adiciona headers ao microserviço:
   ```
   X-User-Id: 550e8400-e29b-41d4-a716-446655440000
   X-User-Email: user@example.com
   X-Request-Id: f47ac10b-58cc-4372-a567-0e02b2c3d479
   ```
4. Gateway roteia para microserviço: `http://microservice:8081/api/v1/pets`
5. Microserviço recebe requisição com headers e isola dados do usuário

### 3. Token Expirado (Refresh)

```bash
curl -X POST http://localhost:8080/auth/refresh \
  -H "Authorization: Bearer <token-expirado>"
```

**Resposta (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

## ⚙️ Configuração

### Variáveis de Ambiente (`.env`)

```env
# Aplicação
SPRING_APPLICATION_NAME=help-pet-gateway
SERVER_PORT=8080

# JWT
JWT_SECRET=5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437
JWT_EXPIRATION=86400000

# Roteamento
BACKEND_SERVICE_URL=http://localhost:8081

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200

# Logging
LOGGING_LEVEL_COM_HELPPET=DEBUG
LOGGING_LEVEL_ROOT=INFO
```

**Notas:**
- `JWT_SECRET`: Use um valor forte em produção (mínimo 64 caracteres hex)
- `JWT_EXPIRATION`: Em milissegundos (86400000 = 24 horas)
- `BACKEND_SERVICE_URL`: URL do microserviço (deve estar acessível)

### application.yml

```yaml
spring:
  application:
    name: ${SPRING_APPLICATION_NAME}
  cloud:
    gateway:
      routes:
        - id: api_route
          uri: ${BACKEND_SERVICE_URL}
          predicates:
            - Path=/api/v1/**
          filters:
            - AuthFilter
            - RequestIdFilter

server:
  port: ${SERVER_PORT}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

logging:
  level:
    com.helppet: ${LOGGING_LEVEL_COM_HELPPET}
    root: ${LOGGING_LEVEL_ROOT}
  file:
    name: logs/help-pet-gateway.log
    max-size: 10MB
    max-history: 30
```

## 🚀 Como Rodar

### Pré-requisitos

- Java 25+
- Maven 3.8+

### 1. Configurar Ambiente

```bash
# Copiar .env
cp .env.example .env

# Editar se necessário
vim .env
```

### 2. Iniciar o Gateway

```bash
# Desenvolvimento
mvn clean spring-boot:run

# Ou, construir e rodar JAR
mvn clean package
java -jar target/gateway-help-pet-g8-1.0.0.jar
```

**Saída esperada:**
```
2025-05-04 14:30:00.123 INFO  c.h.gateway.GatewayApplication - Starting GatewayApplication...
2025-05-04 14:30:02.456 INFO  c.h.gateway.GatewayApplication - Started GatewayApplication in 2.333 seconds
2025-05-04 14:30:02.789 INFO  c.h.gateway.GatewayApplication - Embedded server started: Netty on port 8080
```

### 3. Verificar Saúde

```bash
curl http://localhost:8080/actuator/health
```

**Resposta esperada:**
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 1099511627776,
        "free": 879609917440,
        "threshold": 10485760,
        "exists": true
      }
    },
    "livenessState": {"status": "UP"},
    "readinessState": {"status": "UP"}
  }
}
```

## 📊 Logging

### Estrutura de Logs

Todos os logs incluem Correlation ID para rastreamento de requisições:

```
2025-05-04 14:32:15.123 [pool-1-thread-1] INFO  c.h.g.security.JwtProvider - Generating token for user: user@example.com
2025-05-04 14:32:15.234 [pool-1-thread-1] DEBUG c.h.g.security.JwtAuthFilter - JWT validation successful, user: user@example.com
2025-05-04 14:32:15.345 [pool-1-thread-1] INFO  c.h.g.config.RouteConfig - Routing request to microservice: /api/v1/pets
```

### Contexto MDC (Mapped Diagnostic Context)

```
X-Request-Id: f47ac10b-58cc-4372-a567-0e02b2c3d479
X-User-Id: 550e8400-e29b-41d4-a716-446655440000
X-User-Email: user@example.com
```

### Arquivo de Log

```
logs/help-pet-gateway.log
```

**Rotação de Logs:**
- Tamanho máximo: 10 MB
- Retenção: 30 dias
- Compressão: Automática

## 🏗️ Estrutura de Pacotes

```
com.helppet.gateway/
├── GatewayApplication.java              Main class
│
├── config/
│   ├── RouteConfig.java                 Configuração de roteamento
│   ├── SecurityConfig.java              Configuração de segurança
│   └── CorsConfig.java                  Configuração CORS
│
├── controller/
│   └── AuthController.java              Endpoints: /auth/login, /auth/refresh, /auth/validate
│
├── security/
│   ├── JwtProvider.java                 Geração e validação de JWT
│   └── JwtAuthFilter.java               Filter que valida token e adiciona headers
│
└── dto/
    ├── LoginRequest.java                Email + Password
    ├── LoginResponse.java               AccessToken + Info
    └── TokenRefreshRequest.java         Refresh token
```

## 🔒 Segurança

### JWT (JSON Web Token)

- **Algoritmo:** HS512 (HMAC com SHA-512)
- **Secret:** Armazenado em `.env` (nunca commitar)
- **Expiração:** Configurável (padrão 24 horas)
- **Claims:**
  - `sub`: email do usuário
  - `userId`: ID único do usuário
  - `iat`: Issued At (timestamp)
  - `exp`: Expiration (timestamp)

### Headers de Segurança

O Gateway adiciona os seguintes headers ao rotear para o microserviço:

```
X-User-Id: 550e8400-e29b-41d4-a716-446655440000
X-User-Email: user@example.com
X-Request-Id: f47ac10b-58cc-4372-a567-0e02b2c3d479
```

**Importante:** O microserviço **confia implicitamente** nestes headers. Eles devem vir APENAS do Gateway.

### CORS

Origem permitidas (configurável em `.env`):

```
http://localhost:3000
http://localhost:4200
```

## 📈 Métricas e Health Check

### Endpoints Disponíveis

```
GET  /actuator/health           Status geral
GET  /actuator/info             Informações da app
GET  /actuator/metrics          Métricas expostas
GET  /actuator/metrics/{name}   Métrica específica
```

### Exemplo: Verificar Uptime

```bash
curl http://localhost:8080/actuator/metrics/process.uptime
```

## 🧪 Testes Manuais

### 1. Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "admin123"
  }' | jq
```

### 2. Usar Token

```bash
# Obter token primeiro
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"admin123"}' | jq -r '.accessToken')

# Usar token para acessar API protegida
curl http://localhost:8080/api/v1/pets \
  -H "Authorization: Bearer $TOKEN" | jq
```

### 3. Refresh Token

```bash
curl -X POST http://localhost:8080/auth/refresh \
  -H "Authorization: Bearer <token>" | jq
```

### 4. Validar Token

```bash
curl http://localhost:8080/auth/validate \
  -H "Authorization: Bearer <token>" | jq
```

## 🛠️ Desenvolvimento

### Build

```bash
mvn clean package
```

### Build com Testes

```bash
mvn clean verify
```

### Executar Apenas Testes

```bash
mvn test
```

### Executar Somente Testes de Integração

```bash
mvn verify -DskipUnitTests
```

## 📦 Dependências Principais

| Dependência | Versão | Propósito |
|---|---|---|
| Spring Cloud Gateway | 2025.1.0 | Roteamento e proxy reverso |
| Spring Security | 4.0.2 | Segurança e autenticação |
| JWT JJWT | 0.12.5 | Criação e validação de JWT |
| Spring Boot Actuator | 4.0.2 | Health check e métricas |
| Lombok | 1.18.30 | Reduzir boilerplate |
| Spring DotEnv | Latest | Variáveis de ambiente |

## 🚨 Troubleshooting

### Erro: "JWT validation failed"

```
401 Unauthorized
```

**Causas possíveis:**
- Token expirado
- Token inválido
- Secret incorreto em `.env`

**Solução:**
```bash
# Fazer novo login
curl -X POST http://localhost:8080/auth/login ...
```

### Erro: "Cannot reach backend service"

```
503 Service Unavailable
```

**Causas possíveis:**
- Microserviço não está rodando
- `BACKEND_SERVICE_URL` incorreto em `.env`

**Solução:**
```bash
# Verificar se microserviço está online
curl http://localhost:8081/actuator/health

# Editar .env
vim .env
```

### Erro: "CORS policy"

```
Access to XMLHttpRequest blocked by CORS policy
```

**Causas possíveis:**
- Origem não permitida em `CORS_ALLOWED_ORIGINS`
- Header `Authorization` não sendo exposto

**Solução:**
```bash
# Adicionar origem em .env
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200
```

## 📚 Referências

- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [Spring Security](https://spring.io/projects/spring-security)
- [JWT JJWT](https://github.com/jwtk/jjwt)
- [Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
- [JWT.io](https://jwt.io)

---

**Versão:** 1.0.0  
**Status:** ✅ Production Ready  
**Última atualização:** Maio 2025
