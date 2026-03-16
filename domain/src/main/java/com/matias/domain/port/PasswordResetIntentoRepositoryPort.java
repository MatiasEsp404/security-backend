package com.matias.domain.port;

import com.matias.domain.model.PasswordResetIntento;
import com.matias.domain.model.Usuario;

import java.time.Instant;
import java.util.Optional;

/**
 * Puerto de repositorio para gestionar intentos de reseteo de contraseña.
 */
public interface PasswordResetIntentoRepositoryPort {

    /**
     * Guarda un intento de reseteo de contraseña.
     *
     * @param intento intento a guardar
     * @return intento guardado
     */
    PasswordResetIntento save(PasswordResetIntento intento);

    /**
     * Encuentra el último intento de reseteo de un usuario.
     *
     * @param usuario usuario a consultar
     * @return Optional con el último intento si existe
     */
    Optional<PasswordResetIntento> findUltimoIntentoByUsuario(Usuario usuario);

    /**
     * Cuenta los intentos de un usuario después de una fecha específica.
     *
     * @param usuario usuario a consultar
     * @param fechaIntento fecha límite
     * @return cantidad de intentos
     */
    long countByUsuarioAndFechaIntentoAfter(Usuario usuario, Instant fechaIntento);

    /**
     * Elimina intentos anteriores a una fecha específica.
     *
     * @param instant fecha límite
     * @return cantidad de intentos eliminados
     */
    int deleteByFechaIntentoBefore(Instant instant);
}
