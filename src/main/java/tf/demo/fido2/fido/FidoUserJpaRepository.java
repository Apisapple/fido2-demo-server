package tf.demo.fido2.fido;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface FidoUserJpaRepository extends JpaRepository<FidoUserEntity, Long> {
  Optional<FidoUserEntity> findByUsername(String username);
}
