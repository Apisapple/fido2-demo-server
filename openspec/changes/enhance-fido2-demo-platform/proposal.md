## Why

The service exposes FIDO2 ceremony APIs but lacks a browser client, lifecycle safeguards, and management visibility, making real passkey demonstrations and behavior validation difficult. This change makes the demo usable end-to-end while establishing safer ceremony handling and a production-transition baseline.

## What Changes

- Add a browser-hosted demo client that performs WebAuthn registration and authentication, converts WebAuthn JSON fields, sends credential results to the server, and displays success, failure, and user-verification state.
- Add end-to-end FIDO ceremony tests covering success, invalid or wrong-type ceremony IDs, challenge expiration, replay prevention, duplicate credentials, and signature-counter behavior.
- Make ceremony consumption atomic under concurrent requests and periodically remove expired ceremonies.
- Add demo-oriented APIs to list and delete registered credentials and expose each credential's last authentication time and signature counter.
- Support username-less (discoverable credential) authentication and expose authenticator selection, resident-key, and user-verification policy through configuration.
- Provide a consistent API error response and configurable browser-demo security boundaries: CORS, request-size limits, rate limiting, and explicit attestation policy by profile.
- Establish production-transition configuration for persistent database migrations, environment/profile-based relying-party settings, audit logging, metrics, and health checks.

## Capabilities

### New Capabilities

- `browser-webauthn-demo`: Browser UI and protocol translation for demonstrating passkey registration and authentication.
- `fido-ceremony-lifecycle`: Atomic ceremony consumption, expiry cleanup, and lifecycle behavior guarantees.
- `fido-ceremony-integration-testing`: Automated integration coverage of FIDO ceremony success and failure paths.
- `credential-management-api`: Demo APIs and data visibility for registered credentials.
- `discoverable-credential-authentication`: Username-less authentication and configurable authenticator policy.
- `api-security-boundaries`: Consistent API errors and configurable CORS, request limits, rate limits, and attestation policy.
- `production-runtime-foundation`: Persistent-database migration, environment-aware FIDO configuration, and operational observability.

### Modified Capabilities

<!-- None. There are no existing main specs to modify. -->

## Impact

This affects the FIDO API, application services, ceremony and credential persistence, WebAuthn configuration, runtime configuration, test suite, and static web resources. It introduces browser assets, scheduler/rate-limiting and observability dependencies as needed, plus profile-specific database and relying-party configuration.
