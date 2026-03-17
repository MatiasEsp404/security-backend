package com.matias.database.entity;

import com.matias.domain.model.MotivoInvalidacionToken;
import jakarta.persistence.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

/**
 * Entidad JPA que representa un token JWT invalidado en la base de datos.
 */
@Entity
@Table(name = "tokens_invalidos", indexes = {
    @Index(name = "idx_token_hash", columnList = "token_hash"),
    @Index(name = "idx_expiracion", columnList = "expiracion")
})
public class TokenInvalidoEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;
    
    @Column(name = "expiracion", nullable = false)
    private Instant expiracion;
    
    @Column(name = "fecha_invalidacion", nullable = false)
    private Instant fechaInvalidacion;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false, length = 50)
    private MotivoInvalidacionToken motivo;
    
    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;
    
    /**
     * Validación antes de persistir para asegurar integridad de datos.
     */
    @PrePersist
    private void validarAntesDeGuardar() {
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalStateException("El hash del token no puede estar vacío");
        }
        
        if (tokenHash.length() != 64) {
            throw new IllegalStateException("El hash del token debe ser SHA-256 (64 caracteres hex)");
        }
        
        if (expiracion == null) {
            throw new IllegalStateException("La fecha de expiración es obligatoria");
        }
        
        if (fechaInvalidacion == null) {
            fechaInvalidacion = Instant.now();
        }
        
        if (motivo == null) {
            throw new IllegalStateException("El motivo de invalidación es obligatorio");
        }
        
        if (usuarioId == null) {
            throw new IllegalStateException("El ID del usuario es obligatorio");
        }
    }
    
    /**
     * Calcula el hash SHA-256 de un token.
     *
     * @param token token JWT completo
     * @return hash SHA-256 en formato hexadecimal
     */
    public static String calcularHash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al calcular hash SHA-256", e);
        }
    }
    
    // Getters y Setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getTokenHash() {
        return tokenHash;
    }
    
    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }
    
    public Instant getExpiracion() {
        return expiracion;
    }
    
    public void setExpiracion(Instant expiracion) {
        this.expiracion = expiracion;
    }
    
    public Instant getFechaInvalidacion() {
        return fechaInvalidacion;
    }
    
    public void setFechaInvalidacion(Instant fechaInvalidacion) {
        this.fechaInvalidacion = fechaInvalidacion;
    }
    
    public MotivoInvalidacionToken getMotivo() {
        return motivo;
    }
    
    public void setMotivo(MotivoInvalidacionToken motivo) {
        this.motivo = motivo;
    }
    
    public Integer getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }
}
