# Vaga+ Backend

API REST para análise de vagas com IA.

## Stack

- Java 21
- Spring Boot 3.4.1
- PostgreSQL
- JWT (jjwt)
- OAuth2 (Google, GitHub, LinkedIn)
- Resend (email)
- Flyway (migrations)

## Configuração

### 1. Variáveis de Ambiente

Copie `.env.example` para `.env` e preencha:

```bash
cp .env.example .env
```

### 2. Banco de Dados

Crie o banco PostgreSQL:

```sql
CREATE DATABASE vagamais;
```

### 3. OAuth Providers

Configure os providers em:
- Google: https://console.cloud.google.com/apis/credentials
- GitHub: https://github.com/settings/developers
- LinkedIn: https://www.linkedin.com/developers/apps/

### 4. Resend (Email)

Crie conta em https://resend.com e obtenha a API key.

## Executar

```bash
# Desenvolvimento
mvn spring-boot:run

# Build
mvn clean package -DskipTests

# Executar JAR
java -jar target/vagamais-backend-0.0.1-SNAPSHOT.jar
```

## Endpoints

### Auth

- `POST /api/auth/register` - Cadastro
- `POST /api/auth/login` - Login
- `POST /api/auth/confirm-email` - Confirmar email
- `POST /api/auth/resend-confirmation` - Reenviar confirmação
- `POST /api/auth/refresh` - Refresh token
- `GET /api/auth/oauth/google/callback` - OAuth Google
- `GET /api/auth/oauth/github/callback` - OAuth GitHub
- `GET /api/auth/oauth/linkedin/callback` - OAuth LinkedIn

### Áreas de Atuação

- `GET /api/areas-atuacao` - Listar áreas ativas

## Deploy

### Railway

1. Conecte o repositório
2. Configure as variáveis de ambiente
3. Deploy automático

### Vercel (Frontend)

Configure `NEXT_PUBLIC_API_URL` apontando para o backend.

## Estrutura

```
src/main/java/br/com/vagamais/
├── config/          # Security, JWT, CORS
├── controller/      # REST endpoints
├── service/         # Lógica de negócio
├── repository/      # JPA repositories
├── model/           # Entidades
├── dto/             # Request/Response DTOs
└── exception/       # Exception handlers
```

## Licença

MIT
