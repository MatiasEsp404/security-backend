package com.matias.web.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@ConditionalOnProperty(
    name = "springdoc.api-docs.enabled",
    havingValue = "true",
    matchIfMissing = false
)
@Configuration
public class SwaggerConfig {

  private final String projectName = "Security Backend API";
  private final String projectDescription = "API REST para sistema de seguridad con autenticación JWT";

  @Value("${project.version}")
  private String projectVersion;

  @PostConstruct
  public void informar() {
    log.info("Swagger activado.");
  }

  @Bean
  public OpenAPI customizeOpenApi() {
    final String securitySchemeName = "bearerAuth";
    return new OpenAPI()
        .info(new Info()
            .title(projectName)
            .description(projectDescription)
            .version(projectVersion)
        )
        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
        .components(new Components()
            .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")));
  }
}
