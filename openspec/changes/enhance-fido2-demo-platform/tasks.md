## 1. Baseline configuration and data model

- [x] 1.1 Add validated FIDO properties for authenticator attachment, resident-key requirement, user-verification requirement, ceremony retention, and explicit profile-specific attestation policy.
- [x] 1.2 Split demo/development and production configuration, externalize relying-party ID and origins, and add PostgreSQL datasource settings for production.
- [x] 1.3 Add Flyway and operational dependencies, establish migration configuration, and write initial migrations for existing FIDO tables plus lifecycle fields.
- [x] 1.4 Extend ceremony persistence with type, expiry, lifecycle state, consumed time, and query support for cleanup.
- [x] 1.5 Extend credential persistence and mappings with last successful authentication time and ensure signature-counter state is queryable.

## 2. Ceremony lifecycle correctness

- [x] 2.1 Define application-level ceremony lifecycle errors and replace read-then-delete with a transactional atomic claim operation scoped by ID and expected type.
- [x] 2.2 Update registration completion to claim the ceremony before verification, reject terminal lifecycle states, and record the final outcome safely.
- [x] 2.3 Update authentication completion with the same atomic lifecycle semantics and signature-counter persistence policy.
- [x] 2.4 Add a configurable scheduled job that removes expired and retention-aged ceremony records.
- [x] 2.5 Add structured audit events for ceremony start, completion, rejection, consumption race, and cleanup without response secrets.

## 3. Authentication modes and credential management

- [x] 3.1 Apply configurable authenticator, resident-key, and user-verification policies when generating registration and authentication options.
- [x] 3.2 Add username-less authentication start and finish flows that resolve the credential owner from a discoverable credential while preserving username-based compatibility.
- [x] 3.3 Add credential management response models and repository/service queries that return non-secret metadata, last authentication time, and signature counter.
- [x] 3.4 Add user-scoped credential list and delete/revoke endpoints with idempotent, non-disclosing ownership behavior.
- [x] 3.5 Document and enforce the demo-profile access boundary for management endpoints, leaving production protection configurable by the hosting application.

## 4. API safety and operations

- [x] 4.1 Add `@RestControllerAdvice` mappings that produce stable safe error responses for validation, malformed payloads, ceremony lifecycle failures, and unexpected errors.
- [x] 4.2 Add restrictive, property-driven CORS and HTTP request-size configuration for browser clients.
- [x] 4.3 Add configurable endpoint rate limiting before expensive ceremony processing and return a structured throttling response.
- [x] 4.4 Configure Actuator health and metrics exposure appropriate to demo and production profiles.
- [x] 4.5 Verify production startup requires explicit attestation policy and does not use JPA schema-update mode.

## 5. Browser demo client

- [x] 5.1 Add a same-origin static browser demo page with registration, username authentication, username-less authentication, and credential-management controls.
- [x] 5.2 Implement reusable base64url-to-binary and WebAuthn-response-to-JSON conversion helpers for all required public-key fields.
- [x] 5.3 Connect the UI to start/finish APIs, handle in-progress state, and display server errors, browser errors, success results, and user-verification status.
- [x] 5.4 Add user-facing secure-context and unsupported-browser guidance and ensure raw credential responses are not rendered or logged.

## 6. Integration and regression tests

- [ ] 6.1 Create reusable valid WebAuthn registration and assertion fixtures/test utilities compatible with the configured relying party.
- [ ] 6.2 Add integration tests for successful registration and username-based and username-less authentication, including credential metadata and counter updates.
- [ ] 6.3 Add integration tests for missing, malformed, wrong-type, expired, and reused ceremony IDs and assert safe API errors.
- [x] 6.4 Add a concurrency integration test proving only one finish request can consume the same ceremony.
- [ ] 6.5 Add integration tests for duplicate credential registration and accepted or rejected signature-counter transitions according to policy.
- [ ] 6.6 Add API tests for credential ownership/deletion, CORS/error mappings, request limits, rate limits, profile configuration, cleanup scheduling, audit events, health, and metrics.

## 7. Verification and documentation

- [x] 7.1 Update application configuration documentation with demo and production environment variables, origins, attestation, persistence, and operational endpoint guidance.
- [x] 7.2 Document browser demo usage, supported flows, credential management behavior, and the security limitations of an unauthenticated demo.
- [x] 7.3 Run `./gradlew test`, `./gradlew spotlessCheck`, and the OpenSpec validation for this change; resolve all failures.
