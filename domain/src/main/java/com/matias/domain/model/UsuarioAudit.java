package com.matias.domain.model;

import java.time.Instant;

/**
 * Modelo de dominio que representa una revisión de auditoría de un usuario
 */
public record UsuarioAudit(
    Integer usuarioId,
    String email,
    String nombre,
    String apellido,
    Boolean activo,
    Boolean emailVerificado,
    Integer revision,
    Instant fechaRevision,
    String usuarioModificador,
    TipoRevision tipoRevision
) {
}
