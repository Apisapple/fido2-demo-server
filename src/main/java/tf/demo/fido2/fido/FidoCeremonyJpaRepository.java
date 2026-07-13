package tf.demo.fido2.fido;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface FidoCeremonyJpaRepository extends JpaRepository<FidoCeremonyEntity, UUID> {}
