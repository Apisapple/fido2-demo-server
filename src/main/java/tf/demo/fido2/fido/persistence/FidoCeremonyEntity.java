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
    AUTHENTICATION
  }

  @Id private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Type type;

  @Column(nullable = false)
  private String username;

  @Lob
  @Column(nullable = false)
  private String requestJson;

  @Column(nullable = false)
  private Instant expiresAt;

  public FidoCeremonyEntity(
      UUID id, Type type, String username, String requestJson, Instant expiresAt) {
    this.id = id;
    this.type = type;
    this.username = username;
    this.requestJson = requestJson;
    this.expiresAt = expiresAt;
  }

  public boolean isExpired(Instant now) {
    return !expiresAt.isAfter(now);
  }
}
