package tf.demo.fido2.fido.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("web")
public record WebSecurityProperties(
        @DefaultValue Set<String> corsAllowedOrigins,
        @Min(1) @DefaultValue("65536") long maxRequestBytes,
        @Min(1) @DefaultValue("20") int rateLimitCapacity,
        @NotNull @DefaultValue("PT1M") Duration rateLimitRefillPeriod) {}
