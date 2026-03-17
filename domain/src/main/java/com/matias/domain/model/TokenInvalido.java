package com.matias.domain.model;

import java.time.Instant;

/**
 * Modelo de dominio que representa un token JWT invalidado.
 * Los tokens invalidados no pueden ser usados para autenticación.
 */
public record TokenInvalido(
    Integer id,
    String tokenHash,
    Instant expiracion,
    Instant fechaInvalidacion,
    MotivoInvalidacionToken motivo,
    Integer usuarioId
) {
    /**
     * Constructor para crear un nuevo token inválido (sin ID).
     */
    public TokenInvalido(
            String tokenHash,
            Instant expiracion,
            Instant fechaInvalidacion,
            MotivoInvalidacionToken motivo,
            Integer usuarioId) {
        this(null, tokenHash, expiracion, fechaInvalidacion, motivo, usuarioId);
    }
}
