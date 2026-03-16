package com.matias.database.repository;

import com.matias.database.entity.TokenPasswordResetEntity;
import com.matias.database.entity.UsuarioEntity;
import com.matias.domain.model.EstadoTokenVerificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface TokenPasswordResetJpaRepository extends JpaRepository<TokenPasswordResetEntity, Long> {

    Optional<TokenPasswordResetEntity> findByToken(String token);

    @Modifying
    @Query("UPDATE TokenPasswordResetEntity t SET t.estado = :nuevoEstado " +
           "WHERE t.usuario = :usuario AND t.estado = :estadoActual")
    int updateEstadoByUsuarioAndEstado(
            @Param("usuario") UsuarioEntity usuario,
            @Param("estadoActual") EstadoTokenVerificacion estadoActual,
            @Param("nuevoEstado") EstadoTokenVerificacion nuevoEstado
    );

    @Modifying
    @Query("DELETE FROM TokenPasswordResetEntity t " +
           "WHERE t.expiracion < :expiracion AND t.estado = :estado")
    int deleteByExpiracionBeforeAndEstado(
            @Param("expiracion") Instant expiracion,
            @Param("estado") EstadoTokenVerificacion estado
    );
}
