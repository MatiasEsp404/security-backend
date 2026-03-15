package com.matias.domain.exception;

/**
 * 403 Forbidden. Permisos insuficientes, cuentas deshabilitadas.
 */
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String message) {
        super(message);
    }
}
