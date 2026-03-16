package com.matias.database.repository;

import com.matias.database.entity.PasswordResetIntentoEntity;
import com.matias.database.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface PasswordResetIntentoJpaRepository extends JpaRepository<PasswordResetIntentoEntity, Long> {

    @Query("SELECT i FROM PasswordResetIntentoEntity i WHERE i.usuario = :usuario ORDER BY i.fechaIntento DESC LIMIT 1")
    Optional<PasswordResetIntentoEntity> findUltimoIntentoByUsuario(@Param("usuario") UsuarioEntity usuario);

    long countByUsuarioAndFechaIntentoAfter(UsuarioEntity usuario, Instant fechaIntento);

    int deleteByFechaIntentoBefore(Instant instant);
}
