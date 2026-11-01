# 🧠 Segundo Cérebro do Pregador - API

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![MySQL](https://img.shields.io/badge/mysql-%2300f.svg?style=for-the-badge&logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)

Uma API RESTful robusta desenvolvida para pastores e pregadores, focada na organização, estruturação e armazenamento inteligente de sermões e estudos bíblicos.

## 🚀 Sobre o Projeto

O **Segundo Cérebro do Pregador** é uma solução de Micro-SaaS que permite aos usuários gerenciar seu acervo homilético de forma isolada e segura. A plataforma utiliza inteligência na correlação de temas e referências bíblicas, garantindo que o conhecimento e a inspiração nunca sejam perdidos.

### 🛠 Funcionalidades Principais

* **Isolamento de Dados (Multi-tenancy):** Cada usuário possui seu próprio ambiente seguro. Um pastor nunca acessa as notas de outro.
* **Gestão de Planos (FREE & PRO):** Sistema de assinaturas com limites diferenciados. O plano FREE permite **5 notas e 3 temas** (configurável em `PLAN_FREE_MAX_NOTES` e `PLAN_FREE_MAX_THEMES`); o PRO é ilimitado.
* **Fluxo de Onboarding:** Sistema de primeiro acesso com reset de senha obrigatório para maior segurança.
* **Gestão de Temas:** Criação e categorização de temas personalizados (ex: Graça, Família, Escatologia).
* **Acervo de Sermões:** Suporte a textos longos (Tipo `TEXT`), links de áudio e imagens.
* **Referências Bíblicas:** Sistema de indexação de passagens bíblicas vinculadas a cada sermão.
* **Segurança Avançada:** Autenticação via JWT com filtragem de contexto por usuário.

## 🏗 Arquitetura & Tecnologias

* **Java 21** & **Spring Boot 3**
* **Spring Security** com **JWT** para autenticação e autorização.
* **Flyway** para versionamento e migração do banco de dados.
* **MySQL 8** (executando via Docker ou Railway em PRD).
* **Swagger/OpenAPI 3** para documentação interativa e testes de endpoint.
* **Lombok** para redução de boilerplate.
* **Docker & Docker Compose** para orquestração completa do ambiente local.

## 🚦 Como Executar

### Pré-requisitos
* Docker & Docker Compose instalados.
* Git.

### Passo a Passo

1.  **Clone o repositório:**
    ```bash
    git clone https://github.com/tiagosilva91/segundoCerebroApi.git
    cd segundoCerebroApi
    ```

2.  **Suba os containers (API + Banco):**
    ```bash
    docker compose up -d --build
    ```

3.  **Acesse a documentação Interativa:**
    Com os containers rodando, acesse o Swagger UI para testar os endpoints:
    👉 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

    O Swagger só fica público com `API_DOCS_PUBLIC=true` — o `docker-compose.yml`
    já define isso para desenvolvimento. Em produção o padrão é `false`.

### Sem Docker (MySQL local)

Quando não há daemon Docker disponível:

```bash
# MySQL 8 com a collation que as migrations assumem
mysqld --user=mysql --character-set-server=utf8mb4 \
       --collation-server=utf8mb4_0900_ai_ci
mysql -e "CREATE DATABASE IF NOT EXISTS segundocerebro_db \
          CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"

mvn -DskipTests package
PAYMENT_ALLOW_SIMULATED_UPGRADE=true API_DOCS_PUBLIC=true \
  java -jar target/segundoCerebroApi-0.0.1-SNAPSHOT.jar
```

Os testes de integração precisam do mesmo MySQL e usam o schema
`segundocerebro_test`, separado do de desenvolvimento:

```bash
mvn test
```

## 📖 Endpoints Principais

| Categoria | Método | Endpoint | Descrição |
| :--- | :--- | :--- | :--- |
| **Auth** | POST | `/api/v1/auth/login` | Gera o Token JWT e indica se é primeiro acesso |
| **Auth** | POST | `/api/v1/auth/first-access-password` | Altera senha e finaliza onboarding |
| **Auth** | GET | `/api/v1/auth/me` | Retorna dados da sessão atual |
| **Temas** | POST | `/api/v1/themes` | Cadastra um novo tema para o pastor |
| **Temas** | GET | `/api/v1/themes` | Lista temas do usuário logado |
| **Notas** | POST | `/api/v1/notes` | Salva um novo sermão (valida limite do plano) |
| **Notas** | GET | `/api/v1/notes` | Lista todos os sermões do usuário |
| **Usuários** | POST | `/api/v1/users` | Cadastro público de novos usuários |
| **Auth** | POST | `/api/v1/auth/change-password` | Troca a senha do usuário autenticado |
| **Auth** | POST | `/api/v1/auth/forgot-password` | Envia e-mail com token de recuperação |
| **Auth** | POST | `/api/v1/auth/reset-password` | Redefine a senha a partir do token |
| **Temas** | PUT / DELETE | `/api/v1/themes/{id}` | Edita ou remove um tema |
| **Notas** | GET | `/api/v1/notes/{id}` | Detalha um sermão |
| **Notas** | PUT / DELETE | `/api/v1/notes/{id}` | Edita ou remove um sermão |
| **Plano** | GET | `/api/v1/plan/usage` | Uso e limites do plano (limite `null` = ilimitado) |
| **Pagamento** | POST | `/api/v1/payment/checkout` | Inicia o checkout do plano PRO |
| **Pagamento** | POST | `/api/v1/payment/webhook` | Recebe a confirmação do gateway |
| **Pagamento** | POST | `/api/v1/payment/simulate-upgrade` | Upgrade imediato (apenas em dev) |
| **Busca** | GET | `/api/v1/search?q=` | Busca combinada em notas e no texto bíblico |
| **Bíblia** | GET | `/api/v1/bible/translations` | Versões disponíveis |
| **Bíblia** | GET | `/api/v1/bible/books` | Livros, com total de capítulos |
| **Bíblia** | GET | `/api/v1/bible/passages` | Capítulo ou intervalo de versículos |
| **Bíblia** | GET | `/api/v1/bible/reference?ref=` | Passagem por referência ("João 3:16") |
| **Bíblia** | GET | `/api/v1/bible/search?q=` | Busca no texto bíblico |
| **Usuários** | GET/PUT/DELETE | `/api/v1/users/**` | Gestão de usuários (exige ROLE_ADMIN) |

> A lista completa e sempre atualizada está no Swagger (`/swagger-ui.html`).

## ⚙️ Variáveis de Ambiente

Nenhuma é obrigatória em desenvolvimento: todas têm padrão em
`application.properties`. Em produção, `JWT_SECRET` é indispensável.

| Variável | Padrão | Descrição |
| :--- | :--- | :--- |
| `JWT_SECRET` | placeholder versionado | **Troque em produção.** Gere com `openssl rand -base64 32`. |
| `MYSQLHOST` / `MYSQLPORT` / `MYSQLDATABASE` / `MYSQLUSER` / `MYSQLPASSWORD` | `localhost` / `3306` / `segundocerebro_db` / `root` / `root` | Conexão com o banco (a Railway injeta automaticamente). |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:5173` | Origens liberadas, separadas por vírgula. Sem curinga. |
| `FRONTEND_URL` | `http://localhost:3000` | Base dos links de e-mail e do retorno do pagamento. |
| `API_DOCS_PUBLIC` | `false` | Libera o Swagger sem autenticação. |
| `PLAN_FREE_MAX_NOTES` / `PLAN_FREE_MAX_THEMES` | `5` / `3` | Limites do plano FREE. |
| `SMTP_HOST` / `SMTP_PORT` / `SMTP_USERNAME` / `SMTP_PASSWORD` | Gmail / `587` / vazio | Envio do e-mail de recuperação de senha. |
| `MAIL_FROM` | `SMTP_USERNAME` | Remetente dos e-mails. |
| `MAIL_LOG_RESET_TOKEN` | `false` | **Só desenvolvimento.** Imprime o token de recuperação no log quando o SMTP falha. |
| `PAYMENT_WEBHOOK_SECRET` | vazio | Segredo conferido no header `X-Webhook-Secret`. Sem ele o webhook rejeita tudo. |
| `PAYMENT_ALLOW_SIMULATED_UPGRADE` | `false` | Libera `POST /payment/simulate-upgrade`. **Nunca `true` em produção.** |
| `PAYMENT_PRO_PRICE_CENTS` | `1990` | Preço do plano PRO, em centavos. |
| `RATE_LIMIT_LOGIN_ATTEMPTS` / `RATE_LIMIT_LOGIN_WINDOW` | `10` / `5` min | Tentativas de login **malsucedidas** por IP. |
| `RATE_LIMIT_FORGOT_ATTEMPTS` / `RATE_LIMIT_FORGOT_WINDOW` | `3` / `15` min | Pedidos de recuperação por IP. |
| `BIBLE_API_URL` / `BIBLE_DEFAULT_TRANSLATION` | `https://bolls.life` / `ACF11` | Provedor do texto bíblico. |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` / `ADMIN_CPF` | vazio | Cria (ou promove) um administrador no boot — veja abaixo. |

## 👤 Criando um administrador

As rotas `GET/PUT/DELETE /api/v1/users/**` exigem `ROLE_ADMIN`, mas toda conta
nasce como `USER` e não há endpoint para promover ninguém. Para ter um admin,
defina as variáveis e reinicie a aplicação:

```bash
ADMIN_EMAIL=admin@exemplo.com ADMIN_PASSWORD=trocar-no-primeiro-acesso \
ADMIN_CPF=00000000000 java -jar target/segundoCerebroApi-0.0.1-SNAPSHOT.jar
```

Se o e-mail já pertencer a uma conta, ela é apenas promovida (a senha não é
alterada). Numa conta nova, `firstLogin` vem `true`, então a senha de bootstrap
precisa ser trocada no primeiro acesso.

## 🔒 Segurança (CORS & JWT)

A API já está configurada para aceitar requisições de front-ends modernos (React/Vite) através de uma configuração de CORS robusta. Em produção, a API suporta HTTPS via proxy reverso.

Para testar rotas protegidas no Swagger:

1.  Faça login via `/auth/login`.
2.  Copie o token gerado.
3.  Clique no botão **Authorize** no topo do Swagger e cole o token.

---
Desenvolvido por [Tiago Silva](https://github.com/tiagosilva91) 🚀
