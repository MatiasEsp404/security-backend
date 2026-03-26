package com.matias.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Modelo de dominio que representa la asignación de un rol a un usuario.
 * Permite rastrear cuándo se asignó cada rol.
 */
public class UsuarioRol {
    
    private Integer id;
    private Integer usuarioId;
    private Rol rol;
    private Instant fechaAsignacion;

    public UsuarioRol() {
    }

    public UsuarioRol(Integer id, Integer usuarioId, Rol rol, Instant fechaAsignacion) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.rol = rol;
        this.fechaAsignacion = fechaAsignacion;
    }

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

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Instant getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(Instant fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioRol that = (UsuarioRol) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(usuarioId, that.usuarioId) &&
                rol == that.rol &&
                Objects.equals(fechaAsignacion, that.fechaAsignacion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, usuarioId, rol, fechaAsignacion);
    }

    @Override
    public String toString() {
        return "UsuarioRol{" +
                "id=" + id +
                ", usuarioId=" + usuarioId +
                ", rol=" + rol +
                ", fechaAsignacion=" + fechaAsignacion +
                '}';
    }

    // Builder Pattern

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer id;
        private Integer usuarioId;
        private Rol rol;
        private Instant fechaAsignacion;

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder usuarioId(Integer usuarioId) {
            this.usuarioId = usuarioId;
            return this;
        }

        public Builder rol(Rol rol) {
            this.rol = rol;
            return this;
        }

        public Builder fechaAsignacion(Instant fechaAsignacion) {
            this.fechaAsignacion = fechaAsignacion;
            return this;
        }

        public UsuarioRol build() {
            return new UsuarioRol(id, usuarioId, rol, fechaAsignacion);
        }
    }
}
