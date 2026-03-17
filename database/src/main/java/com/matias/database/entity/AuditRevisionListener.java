package com.matias.database.entity;

import com.matias.domain.port.AuditUserProvider;
import org.hibernate.envers.RevisionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Listener que intercepta la creación de revisiones de auditoría
 * para enriquecerlas con información del usuario actual.
 */
@Component
public class AuditRevisionListener implements RevisionListener {

    private static AuditUserProvider auditUserProvider;

    @Autowired
    public void setAuditUserProvider(AuditUserProvider auditUserProvider) {
        AuditRevisionListener.auditUserProvider = auditUserProvider;
    }

    @Override
    public void newRevision(Object revisionEntity) {
        if (revisionEntity instanceof AuditRevisionEntity auditRevision && auditUserProvider != null) {
            auditRevision.setUsuarioEmail(auditUserProvider.getCurrentUserEmail());
            auditRevision.setUsuarioId(auditUserProvider.getCurrentUserId());
        }
    }
}
