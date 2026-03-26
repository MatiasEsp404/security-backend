package com.matias.application.dto.internal;

import lombok.Builder;

/**
 * Query para paginación y ordenamiento.
 * Objeto de la capa de aplicación que desacopla el controlador de los detalles del repositorio.
 */
@Builder
public record PageQuery(
    int page,
    int size,
    String sortBy,
    SortDirection direction
) {
    public enum SortDirection {
        ASC, DESC
    }
}
