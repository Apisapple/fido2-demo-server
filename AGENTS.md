# Repository Guidelines

## Project Structure & Architecture

This is a single-module Spring Boot 4 application built with Gradle and Java 21. Production code is in `src/main/java/tf/demo/fido2`; tests mirror that package structure under `src/test/java`.

The FIDO2 feature is organized by responsibility in `fido/`: `api/` contains HTTP controllers, `application/` owns ceremony use cases and transactions, `config/` wires WebAuthn and properties, and `persistence/` holds JPA entities, repositories, and the Yubico credential adapter. The smaller `memo/` feature demonstrates domain data, services, and interchangeable storage implementations. Runtime configuration belongs in `src/main/resources/application.yaml`.

## Build, Test, and Development Commands

- `./gradlew build` compiles, runs tests, and applies verification tasks.
- `./gradlew test` runs the JUnit 5 test suite.
- `./gradlew bootRun` starts the service locally; the default H2 database is in memory.
- `./gradlew spotlessCheck` verifies Java, Gradle, and shell-script formatting.
- `./gradlew spotlessApply` rewrites supported files to the required format.

Use the Gradle wrapper rather than a machine-installed Gradle version.

## Coding Style & Naming Conventions

Spotless is authoritative: Java uses `google-java-format`, unused imports are removed, and files end with a newline. Run `spotlessApply` before committing. Keep packages lowercase and aligned with responsibilities. Use PascalCase for types (`FidoCeremonyService`), camelCase for members and methods, and descriptive Spring components such as `FidoCredentialJpaRepository`. Prefer constructor injection and keep controller request validation at the API boundary.

## Testing Guidelines

Tests use JUnit Jupiter, Spring Boot Test, and AssertJ. Name test classes `*Test` and methods as behavior statements, e.g. `findsUsersAndCredentialsByTheirBinaryIdentifiers`. Add focused unit tests for service logic and use `@SpringBootTest` where JPA wiring or application integration is relevant. Run `./gradlew test` and `./gradlew spotlessCheck` before opening a change; no coverage threshold is currently configured.

## Commit & Pull Request Guidelines

Recent history follows Conventional Commit-style subjects, such as `feat(fido2): add ceremony lifecycle service` and `refactor(persistence): reduce entity boilerplate with Lombok`. Use a lowercase type and optional scope; keep the subject imperative and concise. PRs should explain the behavioral change, list verification commands, link the related issue when one exists, and include request/response examples for API changes. Flag configuration or WebAuthn relying-party/origin changes explicitly.

## Security & Configuration

Do not commit credentials or production origins. Before deployment, set `fido.rp-id` and `fido.origins` to the real relying-party domain; mismatches prevent valid WebAuthn ceremonies.
