package com.matias.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de la capa web para credenciales de autenticación.
 * Contiene anotaciones de documentación OpenAPI/Swagger.
 */
@Schema(description = "Credenciales de acceso")
public record LogueoWebRequest(

        @Schema(description = "Email del usuario", example = "usuario@example.com")
        @Email(message = "Formato de email inválido.")
        @NotBlank(message = "El email es obligatorio.")
        String email,

        @Schema(description = "Contraseña", example = "MiPassword123!")
        @NotBlank(message = "La contraseña es obligatoria.")
        String password
) {

}
