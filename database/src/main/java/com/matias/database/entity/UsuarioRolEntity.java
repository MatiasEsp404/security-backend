package com.matias.database.entity;

import com.matias.domain.model.Rol;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "usuarios_roles")
@Audited
public class UsuarioRolEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuarios_id", nullable = false)
    private UsuarioEntity usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "roles", nullable = false)
    private Rol rol;

    @Builder.Default
    @Column(name = "fecha_asignacion", nullable = false)
    private Instant fechaAsignacion = Instant.now();

    public UsuarioRolEntity(UsuarioEntity usuario, Rol rol) {
        this.usuario = usuario;
        this.rol = rol;
        this.fechaAsignacion = Instant.now();
    }

    @PrePersist
    @PreUpdate
    protected void validarReglasDeNegocio() {
        if (this.fechaAsignacion == null) {
            this.fechaAsignacion = Instant.now();
        }
    }
}
