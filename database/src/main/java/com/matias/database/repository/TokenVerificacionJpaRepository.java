package com.matias.database.repository;

import com.matias.database.entity.TokenVerificacionEntity;
import com.matias.domain.model.EstadoTokenVerificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenVerificacionJpaRepository extends JpaRepository<TokenVerificacionEntity, Integer> {
    
    Optional<TokenVerificacionEntity> findByToken(String token);
    
    Optional<TokenVerificacionEntity> findByUsuario_IdAndEstado(Integer usuarioId, EstadoTokenVerificacion estado);
}
