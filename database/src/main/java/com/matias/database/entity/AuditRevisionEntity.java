package com.matias.database.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import java.io.Serializable;

/**
 * Entidad personalizada de revisión para Hibernate Envers.
 * Almacena información adicional sobre cada cambio auditado.
 */
@Entity
@Table(name = "revinfo")
@RevisionEntity(AuditRevisionListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditRevisionEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    @Column(name = "rev")
    private Integer rev;

    @RevisionTimestamp
    @Column(name = "revtstmp")
    private Long revtstmp;

    @Column(name = "usuario_email", length = 100)
    private String usuarioEmail;

    @Column(name = "usuario_id")
    private Integer usuarioId;
}
