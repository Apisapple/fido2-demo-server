package tf.demo.fido2.fido.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fido_users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    public FidoUserEntity(String username, String displayName, byte[] userHandle) {
        this.username = username;
        this.displayName = displayName;
        this.userHandle = userHandle;
    }

    public byte[] getUserHandle() {
        return userHandle.clone();
    }
}
