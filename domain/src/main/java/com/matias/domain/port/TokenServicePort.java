package com.matias.domain.port;

import com.matias.domain.model.MotivoInvalidacionToken;
import com.matias.domain.model.Rol;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface TokenServicePort {
    String extractEmail(String token);
    boolean esTokenValido(String token, String userEmail);
    String generateAccessToken(String email, Set<Rol> roles, String refreshToken);
    String generateRefreshToken(String email);
    List<String> extractRoles(String token);
    Instant extractExpiration(String token);
    boolean esRefreshToken(String token);
    
    /**
     * Invalida un token JWT con el motivo especificado.
     *
     * @param token el token JWT a invalidar
     * @param motivo el motivo de invalidación
     * @param usuarioId ID del usuario propietario del token
     */
    void invalidarToken(String token, MotivoInvalidacionToken motivo, Integer usuarioId);
    
    /**
     * Verifica si un token está invalidado.
     *
     * @param token el token JWT a verificar
     * @return true si el token está invalidado, false en caso contrario
     */
    boolean esTokenInvalidado(String token);
}
