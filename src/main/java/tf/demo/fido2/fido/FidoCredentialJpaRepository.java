package tf.demo.fido2.fido;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface FidoCredentialJpaRepository extends JpaRepository<FidoCredentialEntity, Long> {
  List<FidoCredentialEntity> findAllByUser(FidoUserEntity user);

  Optional<FidoCredentialEntity> findByCredentialId(byte[] credentialId);

  boolean existsByCredentialId(byte[] credentialId);
}
