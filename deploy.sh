#!/bin/bash

# =================================================================
# HelpPet Gateway - Deploy Helper Script
# =================================================================

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# =================================================================
# Funções Auxiliares
# =================================================================

print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

# =================================================================
# Verificação Pré-Deploy
# =================================================================

check_prerequisites() {
    print_header "Verificando Pré-requisitos"

    # Verificar se estamos no diretório correto
    if [ ! -f "pom.xml" ]; then
        print_error "pom.xml não encontrado. Execute este script no diretório raiz do projeto."
        exit 1
    fi
    print_success "pom.xml encontrado"

    # Verificar se Git está inicializado
    if [ ! -d ".git" ]; then
        print_warning "Git não inicializado. Inicializando..."
        git init
        git add .
        git commit -m "Initial commit: Gateway with MySQL and JPA"
    fi
    print_success "Git inicializado"

    # Verificar se tienen credenciales Git configuradas
    if [ -z "$(git config user.name)" ]; then
        print_warning "Git user.name não configurado"
        read -p "Digite seu nome para Git: " git_name
        git config user.name "$git_name"
    fi

    # Verificar se .env está em .gitignore
    if ! grep -q ".env" .gitignore 2>/dev/null; then
        print_warning ".env não está em .gitignore (adicionando agora)"
        echo ".env" >> .gitignore
        git add .gitignore
        git commit -m "Add .env to .gitignore"
    fi
    print_success ".env em .gitignore"

    # Verificar Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven não encontrado. Instale Maven antes de continuar."
        exit 1
    fi
    print_success "Maven instalado"

    # Verificar Java
    if ! command -v java &> /dev/null; then
        print_error "Java não encontrado. Instale Java 17+ antes de continuar."
        exit 1
    fi
    print_success "Java instalado"
}

# =================================================================
# Build Local
# =================================================================

build_local() {
    print_header "Compilando Projeto"

    if mvn clean install -DskipTests; then
        print_success "Build completo!"
    else
        print_error "Build falhou"
        exit 1
    fi
}

# =================================================================
# Build Docker
# =================================================================

build_docker() {
    print_header "Build Docker"

    if [ ! -f "Dockerfile" ]; then
        print_error "Dockerfile não encontrado"
        exit 1
    fi

    read -p "Nome da imagem (default: gateway): " image_name
    image_name=${image_name:-gateway}

    read -p "Tag da imagem (default: latest): " image_tag
    image_tag=${image_tag:-latest}

    if docker build -t "$image_name:$image_tag" .; then
        print_success "Imagem Docker construída: $image_name:$image_tag"

        # Testar imagem
        print_info "Testando imagem Docker..."
        docker run --rm "$image_name:$image_tag" java -version
        print_success "Imagem Docker testada com sucesso"
    else
        print_error "Falha ao construir imagem Docker"
        exit 1
    fi
}

# =================================================================
# Deploy Railway
# =================================================================

deploy_railway() {
    print_header "Deploy no Railway"

    print_info "1. Verifique se tem conta em railroad.app"
    print_info "2. Autentique com: railway login"
    print_info "3. Siga os passos no arquivo DEPLOYMENT.md"

    read -p "Tem Railway CLI instalado? (s/n): " railway_installed

    if [ "$railway_installed" = "s" ] || [ "$railway_installed" = "S" ]; then
        if command -v railway &> /dev/null; then
            print_success "Railway CLI encontrado"

            read -p "URL do GitHub (ex: https://github.com/usuario/juncao-gateway): " github_url

            if [ -z "$github_url" ]; then
                print_error "URL do GitHub é obrigatória"
                return
            fi

            print_info "Dashboard: https://railway.app/dashboard"
            print_info "Novo Projeto: https://railway.app/new"
            print_info "Selecione 'Deploy from GitHub repo'"
            print_info "URL: $github_url"
        else
            print_error "Railway CLI não encontrado"
            print_info "Instale com: npm install -g @railway/cli"
        fi
    else
        print_warning "Instale Railway CLI primeiro: npm install -g @railway/cli"
    fi
}

# =================================================================
# Deploy Render
# =================================================================

deploy_render() {
    print_header "Deploy no Render"

    print_info "1. Acesse render.com"
    print_info "2. Faça login com GitHub"
    print_info "3. Crie novo Web Service"
    print_info "4. Configure:"
    echo "   - Repository: seu repositório GitHub"
    echo "   - Build Command: ./mvnw clean package -DskipTests"
    echo "   - Start Command: java -jar target/gateway-1.0.0.jar"
    print_info "5. Adicione variáveis de ambiente"

    echo ""
    print_warning "Certifique-se de que o código está no GitHub antes!"
}

# =================================================================
# Deploy Google Cloud Run
# =================================================================

deploy_gcp() {
    print_header "Deploy no Google Cloud Run"

    print_info "1. Instale Google Cloud SDK"
    print_info "2. Execute: gcloud auth login"
    print_info "3. Crie um projeto: gcloud projects create juncao-gateway"
    print_info "4. Configure: gcloud config set project juncao-gateway"
    print_info "5. Ative APIs: gcloud services enable containerregistry.googleapis.com run.googleapis.com"

    if command -v gcloud &> /dev/null; then
        print_success "Google Cloud SDK encontrado"

        read -p "Proceder com deploy? (s/n): " proceed

        if [ "$proceed" = "s" ] || [ "$proceed" = "S" ]; then
            read -p "ID do Projeto GCP: " gcp_project

            if [ -z "$gcp_project" ]; then
                print_error "ID do projeto é obrigatório"
                return
            fi

            print_info "Fazendo build e push da imagem..."
            gcloud auth configure-docker

            docker build -t "gcr.io/$gcp_project/gateway:latest" .
            docker push "gcr.io/$gcp_project/gateway:latest"

            print_info "Fazendo deploy no Cloud Run..."
            gcloud run deploy gateway \
                --image "gcr.io/$gcp_project/gateway:latest" \
                --platform managed \
                --region us-central1 \
                --memory 512Mi \
                --allow-unauthenticated

            print_success "Deploy concluído!"
        fi
    else
        print_error "Google Cloud SDK não encontrado"
        print_info "Instale em: https://cloud.google.com/sdk/docs/install"
    fi
}

# =================================================================
# Setup Variáveis de Ambiente
# =================================================================

setup_env_vars() {
    print_header "Configurar Variáveis de Ambiente"

    if [ -f ".env" ]; then
        print_info "Arquivo .env já existe"
        read -p "Atualizar? (s/n): " update_env

        if [ "$update_env" != "s" ] && [ "$update_env" != "S" ]; then
            return
        fi
    fi

    # Gerar JWT Secret aleatório
    jwt_secret=$(openssl rand -base64 32)

    cat > .env << EOF
# =====================================================
# DATABASE MYSQL CONFIGURATION
# =====================================================
DB_HOST=localhost
DB_PORT=3306
DB_NAME=helppet_gateway
DB_USERNAME=root
DB_PASSWORD=root

# =====================================================
# JPA / HIBERNATE CONFIGURATION
# =====================================================
JPA_DIALECT=org.hibernate.dialect.MySQLDialect
JPA_HIBERNATE_DDL_AUTO=update
JPA_HIBERNATE_FORMAT_SQL=true
JPA_SHOW_SQL=false
JPA_HIBERNATE_USE_SQL_COMMENTS=true

# =====================================================
# JWT SECURITY
# =====================================================
JWT_SECRET=$jwt_secret
JWT_EXPIRATION=86400000

# =====================================================
# SERVER CONFIGURATION
# =====================================================
SERVER_PORT=8080
SPRING_APPLICATION_NAME=help-pet-gateway

# =====================================================
# REDIS CONFIGURATION (Rate Limiting)
# =====================================================
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# =====================================================
# CORS CONFIGURATION
# =====================================================
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200

# =====================================================
# RATE LIMITING CONFIGURATION
# =====================================================
RATE_LIMIT_REPLENISH_RATE=20
RATE_LIMIT_BURST_CAPACITY=40

# =====================================================
# MICROSERVICES URLS
# =====================================================
HELPPET_SERVICE_URL=http://localhost:8081

# =====================================================
# LOGGING CONFIGURATION
# =====================================================
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_HELPPET=DEBUG
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK=INFO
EOF

    print_success ".env criado com sucesso!"
    print_info "JWT_SECRET gerado: $jwt_secret"
    print_warning "MUDE AS SENHAS EM PRODUÇÃO!"
}

# =================================================================
# Push para GitHub
# =================================================================

push_github() {
    print_header "Push para GitHub"

    read -p "URL do repositório GitHub: " github_url

    if [ -z "$github_url" ]; then
        print_error "URL é obrigatória"
        return
    fi

    if ! git remote | grep -q "origin"; then
        git remote add origin "$github_url"
        print_success "Remote 'origin' adicionado"
    fi

    read -p "Mensagem de commit (default: Update deployment configuration): " commit_msg
    commit_msg=${commit_msg:-Update deployment configuration}

    git add .
    git commit -m "$commit_msg" || print_info "Nada para fazer commit"

    read -p "Branch (default: main): " branch
    branch=${branch:-main}

    if git push -u origin "$branch"; then
        print_success "Push para GitHub concluído!"
        print_info "URL: $github_url"
    else
        print_error "Falha ao fazer push"
    fi
}

# =================================================================
# Menu Principal
# =================================================================

show_menu() {
    echo ""
    print_header "HelpPet Gateway - Deploy Helper"
    echo ""
    echo "Escolha uma opção:"
    echo "1. Verificar Pré-requisitos"
    echo "2. Build Local (Maven)"
    echo "3. Build Docker"
    echo "4. Setup Variáveis de Ambiente (.env)"
    echo "5. Push para GitHub"
    echo "6. Deploy Railway (Recomendado)"
    echo "7. Deploy Render"
    echo "8. Deploy Google Cloud Run"
    echo "9. Deploy AWS Elastic Beanstalk"
    echo "0. Sair"
    echo ""
}

# =================================================================
# Main Loop
# =================================================================

main() {
    while true; do
        show_menu
        read -p "Opção: " choice

        case $choice in
            1) check_prerequisites ;;
            2) build_local ;;
            3) build_docker ;;
            4) setup_env_vars ;;
            5) push_github ;;
            6) deploy_railway ;;
            7) deploy_render ;;
            8) deploy_gcp ;;
            9)
                print_header "Deploy AWS Elastic Beanstalk"
                print_info "1. Instale AWS CLI: https://aws.amazon.com/cli/"
                print_info "2. Instale EB CLI: pip install awsebcli"
                print_info "3. Execute: eb init -p 'Java 17' gateway"
                print_info "4. Execute: eb create gateway-env"
                print_info "5. Execute: eb deploy"
                ;;
            0)
                print_success "Até logo!"
                exit 0
                ;;
            *)
                print_error "Opção inválida"
                ;;
        esac

        echo ""
        read -p "Pressione Enter para continuar..."
    done
}

# Executar
main

