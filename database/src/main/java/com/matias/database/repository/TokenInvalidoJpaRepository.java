package com.matias.database.repository;

import com.matias.database.entity.TokenInvalidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repositorio JPA para la gestión de tokens invalidados.
 */
@Repository
public interface TokenInvalidoJpaRepository extends JpaRepository<TokenInvalidoEntity, Integer> {
    
    /**
     * Verifica si existe un token con el hash especificado.
     *
     * @param tokenHash hash SHA-256 del token
     * @return true si existe, false en caso contrario
     */
    boolean existsByTokenHash(String tokenHash);
    
    /**
     * Elimina todos los tokens cuya fecha de expiración sea anterior a la fecha especificada.
     *
     * @param fecha fecha límite
     */
    void deleteByExpiracionBefore(Instant fecha);
    
    /**
     * Busca todos los tokens invalidados de un usuario específico.
     *
     * @param usuarioId ID del usuario
     * @return lista de tokens del usuario
     */
    List<TokenInvalidoEntity> findByUsuarioId(Integer usuarioId);
}
