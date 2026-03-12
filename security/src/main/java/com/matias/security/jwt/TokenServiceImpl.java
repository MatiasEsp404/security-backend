package com.matias.security.jwt;

import com.matias.domain.model.Rol;
import com.matias.domain.port.TokenServicePort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Service
public class TokenServiceImpl implements TokenServicePort {

    @Value("${jwt.secret:1234567890123456789012345678901234567890}")
    private String secretKey;

    private final long accessExpirationMillis = 1000 * 60 * 15; // 15 mins
    private final long refreshExpirationMillis = 1000 * 60 * 60 * 24 * 7; // 7 days

    @PostConstruct
    public void validateSecretKey() {
        int keyBits = secretKey.getBytes(StandardCharsets.UTF_8).length * 8;
        if (keyBits < 256) {
            System.err.println("JWT secret key es menor a 256 bits.");
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public boolean esTokenValido(String token, String userEmail) {
        String email = extractEmail(token);
        if (!userEmail.equals(email) || isTokenExpired(token)) {
            return false;
        }
        return true;
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    @Override
    public String generateAccessToken(String email, Set<Rol> roles, String refreshToken) {
        List<String> rolesStr = (roles != null) ? roles.stream().map(Rol::name).toList() : List.of();
        String sid = generateSessionId(refreshToken);

        return Jwts.builder()
                .subject(email)
                .claim("roles", rolesStr)
                .claim("sid", sid)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpirationMillis))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    private String generateSessionId(String refreshToken) {
        if (refreshToken == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible", e);
        }
    }

    @Override
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMillis))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("roles", List.class);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public Instant extractExpiration(String token) {
        return extractAllClaims(token).getExpiration().toInstant();
    }

    @Override
    public boolean esRefreshToken(String token) {
        Claims claims = extractAllClaims(token);
        Date issuedAt = claims.getIssuedAt();
        Date expiration = claims.getExpiration();
        long duracionToken = expiration.getTime() - issuedAt.getTime();
        long margen = 5000;
        return Math.abs(duracionToken - refreshExpirationMillis) < margen;
    }
}
