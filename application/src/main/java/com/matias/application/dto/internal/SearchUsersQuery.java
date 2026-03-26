package com.matias.application.dto.internal;

import com.matias.domain.model.Rol;
import lombok.Builder;

import java.time.Instant;
import java.util.Set;

/**
 * Query para buscar usuarios con filtros.
 * Objeto de la capa de aplicación que desacopla el controlador de los detalles del repositorio.
 */
@Builder
public record SearchUsersQuery(
    String search,
    Boolean activo,
    Boolean emailVerificado,
    Set<Rol> roles,
    Instant fechaDesde,
    Instant fechaHasta
) {
}
