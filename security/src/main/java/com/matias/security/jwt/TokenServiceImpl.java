package com.matias.security.jwt;

import com.matias.domain.model.MotivoInvalidacionToken;
import com.matias.domain.model.Rol;
import com.matias.domain.model.TokenInvalido;
import com.matias.domain.port.TokenInvalidoRepositoryPort;
import com.matias.domain.port.TokenServicePort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Slf4j
@Service
public class TokenServiceImpl implements TokenServicePort {
    
    private final TokenInvalidoRepositoryPort tokenInvalidoRepository;

    @Value("${jwt.secret}")
    private String secretKey;

    @DurationUnit(ChronoUnit.MILLIS)
    @Value("${jwt.access-token.expiration}")
    private Duration accessExpiration;

    @DurationUnit(ChronoUnit.MILLIS)
    @Value("${jwt.refresh-token.expiration}")
    private Duration refreshExpiration;
    
    public TokenServiceImpl(TokenInvalidoRepositoryPort tokenInvalidoRepository) {
        this.tokenInvalidoRepository = tokenInvalidoRepository;
    }

    @PostConstruct
    public void validateSecretKey() {
        int keyBits = secretKey.getBytes().length * 8;
        if (keyBits < 256) {
            log.warn("JWT secret key es menor a 256 bits. Longitud actual: {} bits. Configura una clave más segura en producción.", keyBits);
        } else {
            log.info("JWT secret key configurada correctamente ({} bits)", keyBits);
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

        // Verificar si el token está invalidado
        if (esTokenInvalidado(token)) {
            log.debug("Token invalidado detectado para usuario: {}", email);
            return false;
        }

        return true;
    }

    public String extractSessionId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("sid", String.class);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    @Override
    public String generateAccessToken(String email, Set<Rol> roles, String refreshToken) {
        List<String> rolesStr = roles.stream().map(Rol::name).toList();
        String sid = generateSessionId(refreshToken);

        return Jwts.builder()
                .subject(email)
                .claim("roles", rolesStr)
                .claim("sid", sid)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiration.toMillis()))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    private String generateSessionId(String refreshToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible en este entorno", e);
        }
    }

    @Override
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration.toMillis()))
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
        return Math.abs(duracionToken - refreshExpiration.toMillis()) < margen;
    }
    
    @Override
    public void invalidarToken(String token, MotivoInvalidacionToken motivo, Integer usuarioId) {
        String tokenHash = calcularHashToken(token);
        Instant expiracion = extractExpiration(token);
        Instant fechaInvalidacion = Instant.now();
        
        TokenInvalido tokenInvalido = new TokenInvalido(
            tokenHash,
            expiracion,
            fechaInvalidacion,
            motivo,
            usuarioId
        );
        
        tokenInvalidoRepository.invalidar(tokenInvalido);
        log.info("Token invalidado - Usuario: {}, Motivo: {}", usuarioId, motivo);
    }
    
    @Override
    public boolean esTokenInvalidado(String token) {
        String tokenHash = calcularHashToken(token);
        return tokenInvalidoRepository.existeTokenInvalido(tokenHash);
    }
    
    /**
     * Calcula el hash SHA-256 de un token para búsqueda eficiente.
     *
     * @param token token JWT completo
     * @return hash SHA-256 en formato hexadecimal
     */
    private String calcularHashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al calcular hash SHA-256 del token", e);
        }
    }
}
