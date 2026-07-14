# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Microservices architecture study project (Java + Spring Cloud) simulating a credit-card evaluation ecosystem. It doubles as a portfolio piece for backend Java roles — code changes should keep that audience in mind (clean, idiomatic, no dead code).

Five Maven modules, each an independent Spring Boot application:

- `eurekaserver` — service discovery (Eureka Server), port 8761
- `mscloudgateway` — API Gateway (Spring Cloud Gateway, WebFlux), OAuth2/JWT resource server backed by Keycloak, port 8080
- `msclientes` — customer CRUD (H2, JPA)
- `mscartoes` — card catalog + card-per-customer records (H2, JPA); also a RabbitMQ consumer
- `msavaliadorcredito` — credit evaluation orchestrator; calls `msclientes`/`mscartoes` synchronously via Feign, and publishes card-issuance requests to RabbitMQ

## Build, test, run

Build the whole reactor (skip tests — see "Known gaps" below for why):
```
mvn -B -ntp package -DskipTests
```
Run a single module's tests:
```
mvn -pl <module> test
```
e.g. `mvn -pl msavaliadorcredito test`

Run a service locally:
```
cd <module>
mvn spring-boot:run
```
Startup order matters (each service registers with Eureka and/or depends on RabbitMQ/Keycloak): RabbitMQ → Keycloak → `eurekaserver` → `msclientes` → `mscartoes` → `msavaliadorcredito` → `mscloudgateway`.

CI (`.github/workflows/ci.yml`) only runs `package -DskipTests` on JDK 11 — it does not execute tests. `mscartoes` has a `@RabbitListener` that connects to a real broker at context startup, and the gateway's Keycloak `issuer-uri` may be fetched eagerly, so `contextLoads()` tests fail without those services running. Keep this in mind before wiring `mvn test` into CI — it needs RabbitMQ/Keycloak service containers or Testcontainers first.

Requires JDK 11 specifically — the Lombok version pinned by the Spring Boot 2.6.x/2.7.x parent does not support JDK 21 (confirmed: build fails under JDK 21). Use `.jdks/ms-11.0.31` or equivalent if multiple JDKs are installed locally.

A single Maven wrapper lives at the repo root (`./mvnw`, `./mvnw.cmd`) covering the whole reactor — there are no per-module wrappers.

## Architecture

- **Sync path**: `msavaliadorcredito` → Feign clients (`ClienteResourceClient`, `CartoeesResourceClient`) → `msclientes` / `mscartoes`, resolved via Eureka service discovery (`lb://` in the gateway, service-name-based in Feign `@FeignClient(value = "...")`).
- **Async path**: `msavaliadorcredito` publishes to the `emissao-cartoes` RabbitMQ queue (`SolicitacaoEmissaoCartaoPublisher`); `mscartoes` consumes it (`EmissaoCartaoSubscriber`) and persists the resulting `ClienteCartao`. Only the publisher side declares the `Queue` bean — the consumer side does not (a known gap, tracked as a GitHub issue).
- **Gateway routing**: `mscloudgateway` uses Spring Cloud Gateway's discovery locator plus explicit routes in `MscloudgatewayApplication#routes()`, all pointing at `lb://<eureka-service-name>`.
- **Security boundary**: only the gateway validates JWTs (OAuth2 resource server against Keycloak, `issuer-uri` in `mscloudgateway/src/main/resources/application.yml`). The three business services (`msclientes`, `mscartoes`, `msavaliadorcredito`) have no Spring Security dependency at all — the trust model assumes they're never reachable directly, only through the gateway. Don't add auth logic to those services piecemeal; if that changes, it's an explicit architectural decision (resource server config in every service), not a small patch.
- **DTOs are duplicated per module on purpose** (e.g. `Cartao` exists separately in `mscartoes` and as a client-side shape in `msavaliadorcredito.infra.clients`). This is normal in a microservices layout — don't introduce a shared model/library module to "deduplicate" it.
- Each service uses its own file-based logging (`./logs/log-file.log`) and an in-memory H2 instance where applicable — data does not survive restarts and is not shared across multiple instances of the same service.

## Known gaps (don't rediscover — check open GitHub issues first)

The repo has an active backlog of tracked issues from a full architecture/security review (issues #3–#19 on GitHub) covering: Spring Boot version drift between modules (each module currently pins a different 2.6.x/2.7.0 parent version independent of the root `pom.xml`), missing Bean Validation on all DTOs, a RabbitMQ consumer that can loop forever on a bad message, no `@RestControllerAdvice` anywhere (Feign error messages leak to HTTP responses), Dockerfiles with `ARG` instead of `ENV` for runtime variables, and zero real test coverage (every module has only an empty `contextLoads()`). Before proposing a fix in one of these areas, check whether it's already an open issue or in-flight PR.
