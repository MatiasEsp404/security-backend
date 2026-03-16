package com.matias.database.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "password_reset_intentos")
public class PasswordResetIntentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    @Column(name = "fecha_intento", nullable = false)
    private Instant fechaIntento;

    public PasswordResetIntentoEntity(UsuarioEntity usuario, String ipOrigen) {
        this.usuario = usuario;
        this.ipOrigen = ipOrigen;
        this.fechaIntento = Instant.now();
    }
}
