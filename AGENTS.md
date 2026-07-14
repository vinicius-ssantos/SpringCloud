# AGENTS.md

Guidance for AI coding agents (Codex, Cursor, etc.) working in this repository. See `CLAUDE.md` for the Claude Code-specific version — the two should stay in sync.

## Project overview

Java/Spring Cloud microservices study project simulating a credit-card evaluation ecosystem: `eurekaserver` (discovery), `mscloudgateway` (API Gateway + OAuth2/Keycloak), `msclientes`, `mscartoes` (RabbitMQ consumer), `msavaliadorcredito` (Feign + RabbitMQ publisher). Maven multi-module reactor, one Spring Boot app per module. It also serves as a portfolio piece for backend Java job applications — keep that bar in mind: no leftover dead code, no accidental imports, no half-finished changes.

## Setup / environment

- JDK **11** required. The Lombok version pinned by the Spring Boot 2.6.x/2.7.x parent breaks under JDK 21 — confirmed by direct build failure. If multiple JDKs are installed, select the JDK 11 one explicitly (`JAVA_HOME`) before running Maven.
- A single Maven wrapper lives at the repo root (`./mvnw`, `./mvnw.cmd`) covering the whole reactor — there are no per-module wrappers anymore. Run it from the root, e.g. `./mvnw -pl mscartoes -am test`.

## Build & test

```
mvn -B -ntp package -DskipTests   # full reactor build
mvn -pl <module> test             # tests for one module, e.g. mvn -pl mscartoes test
```

Tests are **not** executed in CI today (`.github/workflows/ci.yml` only runs `package -DskipTests`), because `mscartoes` opens a real RabbitMQ connection at context startup and the gateway may eagerly hit Keycloak's OIDC discovery — neither is available in the CI job. Every module's test suite is currently just an empty `contextLoads()`. If you add real tests that need RabbitMQ/Keycloak, they will not run in CI until that infrastructure is added (Testcontainers or service containers) — say so explicitly rather than assuming green CI means the new tests ran.

## Code style

- Lombok (`@Data`, `@RequiredArgsConstructor`, `@Getter`) is the norm for DTOs/entities/services — follow it rather than writing manual boilerplate.
- DTOs are intentionally duplicated per module (e.g. a "Cartao" shape exists both as a JPA entity in `mscartoes` and as a plain client-side POJO in `msavaliadorcredito`). This is the correct pattern for microservices — do not introduce a shared model module to deduplicate it.
- Package layout per module follows `application` (controllers/services/DTOs), `domain` (entities), `infra` (repositories, Feign clients, messaging) — match it for new code.

## Architecture notes an agent needs before touching cross-service code

- Sync inter-service calls go through Feign + Eureka service discovery (`@FeignClient(value = "<eureka-service-name>")`), not hardcoded URLs.
- Async card issuance goes through the RabbitMQ queue `emissao-cartoes`: `msavaliadorcredito` publishes, `mscartoes` consumes. Only the publisher currently declares the `Queue` bean.
- Only `mscloudgateway` validates JWTs (OAuth2 resource server / Keycloak). `msclientes`, `mscartoes`, `msavaliadorcredito` have no security layer at all — this is a deliberate (if debatable) trust-boundary decision, not an oversight to "fix" opportunistically inside an unrelated change.

## Before opening a PR

- Check open GitHub issues first — this repo has a tracked backlog (issues #3–#19) from a full review covering version drift across module `pom.xml`s, missing input validation, RabbitMQ error handling, Dockerfile issues, and test coverage. Don't re-report or re-fix something already tracked without checking.
- Run the reactor build (`mvn package -DskipTests`) before committing — there is no local pre-commit hook enforcing this.
- Reference the issue number being addressed in the PR description.
