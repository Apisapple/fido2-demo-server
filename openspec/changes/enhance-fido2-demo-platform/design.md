## Context

The Spring Boot service currently provides server-side FIDO2 registration and username-based authentication APIs backed by JPA. Ceremony records are read before deletion, errors can escape as framework defaults, and no browser client demonstrates the server's WebAuthn payloads. H2 with schema updates is suitable for local iteration but not a persistent deployment. The change crosses HTTP, application, persistence, configuration, browser assets, and operations.

## Goals / Non-Goals

**Goals:**

- Deliver an immediately usable same-origin browser demonstration of passkey registration and authentication.
- Guarantee that a ceremony is consumed at most once, including concurrent requests, and bound ceremony storage through expiry cleanup.
- Make credential state observable and manageable for the demo.
- Support discoverable-credential authentication and configurable authenticator policy.
- Make expected client failures safe and consistent, while providing deployment-oriented configuration and observability foundations.
- Define integration coverage for the full ceremony lifecycle and security-sensitive failure cases.

**Non-Goals:**

- Building a production identity-management system, account recovery flow, or multi-tenant authorization model.
- Supporting every browser, authenticator extension, or attestation format beyond the selected WebAuthn library capabilities.
- Enforcing a specific global rate-limit implementation for all future production topologies; the limit remains configurable and deployer-owned.
- Migrating an existing production database automatically without an operator-approved data migration plan.

## Decisions

### Serve a lightweight, same-origin static demo client

Spring Boot will serve a small HTML/CSS/JavaScript client from static resources. It will call the existing and newly added APIs with `fetch`, translate base64url fields in public-key options and credential responses into browser-compatible `ArrayBuffer` values and JSON respectively, and render ceremony outcomes and `userVerified` information.

This keeps the demo runnable with `bootRun`, avoids a separate frontend build/deployment, and remains useful when CORS is disabled by default. A standalone SPA was considered, but adds toolchain and cross-origin deployment overhead disproportionate to the demo.

### Consume ceremonies with an atomic persistence operation

The persistence boundary will expose a consume operation that atomically transitions a pending, unexpired ceremony to consumed (or atomically claims/deletes it) by ceremony ID and expected type. The application layer will perform verification only after it owns the ceremony and will reject missing, expired, wrong-type, or already-consumed records without invoking the verifier.

An optimistic conditional update or pessimistic row lock inside a transaction is preferred over read-then-delete because it creates a database-enforced single-winner outcome. A purely in-memory lock was rejected because it fails across application instances.

### Retain consumed-record diagnostics briefly, then delete expired data on a schedule

Ceremonies will include lifecycle state and expiry metadata. A scheduled cleanup removes expired (and, if retained, aged consumed) records. This supports deterministic replay rejection while preventing unbounded table growth. Immediate deletion alone was considered, but cannot distinguish reuse from unknown IDs and makes lifecycle diagnostics less useful.

### Treat management endpoints as demo APIs with explicit resource ownership

Credential list and delete endpoints will resolve the owning user and return credential ID, transport/type metadata available from the stored credential, last-authentication timestamp, and signature counter. Deletion is idempotent from an operator/demo perspective, while cross-user credential identifiers are never disclosed. User lookup semantics and authorization policy will be documented for the unauthenticated demo; production deployments must place these endpoints behind application authentication.

### Add discoverable authentication as a separate ceremony mode

Authentication-option creation without a username will omit `allowCredentials`; the assertion result identifies the user through the stored credential. The server will resolve the credential owner during finish and return the authenticated user identity. Existing username-based authentication remains compatible. This cleanly maps to WebAuthn discoverable credential behavior instead of inferring a username client-side.

Authenticator attachment, resident-key requirement, and user-verification requirement will be represented as validated `FidoProperties` values and applied consistently to registration and authentication options. Hard-coded policy was rejected because the demo needs to showcase platform authenticators and security keys.

### Centralize HTTP error and security-boundary configuration

`@RestControllerAdvice` will map validation, malformed input, not-found, ceremony-lifecycle, and unexpected failures to a versioned, non-sensitive error body with a stable machine-readable code and request correlation identifier. CORS allowed origins, request body limits, and rate-limit settings will be properties with restrictive defaults. Rate limiting will operate before expensive ceremony processing and key by a documented request identity (such as client IP plus endpoint); deployments may replace it with gateway enforcement.

Attestation trust policy will be profile-specific: the demo profile permits untrusted attestation, while a production profile requires an explicit policy choice rather than silently inheriting demo behavior.

### Use profile-driven persistence and standard Spring Boot operations

Local development retains H2 under a development/demo profile. A production profile uses PostgreSQL and Flyway migrations as the schema authority; `ddl-auto:update` is not used in production. Relying-party ID and origins are supplied through environment-compatible configuration. Spring Boot Actuator provides health and metrics endpoints, and structured audit events record ceremony lifecycle and credential management outcomes without storing raw credential response material.

## Risks / Trade-offs

- [Browser/device WebAuthn support differs] → Detect unsupported APIs, show actionable UI errors, and document the secure-context/origin requirement.
- [Atomic claim before cryptographic verification can consume a ceremony for an invalid response] → Treat every terminal finish attempt as one-use by design; clients start a new ceremony after failure.
- [Scheduler timing is not exact] → Enforce expiry during consumption; cleanup only reclaims storage.
- [Management APIs expose sensitive operational state] → Limit data to non-secret metadata and protect or disable the endpoints outside the demo profile.
- [Application-local rate limiting is bypassed across nodes] → Document it as demo/single-node protection and enable gateway/distributed enforcement for scaled deployments.
- [Database migration changes deployment responsibilities] → Validate migrations in CI and retain rollback procedures before production rollout.

## Migration Plan

1. Add Flyway migrations that create or evolve ceremony and credential lifecycle fields without dropping existing data.
2. Ship the demo profile with H2, explicit demo attestation policy, and the static client; preserve existing username-based API behavior.
3. Introduce atomic consumption and integration tests before enabling the cleanup scheduler.
4. Configure production environments with PostgreSQL, Flyway, environment-provided `fido.rp-id` and origins, restrictive CORS/rate limits, and an explicit attestation policy.
5. Roll back application code only after confirming migrations are backward-compatible; database rollbacks require a separately tested migration or restore procedure.

## Open Questions

- Which authentication mechanism, if any, protects credential-management endpoints in the target demo environment?
- Which reverse proxy or gateway will provide distributed rate limiting in production?
- What production attestation trust model and acceptable authenticator attestation formats are required?
- What retention period is desired for consumed ceremony audit records before cleanup?
