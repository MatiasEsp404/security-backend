package com.matias.application.dto.response;

/**
 * DTO de respuesta tras el registro de un usuario.
 * Contrato limpio de la capa de aplicación sin dependencias de infraestructura.
 */
public record RegistroResponse(
        Integer id,
        String email
) {

}
