# 🌐 DEPLOY AZURE PELO PORTAL - GUIA EM PORTUGUÊS (Brasil)

**Data:** 18 de maio de 2026  
**Idioma:** Português Brasileiro  
**Portal:** https://portal.azure.com (em PT-BR)  
**Tempo:** ~30 minutos

---

## ✅ VOCÊ JÁ FEZ CERTO!

Se criou um **Aplicativo Web** nos "Serviços de Aplicativos", está no caminho certo! ✓

---

## 📍 VOCÊ ESTÁ AQUI

```
Azure Portal (PT-BR)
→ Serviços de Aplicativos
→ [Seu Aplicativo Web]
```

---

## 🎯 PRÓXIMOS PASSOS (CONTINUANDO DAQUI)

### PASSO 1: CONFIGURAR VARIÁVEIS DE AMBIENTE (5 min)

**Você está no seu aplicativo web. Agora:**

```
1. Menu esquerdo → procurar "Configuração"
   (ou "Settings" → "Configuration")

2. Clique em "Configuração" (você verá abas)

3. Clique na aba "Configurações da aplicação"
   (ou "Application settings")
```

### PASSO 2: ADICIONAR VARIÁVEIS (10 min)

**Clique em "+ Novo parâmetro de aplicação"**

Adicione estas variáveis UMA POR UMA:

```
1. SPRING_PROFILES_ACTIVE = prod

2. JWT_SECRET = (gere com comando abaixo)

3. JWT_EXPIRATION = 86400000

4. INTERNAL_SERVICE_TOKEN = (gere com comando abaixo)

5. G1_URL = https://seu-g1.azurewebsites.net

6. G2_URL = https://seu-g2.azurewebsites.net

7. G3_URL = https://seu-g3.azurewebsites.net

8. G4_URL = https://seu-g4.azurewebsites.net

9. G5_URL = https://seu-g5.azurewebsites.net

10. CORS_ALLOWED_ORIGINS = https://seu-frontend.com

11. REDIS_HOST = seu-redis.redis.cache.windows.net

12. REDIS_PORT = 6380

13. REDIS_PASSWORD = sua-senha-redis

14. RATE_LIMIT_REPLENISH_RATE = 20

15. RATE_LIMIT_BURST_CAPACITY = 40
```

### PASSO 3: GERAR CHAVES (1 min)

**Execute no seu computador (terminal):**

```bash
# Gerar JWT_SECRET
openssl rand -hex 32

# Copie o valor gerado e cole como JWT_SECRET

# Fazer de novo para INTERNAL_SERVICE_TOKEN
openssl rand -hex 32

# Copie e cole como INTERNAL_SERVICE_TOKEN
```

### PASSO 4: SALVAR (1 min)

```
Depois de adicionar todas as variáveis
→ Clique em "Guardar" (botão azul no topo)
→ Pode aparecer uma mensagem pedindo para reiniciar
→ Clique "Continuar"
```

---

## 🔄 REINICIAR APLICAÇÃO (2 min)

**Seu aplicativo web vai reiniciar automaticamente**

OU pode reiniciar manualmente:

```
Menu superior (onde está o nome do app)
→ Procure por "Reiniciar" (ou "Restart")
→ Clique
→ Confirme "Sim"

Aguarde ~30 segundos
```

---

## ✅ TESTAR SE ESTÁ FUNCIONANDO (2 min)

### PASSO 1: Copiar URL do seu aplicativo

```
Seu aplicativo web
→ Menu superior → procure por "URL" ou "Visão geral"
→ Deve aparecer algo como:
   https://helpet-gateway.azurewebsites.net
```

### PASSO 2: Testar no navegador

**Abra no navegador:**

```
https://seu-app.azurewebsites.net/api/health
```

**Você deve ver algo assim:**

```json
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

---

## 🚨 SE NÃO FUNCIONAR (troubleshooting)

### ❌ Retorna erro 503 ou DOWN

```
1. Seu aplicativo
   → Menu esquerdo: "Log de fluxo" (ou "Log stream")
   → Veja qual é o erro
   
2. Verifique se as variáveis estão corretas:
   → Configuração → Configurações da aplicação
   → G1_URL, G2_URL, etc estão apontando para seus serviços?
   → REDIS_HOST está correto?

3. Se ainda não funcionar:
   → Reinicie novamente
   → Aguarde 2 minutos
   → Tente de novo
```

### ❌ Retorna erro 404 ou não encontra

```
→ Sua URL pode estar errada
→ Copie a URL do Azure (no topo do seu app)
→ Adicione /api/health no final
```

---

## 📍 MAPA DO PORTAL EM PORTUGUÊS

```
Portal Azure
├── Home (topo esquerdo, logo Azure)
│
├── Barra de busca (topo, no meio)
│   └── Digitar o que procura
│
├── Menu esquerdo
│   ├── Home
│   ├── Todos os recursos
│   ├── Grupos de recursos
│   └── Serviços de Aplicativos
│
└── Seu Aplicativo Web
    ├── Visão geral
    ├── Configuração (Settings)
    │   ├── Configurações da aplicação
    │   └── Configurações gerais
    ├── Log de fluxo (para ver erros)
    ├── Reiniciar
    └── Parar/Iniciar
```

---

## 🎯 CHECKLIST - VOCÊ FEZ TUDO?

```
☐ Criou aplicativo web nos Serviços de Aplicativos
☐ Acessou: Configuração → Configurações da aplicação
☐ Adicionou 15 variáveis (JWT_SECRET, G1_URL, etc)
☐ Clicou "Guardar" e reiniciou
☐ Aguardou ~2 minutos para reiniciar
☐ Testou /api/health no navegador
☐ Recebeu resposta com "gateway_status": "UP"
```

---

## 🔗 LINKS ÚTEIS (EM PORTUGUÊS)

```
Portal Azure: https://portal.azure.com
Minha assinatura: https://portal.azure.com/#blade/Microsoft_Azure_Billing/SubscriptionsBlade
Meus grupos de recursos: https://portal.azure.com/#blade/HubsExtension/BrowseResourceGroupBlade
Meus serviços de aplicativos: https://portal.azure.com/#blade/HubsExtension/BrowseResourceBlade/resourceType/Microsoft.Web%2Fsites
```

---

## 📋 VALORES QUE VOCÊ VAI USAR

**IMPORTANTE: Mude estes valores para seus serviços:**

```
G1_URL = https://seu-servico-g1.azurewebsites.net
         (ou a URL real do seu G1)

G2_URL = https://seu-servico-g2.azurewebsites.net
         (ou a URL real do seu G2)

G3_URL = https://seu-servico-g3.azurewebsites.net
         (ou a URL real do seu G3)

G4_URL = https://seu-servico-g4.azurewebsites.net
         (ou a URL real do seu G4)

G5_URL = https://seu-servico-g5.azurewebsites.net
         (ou a URL real do seu G5)

CORS_ALLOWED_ORIGINS = https://seu-frontend.com
                       (ou http://localhost:3000 para teste)

REDIS_HOST = seu-redis-name.redis.cache.windows.net
             (procure no seu Redis no Azure)

REDIS_PASSWORD = copie da sua configuração Redis
```

---

## 💡 DICAS RÁPIDAS

```
🔍 Não achou? Use a barra de busca (topo do portal)

📌 Quer voltar? Clique no logo Azure no topo esquerdo (Home)

⭐ Quer favoritar? Clique na estrela (⭐) para adicionar a Favoritos

⏱️ Demorando? Clique no sino (🔔) para ver notificações

🖱️ Não vê a aba? Scroll para a direita nas abas

📋 Copia fácil? Clique no ícone de copiar (📋) ao lado dos valores
```

---

## ❓ DÚVIDAS COMUNS

### P: Meu app está "Parado"?
```
R: Vai parar se ficou inativo. Procure por "Iniciar" no topo do app
```

### P: Preciso fazer algo com o Container Registry?
```
R: Se você criou a imagem, já deve estar lá. 
   Se não, vá para:
   → Menu esquerdo → Registros de Contêiner
   → Procure por "helppetregistry" ou similar
```

### P: Quero mudar alguma variável depois?
```
R: Volta em Configuração → Configurações da aplicação
   → Clique no valor → edita → clica em check
   → Clica Guardar no topo
   → App vai reiniciar automático
```

### P: Como vejo os erros se algo der errado?
```
R: Menu esquerdo → Log de fluxo (Log stream)
   → Vê os erros em tempo real
```

---

## ✅ PRONTO!

Se chegou até aqui e o `/api/health` retornou `"gateway_status": "UP"`, seu gateway **ESTÁ 100% DEPLOYADO NO AZURE**! 🎉

---

## 🎯 PRÓXIMAS AÇÕES

### Testar login:
```
https://seu-app.azurewebsites.net/api/v1/auth/login
(POST com email e senha)
```

### Testar requisição com autenticação:
```
https://seu-app.azurewebsites.net/api/v1/pets
(GET com token Bearer)
```

### Monitorar saúde:
```
Volta em: Log de fluxo
E vê o que está acontecendo
```

---

## 📞 PRECISA DE AJUDA?

Se algo não funcionar:

1. Verifique os valores das variáveis
2. Veja o Log de fluxo (erro deve estar lá)
3. Reinicie o app
4. Teste de novo

---

**Portal Azure em Português | Guia Prático | ~30 minutos**
