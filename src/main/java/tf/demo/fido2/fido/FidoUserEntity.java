package tf.demo.fido2.fido;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "fido_users")
class FidoUserEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  private String displayName;

  @Lob
  @Column(nullable = false, unique = true)
  private byte[] userHandle;

  protected FidoUserEntity() {}

  FidoUserEntity(String username, String displayName, byte[] userHandle) {
    this.username = username;
    this.displayName = displayName;
    this.userHandle = userHandle;
  }

  Long getId() {
    return id;
  }

  String getUsername() {
    return username;
  }

  String getDisplayName() {
    return displayName;
  }

  byte[] getUserHandle() {
    return userHandle.clone();
  }
}
