package tf.demo.fido2.fido;

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
import com.yubico.webauthn.data.UserVerificationRequirement;
import java.security.SecureRandom;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

  FidoService(
      RelyingParty relyingParty,
      FidoUserJpaRepository users,
      FidoCredentialJpaRepository credentials,
      FidoCeremonyService ceremonyService,
      ObjectMapper objectMapper) {
    this.relyingParty = relyingParty;
    this.users = users;
    this.credentials = credentials;
    this.ceremonyService = ceremonyService;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public CeremonyOptions startRegistration(String username, String displayName) {
    FidoUserEntity user =
        users.findByUsername(username).orElseGet(() -> users.save(newUser(username, displayName)));
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
                        .userVerification(UserVerificationRequirement.REQUIRED)
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
    AssertionRequest request =
        relyingParty.startAssertion(
            StartAssertionOptions.builder()
                .username(username)
                .userVerification(UserVerificationRequirement.REQUIRED)
                .build());
    return saveCeremony(FidoCeremonyEntity.Type.AUTHENTICATION, username, toJson(request));
  }

  @Transactional(noRollbackFor = IllegalArgumentException.class)
  public AuthenticationResult finishAuthentication(UUID ceremonyId, String credentialJson) {
    FidoCeremonyEntity ceremony =
        ceremonyService.consume(ceremonyId, FidoCeremonyEntity.Type.AUTHENTICATION);
    AssertionRequest request = assertionRequest(ceremony.getRequestJson());
    var result = finishAssertion(request, credentialJson);
    if (!result.isSuccess()) throw new IllegalArgumentException("assertion verification failed");
    FidoCredentialEntity credential =
        credentials
            .findByCredentialId(result.getCredentialId().getBytes())
            .orElseThrow(() -> new IllegalArgumentException("unknown credential"));
    credential.updateSignatureCount(result.getSignatureCount());
    return new AuthenticationResult(ceremony.getUsername(), result.isUserVerified());
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
          FinishRegistrationOptions.builder().request(request).response(response).build());
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
}
