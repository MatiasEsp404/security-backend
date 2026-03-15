package com.matias.security.ratelimit.storage.impl;

import com.matias.security.ratelimit.storage.RateLimitStorage;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Almacenamiento in-memory para rate-limit.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
public class InMemoryRateLimitStorage implements RateLimitStorage {

  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
  private final Map<String, Long> lastAccessTime = new ConcurrentHashMap<>();

  @PostConstruct
  private void notificar() {
    log.info("InMemoryRateLimitStorage inicializado");
  }

  @Override
  public Bucket resolveBucket(String key, BucketConfiguration configuration) {
    lastAccessTime.put(key, System.currentTimeMillis());
    return buckets.computeIfAbsent(key, k -> {
      log.debug("Creando bucket in-memory: {}", key);
      return Bucket.builder()
          .addLimit(configuration.getBandwidths()[0])
          .build();
    });
  }

  @Scheduled(fixedRate = 300000) // Cada 5 minutos
  public void cleanupExpiredBuckets() {
    long now = System.currentTimeMillis();
    long expirationThreshold = 10L * 60 * 1000; // 10 minutos

    int removedCount = 0;
    for (Map.Entry<String, Long> entry : lastAccessTime.entrySet()) {
      if (now - entry.getValue() > expirationThreshold) {
        String key = entry.getKey();
        buckets.remove(key);
        lastAccessTime.remove(key);
        removedCount++;
      }
    }

    if (removedCount > 0) {
      log.info("Limpieza in-memory: {} buckets eliminados. Activos: {}",
          removedCount, buckets.size());
    }
  }

  public int getActiveBucketsCount() {
    return buckets.size();
  }

}
