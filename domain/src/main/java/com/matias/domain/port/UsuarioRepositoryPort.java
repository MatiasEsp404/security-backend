package com.matias.domain.port;

import com.matias.domain.model.Rol;
import com.matias.domain.model.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepositoryPort {
    Optional<Usuario> findById(Integer id);
    Optional<Usuario> findByEmail(String email);
    Usuario save(Usuario usuario);
    void updateStatus(Integer id, boolean active);
    void assignRole(Integer userId, Rol rol);
    void unassignRole(Integer userId, Rol rol);
    boolean existsByUsuarioIdAndRol(Integer userId, Rol rol);
    long count();
    long countByActivo(boolean activo);
    long countByEmailVerificado(boolean verificado);
    long countByFechaCreacionAfter(java.time.Instant date);
}
