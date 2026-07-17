package tf.demo.fido2.fido.application;

import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.UserIdentity;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.demo.fido2.fido.config.FidoProperties;
import tf.demo.fido2.fido.persistence.FidoCeremonyEntity;
import tf.demo.fido2.fido.persistence.FidoCredentialEntity;
import tf.demo.fido2.fido.persistence.FidoCredentialJpaRepository;
import tf.demo.fido2.fido.persistence.FidoUserEntity;
import tf.demo.fido2.fido.persistence.FidoUserJpaRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class FidoService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final RelyingParty relyingParty;
    private final FidoUserJpaRepository users;
    private final FidoCredentialJpaRepository credentials;
    private final FidoCeremonyService ceremonyService;
    private final ObjectMapper objectMapper;
    private final FidoProperties properties;
    private final Clock clock;

    FidoService(
            RelyingParty relyingParty,
            FidoUserJpaRepository users,
            FidoCredentialJpaRepository credentials,
            FidoCeremonyService ceremonyService,
            ObjectMapper objectMapper,
            FidoProperties properties,
            Clock clock) {
        this.relyingParty = relyingParty;
        this.users = users;
        this.credentials = credentials;
        this.ceremonyService = ceremonyService;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public CeremonyOptions startRegistration(String username, String displayName) {
        FidoUserEntity user =
                users.findByUsername(username)
                        .orElseGet(() -> users.save(newUser(username, displayName)));
        UserIdentity identity =
                UserIdentity.builder()
                        .name(user.getUsername())
                        .displayName(user.getDisplayName())
                        .id(new ByteArray(user.getUserHandle()))
                        .build();
        PublicKeyCredentialCreationOptions request =
                relyingParty.startRegistration(
                        StartRegistrationOptions.builder()
                                .user(identity)
                                .authenticatorSelection(
                                        AuthenticatorSelectionCriteria.builder()
                                                .authenticatorAttachment(
                                                        properties.authenticatorAttachment())
                                                .residentKey(properties.residentKey())
                                                .userVerification(properties.userVerification())
                                                .build())
                                .build());
        return saveCeremony(FidoCeremonyEntity.Type.REGISTRATION, username, toJson(request));
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void finishRegistration(UUID ceremonyId, String credentialJson) {
        FidoCeremonyEntity ceremony =
                ceremonyService.consume(ceremonyId, FidoCeremonyEntity.Type.REGISTRATION);
        PublicKeyCredentialCreationOptions request = registrationRequest(ceremony.getRequestJson());
        var result = finishRegistration(request, credentialJson);
        FidoUserEntity user = users.findByUsername(ceremony.getUsername()).orElseThrow();
        if (credentials.existsByCredentialId(result.getKeyId().getId().getBytes())) {
            throw new IllegalArgumentException("credential is already registered");
        }
        credentials.save(
                new FidoCredentialEntity(
                        user,
                        result.getKeyId().getId().getBytes(),
                        result.getPublicKeyCose().getBytes(),
                        result.getSignatureCount()));
    }

    @Transactional
    public CeremonyOptions startAuthentication(String username) {
        return startAuthentication(username, FidoCeremonyEntity.Type.AUTHENTICATION);
    }

    @Transactional
    public CeremonyOptions startDiscoverableAuthentication() {
        return startAuthentication(null, FidoCeremonyEntity.Type.DISCOVERABLE_AUTHENTICATION);
    }

    private CeremonyOptions startAuthentication(String username, FidoCeremonyEntity.Type type) {
        AssertionRequest request =
                relyingParty.startAssertion(
                        username == null || username.isBlank()
                                ? StartAssertionOptions.builder()
                                        .userVerification(properties.userVerification())
                                        .build()
                                : StartAssertionOptions.builder()
                                        .username(username)
                                        .userVerification(properties.userVerification())
                                        .build());
        return saveCeremony(type, username, toJson(request));
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public AuthenticationResult finishAuthentication(UUID ceremonyId, String credentialJson) {
        return finishAuthentication(
                ceremonyId, credentialJson, FidoCeremonyEntity.Type.AUTHENTICATION);
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public AuthenticationResult finishDiscoverableAuthentication(
            UUID ceremonyId, String credentialJson) {
        return finishAuthentication(
                ceremonyId, credentialJson, FidoCeremonyEntity.Type.DISCOVERABLE_AUTHENTICATION);
    }

    @Transactional(readOnly = true)
    public List<CredentialSummary> credentials(String username) {
        FidoUserEntity user =
                users.findByUsername(username)
                        .orElseThrow(() -> new IllegalArgumentException("unknown user"));
        return credentials.findAllByUser(user).stream()
                .map(
                        credential ->
                                new CredentialSummary(
                                        Base64.getUrlEncoder()
                                                .withoutPadding()
                                                .encodeToString(credential.getCredentialId()),
                                        credential.getSignatureCount(),
                                        credential.getLastAuthenticatedAt()))
                .toList();
    }

    @Transactional
    public void deleteCredential(String username, String credentialId) {
        FidoUserEntity user =
                users.findByUsername(username)
                        .orElseThrow(() -> new IllegalArgumentException("unknown credential"));
        byte[] id;
        try {
            id = Base64.getUrlDecoder().decode(credentialId);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("unknown credential");
        }
        FidoCredentialEntity credential =
                credentials
                        .findByCredentialId(id)
                        .orElseThrow(() -> new IllegalArgumentException("unknown credential"));
        if (!credential.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("unknown credential");
        }
        credentials.delete(credential);
    }

    private AuthenticationResult finishAuthentication(
            UUID ceremonyId, String credentialJson, FidoCeremonyEntity.Type expectedType) {
        FidoCeremonyEntity ceremony = ceremonyService.consume(ceremonyId, expectedType);
        AssertionRequest request = assertionRequest(ceremony.getRequestJson());
        var result = finishAssertion(request, credentialJson);
        if (!result.isSuccess())
            throw new IllegalArgumentException("assertion verification failed");
        FidoCredentialEntity credential =
                credentials
                        .findByCredentialId(result.getCredentialId().getBytes())
                        .orElseThrow(() -> new IllegalArgumentException("unknown credential"));
        credential.recordAuthentication(result.getSignatureCount(), Instant.now(clock));
        return new AuthenticationResult(
                credential.getUser().getUsername(), result.isUserVerified());
    }

    private CeremonyOptions saveCeremony(
            FidoCeremonyEntity.Type type, String username, String requestJson) {
        UUID id = ceremonyService.create(type, username, requestJson);
        try {
            return new CeremonyOptions(id, objectMapper.readTree(requestJson));
        } catch (Exception exception) {
            throw new IllegalStateException("could not serialize WebAuthn request", exception);
        }
    }

    private com.yubico.webauthn.RegistrationResult finishRegistration(
            PublicKeyCredentialCreationOptions request, String credentialJson) {
        try {
            var response = PublicKeyCredential.parseRegistrationResponseJson(credentialJson);
            return relyingParty.finishRegistration(
                    FinishRegistrationOptions.builder()
                            .request(request)
                            .response(response)
                            .build());
        } catch (Exception exception) {
            throw new IllegalArgumentException("registration verification failed", exception);
        }
    }

    private PublicKeyCredentialCreationOptions registrationRequest(String json) {
        try {
            return PublicKeyCredentialCreationOptions.fromJson(json);
        } catch (Exception exception) {
            throw new IllegalStateException("could not read WebAuthn request", exception);
        }
    }

    private String toJson(PublicKeyCredentialCreationOptions request) {
        return serializeRequest(request::toJson);
    }

    private String toJson(AssertionRequest request) {
        return serializeRequest(request::toJson);
    }

    private String serializeRequest(JsonSerializer serializer) {
        try {
            return serializer.serialize();
        } catch (Exception exception) {
            throw new IllegalStateException("could not serialize WebAuthn request", exception);
        }
    }

    private AssertionRequest assertionRequest(String json) {
        try {
            return AssertionRequest.fromJson(json);
        } catch (Exception exception) {
            throw new IllegalStateException("could not read WebAuthn request", exception);
        }
    }

    private com.yubico.webauthn.AssertionResult finishAssertion(
            AssertionRequest request, String credentialJson) {
        try {
            var response = PublicKeyCredential.parseAssertionResponseJson(credentialJson);
            return relyingParty.finishAssertion(
                    FinishAssertionOptions.builder().request(request).response(response).build());
        } catch (Exception exception) {
            throw new IllegalArgumentException("authentication verification failed", exception);
        }
    }

    private static FidoUserEntity newUser(String username, String displayName) {
        if (username == null || username.isBlank() || displayName == null || displayName.isBlank())
            throw new IllegalArgumentException("username and displayName are required");
        byte[] handle = new byte[32];
        RANDOM.nextBytes(handle);
        return new FidoUserEntity(username.trim(), displayName.trim(), handle);
    }

    @FunctionalInterface
    private interface JsonSerializer {
        String serialize() throws Exception;
    }

    public record CeremonyOptions(UUID ceremonyId, JsonNode publicKey) {}

    public record AuthenticationResult(String username, boolean userVerified) {}

    public record CredentialSummary(
            String credentialId, long signatureCount, Instant lastAuthenticatedAt) {}
}
