package com.matias.database.repository;

import com.matias.database.entity.TokenVerificacionEntity;
import com.matias.domain.model.EstadoTokenVerificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface TokenVerificacionJpaRepository extends JpaRepository<TokenVerificacionEntity, Integer> {
    
    Optional<TokenVerificacionEntity> findByToken(String token);
    
    Optional<TokenVerificacionEntity> findByUsuario_IdAndEstado(Integer usuarioId, EstadoTokenVerificacion estado);
    
    int deleteByExpiracionBeforeAndEstado(Instant expiracion, EstadoTokenVerificacion estado);
    
    @Modifying
    @Query("UPDATE TokenVerificacionEntity t SET t.estado = :nuevoEstado WHERE t.usuario.id = :usuarioId AND t.estado = :estadoActual")
    int updateEstadoByUsuarioIdAndEstado(@Param("usuarioId") Integer usuarioId, 
                                         @Param("estadoActual") EstadoTokenVerificacion estadoActual, 
                                         @Param("nuevoEstado") EstadoTokenVerificacion nuevoEstado);
}
