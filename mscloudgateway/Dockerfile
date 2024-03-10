# Estágio 1: Estágio de Construção
FROM maven:3.8.5-openjdk-11 as build

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia tod0 o conteúdo do diretório atual para o contêiner em /app
COPY . .

# Executa o Maven para construir a aplicação, ignorando os testes
RUN mvn clean package -DskipTests


# Estágio 2: Estágio Final
FROM openjdk:11

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia o arquivo JAR do estágio de construção para o diretório /app no contêiner final
COPY --from=build ./app/target/*.jar ./app.jar


ARG EUREKA_SERVER=localhost
ARG KEYCLOAK_SERVER=localhost
ARG KEYCLOAK_PORT=8081
# Especifica o comando a ser executado ao iniciar o contêiner
#ENTRYPOINT java -jar app.jar
ENTRYPOINT java -jar -Dspring.profiles.active=production app.jar