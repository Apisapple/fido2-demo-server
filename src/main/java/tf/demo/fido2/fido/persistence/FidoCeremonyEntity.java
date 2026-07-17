package tf.demo.fido2.fido.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fido_ceremonies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FidoCeremonyEntity {
    public enum Type {
        REGISTRATION,
        AUTHENTICATION,
        DISCOVERABLE_AUTHENTICATION
    }

    public enum State {
        PENDING,
        CONSUMED
    }

    @Id private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column private String username;

    @Lob
    @Column(nullable = false)
    private String requestJson;

    @Column(nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private State state;

    private Instant consumedAt;

    public FidoCeremonyEntity(
            UUID id, Type type, String username, String requestJson, Instant expiresAt) {
        this.id = id;
        this.type = type;
        this.username = username;
        this.requestJson = requestJson;
        this.expiresAt = expiresAt;
        this.state = State.PENDING;
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    public boolean isPending() {
        return state == State.PENDING;
    }

    public void consume(Instant consumedAt) {
        if (!isPending()) {
            throw new IllegalStateException("ceremony has already been consumed");
        }
        this.state = State.CONSUMED;
        this.consumedAt = consumedAt;
    }
}
