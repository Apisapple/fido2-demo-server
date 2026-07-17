package tf.demo.fido2.fido.config;

import com.yubico.webauthn.data.AuthenticatorAttachment;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserVerificationRequirement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("fido")
public record FidoProperties(
        @NotBlank String rpId,
        @NotBlank String rpName,
        @NotEmpty Set<String> origins,
        @NotNull Duration challengeTtl,
        @NotNull @DefaultValue("PT24H") Duration ceremonyRetention,
        @NotNull @DefaultValue("PT1M") Duration ceremonyCleanupInterval,
        @NotNull @DefaultValue("REQUIRED") UserVerificationRequirement userVerification,
        @NotNull @DefaultValue("PREFERRED") ResidentKeyRequirement residentKey,
        AuthenticatorAttachment authenticatorAttachment,
        @NotNull @DefaultValue("DEMO_UNTRUSTED") AttestationPolicy attestationPolicy) {
    public enum AttestationPolicy {
        DEMO_UNTRUSTED,
        TRUSTED
    }
}
