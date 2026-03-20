package com.matias.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para solicitudes que solo requieren el email del usuario.
 * Contrato limpio de la capa de aplicación sin dependencias de infraestructura.
 * <p>
 * Usado para:
 * - Reenviar email de verificación
 * - Solicitar reseteo de contraseña
 */
public record EmailRequest(

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    String email
) {

}
