package com.matias.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request para solicitudes que solo requieren el email del usuario.
 * <p>
 * Usado para:
 * - Reenviar email de verificación
 * - Solicitar reseteo de contraseña
 */
@Schema(description = "Email para solicitudes de verificación y reseteo")
public record EmailRequest(

    @Schema(description = "Email del usuario", example = "usuario@example.com")
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    String email
) {

}
