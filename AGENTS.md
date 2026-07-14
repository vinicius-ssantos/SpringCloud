# AGENTS.md

Guidance for AI coding agents (Codex, Cursor, etc.) working in this repository. See `CLAUDE.md` for the Claude Code-specific version — the two should stay in sync.

## Project overview

Java/Spring Cloud microservices study project simulating a credit-card evaluation ecosystem: `eurekaserver` (discovery), `mscloudgateway` (API Gateway + OAuth2/Keycloak), `msclientes`, `mscartoes` (RabbitMQ consumer), `msavaliadorcredito` (Feign + RabbitMQ publisher). Maven multi-module reactor, one Spring Boot app per module. It also serves as a portfolio piece for backend Java job applications — keep that bar in mind: no leftover dead code, no accidental imports, no half-finished changes.

## Setup / environment

- JDK **11** required. The Lombok version pinned by the Spring Boot 2.6.x/2.7.x parent breaks under JDK 21 — confirmed by direct build failure. If multiple JDKs are installed, select the JDK 11 one explicitly (`JAVA_HOME`) before running Maven.
- A single Maven wrapper lives at the repo root (`./mvnw`, `./mvnw.cmd`) covering the whole reactor — there are no per-module wrappers anymore. Run it from the root, e.g. `./mvnw -pl mscartoes -am test`.

## Build & test

```
mvn -B -ntp test                  # full reactor build + test
mvn -pl <module> test             # tests for one module, e.g. mvn -pl mscartoes test
```

CI (`.github/workflows/ci.yml`) runs `mvn test` for real, no `-DskipTests`. An earlier assumption that `mscartoes`' `@RabbitListener` and the gateway's Keycloak OIDC discovery lookup would block context startup without a real broker/IdP turned out to be wrong — verified with neither running, both locally and on GitHub's runner. Most modules still only have an empty `contextLoads()`; `msavaliadorcredito` and `mscartoes` also have real Mockito-based unit tests (`AvaliadorCreditoServiceTest`, `EmissaoCartaoSubscriberTest`). If you add tests that genuinely need a live RabbitMQ/Keycloak (not just a client bean that tolerates connection failure), they will not run in CI until that infrastructure is added (Testcontainers or service containers) — say so explicitly rather than assuming green CI means the new tests ran.

Run the whole stack via `docker compose up -d --build` (`docker-compose.yml` at the repo root) if you need RabbitMQ/Keycloak/Eureka actually running — e.g. to manually verify something these unit tests don't cover. Verified working end-to-end; see `CLAUDE.md` for host ports and the two-realm-file gotcha (`realm-export-curso.json`, not `realm-export.json`).

## Code style

- Lombok (`@Data`, `@RequiredArgsConstructor`, `@Getter`) is the norm for DTOs/entities/services — follow it rather than writing manual boilerplate.
- DTOs are intentionally duplicated per module (e.g. a "Cartao" shape exists both as a JPA entity in `mscartoes` and as a plain client-side POJO in `msavaliadorcredito`). This is the correct pattern for microservices — do not introduce a shared model module to deduplicate it.
- Package layout per module follows `application` (controllers/services/DTOs), `domain` (entities), `infra` (repositories, Feign clients, messaging) — match it for new code.

## Architecture notes an agent needs before touching cross-service code

- Sync inter-service calls go through Feign + Eureka service discovery (`@FeignClient(value = "<eureka-service-name>")`), not hardcoded URLs.
- Async card issuance goes through the RabbitMQ queue `emissao-cartoes`: `msavaliadorcredito` publishes, `mscartoes` consumes. Only the publisher currently declares the `Queue` bean.
- Only `mscloudgateway` validates JWTs (OAuth2 resource server / Keycloak). `msclientes`, `mscartoes`, `msavaliadorcredito` have no security layer at all — this is a deliberate (if debatable) trust-boundary decision, not an oversight to "fix" opportunistically inside an unrelated change.

## Before opening a PR

- Check open GitHub issues first in case something's already tracked (the original review backlog, issues #3–#19, is fully resolved — don't assume those items are still open).
- Run the reactor build and tests (`mvn test`) before committing — there is no local pre-commit hook enforcing this.
- Reference the issue number being addressed in the PR description, if any.
- Dependabot may open PRs with major-version dependency jumps (e.g. straight to Spring Boot 3.x/4.x-era artifacts) — don't merge those without a real migration effort; see "Modernization plan" in the README.
