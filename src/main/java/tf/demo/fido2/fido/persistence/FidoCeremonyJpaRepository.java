package tf.demo.fido2.fido.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FidoCeremonyJpaRepository extends JpaRepository<FidoCeremonyEntity, UUID> {}
