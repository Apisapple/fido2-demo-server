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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fido_credentials")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

  public FidoCredentialEntity(
      FidoUserEntity user, byte[] credentialId, byte[] publicKeyCose, long signatureCount) {
    this.user = user;
    this.credentialId = credentialId;
    this.publicKeyCose = publicKeyCose;
    this.signatureCount = signatureCount;
  }

  public byte[] getCredentialId() {
    return credentialId.clone();
  }

  public byte[] getPublicKeyCose() {
    return publicKeyCose.clone();
  }

  public void updateSignatureCount(long signatureCount) {
    this.signatureCount = signatureCount;
  }
}
