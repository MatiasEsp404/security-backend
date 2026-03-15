package com.matias.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos del usuario registrado")
public record RegistroResponse(

        @Schema(description = "ID del usuario", example = "42")
        Integer id,

        @Schema(description = "Email del usuario", example = "usuario@example.com")
        String email
) {

}
