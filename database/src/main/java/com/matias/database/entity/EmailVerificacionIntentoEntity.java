package com.matias.database.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "email_verificacion_intentos")
public class EmailVerificacionIntentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @Column(name = "fecha_intento", nullable = false)
    private Instant fechaIntento;

    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    public EmailVerificacionIntentoEntity() {
    }

    public EmailVerificacionIntentoEntity(UsuarioEntity usuario, String ipOrigen) {
        this.usuario = usuario;
        this.ipOrigen = ipOrigen;
        this.fechaIntento = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.fechaIntento == null) {
            this.fechaIntento = Instant.now();
        }
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UsuarioEntity getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioEntity usuario) {
        this.usuario = usuario;
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
