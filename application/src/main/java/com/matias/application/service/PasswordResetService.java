package com.matias.application.service;

import com.matias.domain.model.Usuario;

import java.time.Duration;

/**
 * Servicio para gestionar el reseteo de contraseña mediante tokens.
 */
public interface PasswordResetService {

    /**
     * Genera un token de reseteo para el usuario.
     *
     * @param usuario usuario asociado al token
     * @return token generado
     */
    String generarTokenReset(Usuario usuario);

    /**
     * Obtiene la duración de expiración del token configurada.
     *
     * @return duración de expiración
     */
    Duration getExpiracionToken();

    /**
     * Resetea la contraseña usando un token válido.
     *
     * @param token         token de reseteo
     * @param nuevaPassword nueva contraseña en texto plano
     */
    void resetearPassword(String token, String nuevaPassword);

    /**
     * Valida un token de reseteo sin consumirlo.
     *
     * @param token token a validar
     */
    void validarToken(String token);

    /**
     * Valida si el usuario puede solicitar un reseteo de contraseña.
     *
     * @param email    email del usuario
     * @param ipOrigen IP desde donde se realiza la solicitud
     */
    void validarSolicitudReset(String email, String ipOrigen);

    /**
     * Elimina datos obsoletos relacionados a tokens de reseteo.
     *
     * @return cantidad de registros eliminados
     */
    int limpiarDatosObsoletos();
}
