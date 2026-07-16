## ADDED Requirements

### Requirement: Production persistence uses managed migrations
The system SHALL provide a production configuration that uses a persistent PostgreSQL-compatible datasource and Flyway migrations as the database schema authority.

#### Scenario: Production configuration is selected
- **WHEN** the production profile starts with valid datasource settings
- **THEN** Flyway SHALL apply pending migrations and JPA SHALL not use schema-update mode

### Requirement: FIDO relying-party settings are environment configurable
The system SHALL obtain relying-party ID and allowed origins from environment-compatible, profile-aware configuration.

#### Scenario: Deployment supplies relying-party settings
- **WHEN** a runtime environment provides FIDO relying-party and origin values
- **THEN** generated and verified WebAuthn options SHALL use those values

### Requirement: Operational signals are available
The system SHALL expose health and metrics endpoints and emit structured audit events for ceremony and credential-management outcomes without logging credential response secrets.

#### Scenario: Operations endpoint is queried
- **WHEN** an authorized operations client queries health or metrics
- **THEN** the system SHALL provide the configured operational signal

#### Scenario: Credential operation completes
- **WHEN** a ceremony or credential-management operation succeeds or fails
- **THEN** the system SHALL emit an audit event containing outcome metadata and no raw credential response material
