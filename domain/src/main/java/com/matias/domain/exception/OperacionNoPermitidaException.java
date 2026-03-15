package com.matias.domain.exception;

/**
 * 400 Bad Request. Operaciones no permitidas, validaciones de negocio.
 */
public class OperacionNoPermitidaException extends RuntimeException {
    public OperacionNoPermitidaException(String message) {
        super(message);
    }
}
