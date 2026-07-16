package tf.demo.fido2.fido.application;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.demo.fido2.fido.config.FidoProperties;
import tf.demo.fido2.fido.persistence.FidoCeremonyEntity;
import tf.demo.fido2.fido.persistence.FidoCeremonyJpaRepository;

@Service
class FidoCeremonyService {
  private static final Logger log = LoggerFactory.getLogger(FidoCeremonyService.class);
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
    log.info("event=fido_ceremony_started ceremonyId={} type={} username={}", id, type, username);
    return id;
  }

  @Transactional
  FidoCeremonyEntity consume(UUID id, FidoCeremonyEntity.Type expectedType) {
    FidoCeremonyEntity ceremony =
        ceremonies
            .findWithLockById(id)
            .orElseThrow(() -> new CeremonyUnavailableException("unknown ceremony"));
    Instant now = Instant.now(clock);
    if (ceremony.getType() != expectedType || ceremony.isExpired(now) || !ceremony.isPending()) {
      log.info("event=fido_ceremony_rejected ceremonyId={} expectedType={}", id, expectedType);
      throw new CeremonyUnavailableException("expired, consumed, or invalid ceremony");
    }
    ceremony.consume(now);
    log.info("event=fido_ceremony_consumed ceremonyId={} type={}", id, expectedType);
    return ceremony;
  }

  @Transactional
  long removeExpired() {
    return ceremonies.deleteByExpiresAtBefore(
        Instant.now(clock).minus(properties.ceremonyRetention()));
  }
}
