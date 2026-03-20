package com.matias.application.dto.response;

import java.util.List;

/**
 * DTO para respuestas de error.
 * Mensaje principal del error con lista de detalles adicionales.
 */
public record ErrorResponse(
    String mensaje,
    List<String> detalles
) {
}
