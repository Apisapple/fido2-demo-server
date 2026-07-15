package tf.demo.fido2.fido.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "fido_users")
public class FidoUserEntity {
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

  public FidoUserEntity(String username, String displayName, byte[] userHandle) {
    this.username = username;
    this.displayName = displayName;
    this.userHandle = userHandle;
  }

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getDisplayName() {
    return displayName;
  }

  public byte[] getUserHandle() {
    return userHandle.clone();
  }
}
