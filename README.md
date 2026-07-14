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
- OpenAPI/Swagger UI documentation in three of the microservices (see [API documentation](#api-documentation-swagger--openapi))

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

```mermaid
flowchart TB
    Client(["Client"])
    Keycloak[/"Keycloak<br/>OAuth2 Provider :8081"/]
    Gateway["mscloudgateway<br/>API Gateway :8080"]
    Eureka[["eurekaserver<br/>Service Discovery :8761"]]
    Clientes["msclientes<br/>Customer Service"]
    Cartoes["mscartoes<br/>Card Service"]
    Avaliador["msavaliadorcredito<br/>Credit Evaluator"]
    Rabbit{{"RabbitMQ<br/>emissao-cartoes queue"}}

    Client -->|"HTTPS + JWT"| Gateway
    Gateway -.->|"validates JWT"| Keycloak
    Gateway -->|"lb://"| Clientes
    Gateway -->|"lb://"| Cartoes
    Gateway -->|"lb://"| Avaliador

    Clientes -.->|"registers"| Eureka
    Cartoes -.->|"registers"| Eureka
    Avaliador -.->|"registers"| Eureka
    Gateway -.->|"discovers"| Eureka

    Avaliador -->|"Feign (sync)"| Clientes
    Avaliador -->|"Feign (sync)"| Cartoes
    Avaliador ==>|"publish (async)"| Rabbit
    Rabbit ==>|"consume (async)"| Cartoes
```

Solid arrows = gateway routing / synchronous Feign calls · dotted arrows = Eureka registration/discovery · thick arrows = asynchronous RabbitMQ messaging.

The microservices use random ports and register themselves in Eureka. The gateway uses service discovery to route requests by service name.

### Sequence: credit evaluation + card issuance

The most representative flow in the project — combines the synchronous evaluation path with the asynchronous issuance path. Note that `limiteLiberado` is always recomputed by `msavaliadorcredito` before publishing to the queue, never accepted as-is from the request.

```mermaid
sequenceDiagram
    actor Client
    participant GW as mscloudgateway
    participant AV as msavaliadorcredito
    participant CL as msclientes
    participant CT as mscartoes
    participant MQ as RabbitMQ

    rect rgb(240, 248, 255)
    Note over Client,CT: Credit evaluation (synchronous)
    Client->>GW: POST /avaliacoes-credito (cpf, renda) + JWT
    GW->>AV: routes to msavaliadorcredito
    AV->>CL: GET /clientes?cpf= (Feign)
    CL-->>AV: DadosCliente (idade)
    AV->>CT: GET /cartoes?renda= (Feign)
    CT-->>AV: List Cartao (limiteBasico)
    AV->>AV: limiteAprovado = (idade / 10) * limiteBasico
    AV-->>Client: RetornoAvaliacaoCliente
    end

    rect rgb(255, 248, 240)
    Note over Client,MQ: Card issuance (async)
    Client->>GW: POST /avaliacoes-credito/solicitacoes-cartao + JWT
    GW->>AV: routes to msavaliadorcredito
    AV->>CL: GET /clientes?cpf= (Feign)
    CL-->>AV: DadosCliente (idade)
    AV->>CT: GET /cartoes/id (Feign)
    CT-->>AV: Cartao (limiteBasico)
    Note right of AV: limiteLiberado recomputed here,<br/>never trusted from the request
    AV-)MQ: publish DadosSolicitacaoEmissaoCartao
    AV-->>Client: ProtocoloSolicitacaoCartao
    MQ--)CT: consume message
    CT->>CT: persist ClienteCartao
    end
```

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

## API documentation (Swagger / OpenAPI)

`msclientes`, `mscartoes` and `msavaliadorcredito` each expose Swagger UI and the raw OpenAPI document (via `springdoc-openapi-ui`):

```txt
GET /swagger-ui/index.html
GET /v3/api-docs
```

Verified working directly: started `msclientes` standalone and confirmed both return real content (`swagger-ui/index.html` renders the actual Swagger UI page, `/v3/api-docs` returns a valid OpenAPI 3.0.1 document listing the service's real endpoints) — not just that the dependency is present.

Two things to know before trying it:

- These three services bind to a **random port** by default (`server.port: 0`), so there's no fixed URL to paste in a browser. Check the service's own startup log line (`Tomcat started on port(s): ...`) or look it up in the Eureka dashboard (`http://localhost:8761`, requires the `EUREKA_USER`/`EUREKA_PASSWORD` credentials described above) to find the actual port for a given run.
- **Not reachable through `docker compose up` as configured today** — those three services don't publish their ports to the host (see "Quick start with Docker Compose" above), so Swagger UI is only accessible when running a service directly with `mvn spring-boot:run` (or by overriding `server.port` and publishing it yourself).

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

Eureka's basic-auth credentials are externalized via `EUREKA_USER` / `EUREKA_PASSWORD` environment variables. The local-dev defaults (`cursoms-eureka-user` / `changeme-local-only`) let `mvn spring-boot:run` and `docker compose up` work out of the box — set both env vars explicitly for anything beyond local experimentation; don't treat the defaults as real credentials.

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

The realm this points at is exported at `keycloack/realm-export-curso.json` (note the directory is spelled "keycloack", not "keycloak" — a typo baked into the repo layout). If you're importing it manually into a standalone Keycloak instance rather than using `docker compose` (which does this automatically), use that specific file: `keycloack/realm-export.json` also exists but declares the realm as `"Mscourserealm"` (capital M) — importing it instead gives you a realm name that doesn't match the issuer-uri above, and the gateway won't be able to validate tokens against it.

---

## Running locally

### Quick start with Docker Compose

The fastest way to get the whole system running (RabbitMQ, Keycloak with the realm pre-imported, and all five services):

```bash
docker compose up -d --build
```

This was verified end-to-end: all four business services register with Eureka (check `http://cursoms-eureka-user:changeme-local-only@localhost:8761/eureka/apps`), Keycloak imports the `mscourserealm` realm from `keycloack/realm-export-curso.json` automatically, and the gateway (`http://localhost:8090`) enforces JWT auth as expected (a bare request to a protected endpoint gets `401` with `WWW-Authenticate: Bearer`).

Host ports, chosen to avoid colliding with other local services:

| Service | Host port |
|---|---|
| `eurekaserver` | 8761 |
| `rabbitmq` (AMQP / management UI) | 5672 / 15672 |
| `keycloak` | 8181 (mapped to the container's internal 8080) |
| `mscloudgateway` | 8090 (mapped to the container's internal 8080) |

`msclientes`, `mscartoes` and `msavaliadorcredito` bind to a random port *inside* their container (`server.port: 0`) and aren't published to the host — they're only reachable through the gateway or via Eureka-based service discovery, consistent with the [security model](#security-model) above.

Tear down with `docker compose down`.

Each module also has its own standalone `Dockerfile` if you need to build/run a single service's image directly (e.g. `docker build -t mscartoes ./mscartoes`) instead of the full compose stack. They're multi-stage builds (Maven build stage, `eclipse-temurin:11-jre` runtime stage) and run as a non-root user.

### Running without Docker

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

- Add centralized configuration with Spring Cloud Config
- Add distributed tracing with Zipkin/OpenTelemetry
- Add resilience patterns with Resilience4j
- Add integration tests between services
- Improve OpenAPI/Swagger documentation for each service
