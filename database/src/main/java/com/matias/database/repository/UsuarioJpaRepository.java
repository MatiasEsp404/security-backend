package com.matias.database.repository;

import com.matias.database.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.Optional;

@Repository
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Integer> {
    Optional<UsuarioEntity> findByEmail(String email);
    long countByActivo(boolean activo);
    long countByEmailVerificado(boolean verificado);
    long countByFechaCreacionAfter(Instant date);
}
