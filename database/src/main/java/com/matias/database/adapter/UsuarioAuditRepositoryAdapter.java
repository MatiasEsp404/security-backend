package com.matias.database.adapter;

import com.matias.database.entity.AuditRevisionEntity;
import com.matias.database.entity.UsuarioEntity;
import com.matias.domain.model.TipoRevision;
import com.matias.domain.model.UsuarioAudit;
import com.matias.domain.port.UsuarioAuditRepositoryPort;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adaptador que implementa el acceso al historial de auditoría de usuarios
 */
@Repository
public class UsuarioAuditRepositoryAdapter implements UsuarioAuditRepositoryPort {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public PageResult<UsuarioAudit> findAuditByUsuarioId(Integer usuarioId, PageRequest pageRequest) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        // Obtener el total de revisiones
        @SuppressWarnings("unchecked")
        List<Number> revisions = auditReader.createQuery()
                .forRevisionsOfEntity(UsuarioEntity.class, false, true)
                .add(AuditEntity.id().eq(usuarioId))
                .getResultList();

        long total = revisions.size();
        
        // Calcular offset
        int offset = pageRequest.pageNumber() * pageRequest.pageSize();

        // Construir query con ordenamiento
        var query = auditReader.createQuery()
                .forRevisionsOfEntity(UsuarioEntity.class, false, true)
                .add(AuditEntity.id().eq(usuarioId));
        
        // Aplicar ordenamiento
        if (pageRequest.sortDirection() == SortDirection.DESC) {
            query.addOrder(AuditEntity.revisionNumber().desc());
        } else {
            query.addOrder(AuditEntity.revisionNumber().asc());
        }

        // Obtener las revisiones paginadas
        @SuppressWarnings("unchecked")
        List<Object[]> results = query
                .setFirstResult(offset)
                .setMaxResults(pageRequest.pageSize())
                .getResultList();

        List<UsuarioAudit> audits = results.stream()
                .map(this::mapToUsuarioAudit)
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) total / pageRequest.pageSize());
        boolean isFirst = pageRequest.pageNumber() == 0;
        boolean isLast = pageRequest.pageNumber() >= totalPages - 1;

        return new PageResult<>(
                audits,
                pageRequest.pageNumber(),
                pageRequest.pageSize(),
                total,
                totalPages,
                isFirst,
                isLast
        );
    }

    private UsuarioAudit mapToUsuarioAudit(Object[] result) {
        UsuarioEntity usuarioEntity = (UsuarioEntity) result[0];
        AuditRevisionEntity revisionEntity = (AuditRevisionEntity) result[1];
        RevisionType revisionType = (RevisionType) result[2];

        return new UsuarioAudit(
                usuarioEntity.getId(),
                usuarioEntity.getEmail(),
                usuarioEntity.getNombre(),
                usuarioEntity.getApellido(),
                usuarioEntity.getActivo(),
                usuarioEntity.getEmailVerificado(),
                revisionEntity.getRev(),
                Instant.ofEpochMilli(revisionEntity.getRevtstmp()),
                revisionEntity.getUsuarioEmail(),
                mapRevisionType(revisionType)
        );
    }

    private TipoRevision mapRevisionType(RevisionType revisionType) {
        return switch (revisionType) {
            case ADD -> TipoRevision.ADD;
            case MOD -> TipoRevision.MOD;
            case DEL -> TipoRevision.DEL;
        };
    }
}
