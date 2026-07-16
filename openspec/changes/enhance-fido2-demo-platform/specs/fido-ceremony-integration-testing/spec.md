## ADDED Requirements

### Requirement: Integration tests cover FIDO ceremony lifecycle outcomes
The automated test suite SHALL exercise registration and authentication ceremonies through application or HTTP integration boundaries for successful completion, invalid identifiers, wrong types, expiration, and replay attempts.

#### Scenario: Failure cases are regression tested
- **WHEN** a malformed, missing, expired, wrong-type, or reused ceremony is finished
- **THEN** an integration test SHALL verify the expected failure response and lifecycle outcome

### Requirement: Integration tests cover credential integrity cases
The automated test suite SHALL verify duplicate credential rejection and signature-counter update or regression behavior using valid WebAuthn test fixtures.

#### Scenario: Duplicate credential is registered
- **WHEN** a registration attempts to persist an already registered credential identifier
- **THEN** an integration test SHALL verify that the credential is not duplicated and the request fails predictably

#### Scenario: Assertion counter is invalid
- **WHEN** an assertion presents a signature counter that violates the configured counter policy
- **THEN** an integration test SHALL verify rejection or the documented policy outcome
