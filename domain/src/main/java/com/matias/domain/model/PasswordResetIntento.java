package com.matias.domain.model;

import java.time.Instant;

/**
 * Modelo de dominio que representa un intento de reseteo de contraseña.
 * Utilizado para implementar lógica anti-abuso.
 */
public class PasswordResetIntento {

    private Long id;
    private Usuario usuario;
    private String ipOrigen;
    private Instant fechaIntento;

    public PasswordResetIntento() {
    }

    public PasswordResetIntento(Usuario usuario, String ipOrigen) {
        this.usuario = usuario;
        this.ipOrigen = ipOrigen;
        this.fechaIntento = Instant.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    public void setIpOrigen(String ipOrigen) {
        this.ipOrigen = ipOrigen;
    }

    public Instant getFechaIntento() {
        return fechaIntento;
    }

    public void setFechaIntento(Instant fechaIntento) {
        this.fechaIntento = fechaIntento;
    }
}
