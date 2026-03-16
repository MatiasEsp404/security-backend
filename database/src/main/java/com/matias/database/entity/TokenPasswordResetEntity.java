package com.matias.database.entity;

import com.matias.domain.model.EstadoTokenVerificacion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tokens_password_reset")
public class TokenPasswordResetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @Column(nullable = false)
    private Instant expiracion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTokenVerificacion estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    public TokenPasswordResetEntity(String token, Instant expiracion, UsuarioEntity usuario) {
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
}
