package com.matias.application.dto.request;

import com.matias.application.validation.Password;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request para confirmar el reseteo de contraseña.
 * <p>
 * Contiene el token de reseteo recibido por email y la nueva contraseña.
 */
@Schema(description = "Datos para confirmar el reseteo de contraseña")
public record ResetPasswordRequest(

    @Schema(description = "Token de reseteo", example = "abc123def456")
    @NotBlank(message = "El token es obligatorio")
    String token,

    @Schema(description = "Nueva contraseña", example = "NuevaPassword123!")
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 32, message = "La contraseña debe tener entre 8 y 32 caracteres")
    @Password
    String nuevaPassword
) {

}
