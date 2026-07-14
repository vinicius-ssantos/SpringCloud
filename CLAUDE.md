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

Build and test the whole reactor:
```
mvn -B -ntp test
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

Or run the whole stack via `docker compose up -d --build` (`docker-compose.yml` at the repo root) — RabbitMQ, Keycloak (realm auto-imported from `keycloack/realm-export-curso.json` — note the lowercase "mscourserealm", not `realm-export.json`, which has a differently-cased realm name that doesn't match the gateway's issuer-uri), and all five services. Verified working end-to-end. Host ports: eurekaserver 8761, rabbitmq 5672/15672, keycloak 8181, gateway 8090 (chosen to dodge collisions with unrelated local services — internal container ports are the defaults).

CI (`.github/workflows/ci.yml`) runs `mvn test` on JDK 11 — tests execute for real, no `-DskipTests`. The earlier assumption that `mscartoes`' `@RabbitListener` and the gateway's Keycloak `issuer-uri` lookup would block context startup without a real broker/IdP turned out to be wrong: Spring AMQP's listener container retries the broker connection asynchronously instead of failing context refresh, and the resource server's issuer lookup doesn't block startup either. Verified both locally (no RabbitMQ/Keycloak running anywhere) and on GitHub's runner.

Requires JDK 11 specifically — the Lombok version pinned by the Spring Boot 2.6.x/2.7.x parent does not support JDK 21 (confirmed: build fails under JDK 21). Use `.jdks/ms-11.0.31` or equivalent if multiple JDKs are installed locally.

A single Maven wrapper lives at the repo root (`./mvnw`, `./mvnw.cmd`) covering the whole reactor — there are no per-module wrappers.

## Architecture

- **Sync path**: `msavaliadorcredito` → Feign clients (`ClienteResourceClient`, `CartoeesResourceClient`) → `msclientes` / `mscartoes`, resolved via Eureka service discovery (`lb://` in the gateway, service-name-based in Feign `@FeignClient(value = "...")`).
- **Async path**: `msavaliadorcredito` publishes to the `emissao-cartoes` RabbitMQ queue (`SolicitacaoEmissaoCartaoPublisher`); `mscartoes` consumes it (`EmissaoCartaoSubscriber`) and persists the resulting `ClienteCartao`. Only the publisher side declares the `Queue` bean — the consumer side does not (a known gap, tracked as a GitHub issue).
- **Gateway routing**: `mscloudgateway` uses Spring Cloud Gateway's discovery locator plus explicit routes in `MscloudgatewayApplication#routes()`, all pointing at `lb://<eureka-service-name>`.
- **Security boundary**: only the gateway validates JWTs (OAuth2 resource server against Keycloak, `issuer-uri` in `mscloudgateway/src/main/resources/application.yml`). The three business services (`msclientes`, `mscartoes`, `msavaliadorcredito`) have no Spring Security dependency at all — the trust model assumes they're never reachable directly, only through the gateway. Don't add auth logic to those services piecemeal; if that changes, it's an explicit architectural decision (resource server config in every service), not a small patch.
- **DTOs are duplicated per module on purpose** (e.g. `Cartao` exists separately in `mscartoes` and as a client-side shape in `msavaliadorcredito.infra.clients`). This is normal in a microservices layout — don't introduce a shared model/library module to "deduplicate" it.
- Each service uses its own file-based logging (`./logs/log-file.log`) and an in-memory H2 instance where applicable — data does not survive restarts and is not shared across multiple instances of the same service.

## Known gaps

The full architecture/security review that produced issues #3–#19 has been fully resolved (all merged and closed). Remaining test coverage is still thin: `msavaliadorcredito` and `mscartoes` have real Mockito-based unit tests (`AvaliadorCreditoServiceTest`, `EmissaoCartaoSubscriberTest`) covering the credit-limit rule and the RabbitMQ consumer; every other module still only has an empty `contextLoads()`. Broader coverage (`@WebMvcTest` for controllers, `@DataJpaTest` for repositories) is a reasonable next step, not yet done.

Dependabot is configured (`.github/dependabot.yml`) and will propose dependency bumps, including occasional major-version jumps that skip straight to Spring Boot 3.x/4.x-era artifacts (already seen once) — don't merge those without a real migration effort; see "Modernization plan" in the README.
