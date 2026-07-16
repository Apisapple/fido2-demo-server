## ADDED Requirements

### Requirement: Browser client performs WebAuthn ceremonies
The system SHALL serve a same-origin browser demo that starts registration and authentication ceremonies, calls `navigator.credentials.create()` or `navigator.credentials.get()`, and submits each resulting credential to the corresponding finish API.

#### Scenario: Registration succeeds from the demo
- **WHEN** a supported browser completes a registration ceremony in the demo
- **THEN** the client SHALL submit the credential response and display the successful result

#### Scenario: Authentication succeeds from the demo
- **WHEN** a supported browser completes an authentication ceremony in the demo
- **THEN** the client SHALL submit the assertion response and display the authenticated result

### Requirement: Browser client translates WebAuthn JSON
The browser demo SHALL convert all binary public-key option fields received as base64url JSON into WebAuthn-compatible binary values and convert binary credential response fields back to base64url JSON before submission.

#### Scenario: Options include binary fields
- **WHEN** the server returns public-key options containing challenge, user, credential, or extension binary values
- **THEN** the demo SHALL pass equivalent binary values to the browser WebAuthn API

### Requirement: Browser client communicates ceremony state
The browser demo SHALL display success, failure, and user-verification status for each completed ceremony without exposing raw private or credential-response data.

#### Scenario: Authenticator user verification is available
- **WHEN** an authentication or registration response reports user verification
- **THEN** the demo SHALL display whether user verification was performed

#### Scenario: Browser or server rejects a ceremony
- **WHEN** a WebAuthn call or finish API returns an error
- **THEN** the demo SHALL display a human-readable failure state and retain no stale in-progress state
