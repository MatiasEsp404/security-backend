package com.matias.security.ratelimit.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matias.application.dto.response.ErrorResponse;
import com.matias.security.ratelimit.storage.RateLimitStorage;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro de rate-limit por IP y endpoint.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitFilter extends OncePerRequestFilter {

  private final RateLimitStorage storage;
  private final ObjectMapper objectMapper;

  @PostConstruct
  public void informar() {
    log.info("Rate Limit Filter activado. Storage: {}", storage.getClass().getSimpleName());
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    String ip = request.getRemoteAddr();
    String path = request.getRequestURI();
    String key = "rate-limit:" + ip + ":" + path;

    try {
      BucketConfiguration bucketConfig = createBucketConfigForPath(path);
      Bucket bucket = storage.resolveBucket(key, bucketConfig);

      if (bucket.tryConsume(1)) {
        filterChain.doFilter(request, response);
      } else {
        log.warn("Rate limit excedido. IP {} Path {}", ip, path);
        writeErrorResponse(response);
      }
    } catch (Exception e) {
      log.error("Error en storage de rate-limit. IP {} Path {} - Permitiendo request (fail-open)",
          ip, path, e);
      filterChain.doFilter(request, response);
    }
  }

  private BucketConfiguration createBucketConfigForPath(String path) {
    if (path.contains("/auth/login")) {
      return createBucketConfig(5, 60); // 5 intentos por minuto
    } else if (path.contains("/auth/registro")) {
      return createBucketConfig(3, 300); // 3 registros cada 5 minutos
    } else if (path.contains("/auth/resend-verification")) {
      return createBucketConfig(3, 900); // 3 reenvíos cada 15 minutos
    } else if (path.contains("/auth/refresh")) {
      return createBucketConfig(20, 60); // 20 refreshes por minuto
    } else if (path.contains("/auth/password-reset/request")) {
      return createBucketConfig(3, 900); // 3 solicitudes cada 15 minutos
    }
    return createBucketConfig(30, 60); // Default: 30 requests por minuto
  }

  private BucketConfiguration createBucketConfig(int capacity, int seconds) {
    Refill refill = Refill.intervally(capacity, Duration.ofSeconds(seconds));
    return BucketConfiguration.builder()
        .addLimit(Bandwidth.classic(capacity, refill))
        .build();
  }

  private void writeErrorResponse(HttpServletResponse response) throws IOException {
    response.setContentType("application/json;charset=UTF-8");
    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    ErrorResponse errorResponse = new ErrorResponse("Demasiadas solicitudes",
        List.of("Intenta nuevamente más tarde."));
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }

}
