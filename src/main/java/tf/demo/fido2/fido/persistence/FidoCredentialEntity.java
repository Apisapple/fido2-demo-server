package tf.demo.fido2.fido.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "fido_credentials")
public class FidoCredentialEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private FidoUserEntity user;

  @Lob
  @Column(nullable = false, unique = true)
  private byte[] credentialId;

  @Lob
  @Column(nullable = false)
  private byte[] publicKeyCose;

  @Column(nullable = false)
  private long signatureCount;

  protected FidoCredentialEntity() {}

  public FidoCredentialEntity(
      FidoUserEntity user, byte[] credentialId, byte[] publicKeyCose, long signatureCount) {
    this.user = user;
    this.credentialId = credentialId;
    this.publicKeyCose = publicKeyCose;
    this.signatureCount = signatureCount;
  }

  public FidoUserEntity getUser() {
    return user;
  }

  public byte[] getCredentialId() {
    return credentialId.clone();
  }

  public byte[] getPublicKeyCose() {
    return publicKeyCose.clone();
  }

  public long getSignatureCount() {
    return signatureCount;
  }

  public void updateSignatureCount(long signatureCount) {
    this.signatureCount = signatureCount;
  }
}
