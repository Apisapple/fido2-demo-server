package tf.demo.fido2.fido.config;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import tf.demo.fido2.fido.persistence.FidoCredentialRepository;

@Configuration
@EnableConfigurationProperties(FidoProperties.class)
class FidoConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    RelyingParty relyingParty(
            FidoProperties properties,
            FidoCredentialRepository credentials,
            Environment environment) {
        if (environment.matchesProfiles("prod")
                && properties.attestationPolicy()
                        == FidoProperties.AttestationPolicy.DEMO_UNTRUSTED) {
            throw new IllegalStateException(
                    "The prod profile requires a trusted FIDO attestation policy");
        }
        return RelyingParty.builder()
                .identity(
                        RelyingPartyIdentity.builder()
                                .id(properties.rpId())
                                .name(properties.rpName())
                                .build())
                .credentialRepository(credentials)
                .origins(properties.origins())
                .allowUntrustedAttestation(
                        properties.attestationPolicy()
                                == FidoProperties.AttestationPolicy.DEMO_UNTRUSTED)
                .validateSignatureCounter(true)
                .build();
    }
}
