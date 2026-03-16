package com.matias.domain.model;

import java.time.Instant;

public class EmailVerificacionIntento {
    private Integer id;
    private Integer usuarioId;
    private Instant fechaIntento;
    private String ipOrigen;

    public EmailVerificacionIntento() {
    }

    public EmailVerificacionIntento(Integer usuarioId, String ipOrigen) {
        this.usuarioId = usuarioId;
        this.ipOrigen = ipOrigen;
        this.fechaIntento = Instant.now();
    }

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

    public Instant getFechaIntento() {
        return fechaIntento;
    }

    public void setFechaIntento(Instant fechaIntento) {
        this.fechaIntento = fechaIntento;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    public void setIpOrigen(String ipOrigen) {
        this.ipOrigen = ipOrigen;
    }
}
