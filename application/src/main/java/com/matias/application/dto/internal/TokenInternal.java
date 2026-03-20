package com.matias.application.dto.internal;

/**
 * DTO interno para transportar tokens entre capas.
 * No se expone directamente en la API REST.
 */
public record TokenInternal(
        String accessToken,
        String refreshToken
) {

}
