package com.matias.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO para respuestas de error.
 */
@Schema(description = "Respuesta de error con mensaje y detalles")
public record ErrorResponse(

    @Schema(description = "Mensaje principal del error", example = "Error de validación")
    String mensaje,

    @Schema(description = "Lista de detalles adicionales del error", example = "[\"El email no puede estar vacío\", \"La contraseña debe tener al menos 8 caracteres\"]")
    List<String> detalles
) {
}
