# Deployment do Gateway HelpPet

## ⚠️ Importante: Limitações da Vercel para Java

A **Vercel não suporta nativamente Java/Spring Boot**. Vercel é otimizada para:
- Node.js
- Python
- Go
- Ruby

Para Java, recomendamos plataformas mais apropriadas.

---

## ✅ Opções Recomendadas (em ordem de recomendação)

### 1. **Railway** (Recomendado ⭐⭐⭐)

**Prós:**
- ✅ Suporte nativo para Java/Spring Boot
- ✅ Deploy automático via GitHub
- ✅ Fácil gerenciamento de variáveis de ambiente
- ✅ MySQL integrado disponível
- ✅ Preço justo ($5-20/mês)
- ✅ Domínio customizado incluído

**Passo a Passo:**

#### 1.1 Preparar Repositório GitHub

```bash
cd /home/3DM/Documentos/backend/repositories/juncao/gateway

# Inicializar Git (se não existir)
git init

# Adicionar arquivos
git add .
git commit -m "Initial commit: Gateway with MySQL and JPA configuration"

# Criar repositório no GitHub e fazer push
git remote add origin https://github.com/SEU_USUARIO/juncao-gateway.git
git branch -M main
git push -u origin main
```

#### 1.2 Criar Conta Railway

1. Acesse [🚂 railway.app](https://railway.app)
2. Clique em "Login" e autentique com GitHub
3. Autorize Railway a acessar seus repositórios

#### 1.3 Criar Novo Projeto

1. Clique em "New Project"
2. Selecione "Deploy from GitHub repo"
3. Escolha o repositório `juncao-gateway`
4. Railway detectará automaticamente que é Java

#### 1.4 Configurar MySQL no Railway

1. No dashboard do projeto, clique em "*" (New Service)
2. Selecione "Add from Marketplace"
3. Escolha "MySQL"
4. Configure:
   - MYSQL_ROOT_PASSWORD: `root` (mude em produção!)
   - MYSQL_DATABASE: `helppet_gateway`
5. Railway criará automaticamente:
   - `DATABASE_URL` (variável de ambiente)
   - Host e credenciais

#### 1.5 Configurar Variáveis de Ambiente

No dashboard Railway:

1. Clique no serviço `gateway`
2. Vá em "Variables"
3. Adicione as variáveis:

```env
# Database
DB_HOST=${{ Awaits.DB_HOST }}
DB_PORT=${{ Awaits.DB_PORT }}
DB_NAME=helppet_gateway
DB_USERNAME=${{ Awaits.DB_USERNAME }}
DB_PASSWORD=${{ Awaits.DB_PASSWORD }}

# JWT
JWT_SECRET=sua-chave-super-secreta-em-producao-change-this-now

# Server
SERVER_PORT=8080

# Redis (adicionar como novo serviço depois)
REDIS_HOST=${{ Awaits.REDIS_HOST }}
REDIS_PORT=${{ Awaits.REDIS_PORT }}
```

#### 1.6 Deploy Automático

Railway fará deploy automaticamente quando você fizer push no GitHub:

```bash
git add .
git commit -m "Update deployment configuration"
git push origin main
```

**Logs em tempo real:**
```bash
# Via CLI Railway
railway logs
```

---

### 2. **Render** (Alternativa Excelente ⭐⭐⭐)

**Prós:**
- ✅ Suporte Java nativo
- ✅ Tier free incluído
- ✅ PostgreSQL/MySQL disponível
- ✅ HTTPS automático
- ✅ Muito simples de usar

**Passo a Passo:**

#### 2.1 Criar Conta Render

1. Acesse [render.com](https://render.com)
2. Clique "Sign up" e autentique com GitHub

#### 2.2 Criar Web Service

1. Clique "New +" → "Web Service"
2. Conecte repositório GitHub `juncao-gateway`
3. Configure:
   - **Name:** `helppet-gateway`
   - **Runtime:** `Java 17`
   - **Build Command:** `./mvnw clean package -DskipTests`
   - **Start Command:** `java -jar target/gateway-1.0.0.jar`
   - **Plan:** `Free` (ou Pro se precisar)

#### 2.3 Adicionar MySQL

1. Clique "New +" → "MySQL"
2. Configure:
   - **Name:** `helppet-db`
   - **Database Name:** `helppet_gateway`
   - **User:** `postgres` (Render usa PostgreSQL, não MySQL nativo)

#### 2.4 Configurar Variáveis de Ambiente

```env
DB_HOST=seu-render-db-host.render.com
DB_PORT=3306
DB_NAME=helppet_gateway
DB_USERNAME=root
DB_PASSWORD=senha-segura

JWT_SECRET=sua-chave-secreta-mudada-para-producao
SERVER_PORT=8080
```

#### 2.5 Deploy

Render fará deploy automático ao detectar push no GitHub.

---

### 3. **Google Cloud Run** (Para Produção Escalável ⭐⭐⭐⭐)

**Prós:**
- ✅ Serverless (paga só pelo que usa)
- ✅ Escalabilidade automática ilimitada
- ✅ Suporte Java completo
- ✅ Integração com Google Cloud Services
- ✅ Preço muito competitivo

**Passo a Passo:**

#### 3.1 Instalar Google Cloud CLI

```bash
# macOS
brew install google-cloud-sdk

# Ubuntu/Debian
curl https://sdk.cloud.google.com | bash
exec -l $SHELL

# Verificar
gcloud --version
```

#### 3.2 Autenticar no Google Cloud

```bash
gcloud auth login
gcloud auth application-default login
```

#### 3.3 Criar Projeto Google Cloud

```bash
# Criar novo projeto
gcloud projects create juncao-gateway --name="HelpPet Gateway"

# Definir projeto ativo
gcloud config set project juncao-gateway

# Ativar APIs necessárias
gcloud services enable containerregistry.googleapis.com
gcloud services enable run.googleapis.com
gcloud services enable compute.googleapis.com
```

#### 3.4 Configurar Docker

```bash
# Configurar Docker para usar Google Cloud
gcloud auth configure-docker

# Construir imagem
docker build -t gcr.io/juncao-gateway/gateway:latest .

# Fazer push
docker push gcr.io/juncao-gateway/gateway:latest
```

#### 3.5 Deploy no Cloud Run

```bash
gcloud run deploy gateway \
  --image gcr.io/juncao-gateway/gateway:latest \
  --platform managed \
  --region us-central1 \
  --memory 512Mi \
  --cpu 1 \
  --timeout 3600 \
  --set-env-vars "DB_HOST=seu-cloud-sql-host,DB_USERNAME=root,DB_PASSWORD=senha,JWT_SECRET=chave-secreta" \
  --allow-unauthenticated
```

**Resultado:**
```
Service [gateway] revision [gateway-00001-...] has been deployed and is serving 100 percent of traffic.
Service URL: https://gateway-xxxxx-uc.a.run.app
```

---

### 4. **AWS Elastic Beanstalk** (Solução Corporativa ⭐⭐⭐⭐)

**Prós:**
- ✅ Suporte Java completo
- ✅ Escalabilidade automática
- ✅ Gerenciamento simplificado
- ✅ Integração com RDS MySQL
- ✅ Free tier disponível

**Passo a Passo:**

#### 4.1 Instalar AWS CLI

```bash
# macOS
brew install awscli

# Ubuntu/Debian
apt-get install awscli

# Configurar credenciais
aws configure
```

#### 4.2 Criar Aplicação Elastic Beanstalk

```bash
# Criar arquivo .ebignore
echo "target/" >> .ebignore
echo ".git/" >> .ebignore
echo ".gitignore" >> .ebignore

# Inicializar EB
eb init -p "Java 17 running on 64bit Amazon Linux 2" gateway --region us-east-1

# Criar ambiente
eb create gateway-env

# Deploy
eb deploy

# Abrir aplicação
eb open
```

#### 4.3 Configurar Variáveis de Ambiente

```bash
eb setenv DB_HOST=seu-rds-mysql-endpoint \
          DB_USERNAME=admin \
          DB_PASSWORD=senha-segura \
          JWT_SECRET=chave-secreta
```

---

## 📋 Tabela Comparativa

| Plataforma | Java | MySQL | Deploy | Preço | Recomendação |
|---|:---:|:---:|:---:|---|---|
| **Railway** | ✅ | ✅ | ⭐⭐⭐⭐⭐ | $5-20 | ✅ Melhor para começar |
| **Render** | ✅ | ✅ | ⭐⭐⭐⭐ | Free+ | ✅ Segunda opção |
| **Google Cloud Run** | ✅ | ✅ | ⭐⭐⭐ | Pay-as-you-go | ✅ Melhor em escala |
| **AWS EB** | ✅ | ✅ | ⭐⭐⭐ | Free+$0.02/h | ✅ Corporativo |
| **Vercel** | ❌ | N/A | ❌ | - | ❌ Não recomendado |

---

## 🚀 Recomendação Final

**Para começar agora:** Use **Railway**
- Setup mais rápido
- Melhor documentação
- Preço justo
- Suporte excelente

**Para produção:** Use **Google Cloud Run**
- Melhor escalabilidade
- Preço competitivo
- Infraestrutura robusta

---

## ✅ Checklist Antes de Deploy

- [ ] Código está no GitHub
- [ ] `.env` está no `.gitignore`
- [ ] Variáveis de ambiente configuradas
- [ ] Docker builds localmente: `docker build -t test .`
- [ ] `pom.xml` tem todas as dependências
- [ ] Testes passam: `mvn test`
- [ ] JWT_SECRET é diferente do padrão
- [ ] Banco de dados está criado
- [ ] Redis está configurado (se necessário)

---

## 🔒 Variáveis Sensíveis em Produção

**NUNCA faça commit de:**
- Senhas de banco de dados
- JWT_SECRET
- API keys
- Credenciais AWS/GCP

**Sempre use:**
- Variáveis de ambiente
- Secret managers (AWS Secrets Manager, Google Secret Manager)
- `.env` no `.gitignore`

---

## 📚 Recursos Úteis

- [Railway Documentation](https://docs.railway.app)
- [Render Documentation](https://render.com/docs)
- [Google Cloud Run Guide](https://cloud.google.com/run/docs)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices)
- [Spring Boot on Cloud](https://spring.io/cloud)

---

## 💡 Próximos Passos

1. **Escolher plataforma** (recomendado: Railway)
2. **Fazer push do código** para GitHub
3. **Configurar variáveis de ambiente**
4. **Testar em staging** antes de produção
5. **Configurar monitoramento** e alertas
6. **Setup CI/CD** se necessário

