# ESTÁGIO 1: Build (Aproveitando o cache do Maven)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 1. Copiar apenas o pom.xml primeiro para baixar as dependências
# Isso acelera o build se o código mudar, mas as dependências não.
COPY pom.xml .
RUN mvn dependency:go-offline

# 2. Agora copiar o código fonte e buildar o projeto
COPY src ./src
RUN mvn clean package -DskipTests

# ESTÁGIO 2: Runtime (Imagem final leve e segura)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Criar um usuário não-root por segurança (Boa prática de arquitetura)
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Copiar apenas o JAR gerado no estágio de build
COPY --from=build /app/target/*.jar app.jar

# Variáveis de ambiente para ajuste de memória da JVM
# Java 21 já reconhece bem os limites de container, mas Xmx ajuda a evitar picos
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

EXPOSE 8080

# Executar a aplicação
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]