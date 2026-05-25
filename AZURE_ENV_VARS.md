# Variáveis de Ambiente — Azure App Service

Configure em:
**App Service → Configuração → Configurações do aplicativo**

---

## ⚡ Obrigatórias (sem elas o app não sobe)

| Variável | Valor / Exemplo | Descrição |
|---|---|---|
| `WEBSITES_PORT` | `8080` | Porta que o container expõe |
| `SPRING_PROFILES_ACTIVE` | `prod` | Ativa o `application-prod.yml` |
| `JWT_SECRET` | `<openssl rand -hex 64>` | Segredo JWT — mínimo 64 chars, deve ser igual ao dos microserviços |
| `INTERNAL_SERVICE_TOKEN` | `<openssl rand -hex 32>` | Token entre gateway e microserviços |
| `CORS_ALLOWED_ORIGINS` | `https://seu-app.vercel.app` | URL do frontend (sem barra no final) |
| `REDIS_PASSWORD` | `<senha do painel Aiven>` | Senha do Redis Aiven |

---

## Redis Aiven (já configurado como padrão no application-prod.yml)

| Variável | Valor padrão | Observação |
|---|---|---|
| `REDIS_HOST` | `gateway-help-pet-g8-vhzzlk.i.aivencloud.com` | Já é o default — só mude se trocar o Redis |
| `REDIS_PORT` | `16321` | Já é o default |
| `REDIS_USERNAME` | `default` | Já é o default |
| `REDIS_PASSWORD` | *(obrigatório)* | Pegar no painel Aiven → Redis → Connection Info |

> **Formato completo Aiven:** `rediss://default:<SENHA>@gateway-help-pet-g8-vhzzlk.i.aivencloud.com:16321`  
> SSL está fixo como `true` no `application-prod.yml` (Aiven exige `rediss://`).

---

## URLs dos Microserviços

Configure quando cada serviço estiver no ar:

| Variável | Descrição |
|---|---|
| `G1_URL` | URL do App Service G1 (Auth + Users) — ex: `https://helppet-g1.azurewebsites.net` |
| `G2_URL` | URL do App Service G2 (Pets) |
| `G3_URL` | URL do App Service G3 (Adoption) |
| `G4_URL` | URL do App Service G4 (Chat) |
| `G5_URL` | URL do App Service G5 (Notifications) |

---

## Opcionais (têm padrão)

| Variável | Padrão | Descrição |
|---|---|---|
| `JWT_EXPIRATION` | `86400000` | Expiração JWT em ms (24h) |
| `RATE_LIMIT_REPLENISH_RATE` | `20` | Requisições/seg por IP |
| `RATE_LIMIT_BURST_CAPACITY` | `40` | Burst máximo |
| `MANAGEMENT_HEALTH_REDIS_ENABLED` | `true` | Inclui Redis no health check |

---

## Passos para subir no Azure

### Opção A — Deploy manual (Azure CLI)

```bash
# 1. Login
az login
az acr login --name <seu-acr>

# 2. Build e push
docker build -t <seu-acr>.azurecr.io/helppet-gateway:latest ./gateway
docker push <seu-acr>.azurecr.io/helppet-gateway:latest

# 3. Atualiza App Service
az webapp config container set \
  --name <app-service-name> \
  --resource-group <resource-group> \
  --container-image-name <seu-acr>.azurecr.io/helppet-gateway:latest

az webapp restart --name <app-service-name> --resource-group <resource-group>
```

Ou use o script interativo:
```bash
chmod +x deploy.sh && ./deploy.sh   # escolha opção 6 (Azure)
```

### Opção B — CI/CD automático (GitHub Actions)

O workflow `.github/workflows/azure-deploy.yml` roda automaticamente em cada push para `main`/`master`.

Adicione estes secrets no GitHub (**Settings → Secrets and variables → Actions**):

| Secret | Como obter |
|---|---|
| `AZURE_CREDENTIALS` | `az ad sp create-for-rbac --name "helppet-gateway-sp" --role contributor --scopes /subscriptions/<sub-id>/resourceGroups/<rg> --sdk-auth` |
| `ACR_NAME` | Nome do seu Azure Container Registry |
| `AZURE_RESOURCE_GROUP` | Nome do Resource Group |
| `APP_SERVICE_NAME` | Nome do App Service |

---

## Health check do Azure App Service

Configure em **App Service → Configuração → Health check**:
- **Caminho:** `/actuator/health`
- **Intervalo:** 30 segundos

Endpoints disponíveis:
- `/actuator/health` — status geral (público)
- `/actuator/metrics` — métricas internas
- `/actuator/prometheus` — scraping Prometheus
- `/api/health` — health agregado dos microserviços G1-G5

---

## Gerar segredos no terminal

```bash
# JWT_SECRET (64 bytes hex = 128 chars)
openssl rand -hex 64

# INTERNAL_SERVICE_TOKEN (32 bytes hex = 64 chars)
openssl rand -hex 32
```
