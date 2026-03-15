package com.matias.domain.exception;

/**
 * 409 Conflict. Duplicados, estados conflictivos.
 */
public class ConflictoException extends RuntimeException {
    public ConflictoException(String message) {
        super(message);
    }
}
