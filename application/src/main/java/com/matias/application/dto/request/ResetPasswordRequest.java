package com.matias.application.dto.request;

import com.matias.application.validation.Password;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para confirmar el reseteo de contraseña.
 * Contrato limpio de la capa de aplicación sin dependencias de infraestructura.
 * <p>
 * Contiene el token de reseteo recibido por email y la nueva contraseña.
 */
public record ResetPasswordRequest(

    @NotBlank(message = "El token es obligatorio")
    String token,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 32, message = "La contraseña debe tener entre 8 y 32 caracteres")
    @Password
    String nuevaPassword
) {

}
