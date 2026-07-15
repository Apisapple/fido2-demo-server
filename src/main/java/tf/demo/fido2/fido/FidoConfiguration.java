package tf.demo.fido2.fido;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FidoProperties.class)
class FidoConfiguration {

  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  RelyingParty relyingParty(FidoProperties properties, FidoCredentialRepository credentials) {
    return RelyingParty.builder()
        .identity(
            RelyingPartyIdentity.builder().id(properties.rpId()).name(properties.rpName()).build())
        .credentialRepository(credentials)
        .origins(properties.origins())
        .allowUntrustedAttestation(true)
        .validateSignatureCounter(true)
        .build();
  }
}
