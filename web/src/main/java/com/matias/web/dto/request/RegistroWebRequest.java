package com.matias.web.dto.request;

import com.matias.application.validation.CharactersWithWhiteSpaces;
import com.matias.application.validation.Password;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de la capa web para registro de usuario.
 * Contiene anotaciones de documentación OpenAPI/Swagger.
 */
@Schema(description = "Datos de registro de un nuevo usuario")
public record RegistroWebRequest(

        @Schema(description = "Email del usuario", example = "usuario@example.com")
        @NotBlank(message = "El email no puede estar vacío")
        @Email(message = "Debe ingresar un email válido")
        String email,

        @Schema(description = "Contraseña", example = "MiPassword123!")
        @NotBlank(message = "La contraseña no puede estar vacía")
        @Size(min = 8, max = 32, message = "La contraseña debe tener entre 8 y 32 caracteres")
        @Password
        String password,

        @Schema(description = "Nombre del usuario", example = "Juan")
        @NotBlank(message = "El nombre no puede estar vacío")
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
        @CharactersWithWhiteSpaces
        String nombre,

        @Schema(description = "Apellido del usuario", example = "Pérez")
        @NotBlank(message = "El apellido no puede estar vacío")
        @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
        @CharactersWithWhiteSpaces
        String apellido

) {

}
