package tf.demo.fido2.fido;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface FidoCredentialJpaRepository extends JpaRepository<FidoCredentialEntity, Long> {
  List<FidoCredentialEntity> findAllByUser(FidoUserEntity user);
}
