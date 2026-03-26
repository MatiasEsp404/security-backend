package com.matias.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Usuario {
    
    private Integer id;
    private String email;
    private String password;
    private String nombre;
    private String apellido;
    private Instant fechaCreacion;
    private Instant fechaActualizacion;
    private Boolean activo;
    private Boolean emailVerificado;
    private Set<UsuarioRol> usuarioRoles;

    public Usuario() {
    }

    public Usuario(Integer id, String email, String password, String nombre, String apellido,
                   Instant fechaCreacion, Instant fechaActualizacion, Boolean activo,
                   Boolean emailVerificado, Set<UsuarioRol> usuarioRoles) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.activo = activo;
        this.emailVerificado = emailVerificado;
        this.usuarioRoles = usuarioRoles;
    }

    public Usuario(String email, String password) {
        this.email = email;
        this.password = password;
        this.fechaCreacion = Instant.now();
        this.activo = true;
        this.emailVerificado = false;
    }

    /**
     * Método helper para obtener solo los roles (sin información de asignación).
     * Mantiene compatibilidad con código existente que solo necesita los roles.
     *
     * @return Set de roles del usuario
     */
    public Set<Rol> getRoles() {
        if (usuarioRoles == null) {
            return Set.of();
        }
        return usuarioRoles.stream()
                .map(UsuarioRol::getRol)
                .collect(Collectors.toSet());
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Instant getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(Instant fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Boolean getEmailVerificado() {
        return emailVerificado;
    }

    public void setEmailVerificado(Boolean emailVerificado) {
        this.emailVerificado = emailVerificado;
    }

    public Set<UsuarioRol> getUsuarioRoles() {
        return usuarioRoles;
    }

    public void setUsuarioRoles(Set<UsuarioRol> usuarioRoles) {
        this.usuarioRoles = usuarioRoles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id) &&
                Objects.equals(email, usuario.email) &&
                Objects.equals(password, usuario.password) &&
                Objects.equals(nombre, usuario.nombre) &&
                Objects.equals(apellido, usuario.apellido) &&
                Objects.equals(fechaCreacion, usuario.fechaCreacion) &&
                Objects.equals(fechaActualizacion, usuario.fechaActualizacion) &&
                Objects.equals(activo, usuario.activo) &&
                Objects.equals(emailVerificado, usuario.emailVerificado) &&
                Objects.equals(usuarioRoles, usuario.usuarioRoles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, password, nombre, apellido, fechaCreacion,
                fechaActualizacion, activo, emailVerificado, usuarioRoles);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                ", fechaActualizacion=" + fechaActualizacion +
                ", activo=" + activo +
                ", emailVerificado=" + emailVerificado +
                ", usuarioRoles=" + usuarioRoles +
                '}';
    }

    // Builder Pattern

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer id;
        private String email;
        private String password;
        private String nombre;
        private String apellido;
        private Instant fechaCreacion;
        private Instant fechaActualizacion;
        private Boolean activo;
        private Boolean emailVerificado;
        private Set<UsuarioRol> usuarioRoles;

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public Builder apellido(String apellido) {
            this.apellido = apellido;
            return this;
        }

        public Builder fechaCreacion(Instant fechaCreacion) {
            this.fechaCreacion = fechaCreacion;
            return this;
        }

        public Builder fechaActualizacion(Instant fechaActualizacion) {
            this.fechaActualizacion = fechaActualizacion;
            return this;
        }

        public Builder activo(Boolean activo) {
            this.activo = activo;
            return this;
        }

        public Builder emailVerificado(Boolean emailVerificado) {
            this.emailVerificado = emailVerificado;
            return this;
        }

        public Builder usuarioRoles(Set<UsuarioRol> usuarioRoles) {
            this.usuarioRoles = usuarioRoles;
            return this;
        }

        public Usuario build() {
            return new Usuario(id, email, password, nombre, apellido, fechaCreacion,
                    fechaActualizacion, activo, emailVerificado, usuarioRoles);
        }
    }
}
