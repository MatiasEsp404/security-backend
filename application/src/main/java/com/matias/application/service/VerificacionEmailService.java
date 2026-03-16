package com.matias.application.service;

import com.matias.domain.model.Usuario;

import java.time.Duration;

public interface VerificacionEmailService {
    
    /**
     * Genera un token de verificación para el usuario
     * @param usuario Usuario para el cual generar el token
     * @return Token generado
     */
    String generarTokenVerificacion(Usuario usuario);
    
    /**
     * Verifica el email usando el token proporcionado
     * @param token Token de verificación
     * @return Usuario con email verificado
     */
    Usuario verificarEmail(String token);
    
    /**
     * Valida si el usuario puede solicitar un reenvío de email
     * @param usuario Usuario que solicita el reenvío
     * @param ipOrigen IP desde donde se realiza la solicitud
     */
    void validarReenvio(Usuario usuario, String ipOrigen);
    
    /**
     * Limpia datos obsoletos de verificación
     * @return Cantidad de registros eliminados
     */
    int limpiarDatosObsoletos();
    
    /**
     * Obtiene la duración de expiración del token
     * @return Duración de expiración
     */
    Duration getExpiracionToken();
}
