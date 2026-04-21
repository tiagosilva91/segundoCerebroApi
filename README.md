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
* **Gestão de Temas:** Criação e categorização de temas personalizados (ex: Graça, Família, Escatologia).
* **Acervo de Sermões:** Suporte a textos longos (Tipo `TEXT`), links de áudio e imagens.
* **Referências Bíblicas:** Sistema de indexação de passagens bíblicas vinculadas a cada sermão.
* **Segurança Avançada:** Autenticação via JWT com filtragem de contexto por usuário.

## 🏗 Arquitetura & Tecnologias

* **Java 21** & **Spring Boot 3**
* **Spring Security** com **JWT** para autenticação e autorização.
* **Flyway** para versionamento e migração do banco de dados.
* **MySQL 8** (executando via Docker).
* **Swagger/OpenAPI 3** para documentação interativa e testes de endpoint.
* **Lombok** para redução de boilerplate.
* **Docker & Docker Compose** para orquestração completa do ambiente.

## 🚦 Como Executar

### Pré-requisitos
* Docker & Docker Compose instalados.
* Git.

### Passo a Passo

1.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/tiagosilva91/projetoTeste.git](https://github.com/tiagosilva91/projetoTeste.git)
    cd projetoTeste
    ```

2.  **Suba os containers (API + Banco):**
    ```bash
    docker compose up -d --build
    ```

3.  **Acesse a documentação Interativa:**
    Com os containers rodando, acesse o Swagger UI para testar os endpoints:
    👉 [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## 📖 Endpoints Principais

| Categoria | Método | Endpoint | Descrição |
| :--- | :--- | :--- | :--- |
| **Auth** | POST | `/api/v1/auth/login` | Gera o Token JWT |
| **Auth** | GET | `/api/v1/auth/me` | Retorna dados da sessão atual |
| **Temas** | POST | `/api/v1/themes` | Cadastra um novo tema para o pastor |
| **Temas** | GET | `/api/v1/themes` | Lista temas do usuário logado |
| **Notas** | POST | `/api/v1/notes` | Salva um novo sermão/nota |
| **Notas** | GET | `/api/v1/notes` | Lista todos os sermões do usuário |

## 🔒 Segurança (CORS & JWT)

A API já está configurada para aceitar requisições de front-ends modernos (React/Vite) através de uma configuração de CORS robusta. Para testar rotas protegidas no Swagger:

1.  Faça login via `/auth/login`.
2.  Copie o token gerado.
3.  Clique no botão **Authorize** no topo do Swagger e cole o token.

---
Desenvolvido por [Tiago Silva](https://github.com/tiagosilva91) 🚀
