## ADDED Requirements

### Requirement: API failures use a consistent safe response
The system SHALL return a stable structured error body and appropriate HTTP status for validation, invalid input, unavailable ceremonies, and unexpected server failures, without exposing internal exception details.

#### Scenario: Client submits invalid input
- **WHEN** a request fails validation or violates an API precondition
- **THEN** the API SHALL return a machine-readable error code, a safe message, and an appropriate 4xx status

### Requirement: Browser-facing security boundaries are configurable
The system SHALL make CORS origins, request size limits, and rate-limit behavior configurable with restrictive defaults suitable for a same-origin demo.

#### Scenario: Cross-origin request is not allowed
- **WHEN** a browser request originates from an unconfigured origin
- **THEN** the system SHALL not grant CORS access to that origin

### Requirement: Attestation policy is explicit by profile
The system SHALL configure attestation trust policy per runtime profile and SHALL not implicitly enable untrusted attestation in a production profile.

#### Scenario: Production profile starts
- **WHEN** the production profile is active
- **THEN** startup configuration SHALL require an explicit production attestation policy
