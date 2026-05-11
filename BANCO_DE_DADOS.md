# Configuração do Banco de Dados MySQL

## Visão Geral

O gateway HelpPet agora está configurado para usar MySQL com:
- **JPA/Hibernate ORM** para mapeamento relacional de objetos
- **Connection Pool (HikariCP)** para gerenciamento eficiente de conexões
- **Variáveis de ambiente** via arquivo `.env`

---

## Arquivo .env

O arquivo `.env` contém todas as configurações necessárias:

### Banco de Dados
```env
DB_HOST=localhost           # Host do MySQL
DB_PORT=3306               # Porta padrão MySQL
DB_NAME=helppet_gateway    # Nome do banco de dados
DB_USERNAME=root           # Usuário MySQL
DB_PASSWORD=root           # Senha MySQL
```

### JPA/Hibernate
```env
JPA_DIALECT=org.hibernate.dialect.MySQLDialect
JPA_HIBERNATE_DDL_AUTO=update              # Cria/atualiza tabelas automaticamente
JPA_HIBERNATE_FORMAT_SQL=true              # Formata SQL para legibilidade
JPA_SHOW_SQL=false                         # Não mostra SQL no console (false em produção)
JPA_HIBERNATE_USE_SQL_COMMENTS=true        # Adiciona comentários SQL
```

---

## Requisitos Prévios

### 1. MySQL Instalado e Rodando

```bash
# Ubuntu/Debian
sudo apt-get install mysql-server

# archlinux
yay -S mysql

# macOS
brew install mysql

# Verificar se está rodando
mysql --version
```

### 2. Criar o Banco de Dados

```bash
# Conectar ao MySQL
mysql -u root -p

# Ou copie o SQL abaixo em um cliente MySQL (DBeaver, MySQL Workbench, etc)
```

```sql
-- Criar banco de dados
CREATE DATABASE IF NOT EXISTS helppet_gateway 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Criar usuário (opcional, já que usaremos root)
CREATE USER 'helppet'@'localhost' IDENTIFIED BY 'helppet123';
GRANT ALL PRIVILEGES ON helppet_gateway.* TO 'helppet'@'localhost';
FLUSH PRIVILEGES;

-- Usar o banco
USE helppet_gateway;
```

---

## Configuração do Application.yml

A configuração do Spring Boot está integrada com as variáveis do `.env`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/helppet_gateway?useSSL=false&serverTimezone=UTC
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20        # Conexões máximas
      minimum-idle: 5              # Conexões mínimas em espera
      connection-timeout: 30000    # Timeout de conexão (ms)
      idle-timeout: 600000         # Timeout de inatividade (ms)
      max-lifetime: 1800000        # Tempo máximo de vida (ms)

  jpa:
    database-platform: org.hibernate.dialect.MySQLDialect
    hibernate:
      ddl-auto: update             # Atualiza schema automaticamente
      format_sql: true             # Formata SQL
      use_sql_comments: true       # Adiciona comentários
    show-sql: false                # Não mostra SQL em produção
```

---

## Como Executar

### Opção 1: Com Docker Compose (Recomendado)

Crie um arquivo `docker-compose.yml`:

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: helppet-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: helppet_gateway
      MYSQL_CHARSET: utf8mb4
      MYSQL_COLLATION: utf8mb4_unicode_ci
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: helppet-redis
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  gateway:
    build: .
    container_name: helppet-gateway
    ports:
      - "8080:8080"
    environment:
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: helppet_gateway
      DB_USERNAME: root
      DB_PASSWORD: root
      REDIS_HOST: redis
      REDIS_PORT: 6379
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
    env_file:
      - .env

volumes:
  mysql_data:
```

**Executar:**

```bash
docker-compose up -d
```

---

### Opção 2: MySQL Local + Spring Boot

1. **Iniciar MySQL:**
```bash
# macOS
brew services start mysql

# Ubuntu/Debian
sudo service mysql start

# Ou manualmente
mysqld
```

2. **Criar banco de dados:**
```sql
CREATE DATABASE helppet_gateway CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. **Compilar e executar o gateway:**

```bash
cd /home/3DM/Documentos/backend/repositories/juncao/gateway

# Compilar
mvn clean install -DskipTests

# Executar
java -jar target/gateway-1.0.0.jar
```

**Ou com Maven:**

```bash
mvn spring-boot:run
```

---

## Variáveis de Ambiente

### Desenvolvimento (padrão)
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=helppet_gateway
export DB_USERNAME=root
export DB_PASSWORD=root
export REDIS_HOST=localhost
export REDIS_PORT=6379
export JWT_SECRET=dev-secret-key
```

### Teste/Staging
```bash
export DB_HOST=db.staging.local
export DB_PORT=3306
export DB_NAME=helppet_gateway_staging
export DB_USERNAME=staging_user
export DB_PASSWORD=secure_password_here
export JWT_SECRET=staging-secret-key-change-this
```

### Produção
```bash
export DB_HOST=prod-db.example.com
export DB_PORT=3306
export DB_NAME=helppet_gateway_prod
export DB_USERNAME=prod_user
export DB_PASSWORD=$(aws secretsmanager get-secret-value --secret-id db-password --query SecretString --output text)
export JWT_SECRET=$(aws secretsmanager get-secret-value --secret-id jwt-secret --query SecretString --output text)
```

---

## Verificar Conectividade

### Teste 1: Ping ao MySQL

```bash
mysql -h localhost -u root -proot -e "SELECT 1"
```

### Teste 2: Verificar Health Check do Gateway

```bash
curl http://localhost:8080/actuator/health/db
```

Resposta esperada:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    }
  }
}
```

### Teste 3: Logs da Aplicação

```bash
tail -f logs/gateway.log | grep -i "database\|hibernate\|mysql"
```

---

## Propriedades Hibernate Importantes

| Propriedade | Valor Recomendado | Descrição |
|---|---|---|
| `ddl-auto` | `update` (dev), `validate` (prod) | Estratégia de migração do schema |
| `format_sql` | `true` | Formata SQL para legibilidade |
| `show_sql` | `false` (prod) | Mostra SQL no console |
| `jdbc.batch_size` | `20` | Tamanho do batch INSERT/UPDATE |
| `dialect` | `MySQLDialect` | Dialeto do banco de dados |

---

## Troubleshooting

### Erro: "Connection refused"

```
java.sql.SQLException: jdbc:mysql://localhost:3306/helppet_gateway
```

**Solução:**
1. Verificar se MySQL está rodando: `mysql -u root -p`
2. Verificar credenciais no `.env`
3. Verificar se banco existe: `SHOW DATABASES;`

---

### Erro: "Access denied for user"

```
java.sql.SQLInvalidAuthorizationSpecException: Access denied for user 'root'@'localhost'
```

**Solução:**
1. Resetar senha MySQL: `mysqladmin -u root password root`
2. Atualizar `.env` com credenciais corretas
3. Reiniciar aplicação

---

### Erro: "No database selected"

```
java.sql.SQLSyntaxErrorException: No database selected
```

**Solução:**
1. Criar banco: `CREATE DATABASE helppet_gateway;`
2. Verificar se `DB_NAME` está correto no `.env`

---

## Monitoramento

### Ver Conexões Ativas

```sql
SELECT * FROM INFORMATION_SCHEMA.PROCESSLIST;
```

### Ver Status do Pool

```bash
# Via endpoint Actuator
curl http://localhost:8080/actuator/metrics/hikaricp.connections
```

```json
{
  "name": "hikaricp.connections",
  "measurements": [
    {"statistic": "VALUE", "value": 5}
  ]
}
```

---

## Próximos Passos

1. ✅ Configurar MySQL
2. ✅ Criar banco de dados
3. ✅ Configurar variáveis de ambiente (`.env`)
4. ✅ Compilar projeto: `mvn clean install`
5. ✅ Executar aplicação: `java -jar target/gateway-1.0.0.jar`
6. ✅ Testar conectividade: `curl http://localhost:8080/actuator/health`

---

## Referências

- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [Hibernate Documentation](https://hibernate.org/orm/documentation)
- [MySQL JDBC Driver](https://dev.mysql.com/doc/connector-j/en/)
- [HikariCP Documentation](https://github.com/brettwooldridge/HikariCP)

