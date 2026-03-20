package com.matias.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de la capa web para solicitudes basadas en email.
 * Contiene anotaciones de documentación OpenAPI/Swagger.
 * <p>
 * Usado para:
 * - Reenviar email de verificación
 * - Solicitar reseteo de contraseña
 */
@Schema(description = "Email para solicitudes de verificación y reseteo")
public record EmailWebRequest(

    @Schema(description = "Email del usuario", example = "usuario@example.com")
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    String email
) {

}
