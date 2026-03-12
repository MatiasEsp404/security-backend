package com.matias.database.repository;

import com.matias.database.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;

@Repository
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Integer> {
    long countByActivo(boolean activo);
    long countByEmailVerificado(boolean emailVerificado);
    long countByFechaCreacionAfter(Instant fechaCreacion);
}
