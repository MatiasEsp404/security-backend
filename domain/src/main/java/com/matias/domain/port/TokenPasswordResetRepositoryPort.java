package com.matias.domain.port;

import com.matias.domain.model.EstadoTokenVerificacion;
import com.matias.domain.model.TokenPasswordReset;
import com.matias.domain.model.Usuario;

import java.time.Instant;
import java.util.Optional;

/**
 * Puerto de repositorio para gestionar tokens de reseteo de contraseña.
 */
public interface TokenPasswordResetRepositoryPort {

    /**
     * Guarda un token de reseteo.
     *
     * @param token token a guardar
     * @return token guardado
     */
    TokenPasswordReset save(TokenPasswordReset token);

    /**
     * Busca un token por su valor.
     *
     * @param token valor del token
     * @return Optional con el token si existe
     */
    Optional<TokenPasswordReset> findByToken(String token);

    /**
     * Actualiza el estado de tokens de un usuario.
     *
     * @param usuario usuario asociado
     * @param estadoActual estado actual de los tokens
     * @param nuevoEstado nuevo estado a asignar
     * @return cantidad de tokens actualizados
     */
    int updateEstadoByUsuarioAndEstado(Usuario usuario, EstadoTokenVerificacion estadoActual, EstadoTokenVerificacion nuevoEstado);

    /**
     * Elimina tokens expirados con un estado específico.
     *
     * @param expiracion fecha de expiración límite
     * @param estado estado de los tokens a eliminar
     * @return cantidad de tokens eliminados
     */
    int deleteByExpiracionBeforeAndEstado(Instant expiracion, EstadoTokenVerificacion estado);
}
