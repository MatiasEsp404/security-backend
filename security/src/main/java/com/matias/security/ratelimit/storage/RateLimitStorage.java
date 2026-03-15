package com.matias.security.ratelimit.storage;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;

/**
 * Almacenamiento para buckets de rate limiting.
 */
public interface RateLimitStorage {

  /**
   * Obtiene el bucket asociado a la clave, creándolo si no existe.
   *
   * @param key           clave del bucket
   * @param configuration configuración del bucket
   * @return bucket correspondiente
   */
  Bucket resolveBucket(String key, BucketConfiguration configuration);

}
