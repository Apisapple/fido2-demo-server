package tf.demo.fido2.fido;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class FidoCredentialRepository implements CredentialRepository {
  private final FidoUserJpaRepository users;
  private final FidoCredentialJpaRepository credentials;

  FidoCredentialRepository(FidoUserJpaRepository users, FidoCredentialJpaRepository credentials) {
    this.users = users;
    this.credentials = credentials;
  }

  @Override
  @Transactional(readOnly = true)
  public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
    return users.findByUsername(username).stream()
        .flatMap(user -> credentials.findAllByUser(user).stream())
        .map(
            c ->
                PublicKeyCredentialDescriptor.builder()
                    .id(new ByteArray(c.getCredentialId()))
                    .build())
        .collect(Collectors.toSet());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ByteArray> getUserHandleForUsername(String username) {
    return users.findByUsername(username).map(user -> new ByteArray(user.getUserHandle()));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
    return users.findAll().stream()
        .filter(user -> Arrays.equals(user.getUserHandle(), userHandle.getBytes()))
        .map(FidoUserEntity::getUsername)
        .findFirst();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
    return lookupAll(credentialId).stream()
        .filter(credential -> credential.getUserHandle().equals(userHandle))
        .findFirst();
  }

  @Override
  @Transactional(readOnly = true)
  public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
    return credentials.findAll().stream()
        .filter(c -> Arrays.equals(c.getCredentialId(), credentialId.getBytes()))
        .map(
            c ->
                RegisteredCredential.builder()
                    .credentialId(new ByteArray(c.getCredentialId()))
                    .userHandle(new ByteArray(c.getUser().getUserHandle()))
                    .publicKeyCose(new ByteArray(c.getPublicKeyCose()))
                    .signatureCount(c.getSignatureCount())
                    .build())
        .collect(Collectors.toSet());
  }
}
