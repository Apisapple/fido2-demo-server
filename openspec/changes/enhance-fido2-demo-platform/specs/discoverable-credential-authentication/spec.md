## ADDED Requirements

### Requirement: Username-less authentication is supported
The system SHALL allow a client to start authentication without a username and SHALL resolve the authenticated user from the returned registered credential.

#### Scenario: Discoverable credential authenticates a user
- **WHEN** a client completes a username-less authentication with a registered discoverable credential
- **THEN** the system SHALL verify the assertion and return the credential's owning user as authenticated

### Requirement: Authenticator policy is configurable
The system SHALL expose authenticator attachment, resident-key, and user-verification requirements through validated FIDO configuration properties.

#### Scenario: Platform-authenticator demo profile is configured
- **WHEN** the active configuration requires a platform authenticator and user verification
- **THEN** generated registration options SHALL express those requirements

#### Scenario: Security-key demo profile is configured
- **WHEN** the active configuration allows or requires a cross-platform authenticator
- **THEN** generated registration options SHALL express the configured attachment policy
