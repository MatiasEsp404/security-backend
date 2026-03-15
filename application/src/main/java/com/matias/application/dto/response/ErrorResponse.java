package com.matias.application.dto.response;

import java.util.List;

/**
 * DTO para respuestas de error.
 */
public record ErrorResponse(
    String mensaje,
    List<String> detalles
) {
}
