## ADDED Requirements

### Requirement: Credential metadata is queryable
The system SHALL provide a demo API that lists a user's registered credentials with non-secret credential metadata, last successful authentication time, and current signature counter.

#### Scenario: User has registered credentials
- **WHEN** a client requests credential management data for a user with credentials
- **THEN** the API SHALL return each credential's identifier, metadata, last authentication time, and signature counter

### Requirement: Credentials can be deleted
The system SHALL provide a demo API to delete a specified credential while preventing deletion requests from disclosing or modifying another user's credential.

#### Scenario: Owner deletes a credential
- **WHEN** a credential deletion request identifies its owning user and credential
- **THEN** the system SHALL remove or revoke the credential and subsequent authentication with it SHALL fail

#### Scenario: Credential does not belong to user
- **WHEN** a deletion request references a credential not owned by the identified user
- **THEN** the system SHALL return a non-sensitive not-found or forbidden response and SHALL not modify the credential
