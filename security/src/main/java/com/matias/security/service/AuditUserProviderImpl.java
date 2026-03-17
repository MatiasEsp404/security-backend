package com.matias.security.service;

import com.matias.domain.port.AuditUserProvider;
import com.matias.security.model.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Implementación del puerto AuditUserProvider que obtiene información
 * del usuario autenticado desde el contexto de Spring Security.
 */
@Component
public class AuditUserProviderImpl implements AuditUserProvider {

    @Override
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "SYSTEM";
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof SecurityUser securityUser) {
            return securityUser.getUsername();
        }
        
        if (principal instanceof String) {
            return (String) principal;
        }
        
        return "SYSTEM";
    }

    @Override
    public Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof SecurityUser securityUser) {
            return securityUser.getUsuario().getId();
        }
        
        return null;
    }
}
