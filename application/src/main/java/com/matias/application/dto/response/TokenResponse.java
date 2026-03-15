package com.matias.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token JWT generado para el usuario")
public record TokenResponse(

        @Schema(description = "Access Token JWT (duración corta)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken
) {

}
