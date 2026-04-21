# 🧠 Segundo Cérebro do Pregador - API

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![MySQL](https://img.shields.io/badge/mysql-%2300f.svg?style=for-the-badge&logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)

Uma API RESTful robusta desenvolvida para pastores e pregadores, focada na organização, estruturação e armazenamento inteligente de sermões e estudos bíblicos.

## 🚀 Sobre o Projeto

O **Segundo Cérebro do Pregador** é uma solução de Micro-SaaS que permite aos usuários gerenciar seu acervo homilético de forma isolada e segura. A plataforma utiliza inteligência na correlação de temas e referências bíblicas, garantindo que o conhecimento nunca seja perdido.

### 🛠 Funcionalidades Principais

- **Isolamento de Dados (Multi-tenancy):** Cada usuário possui seu próprio ambiente seguro.
- **Gestão de Temas:** Criação e categorização de temas personalizados.
- **Acervo de Sermões:** Notas ricas com suporte a texto longo, links de áudio e imagem.
- **Referências Bíblicas:** Sistema de indexação de passagens bíblicas por nota.
- **Autenticação JWT:** Segurança baseada em tokens para proteção de dados sensíveis.

## 🏗 Arquitetura & Tecnologias

- **Java 21** & **Spring Boot 3**
- **Spring Security** com **JWT** para autenticação.
- **Flyway** para versionamento de banco de dados.
- **MySQL 8** como banco de dados relacional.
- **Swagger/OpenAPI** para documentação interativa.
- **Docker & Docker Compose** para orquestração de ambiente.

## 🚦 Como Executar

### Pré-requisitos
- Docker & Docker Compose instalados.
- Java 21 (para desenvolvimento local fora do container).

### Passo a Passo

1. **Clone o repositório:**
   ```bash
   git clone [https://github.com/tiagosilva91/projetoTeste.git](https://github.com/tiagosilva91/projetoTeste.git)
   cd projetoTeste
