package tf.demo.fido2.fido.config;

import java.time.Duration;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("fido")
public record FidoProperties(
    String rpId, String rpName, Set<String> origins, Duration challengeTtl) {}
