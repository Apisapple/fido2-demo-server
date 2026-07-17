package tf.demo.fido2.fido.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FidoUserJpaRepository extends JpaRepository<FidoUserEntity, Long> {
    Optional<FidoUserEntity> findByUsername(String username);

    Optional<FidoUserEntity> findByUserHandle(byte[] userHandle);
}
