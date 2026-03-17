package com.matias.domain.port;

import com.matias.domain.model.TokenInvalido;

import java.time.Instant;
import java.util.List;

/**
 * Puerto (interfaz) para el repositorio de tokens invalidados.
 * Define las operaciones necesarias para gestionar tokens JWT revocados.
 */
public interface TokenInvalidoRepositoryPort {
    
    /**
     * Guarda un token invalidado en el repositorio.
     *
     * @param tokenInvalido el token a invalidar
     * @return el token guardado con su ID asignado
     */
    TokenInvalido invalidar(TokenInvalido tokenInvalido);
    
    /**
     * Verifica si existe un token invalidado con el hash especificado.
     *
     * @param tokenHash hash SHA-256 del token
     * @return true si el token está invalidado, false en caso contrario
     */
    boolean existeTokenInvalido(String tokenHash);
    
    /**
     * Elimina todos los tokens cuya fecha de expiración sea anterior a la fecha especificada.
     * Útil para limpieza periódica de tokens expirados.
     *
     * @param fecha fecha límite de expiración
     */
    void eliminarTokensExpirados(Instant fecha);
    
    /**
     * Busca todos los tokens invalidados de un usuario específico.
     *
     * @param usuarioId ID del usuario
     * @return lista de tokens invalidados del usuario
     */
    List<TokenInvalido> buscarPorUsuario(Integer usuarioId);
}
