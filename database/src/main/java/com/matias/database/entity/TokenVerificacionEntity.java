package com.matias.database.entity;

import com.matias.domain.model.EstadoTokenVerificacion;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "tokens_verificacion")
public class TokenVerificacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String token;

    @Column(nullable = false)
    private Instant expiracion;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTokenVerificacion estado;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    public TokenVerificacionEntity() {
    }

    public TokenVerificacionEntity(String token, Instant expiracion, UsuarioEntity usuario) {
        this.token = token;
        this.expiracion = expiracion;
        this.usuario = usuario;
        this.estado = EstadoTokenVerificacion.PENDIENTE;
        this.fechaCreacion = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = Instant.now();
        }
        if (this.estado == null) {
            this.estado = EstadoTokenVerificacion.PENDIENTE;
        }
        if (this.token == null || this.token.isBlank()) {
            throw new IllegalStateException("No se puede persistir un token vacío");
        }
        if (this.usuario == null) {
            throw new IllegalStateException("Debe especificar el usuario al que pertenece el token");
        }
        if (this.expiracion == null) {
            throw new IllegalStateException("Debe especificar la fecha de expiración del token");
        }
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

    public UsuarioEntity getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioEntity usuario) {
        this.usuario = usuario;
    }
}
