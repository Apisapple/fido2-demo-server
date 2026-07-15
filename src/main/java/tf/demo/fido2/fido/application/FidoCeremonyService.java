package tf.demo.fido2.fido.application;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tf.demo.fido2.fido.config.FidoProperties;
import tf.demo.fido2.fido.persistence.FidoCeremonyEntity;
import tf.demo.fido2.fido.persistence.FidoCeremonyJpaRepository;

@Service
class FidoCeremonyService {
  private final FidoCeremonyJpaRepository ceremonies;
  private final FidoProperties properties;
  private final Clock clock;

  FidoCeremonyService(
      FidoCeremonyJpaRepository ceremonies, FidoProperties properties, Clock clock) {
    this.ceremonies = ceremonies;
    this.properties = properties;
    this.clock = clock;
  }

  UUID create(FidoCeremonyEntity.Type type, String username, String requestJson) {
    UUID id = UUID.randomUUID();
    ceremonies.save(
        new FidoCeremonyEntity(
            id, type, username, requestJson, Instant.now(clock).plus(properties.challengeTtl())));
    return id;
  }

  FidoCeremonyEntity consume(UUID id, FidoCeremonyEntity.Type expectedType) {
    FidoCeremonyEntity ceremony =
        ceremonies.findById(id).orElseThrow(() -> new IllegalArgumentException("unknown ceremony"));
    ceremonies.delete(ceremony);
    if (ceremony.getType() != expectedType || ceremony.isExpired(Instant.now(clock))) {
      throw new IllegalArgumentException("expired or invalid ceremony");
    }
    return ceremony;
  }
}
