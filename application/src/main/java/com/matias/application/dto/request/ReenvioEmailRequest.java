package com.matias.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ReenvioEmailRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe ser válido")
        String email
) {
}
