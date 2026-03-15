package com.matias.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales de acceso")
public record LogueoRequest(

        @Schema(description = "Email del usuario", example = "usuario@example.com")
        @Email(message = "Formato de email inválido.")
        @NotBlank(message = "El email es obligatorio.")
        String email,

        @Schema(description = "Contraseña", example = "MiPassword123!")
        @NotBlank(message = "La contraseña es obligatoria.")
        String password
) {

}
