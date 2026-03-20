package com.matias.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de la capa web con el token de acceso JWT.
 * Contiene anotaciones de documentación OpenAPI/Swagger.
 */
@Schema(description = "Token JWT generado para el usuario")
public record TokenWebResponse(

        @Schema(description = "Access Token JWT (duración corta)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken
) {

}
