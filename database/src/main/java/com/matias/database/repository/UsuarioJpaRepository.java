package com.matias.database.repository;

import com.matias.database.entity.UsuarioEntity;
import com.matias.domain.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Integer>, 
                                               JpaSpecificationExecutor<UsuarioEntity> {
    Optional<UsuarioEntity> findByEmail(String email);
    
    @Query("SELECT u FROM UsuarioEntity u LEFT JOIN FETCH u.usuarioRoles WHERE u.email = :email")
    Optional<UsuarioEntity> findByEmailWithRoles(@Param("email") String email);
    
    long countByActivo(boolean activo);
    long countByEmailVerificado(boolean verificado);
    long countByFechaCreacionAfter(Instant date);
    
    @Query("SELECT ur.rol, COUNT(DISTINCT u.id) FROM UsuarioEntity u " +
           "JOIN u.usuarioRoles ur " +
           "GROUP BY ur.rol")
    List<Object[]> countUsuariosPorRol();
}
