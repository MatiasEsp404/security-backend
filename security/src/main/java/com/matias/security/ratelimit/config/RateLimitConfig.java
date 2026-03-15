package com.matias.security.ratelimit.config;

import com.matias.security.ratelimit.properties.RateLimitProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@Configuration
@EnableScheduling
@EnableConfigurationProperties(RateLimitProperties.class)
@ConditionalOnProperty(name = "app.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitConfig {

  public RateLimitConfig() {
    log.info("Rate Limiting habilitado con almacenamiento IN-MEMORY");
  }

}
