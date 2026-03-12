package com.matias.domain.port;

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
}
