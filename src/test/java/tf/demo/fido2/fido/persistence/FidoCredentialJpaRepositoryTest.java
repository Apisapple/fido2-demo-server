package tf.demo.fido2.fido.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FidoCredentialJpaRepositoryTest {
  @Autowired private FidoUserJpaRepository users;
  @Autowired private FidoCredentialJpaRepository credentials;

  @Test
  void findsUsersAndCredentialsByTheirBinaryIdentifiers() {
    byte[] userHandle = {1, 2, 3};
    byte[] credentialId = {4, 5, 6};
    FidoUserEntity user = users.save(new FidoUserEntity("alice", "Alice", userHandle));
    credentials.save(new FidoCredentialEntity(user, credentialId, new byte[] {7, 8, 9}, 0));

    assertThat(users.findByUserHandle(userHandle))
        .map(FidoUserEntity::getUsername)
        .contains("alice");
    assertThat(credentials.existsByCredentialId(credentialId)).isTrue();
    assertThat(credentials.findByCredentialId(credentialId)).isPresent();
  }
}
