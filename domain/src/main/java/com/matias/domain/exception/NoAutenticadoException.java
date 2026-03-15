package com.matias.domain.exception;

/**
 * 401 Unauthorized. Tokens inválidos, sesión expirada.
 */
public class NoAutenticadoException extends RuntimeException {
    public NoAutenticadoException(String message) {
        super(message);
    }
}
