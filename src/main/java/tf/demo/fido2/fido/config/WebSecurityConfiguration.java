package tf.demo.fido2.fido.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(WebSecurityProperties.class)
class WebSecurityConfiguration implements WebMvcConfigurer {
  private final WebSecurityProperties properties;

  WebSecurityConfiguration(WebSecurityProperties properties) {
    this.properties = properties;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    if (!properties.corsAllowedOrigins().isEmpty()) {
      registry
          .addMapping("/api/**")
          .allowedOrigins(properties.corsAllowedOrigins().toArray(String[]::new))
          .allowedMethods("GET", "POST", "DELETE");
    }
  }

  @Configuration
  @Order(Ordered.HIGHEST_PRECEDENCE)
  static class FidoRequestLimitFilter extends OncePerRequestFilter {
    private final WebSecurityProperties properties;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    FidoRequestLimitFilter(WebSecurityProperties properties) {
      this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
      return !request.getRequestURI().startsWith("/api/fido/");
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {
      if (request.getContentLengthLong() > properties.maxRequestBytes()) {
        reject(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "REQUEST_TOO_LARGE");
        return;
      }
      String key = request.getRemoteAddr() + ':' + request.getRequestURI();
      Window window = windows.compute(key, (ignored, value) -> allow(value, Instant.now()));
      if (window.requests() > properties.rateLimitCapacity()) {
        reject(response, 429, "RATE_LIMITED");
        return;
      }
      chain.doFilter(request, response);
    }

    private Window allow(Window current, Instant now) {
      if (current == null
          || !current.startedAt().plus(properties.rateLimitRefillPeriod()).isAfter(now)) {
        return new Window(now, 1);
      }
      return new Window(current.startedAt(), current.requests() + 1);
    }

    private void reject(HttpServletResponse response, int status, String code) throws IOException {
      response.setStatus(status);
      response.setContentType("application/json");
      response.getWriter().write("{\"code\":\"" + code + "\",\"message\":\"Request rejected.\"}");
    }

    private record Window(Instant startedAt, int requests) {}
  }
}
