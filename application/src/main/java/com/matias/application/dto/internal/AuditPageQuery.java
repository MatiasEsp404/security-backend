package com.matias.application.dto.internal;

import lombok.Builder;

/**
 * Query para paginación de auditoría.
 * Objeto de la capa de aplicación que desacopla el controlador de los detalles del repositorio.
 */
@Builder
public record AuditPageQuery(
    int page,
    int size,
    SortDirection direction
) {
    public enum SortDirection {
        ASC, DESC
    }
}
