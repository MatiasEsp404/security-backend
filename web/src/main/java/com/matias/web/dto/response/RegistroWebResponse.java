package com.matias.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de la capa web con datos del usuario registrado.
 * Contiene anotaciones de documentación OpenAPI/Swagger.
 */
@Schema(description = "Datos del usuario registrado")
public record RegistroWebResponse(

        @Schema(description = "ID del usuario", example = "42")
        Integer id,

        @Schema(description = "Email del usuario", example = "usuario@example.com")
        String email
) {

}
