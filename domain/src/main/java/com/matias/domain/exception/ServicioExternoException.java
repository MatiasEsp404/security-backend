package com.matias.domain.exception;

/**
 * 503 Service Unavailable. Errores de servicios externos (email, APIs).
 */
public class ServicioExternoException extends RuntimeException {
    public ServicioExternoException(String message) {
        super(message);
    }
}
