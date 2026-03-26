package com.matias.application.dto.internal;

import java.util.List;

/**
 * Resultado paginado genérico para la capa de aplicación.
 * Desacopla el controlador de los detalles del repositorio.
 */
public record PageResult<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    long totalElements,
    int totalPages,
    boolean isFirst,
    boolean isLast
) {
}
