package com.matias.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}
