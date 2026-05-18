# 🌐 DEPLOY AZURE PELO PORTAL - GUIA VISUAL

**Data:** 18 de maio de 2026  
**Método:** Portal Azure (site) - Clique a clique  
**Tempo:** ~30 minutos

---

## 🔗 ACESSE: https://portal.azure.com

---

## 1️⃣ CRIAR RESOURCE GROUP (3 min)

### Passo 1.1 - Buscar "Resource groups"

```
Home → Search Bar (topo) → digitar "Resource groups" → ENTER
```

### Passo 1.2 - Criar novo

```
Resource groups → "+ Create" (botão azul)
```

### Passo 1.3 - Preencher dados

```
Subscription: Sua subscription (escolher)
Resource group name: helpet-rg
Region: East US (ou próximo a você)

→ "Review + Create" → "Create"
```

✅ Aguarde ~30 segundos

---

## 2️⃣ CRIAR CONTAINER REGISTRY (5 min)

### Passo 2.1 - Buscar "Container registries"

```
Home → Search Bar → digitar "Container registries" → ENTER
```

### Passo 2.2 - Criar novo

```
Container registries → "+ Create" (botão azul)
```

### Passo 2.3 - Preencher dados

```
Subscription: Sua subscription
Resource group: helpet-rg (que criamos acima)
Registry name: helppetregistry (deve ser único)
Location: East US
SKU: Standard (padrão)

→ "Review + Create" → "Create"
```

✅ Aguarde ~2 minutos

### Passo 2.4 - Anotar dados importantes

```
Quando criado, clique em "Go to resource" e copie:

🔑 Registry name: helppetregistry
🔗 Login server: helppetregistry.azurecr.io
```

---

## 3️⃣ FAZER BUILD E PUSH DA IMAGEM (15 min)

### Passo 3.1 - Ir para Registry e fazer build

```
Menu lateral → "Repositories" (você estará em helppetregistry)

Será vazio por enquanto. Vamos fazer build pelo portal.
```

### Passo 3.2 - Fazer build direto no portal

```
helppetregistry (seu registro)
→ Menu lateral: "Tasks" (ou "Build tasks")
→ "+ New quick build"
```

### Passo 3.3 - Preencher build task

```
Source:
  - Repository URL: https://github.com/HelpPetSENAI/gateway-help-pet-g8.git
  - Branch: main
  - Dockerfile: Dockerfile (ou gateway/Dockerfile se não achar)

Image name: helpet-gateway:latest
  
→ "Build" (botão azul - vai demorar ~8 min)
```

✅ Você verá em tempo real o build acontecendo

### Passo 3.4 - Verificar se foi criado

```
helppetregistry → Menu lateral → "Repositories"
→ Deve aparecer "helpet-gateway"

Clique nela e deve ter tag "latest"
```

---

## 4️⃣ CRIAR APP SERVICE PLAN (2 min)

### Passo 4.1 - Buscar "App Service plans"

```
Home → Search Bar → digitar "App Service plans" → ENTER
```

### Passo 4.2 - Criar novo

```
App Service plans → "+ Create" (botão azul)
```

### Passo 4.3 - Preencher dados

```
Subscription: Sua subscription
Resource group: helpet-rg
Name: helpet-gateway-plan
Operating System: Linux
Region: East US
Pricing tier: Basic B2 (pode usar B1 para teste - mais barato)

→ "Review + Create" → "Create"
```

✅ Aguarde ~30 segundos

---

## 5️⃣ CRIAR WEB APP (APLICAÇÃO) (5 min)

### Passo 5.1 - Buscar "App Services"

```
Home → Search Bar → digitar "App Services" → ENTER
```

### Passo 5.2 - Criar novo

```
App Services → "+ Create" (botão azul)
```

### Passo 5.3 - Preencher dados básicos

```
Subscription: Sua subscription
Resource group: helpet-rg
Name: helpet-gateway (isso se torna URL: helpet-gateway.azurewebsites.net)
Publish: Docker Container
Operating System: Linux
Region: East US
App Service Plan: helpet-gateway-plan

→ "Next: Docker >" (botão azul)
```

### Passo 5.4 - Configurar Docker

```
Image Source: Azure Container Registry (ACR)
Registry: helppetregistry (escolher)
Image: helpet-gateway
Tag: latest
Startup File: (deixar vazio)

→ "Review + Create" → "Create"
```

✅ Aguarde ~2 minutos

### Passo 5.5 - Quando criado

```
Clique em "Go to resource"

Você estará na página da aplicação.
```

---

## 6️⃣ CONFIGURAR VARIÁVEIS DE AMBIENTE (5 min)

### Passo 6.1 - Abrir configurações

```
helpet-gateway (sua web app)
→ Menu lateral: "Configuration" (Settings → Configuration)
```

### Passo 6.2 - Adicionar Application Settings

```
"Application Settings" tab (já estará aberta)
→ "+ New application setting" (para cada uma abaixo)

Adicione estas variáveis:
```

| Nome | Valor |
|------|-------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `JWT_SECRET` | `gere-com-openssl-rand-hex-32` |
| `JWT_EXPIRATION` | `86400000` |
| `INTERNAL_SERVICE_TOKEN` | `gere-token-novo` |
| `G1_URL` | `https://seu-g1.azurewebsites.net` |
| `G2_URL` | `https://seu-g2.azurewebsites.net` |
| `G3_URL` | `https://seu-g3.azurewebsites.net` |
| `G4_URL` | `https://seu-g4.azurewebsites.net` |
| `G5_URL` | `https://seu-g5.azurewebsites.net` |
| `CORS_ALLOWED_ORIGINS` | `https://seu-frontend.com` |
| `REDIS_HOST` | `seu-redis.redis.cache.windows.net` |
| `REDIS_PORT` | `6380` |
| `REDIS_PASSWORD` | `sua-senha-redis` |
| `RATE_LIMIT_REPLENISH_RATE` | `20` |
| `RATE_LIMIT_BURST_CAPACITY` | `40` |

### Passo 6.3 - Salvar

```
Depois de adicionar todas → "Save" (botão azul no topo)
→ Clique "Continue" se pedir para reiniciar
```

---

## 7️⃣ REINICIAR APLICAÇÃO (2 min)

### Passo 7.1 - Reiniciar

```
helpet-gateway
→ Menu superior: "Restart" (botão azul)
→ Confirme: "Yes"
```

✅ Aguarde ~30-45 segundos

---

## 8️⃣ TESTAR (2 min)

### Passo 8.1 - Obter URL

```
helpet-gateway
→ Menu topo: "Browse" (ou copiar URL em "Overview")

URL será: https://helpet-gateway.azurewebsites.net
```

### Passo 8.2 - Testar Health Check

```
Abra no navegador (ou curl):

https://helpet-gateway.azurewebsites.net/api/health

Deve retornar:
{
  "gateway_status": "UP",
  "overall_status": "UP",
  "services": {...}
}
```

### Passo 8.3 - Ver Logs (se falhar)

```
helpet-gateway
→ Menu lateral: "Log stream"

Você verá logs em tempo real da aplicação
```

---

## 📍 MAPA DO PORTAL - ATALHOS RÁPIDOS

```
Home (início)
├── Search Bar (barra busca no topo)
│   ├── "Resource groups" → criar helpet-rg
│   ├── "Container registries" → criar helppetregistry
│   ├── "App Service plans" → criar helpet-gateway-plan
│   └── "App Services" → criar helpet-gateway
│
├── Favorites (lado esquerdo)
│   └── (você pode fixar aqui para acesso rápido)
│
└── Recent resources (recursos usados recentemente)
    └── Aparecem depois de criados
```

---

## 🎯 ORDEM CORRETA DE CLIQUES

```
1. Home
2. Search "Resource groups" → Create → helpet-rg
3. Search "Container registries" → Create → helppetregistry
4. helppetregistry → Tasks → Build (GitHub link)
5. Search "App Service plans" → Create → helpet-gateway-plan
6. Search "App Services" → Create → helpet-gateway
7. helpet-gateway → Configuration → Add settings
8. helpet-gateway → Restart
9. helpet-gateway → Browse / Copy URL
10. Testar no navegador
```

---

## 🔑 CHAVES E VALORES IMPORTANTES

### Anote esses valores:

```
🔑 Resource Group: helpet-rg
🔑 Registry Name: helppetregistry
🔑 Registry URL: helppetregistry.azurecr.io
🔑 App Service Name: helpet-gateway
🔑 Gateway URL: https://helpet-gateway.azurewebsites.net

🔐 JWT_SECRET: (gerado com openssl)
🔐 INTERNAL_SERVICE_TOKEN: (gerado com openssl)
```

---

## ⚠️ ERROS COMUNS

### ❌ "Build failed" no Container Registry

```
→ Ir para: helppetregistry → Tasks → clique na build
→ Ver logs para achar o erro
→ Problema comum: Dockerfile não existe em "gateway/Dockerfile"
   → Tentar: "Dockerfile" (sem pasta)
```

### ❌ "Health check returning DOWN"

```
→ helpet-gateway → Log stream
→ Ver quais erros aparecem
→ Verificar se G1_URL, G2_URL, etc. estão corretos
```

### ❌ "503 Service Unavailable"

```
→ Redis não conectado
→ G1-G5 URLs erradas
→ Ver logs para detalhes
```

---

## 📊 VERIFICAR TUDO NO PORTAL

### Recurso criado com sucesso?

```
Home → Search "Resource groups"
→ Clique em "helpet-rg"
→ Deve mostrar:
   ✓ helppetregistry (container registry)
   ✓ helpet-gateway-plan (app service plan)
   ✓ helpet-gateway (web app)
```

### Imagem foi buildada?

```
helppetregistry → Repositories
→ Clique em "helpet-gateway"
→ Deve mostrar tag "latest"
```

### Aplicação está rodando?

```
helpet-gateway → Overview
→ "Status" deve estar "Running"
→ "URL" deve ser: https://helpet-gateway.azurewebsites.net
```

---

## 🚀 CHECKLIST VISUAL

```
☐ Home → Search "Resource groups" → Create (helpet-rg)
☐ Home → Search "Container registries" → Create (helppetregistry)
☐ helppetregistry → Tasks → Build (GitHub)
☐ Aguardar ~10 min de build
☐ Home → Search "App Service plans" → Create (helpet-gateway-plan)
☐ Home → Search "App Services" → Create (helpet-gateway)
☐ helpet-gateway → Configuration → Add 14 application settings
☐ helpet-gateway → Restart
☐ helpet-gateway → Browse / Testar /api/health
```

---

## 💡 DICAS RÁPIDAS

```
🔍 Perdeu onde está? Use a barra de search (topo)

📌 Quero voltar rápido? Clique em "Home" (logo Azure no topo esquerdo)

⭐ Fixar recurso? Clique na estrela para adicionar a Favoritos

⏱️ Demorando? Clique no sino (🔔) para ver notificações de progresso

🖱️ Voltar página? Botão voltar do navegador funciona

📋 Copiar valores? Clique no ícone de "copy" (📋) ao lado dos valores
```

---

## 🔗 LINKS DIRETOS

```
Portal Azure: https://portal.azure.com
Sua subscription: https://portal.azure.com/#blade/Microsoft_Azure_Billing/SubscriptionsBlade
Seu Gateway: https://helpet-gateway.azurewebsites.net
Health Check: https://helpet-gateway.azurewebsites.net/api/health
```

---

## ✅ PRONTO!

Quando terminar todos os passos e o health check retornar `UP`, seu gateway está **100% deployed** no Azure! 🎉

---

## 📞 PRÓXIMOS PASSOS

### Testar login:
```
POST https://helpet-gateway.azurewebsites.net/api/v1/auth/login
Headers: Content-Type: application/json
Body: {
  "email": "seu@email.com",
  "password": "senha"
}
```

### Testar requisição autenticada:
```
GET https://helpet-gateway.azurewebsites.net/api/v1/pets
Headers: Authorization: Bearer [token-aqui]
```

---

*Portal Azure | Visual Guide | 30 minutos*
