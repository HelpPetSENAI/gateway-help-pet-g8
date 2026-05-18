# 🚀 TUTORIAL: DEPLOY GATEWAY NO AZURE - PASSO A PASSO

**Data:** 18 de maio de 2026  
**Repositório:** https://github.com/HelpPetSENAI/gateway-help-pet-g8.git (branch main)  
**Status:** Código já está no GitHub ✅

---

## ⏱️ TEMPO TOTAL: ~45 MINUTOS

---

## 1️⃣ INSTALAR AZURE CLI (5 min)

### Se não tiver instalado:

```bash
# Linux/Ubuntu
curl -sL https://aka.ms/InstallAzureCLIDeb | sudo bash

# macOS
brew install azure-cli

# Windows (PowerShell)
Invoke-WebRequest -Uri https://aka.ms/installazurecliwindows -OutFile Azure-Cli.msi; msiexec /i Azure-Cli.msi
```

### Verificar instalação:
```bash
az --version
```

---

## 2️⃣ LOGIN NO AZURE (2 min)

```bash
az login
```

Isso abrirá seu navegador. Faça login com sua conta Azure.

---

## 3️⃣ CRIAR RESOURCE GROUP (2 min)
vou faz
```bash
# Criar grupo de recursos (SE NÃO EXISTIR)
az group create \
  --name helpet-rg \
  --location eastus

# Verificar
az group list --output table
```

---

## 4️⃣ CRIAR CONTAINER REGISTRY (3 min)

```bash
# Criar registry no Azure
az acr create \
  --resource-group helpet-rg \
  --name helppetregistry \
  --sku Standard

# Verificar
az acr list --resource-group helpet-rg --output table
```

**⚠️ Se já existe:**
```bash
az acr show --resource-group helpet-rg --name helppetregistry
```

---

## 5️⃣ FAZER LOGIN NO REGISTRY (1 min)

```bash
az acr login --name helppetregistry

# Esperado:
# Login Succeeded
```

---

## 6️⃣ FAZER BUILD E PUSH DA IMAGEM (10 min)

### Clone/acesse o repositório:

```bash
cd /home/3DM/Documentos/juncao/gateway

# OU clone se não tiver:
# git clone https://github.com/HelpPetSENAI/gateway-help-pet-g8.git
# cd gateway-help-pet-g8
```

### Build e push:

```bash
# Build direto no Azure Registry (recomendado)
az acr build \
  --registry helppetregistry \
  --image helpet-gateway:latest \
  --image helpet-gateway:1.0.0 \
  --file Dockerfile \
  .

# Isso leva ~5-10 minutos (vai compilar com Maven no Azure)
```

### Verificar se foi feito:

```bash
az acr repository list --name helppetregistry
# Deve retornar: helpet-gateway

az acr repository show \
  --name helppetregistry \
  --repository helpet-gateway
```

---

## 7️⃣ CRIAR APP SERVICE PLAN (2 min)

```bash
# Criar plano (SE NÃO EXISTIR)
az appservice plan create \
  --name helpet-gateway-plan \
  --resource-group helpet-rg \
  --is-linux \
  --sku B2

# B2 = 1.75 GB RAM, 2 cores (recomendado para produção)
# Para teste: usar B1 (mais barato)
```

---

## 8️⃣ CRIAR WEB APP (3 min)

```bash
# Criar aplicação no Azure
az webapp create \
  --resource-group helpet-rg \
  --plan helpet-gateway-plan \
  --name helpet-gateway \
  --deployment-container-image-name-user helppetregistry.azurecr.io/helpet-gateway:latest \
  --docker-registry-server-url https://helppetregistry.azurecr.io

# Vai levar alguns minutos...
```

**Seu gateway estará em:** `https://helpet-gateway.azurewebsites.net`

---

## 9️⃣ HABILITAR ACESSO DO APP SERVICE AO REGISTRY (3 min)

```bash
# Habilitar identidade gerenciada
az webapp identity assign \
  --resource-group helpet-rg \
  --name helpet-gateway

# Obter ID
PRINCIPAL_ID=$(az webapp identity show \
  --resource-group helpet-rg \
  --name helpet-gateway \
  --query principalId -o tsv)

echo "Principal ID: $PRINCIPAL_ID"

# Obter subscription ID
SUBSCRIPTION_ID=$(az account show --query id -o tsv)

echo "Subscription ID: $SUBSCRIPTION_ID"

# Dar permissão AcrPull
az role assignment create \
  --assignee $PRINCIPAL_ID \
  --role AcrPull \
  --scope /subscriptions/$SUBSCRIPTION_ID/resourceGroups/helpet-rg/providers/Microsoft.ContainerRegistry/registries/helppetregistry
```

---

## 🔟 CONFIGURAR VARIÁVEIS DE AMBIENTE (5 min)

### Gerar chaves:

```bash
# Gerar JWT_SECRET
JWT_SECRET=$(openssl rand -hex 32)
echo "JWT_SECRET=$JWT_SECRET"

# Gerar INTERNAL_SERVICE_TOKEN
INTERNAL_TOKEN=$(openssl rand -hex 32)
echo "INTERNAL_TOKEN=$INTERNAL_TOKEN"
```

### Configurar no Azure:

```bash
# IMPORTANTE: Mude os valores G1_URL, G2_URL, etc com seus endpoints reais!

az webapp config appsettings set \
  --resource-group helpet-rg \
  --name helpet-gateway \
  --settings \
    SPRING_PROFILES_ACTIVE=prod \
    JWT_SECRET="$JWT_SECRET" \
    JWT_EXPIRATION=86400000 \
    INTERNAL_SERVICE_TOKEN="$INTERNAL_TOKEN" \
    G1_URL=https://seu-g1-azure.azurewebsites.net \
    G2_URL=https://seu-g2-azure.azurewebsites.net \
    G3_URL=https://seu-g3-azure.azurewebsites.net \
    G4_URL=https://seu-g4-azure.azurewebsites.net \
    G5_URL=https://seu-g5-azure.azurewebsites.net \
    CORS_ALLOWED_ORIGINS=https://seu-frontend.com \
    REDIS_HOST=seu-redis-produção.redis.cache.windows.net \
    REDIS_PORT=6380 \
    REDIS_PASSWORD=sua-senha-redis-forte \
    RATE_LIMIT_REPLENISH_RATE=20 \
    RATE_LIMIT_BURST_CAPACITY=40
```

---

## 1️⃣1️⃣ APLICAR CONFIGURAÇÕES (2 min)

```bash
# Reiniciar aplicação
az webapp restart \
  --resource-group helpet-rg \
  --name helpet-gateway

# Esperar alguns segundos...
sleep 30

# Ver logs
az webapp log tail \
  --resource-group helpet-rg \
  --name helpet-gateway
```

---

## 1️⃣2️⃣ TESTAR DEPLOYMENT (5 min)

### Health check:

```bash
# Testar se está up
curl https://helpet-gateway.azurewebsites.net/api/health | jq .

# Esperado:
{
  "gateway_status": "UP",
  "overall_status": "UP",
  "services": {
    "g1_auth_users": "UP",
    "g2_pets": "UP",
    "g3_adoption": "UP",
    "g4_chat": "UP",
    "g5_notifications": "UP"
  }
}
```

### Se falhar:

```bash
# Ver logs
az webapp log tail --resource-group helpet-rg --name helpet-gateway --tail 50

# Ou via portal:
# https://portal.azure.com → helpet-gateway → Deployment → Logs
```

---

## 🎯 RESUMO DE COMANDOS RÁPIDOS

```bash
# 1. Login
az login

# 2. Criar recursos
az group create --name helpet-rg --location eastus
az acr create --resource-group helpet-rg --name helppetregistry --sku Standard

# 3. Build e push
cd gateway-help-pet-g8
az acr build --registry helppetregistry --image helpet-gateway:latest --file Dockerfile .

# 4. Criar App Service
az appservice plan create --name helpet-gateway-plan --resource-group helpet-rg --is-linux --sku B2
az webapp create --resource-group helpet-rg --plan helpet-gateway-plan --name helpet-gateway --deployment-container-image-name-user helppetregistry.azurecr.io/helpet-gateway:latest --docker-registry-server-url https://helppetregistry.azurecr.io

# 5. Configurar
az webapp config appsettings set --resource-group helpet-rg --name helpet-gateway --settings SPRING_PROFILES_ACTIVE=prod JWT_SECRET=... (etc)

# 6. Reiniciar
az webapp restart --resource-group helpet-rg --name helpet-gateway

# 7. Testar
curl https://helpet-gateway.azurewebsites.net/api/health | jq .

# 8. Ver logs
az webapp log tail --resource-group helpet-rg --name helpet-gateway
```

---

## 🔗 URLS IMPORTANTES

| Item | URL |
|------|-----|
| Gateway | https://helpet-gateway.azurewebsites.net |
| Health | https://helpet-gateway.azurewebsites.net/api/health |
| Portal Azure | https://portal.azure.com |
| GitHub (Gateway) | https://github.com/HelpPetSENAI/gateway-help-pet-g8 |

---

## 🐛 TROUBLESHOOTING

### ❌ "Build failed"
```bash
# Verificar Dockerfile
az acr build --registry helppetregistry --image helpet-gateway:debug --file Dockerfile . --debug
```

### ❌ "Container can't connect to Redis"
```bash
# Verificar Redis
az redis show --resource-group helpet-rg --name helpet-redis

# Ou criar Redis:
az redis create --name helpet-redis --resource-group helpet-rg --location eastus --sku Basic --vm-size c0
```

### ❌ "Health check returning DOWN"
```bash
# Ver logs
az webapp log tail --resource-group helpet-rg --name helpet-gateway

# Verificar variáveis
az webapp config appsettings list --resource-group helpet-rg --name helpet-gateway
```

### ❌ "CORS error"
```bash
# Atualizar CORS_ALLOWED_ORIGINS
az webapp config appsettings set \
  --resource-group helpet-rg \
  --name helpet-gateway \
  --settings CORS_ALLOWED_ORIGINS=https://seu-frontend.com
```

---

## 📊 VERIFICAR DEPLOYMENT

### Via CLI:

```bash
# Status
az webapp show --resource-group helpet-rg --name helpet-gateway --query "state"

# URL
az webapp show --resource-group helpet-rg --name helpet-gateway --query "defaultHostName"

# Últimos logs
az webapp log tail --resource-group helpet-rg --name helpet-gateway --tail 100
```

### Via Portal:

1. Acesse: https://portal.azure.com
2. Busque: "helpet-gateway"
3. Clique em "Logs" para ver output
4. Clique em "Deployment" para ver histórico

---

## ✅ CHECKLIST DE DEPLOY

```
☐ Azure CLI instalado (az --version)
☐ Logado no Azure (az login)
☐ Resource Group criado (helpet-rg)
☐ Container Registry criado (helppetregistry)
☐ Imagem built e pushed
☐ App Service Plan criado
☐ Web App criada
☐ Identidade gerenciada habilitada
☐ Permissões AcrPull configuradas
☐ Variáveis de ambiente adicionadas
☐ Aplicação reiniciada
☐ Health check testado
☐ Logs verificados
```

---

## 🎓 PRÓXIMAS ETAPAS

### Hoje:
```
1. ✅ Executar tutorial acima
2. ✅ Testar /api/health
3. ✅ Verificar logs
```

### Amanhã:
```
1. ☐ Testar login (/api/v1/auth/login)
2. ☐ Testar requisições autenticadas
3. ☐ Configurar domínio customizado
```

### Futuro:
```
1. ☐ Setup CI/CD automático (GitHub Actions)
2. ☐ Application Insights (monitoring)
3. ☐ Auto-scaling
4. ☐ WAF (Web Application Firewall)
```

---

## 🚀 COMECE AGORA!

```bash
# 1. Fazer login
az login

# 2. Executar passo 3 do tutorial (criar resource group)
# ... (siga os passos acima)

# 3. Testar
curl https://helpet-gateway.azurewebsites.net/api/health | jq .
```

---

**Dúvidas? Ver AZURE_CHECKLIST.md para mais detalhes**

*Gateway v1.0.0 | Java 17 | Spring Boot 3.3.5 | Azure Pronto*
