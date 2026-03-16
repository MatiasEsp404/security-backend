package com.matias.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Request para confirmar el reseteo de contraseña")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmarResetPasswordRequest {

    @Schema(description = "Token de reseteo recibido por email", example = "abc123def456")
    @NotBlank(message = "El token es obligatorio")
    private String token;

    @Schema(description = "Nueva contraseña", example = "NuevaPassword123!")
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
        message = "La contraseña debe contener al menos un dígito, una minúscula, una mayúscula y un carácter especial"
    )
    private String nuevaPassword;
}
