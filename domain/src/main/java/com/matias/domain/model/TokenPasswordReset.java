package com.matias.domain.model;

import java.time.Instant;

/**
 * Modelo de dominio que representa un token de reseteo de contraseña.
 */
public class TokenPasswordReset {

    private Long id;
    private String token;
    private Instant expiracion;
    private EstadoTokenVerificacion estado;
    private Usuario usuario;
    private Instant fechaCreacion;

    public TokenPasswordReset() {
    }

    public TokenPasswordReset(String token, Instant expiracion, Usuario usuario) {
        this.token = token;
        this.expiracion = expiracion;
        this.usuario = usuario;
        this.estado = EstadoTokenVerificacion.PENDIENTE;
        this.fechaCreacion = Instant.now();
    }

    public boolean estaExpirado() {
        return Instant.now().isAfter(expiracion);
    }

    public void marcarComoUsado() {
        this.estado = EstadoTokenVerificacion.USADO;
    }

    public void marcarComoExpirado() {
        this.estado = EstadoTokenVerificacion.EXPIRADO;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiracion() {
        return expiracion;
    }

    public void setExpiracion(Instant expiracion) {
        this.expiracion = expiracion;
    }

    public EstadoTokenVerificacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoTokenVerificacion estado) {
        this.estado = estado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
