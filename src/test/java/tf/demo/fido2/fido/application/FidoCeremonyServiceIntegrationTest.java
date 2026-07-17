package tf.demo.fido2.fido.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tf.demo.fido2.fido.config.FidoProperties;
import tf.demo.fido2.fido.persistence.FidoCeremonyEntity;
import tf.demo.fido2.fido.persistence.FidoCeremonyJpaRepository;

@SpringBootTest
class FidoCeremonyServiceIntegrationTest {
    @Autowired private FidoCeremonyService ceremonies;
    @Autowired private FidoCeremonyJpaRepository repository;
    @Autowired private FidoProperties properties;

    @Test
    void rejectsAReusedCeremony() {
        UUID id = ceremonies.create(FidoCeremonyEntity.Type.REGISTRATION, "demo", "{}");

        ceremonies.consume(id, FidoCeremonyEntity.Type.REGISTRATION);

        assertThatThrownBy(() -> ceremonies.consume(id, FidoCeremonyEntity.Type.REGISTRATION))
                .isInstanceOf(CeremonyUnavailableException.class);
    }

    @Test
    void rejectsATypeMismatchWithoutConsumingTheCeremony() {
        UUID id = ceremonies.create(FidoCeremonyEntity.Type.AUTHENTICATION, "demo", "{}");

        assertThatThrownBy(() -> ceremonies.consume(id, FidoCeremonyEntity.Type.REGISTRATION))
                .isInstanceOf(CeremonyUnavailableException.class);

        assertThat(ceremonies.consume(id, FidoCeremonyEntity.Type.AUTHENTICATION).isPending())
                .isFalse();
    }

    @Test
    void rejectsExpiredCeremoniesAndCleanupRemovesOldRecords() {
        UUID id = UUID.randomUUID();
        repository.save(
                new FidoCeremonyEntity(
                        id,
                        FidoCeremonyEntity.Type.REGISTRATION,
                        "demo",
                        "{}",
                        Instant.now().minusSeconds(60)));

        assertThatThrownBy(() -> ceremonies.consume(id, FidoCeremonyEntity.Type.REGISTRATION))
                .isInstanceOf(CeremonyUnavailableException.class);

        UUID oldId = UUID.randomUUID();
        repository.save(
                new FidoCeremonyEntity(
                        oldId,
                        FidoCeremonyEntity.Type.REGISTRATION,
                        "demo",
                        "{}",
                        Instant.now().minus(properties.ceremonyRetention()).minusSeconds(1)));

        ceremonies.removeExpired();

        assertThat(repository.existsById(oldId)).isFalse();
    }

    @Test
    void allowsOnlyOneConcurrentConsumer() throws Exception {
        UUID id = ceremonies.create(FidoCeremonyEntity.Type.AUTHENTICATION, "demo", "{}");
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Future<Boolean> first = executor.submit(() -> consumeAfterStart(id, ready, start));
            Future<Boolean> second = executor.submit(() -> consumeAfterStart(id, ready, start));
            ready.await();
            start.countDown();

            assertThat((first.get() ? 1 : 0) + (second.get() ? 1 : 0)).isEqualTo(1);
        }
    }

    private boolean consumeAfterStart(UUID id, CountDownLatch ready, CountDownLatch start)
            throws Exception {
        ready.countDown();
        start.await();
        try {
            ceremonies.consume(id, FidoCeremonyEntity.Type.AUTHENTICATION);
            return true;
        } catch (CeremonyUnavailableException exception) {
            return false;
        }
    }
}
