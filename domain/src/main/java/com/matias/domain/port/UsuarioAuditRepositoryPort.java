package com.matias.domain.port;

import com.matias.domain.model.UsuarioAudit;

import java.util.List;

/**
 * Puerto para acceder al historial de auditoría de usuarios
 * Implementado por el módulo database usando Hibernate Envers
 */
public interface UsuarioAuditRepositoryPort {
    
    /**
     * Obtiene el historial de auditoría de un usuario específico
     * 
     * @param usuarioId ID del usuario
     * @param pageRequest Parámetros de paginación
     * @return Resultado paginado con el historial de auditoría
     */
    PageResult<UsuarioAudit> findAuditByUsuarioId(Integer usuarioId, PageRequest pageRequest);
    
    /**
     * Parámetros de paginación para auditoría
     */
    record PageRequest(
        int pageNumber,
        int pageSize,
        SortDirection sortDirection
    ) {}
    
    /**
     * Dirección de ordenamiento
     */
    enum SortDirection {
        ASC, DESC
    }
    
    /**
     * Resultado paginado genérico
     */
    record PageResult<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean isFirst,
        boolean isLast
    ) {}
}
