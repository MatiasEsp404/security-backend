package com.matias.domain.exception;

/**
 * 404 Not Found. Entidades no encontradas.
 */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
