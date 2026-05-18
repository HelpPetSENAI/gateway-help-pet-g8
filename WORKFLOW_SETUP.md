# 🚀 WORKFLOW GITHUB ACTIONS - SETUP COMPLETO

**Arquivo:** `.github/workflows/main_gateway-help-pet-g8.yml`  
**Função:** Deploy automático no Azure quando fizer push em `main`

---

## ✅ ARQUIVO JÁ CRIADO

O workflow YAML foi criado. Agora você precisa configurar um **Secret** no GitHub.

---

## 🔑 PASSO 1: CRIAR AZURE_CREDENTIALS (5 min)

### No seu computador, execute:

```bash
# Gerar credentials para GitHub
az ad sp create-for-rbac \
  --name "github-actions-gateway" \
  --role contributor \
  --scopes /subscriptions/SEU-SUBSCRIPTION-ID/resourceGroups/helpet-rg

# Output será algo assim:
# {
#   "clientId": "...",
#   "clientSecret": "...",
#   "subscriptionId": "...",
#   "tenantId": "..."
# }
```

### Se não sabe seu SUBSCRIPTION_ID:

```bash
az account show --query id --output tsv
```

---

## 🔐 PASSO 2: ADICIONAR SECRET NO GITHUB (5 min)

### No GitHub:

```
1. Acesse seu repositório:
   https://github.com/HelpPetSENAI/gateway-help-pet-g8

2. Settings (menu topo) → Secrets and variables → Actions

3. "New repository secret"

4. Nome: AZURE_CREDENTIALS

5. Value: (Cole o JSON inteiro do comando anterior)
   {
     "clientId": "...",
     "clientSecret": "...",
     "subscriptionId": "...",
     "tenantId": "..."
   }

6. Clique "Add secret"
```

---

## 🎯 PASSO 3: TESTAR O WORKFLOW (2 min)

### Opção A: Fazer Push para main

```bash
cd seu-repo
git add .
git commit -m "Ativar GitHub Actions workflow"
git push origin main

# Workflow vai rodar automaticamente
```

### Opção B: Rodar manualmente

```
GitHub → Actions → "Deploy Gateway to Azure"
→ "Run workflow" → "Run workflow"
```

---

## 📊 O QUE O WORKFLOW FAZ

```
┌─────────────────────────────────────────┐
│ 1. CHECKOUT                             │
│    Clona seu código                     │
└─────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────┐
│ 2. AZURE LOGIN                          │
│    Usa AZURE_CREDENTIALS secret         │
└─────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────┐
│ 3. BUILD & PUSH                         │
│    Compila Docker no Azure ACR          │
│    (leva ~10 minutos)                   │
└─────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────┐
│ 4. DEPLOY                               │
│    Atualiza App Service com imagem      │
│    (leva ~2 minutos)                    │
└─────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────┐
│ 5. RESTART                              │
│    Reinicia aplicação                   │
│    (leva ~30 segundos)                  │
└─────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────┐
│ 6. TESTE                                │
│    Faz health check                     │
│    Se OK → Sucesso ✅                   │
└─────────────────────────────────────────┘
```

---

## 🕐 TEMPO TOTAL

```
Build Docker:     ~10 min (lento na primeira vez)
Deploy:           ~2 min
Restart:          ~30 sec
Teste:            ~5 sec
────────────────────────
TOTAL:            ~13-15 min
```

---

## ✅ VERIFICAR SE FUNCIONOU

### No GitHub:

```
Seu repositório
→ Actions (menu topo)
→ "Deploy Gateway to Azure"
→ Clique no workflow que rodou

Você verá todos os passos:
✅ Checkout code
✅ Azure Login
✅ Login to Azure Container Registry
✅ Build and push Docker image
✅ Deploy to Azure App Service
✅ Restart Azure Web App
✅ Wait for deployment
✅ Test health endpoint
```

### Se passou em todos:

```
✅ seu gateway foi deployado com sucesso!
✅ Acesse: https://helpet-gateway.azurewebsites.net/api/health
```

---

## 🐛 SE FALHAR

### Erro: "Azure Login failed"

```
→ AZURE_CREDENTIALS está errado
→ Verifique o JSON no GitHub Secret
→ Rode: az ad sp create-for-rbac de novo
```

### Erro: "Build failed"

```
→ Dockerfile tem problema
→ Verifique se está em gateway/Dockerfile
→ Execute localmente: docker build -f gateway/Dockerfile -t test .
```

### Erro: "Health check failed"

```
→ Variáveis de ambiente não estão certas
→ Verifique no App Service: Configuração → Configurações da aplicação
→ Verifique os logs: Log de fluxo
```

---

## 🔄 PRÓXIMAS VEZES

Depois de configurado, **cada vez que fizer push em main**:

```bash
git add .
git commit -m "Minha mudança"
git push origin main

# Workflow roda automaticamente!
# Vá em GitHub → Actions para ver o progresso
```

---

## 📋 CHECKLIST FINAL

```
☐ Executou: az ad sp create-for-rbac
☐ Copiou o JSON gerado
☐ Adicionou em GitHub Secrets (AZURE_CREDENTIALS)
☐ Fez git push (ou acionou manualmente)
☐ Verificou em GitHub → Actions
☐ Viu ✅ em todos os passos
☐ Testou /api/health no navegador
☐ Gateway está UP! 🎉
```

---

## 📞 ARQUIVO WORKFLOW

**Localização:** `.github/workflows/main_gateway-help-pet-g8.yml`

**O que faz:**
- Dispara quando faz push em `main`
- Build Docker no Azure
- Deploy automático
- Testa health check

---

**Pronto para CI/CD automático?** 🚀
