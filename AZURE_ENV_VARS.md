# Variáveis de Ambiente — Azure App Service

Configure estas variáveis em:
**App Service → Configuração → Configurações do aplicativo**

## Obrigatórias

| Variável | Exemplo | Descrição |
|---|---|---|
| `WEBSITES_PORT` | `8080` | Porta que o container expõe |
| `SPRING_PROFILES_ACTIVE` | `prod` | Ativa o application-prod.yml |
| `JWT_SECRET` | `<base64 64 bytes>` | Segredo JWT — deve ser igual ao do microserviço |
| `INTERNAL_SERVICE_TOKEN` | `<base64 32 bytes>` | Token entre gateway e microserviços |
| `CORS_ALLOWED_ORIGINS` | `https://seu-app.vercel.app` | URL do frontend na Vercel (sem barra no final) |
| `REDIS_HOST` | `gateway-help-pet-g8-vhzzlk.i.aivencloud.com` | Host do Redis Aiven |
| `REDIS_PORT` | `16321` | Porta do Redis Aiven |
| `REDIS_USERNAME` | `default` | Usuário do Redis Aiven |
| `REDIS_PASSWORD` | `<senha do painel Aiven>` | Senha do Redis Aiven |
| `G1_URL` | `https://helppet.azurewebsites.net` | URL do microserviço helppet |

## Opcionais (têm padrão)

| Variável | Padrão | Descrição |
|---|---|---|
| ~~`REDIS_SSL`~~ | — | SSL já fixado como `true` no application-prod.yml (Aiven exige) |
| `JWT_EXPIRATION` | `86400000` | Expiração JWT em ms (24h) |
| `RATE_LIMIT_REPLENISH_RATE` | `20` | Requisições/seg por IP |
| `RATE_LIMIT_BURST_CAPACITY` | `40` | Burst máximo |
| `MANAGEMENT_HEALTH_REDIS_ENABLED` | `true` | Inclui Redis no health check |

## Rotas G2-G5 (configure quando os serviços existirem)

| Variável | Descrição |
|---|---|
| `G2_URL` | URL do serviço de pets |
| `G3_URL` | URL do serviço de adoção |
| `G4_URL` | URL do serviço de chat |
| `G5_URL` | URL do serviço de notificações |

## Passos para subir

```bash
# 1. Build da imagem
docker build -t helppet-gateway ./gateway

# 2. Tag para o Azure Container Registry
docker tag helppet-gateway <seu-acr>.azurecr.io/helppet-gateway:latest

# 3. Push
az acr login --name <seu-acr>
docker push <seu-acr>.azurecr.io/helppet-gateway:latest

# 4. Configure o App Service para usar a imagem
# Portal: App Service → Deployment Center → Container Registry
```

## Health check do Azure

Configure em App Service → Configuração → Health check:
- **Caminho:** `/actuator/health`
- **Intervalo:** 30 segundos
