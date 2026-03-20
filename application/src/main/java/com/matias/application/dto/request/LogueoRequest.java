package com.matias.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para credenciales de autenticación.
 * Contrato limpio de la capa de aplicación sin dependencias de infraestructura.
 */
public record LogueoRequest(

        @Email(message = "Formato de email inválido.")
        @NotBlank(message = "El email es obligatorio.")
        String email,

        @NotBlank(message = "La contraseña es obligatoria.")
        String password
) {

}
