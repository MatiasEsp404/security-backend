package com.matias.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

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
    private Set<Rol> roles;

    public Usuario(String email, String password) {
        this.email = email;
        this.password = password;
        this.fechaCreacion = Instant.now();
        this.activo = true;
        this.emailVerificado = false;
    }
}
