package com.matias.database.repository;

import com.matias.database.entity.EmailVerificacionIntentoEntity;
import com.matias.database.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface EmailVerificacionIntentoJpaRepository extends JpaRepository<EmailVerificacionIntentoEntity, Integer> {
    
    @Query("SELECT e FROM EmailVerificacionIntentoEntity e WHERE e.usuario.id = :usuarioId ORDER BY e.fechaIntento DESC LIMIT 1")
    Optional<EmailVerificacionIntentoEntity> findUltimoIntentoByUsuarioId(@Param("usuarioId") Integer usuarioId);
    
    long countByUsuarioIdAndFechaIntentoAfter(Integer usuarioId, Instant fechaLimite);
    
    int deleteByFechaIntentoBefore(Instant fechaLimite);
}
