## ADDED Requirements

### Requirement: Ceremony consumption is single-use and atomic
The system SHALL atomically claim a pending ceremony for its expected type before accepting a finish request, so no more than one concurrent request can verify with the same ceremony.

#### Scenario: Concurrent finish requests use one ceremony
- **WHEN** two finish requests for the same valid ceremony arrive concurrently
- **THEN** exactly one request SHALL proceed to verification and the other SHALL receive a consumed-or-unavailable ceremony error

### Requirement: Ceremony type and expiry are enforced during consumption
The system SHALL reject a ceremony that is missing, already consumed, expired, or requested with a different ceremony type.

#### Scenario: Expired ceremony is submitted
- **WHEN** a finish request references a ceremony after its expiration time
- **THEN** the system SHALL reject it without WebAuthn verification

#### Scenario: Wrong ceremony type is submitted
- **WHEN** a registration finish request references an authentication ceremony, or the reverse
- **THEN** the system SHALL reject it without WebAuthn verification

### Requirement: Expired ceremony storage is cleaned up
The system SHALL periodically remove expired ceremony records and SHALL enforce expiration independently of cleanup timing.

#### Scenario: Cleanup runs after expiry
- **WHEN** the cleanup task executes after ceremony records expire
- **THEN** expired records SHALL be removed according to configured retention rules
