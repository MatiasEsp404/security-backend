package com.matias.domain.model;

/**
 * Motivos por los cuales un token JWT puede ser invalidado.
 */
public enum MotivoInvalidacionToken {
    /**
     * El usuario realizó logout manualmente
     */
    LOGOUT,
    
    /**
     * El usuario cambió su contraseña
     */
    CAMBIO_PASSWORD,
    
    /**
     * Token rotado por políticas de seguridad
     */
    ROTACION,
    
    /**
     * Usuario fue baneado o bloqueado
     */
    USUARIO_BANEADO
}
