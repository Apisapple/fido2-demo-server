package tf.demo.fido2.fido;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fido_ceremonies")
class FidoCeremonyEntity {
  enum Type {
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

  protected FidoCeremonyEntity() {}

  FidoCeremonyEntity(UUID id, Type type, String username, String requestJson, Instant expiresAt) {
    this.id = id;
    this.type = type;
    this.username = username;
    this.requestJson = requestJson;
    this.expiresAt = expiresAt;
  }

  UUID getId() {
    return id;
  }

  Type getType() {
    return type;
  }

  String getUsername() {
    return username;
  }

  String getRequestJson() {
    return requestJson;
  }

  boolean isExpired(Instant now) {
    return !expiresAt.isAfter(now);
  }
}
