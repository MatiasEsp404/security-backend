package com.matias.domain.model;

import java.time.Instant;

public class TokenVerificacion {
    
    private Integer id;
    private String token;
    private Instant expiracion;
    private Instant fechaCreacion;
    private EstadoTokenVerificacion estado;
    private Integer usuarioId;

    public TokenVerificacion() {
    }

    public TokenVerificacion(String token, Instant expiracion, Integer usuarioId) {
        this.token = token;
        this.expiracion = expiracion;
        this.usuarioId = usuarioId;
        this.estado = EstadoTokenVerificacion.PENDIENTE;
        this.fechaCreacion = Instant.now();
    }

    public boolean estaExpirado() {
        return Instant.now().isAfter(this.expiracion);
    }

    public boolean estaUsado() {
        return this.estado == EstadoTokenVerificacion.USADO;
    }

    public void marcarComoUsado() {
        this.estado = EstadoTokenVerificacion.USADO;
    }

    public void marcarComoExpirado() {
        this.estado = EstadoTokenVerificacion.EXPIRADO;
    }

    public boolean esValido() {
        return !estaExpirado() && !estaUsado() && estado == EstadoTokenVerificacion.PENDIENTE;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public EstadoTokenVerificacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoTokenVerificacion estado) {
        this.estado = estado;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }
}
