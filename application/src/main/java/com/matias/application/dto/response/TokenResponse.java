package com.matias.application.dto.response;

/**
 * DTO de respuesta con el token de acceso JWT.
 * Contrato limpio de la capa de aplicación sin dependencias de infraestructura.
 */
public record TokenResponse(
        String accessToken
) {

}
