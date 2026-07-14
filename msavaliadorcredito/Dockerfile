# Estágio 1: Estágio de Construção
FROM maven:3.8.5-openjdk-11 as build

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia tod0 o conteúdo do diretório atual para o contêiner em /app
COPY . .

# Executa o Maven para construir a aplicação, ignorando os testes
RUN mvn clean package -DskipTests


# Estágio 2: Estágio Final
FROM eclipse-temurin:11-jre

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia o arquivo JAR do estágio de construção para o diretório /app no contêiner final
COPY --from=build ./app/target/*.jar ./app.jar

# ENV (não ARG): precisa sobreviver no container em execução, não só no build
ENV RABBITMQ_SERVER=rabbitmq-host
ENV EUREKA_SERVER=localhost

# Roda como usuário não-root
RUN useradd --uid 10001 --no-create-home appuser && chown appuser:appuser /app/app.jar
USER appuser

# Especifica o comando a ser executado ao iniciar o contêiner
#ENTRYPOINT java -jar app.jar
ENTRYPOINT java -jar -Dspring.profiles.active=production app.jar