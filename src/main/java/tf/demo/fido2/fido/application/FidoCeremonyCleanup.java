package tf.demo.fido2.fido.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class FidoCeremonyCleanup {
    private static final Logger log = LoggerFactory.getLogger(FidoCeremonyCleanup.class);
    private final FidoCeremonyService ceremonies;

    FidoCeremonyCleanup(FidoCeremonyService ceremonies) {
        this.ceremonies = ceremonies;
    }

    @Scheduled(fixedDelayString = "${fido.ceremony-cleanup-interval}")
    void removeExpiredCeremonies() {
        long removed = ceremonies.removeExpired();
        if (removed > 0) {
            log.info("event=fido_ceremony_cleanup removed={}", removed);
        }
    }
}
