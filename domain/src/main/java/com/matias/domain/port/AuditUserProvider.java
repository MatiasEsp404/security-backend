package com.matias.domain.port;

/**
 * Puerto para obtener información del usuario actual en contextos de auditoría.
 * Implementado por el módulo de seguridad.
 */
public interface AuditUserProvider {
    
    /**
     * Obtiene el email del usuario autenticado actual
     * 
     * @return Email del usuario, o "SYSTEM" si no hay usuario autenticado
     */
    String getCurrentUserEmail();
    
    /**
     * Obtiene el ID del usuario autenticado actual
     * 
     * @return ID del usuario, o null si no hay usuario autenticado
     */
    Integer getCurrentUserId();
}
