# Spring Cloud Microservices

![Java](https://img.shields.io/badge/Java-11-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen?style=for-the-badge&logo=springboot)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2021.0.9-brightgreen?style=for-the-badge&logo=spring)
![Eureka](https://img.shields.io/badge/Eureka-Service%20Discovery-blue?style=for-the-badge)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Messaging-orange?style=for-the-badge&logo=rabbitmq)
![Keycloak](https://img.shields.io/badge/Keycloak-Auth-blue?style=for-the-badge&logo=keycloak)
![Maven](https://img.shields.io/badge/Maven-Build-red?style=for-the-badge&logo=apachemaven)

Microservices architecture study using Java, Spring Boot, Spring Cloud, Eureka, API Gateway, RabbitMQ and Keycloak.

The project simulates a credit card evaluation ecosystem, with independent services for customers, cards, credit evaluation, service discovery and gateway routing.

---

## Overview

This repository demonstrates common patterns used in distributed systems:

- Service discovery with Eureka Server
- API Gateway with Spring Cloud Gateway
- Dynamic service registration
- Synchronous communication between microservices
- Asynchronous communication with RabbitMQ
- OAuth2/JWT resource server configuration with Keycloak
- Actuator endpoints for observability
- H2 database for local persistence in services
- OpenAPI dependency support in microservices

---

## Services

| Service | Description | Main responsibility |
|---|---|---|
| `eurekaserver` | Service discovery server | Registers and exposes available microservices |
| `mscloudgateway` | API Gateway | Routes requests to services discovered by Eureka |
| `msclientes` | Customer service | Creates and retrieves customers by CPF |
| `mscartoes` | Card service | Creates cards, lists cards by income and customer CPF |
| `msavaliadorcredito` | Credit evaluator | Evaluates customer credit and requests card issuance |

---

## Architecture

```txt
Client
  |
  v
API Gateway - mscloudgateway :8080
  |
  v
Eureka Server - eurekaserver :8761
  |
  |-- msclientes
  |-- mscartoes
  |-- msavaliadorcredito

RabbitMQ
  |
  |-- asynchronous card issuing queue: emissao-cartoes

Keycloak
  |
  |-- OAuth2/JWT issuer for gateway security
```

The microservices use random ports and register themselves in Eureka. The gateway uses service discovery to route requests by service name.

---

## Security model

Only `mscloudgateway` validates JWTs (OAuth2 resource server backed by Keycloak). `msclientes`, `mscartoes` and `msavaliadorcredito` have no authentication layer of their own — they trust that every request they receive already went through the gateway.

This is a deliberate trade-off for a study project, not an oversight: it assumes the three business services are never reachable directly from outside the deployment (e.g. isolated on a private Docker network with only the gateway's port published), so the gateway is the sole point where JWTs are checked. If a service is exposed directly — as it is by default when running everything with plain `mvn spring-boot:run` on `localhost` — that service accepts unauthenticated requests. Don't rely on this setup as-is for anything beyond local experimentation.

---

## Tech stack

- Java 11
- Spring Boot 2.7.18
- Spring Cloud 2021.0.9
- Spring Web
- Spring Data JPA
- Spring Cloud Netflix Eureka
- Spring Cloud Gateway
- Spring Security OAuth2 Resource Server
- RabbitMQ
- Keycloak
- H2 Database
- Spring Boot Actuator
- Maven
- Lombok

---

## Main endpoints

### Customer service - `msclientes`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/clientes` | Creates a customer |
| GET | `/clientes?cpf={cpf}` | Finds customer data by CPF |

### Card service - `mscartoes`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/cartoes` | Creates a card |
| GET | `/cartoes?renda={renda}` | Lists cards available for an income value |
| GET | `/cartoes?cpf={cpf}` | Lists cards linked to a customer CPF |

### Credit evaluator service - `msavaliadorcredito`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/avaliacoes-credito/situacao-cliente?cpf={cpf}` | Gets the customer credit situation |
| POST | `/avaliacoes-credito` | Evaluates customer credit based on CPF and income |
| POST | `/avaliacoes-credito/solicitacoes-cartao` | Requests card issuance through messaging |

---

## Example requests

### Create customer

```http
POST /clientes
Content-Type: application/json
```

```json
{
  "cpf": "12345678900",
  "nome": "Vinicius Santos",
  "idade": 25
}
```

### Evaluate credit

```http
POST /avaliacoes-credito
Content-Type: application/json
```

```json
{
  "cpf": "12345678900",
  "renda": 5000
}
```

### Request card issuance

```http
POST /avaliacoes-credito/solicitacoes-cartao
Content-Type: application/json
```

```json
{
  "idCartao": 1,
  "cpf": "12345678900",
  "endereco": "São Paulo - SP"
}
```

---

## Service configuration

### Eureka Server

```txt
http://localhost:8761
```

Default Eureka credentials configured in the project:

```txt
username: cursoms-eureka-user
password: ecok-usr-eeuramssur
```

### Gateway

```txt
http://localhost:8080
```

The gateway is configured with discovery locator enabled, allowing routes based on registered service names.

### RabbitMQ

Default local configuration:

```txt
host: localhost
port: 5672
username: guest
password: guest
queue: emissao-cartoes
```

### Keycloak

Gateway resource server issuer URI:

```txt
http://localhost:8081/realms/mscourserealm
```

---

## Running locally

### Requirements

- Java 11+
- Maven
- RabbitMQ
- Keycloak

### Suggested startup order

1. Start RabbitMQ
2. Start Keycloak
3. Start `eurekaserver`
4. Start `msclientes`
5. Start `mscartoes`
6. Start `msavaliadorcredito`
7. Start `mscloudgateway`

### Start a service

Enter the service directory and run:

```bash
mvn spring-boot:run
```

Example:

```bash
cd eurekaserver
mvn spring-boot:run
```

---

## Project structure

```txt
eurekaserver/
mscloudgateway/
msclientes/
mscartoes/
msavaliadorcredito/
```

Each service has its own Spring Boot application, configuration file and Maven build.

---

## What this project demonstrates

- Microservices decomposition by business capability
- Service discovery and dynamic routing
- Gateway pattern
- Credit evaluation flow across multiple services
- Messaging-based card issuance request
- OAuth2/JWT integration at the gateway level
- Local development with independent services

---

## Modernization plan (Java 21 / Spring Boot 3.x)

The stack currently targets Java 11 and Spring Boot 2.7.x/Spring Cloud 2021.0.x. Both are viable choices for a while longer, and this project deliberately migrates last, after the basics are solid — a migration is much safer once there's real test coverage to catch regressions, so it follows test coverage and CI improvements rather than preceding them.

**Why not now:** the Lombok version pinned by the Spring Boot 2.6.x/2.7.x parent does not support JDK 21 — confirmed by a direct build failure when compiling this project with JDK 21 installed as the active JDK. Migrating the JDK alone, without also moving to Spring Boot 3.x, isn't an option.

**What the Boot 2.7 → 3.x jump actually breaks here** (mapped against this codebase, not generic migration notes):

- `javax.persistence` → `jakarta.persistence` in every JPA entity (`Cliente`, `Cartao`, `ClienteCartao`, and others using `javax.persistence.*` imports).
- `WebSecurityConfigurerAdapter` (used in `eurekaserver`'s `SecurityConfig`) is removed in Spring Security 6; needs rewriting as a `SecurityFilterChain` bean.
- `springdoc-openapi-ui` 1.6.8 → the 2.x line (different artifact id and auto-configuration).
- `spring-cloud-dependencies` 2021.0.x → the 2023.x train, which is what pairs with Spring Boot 3.x.

**Suggested sequence:** align Spring Boot/Cloud versions across modules (done), fix runtime bugs, add input validation and error handling (done), grow real test coverage, then migrate. With a small codebase like this one and tests in place beforehand, the actual migration is estimated at 1-2 days; attempting it before there's test coverage to lean on would make regressions much harder to catch.

---

## Next improvements

- Add Docker Compose for Eureka, RabbitMQ, Keycloak and all services
- Add centralized configuration with Spring Cloud Config
- Add distributed tracing with Zipkin/OpenTelemetry
- Add resilience patterns with Resilience4j
- Add integration tests between services
- Add GitHub Actions pipeline
- Improve OpenAPI/Swagger documentation for each service
