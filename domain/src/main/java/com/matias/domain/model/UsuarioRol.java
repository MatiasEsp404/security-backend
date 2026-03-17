package com.matias.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Modelo de dominio que representa la asignación de un rol a un usuario.
 * Permite rastrear cuándo se asignó cada rol.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRol {
    
    private Integer id;
    private Integer usuarioId;
    private Rol rol;
    private Instant fechaAsignacion;

    /**
     * Constructor simplificado para crear nuevas asignaciones de rol.
     * La fecha de asignación se establece automáticamente al momento actual.
     *
     * @param usuarioId ID del usuario
     * @param rol Rol a asignar
     */
    public UsuarioRol(Integer usuarioId, Rol rol) {
        this.usuarioId = usuarioId;
        this.rol = rol;
        this.fechaAsignacion = Instant.now();
    }
}
