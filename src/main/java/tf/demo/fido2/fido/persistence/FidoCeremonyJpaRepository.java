package tf.demo.fido2.fido.persistence;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface FidoCeremonyJpaRepository extends JpaRepository<FidoCeremonyEntity, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<FidoCeremonyEntity> findWithLockById(UUID id);

  long deleteByExpiresAtBefore(Instant expiresAt);
}
