package com.matias.application.dto.response;

import com.matias.domain.model.Rol;

import java.util.Set;

/**
 * DTO de respuesta con información del usuario.
 * Contrato limpio de la capa de aplicación sin dependencias de infraestructura.
 */
public record UsuarioResponse(
    Long id,
    String email,
    String nombre,
    String apellido,
    Set<Rol> roles,
    Boolean activo,
    Boolean emailVerificado
) {
}
