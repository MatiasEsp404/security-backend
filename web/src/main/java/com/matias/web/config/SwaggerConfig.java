package com.matias.web.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de SpringDoc OpenAPI para documentación automática de la API.
 * 
 * <p>Proporciona una interfaz Swagger UI interactiva para explorar y probar
 * los endpoints REST de la aplicación.
 * 
 * <p>Incluye configuración de autenticación JWT mediante Bearer tokens.
 * 
 * @author Matias
 * @since 1.0.0
 */
@Configuration
public class SwaggerConfig {

    /**
     * Configura la especificación OpenAPI personalizada para la aplicación.
     * 
     * <p>Define:
     * <ul>
     *   <li>Información general de la API (título, versión, descripción, contacto, licencia)</li>
     *   <li>Esquema de seguridad JWT Bearer</li>
     *   <li>Requisitos de seguridad globales</li>
     * </ul>
     * 
     * @return instancia configurada de OpenAPI
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Security Backend API")
                        .version("1.0.0")
                        .description("""
                                API REST para sistema de seguridad con autenticación JWT.
                                
                                Incluye funcionalidades de:
                                - Registro y autenticación de usuarios
                                - Verificación de email
                                - Recuperación de contraseña
                                - Gestión de usuarios y roles
                                - Administración del sistema
                                """)
                        .contact(new Contact()
                                .name("Matias")
                                .email("contact@example.com")
                                .url("https://github.com/MatiasEsp404/security-backend"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingrese el token JWT obtenido del endpoint /auth/login")));
    }
}
