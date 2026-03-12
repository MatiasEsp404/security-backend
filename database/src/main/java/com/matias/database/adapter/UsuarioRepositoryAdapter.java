package com.matias.database.adapter;

import com.matias.database.entity.UsuarioEntity;
import com.matias.database.entity.UsuarioRolEntity;
import com.matias.database.mapper.UsuarioMapper;
import com.matias.database.repository.UsuarioJpaRepository;
import com.matias.domain.model.Rol;
import com.matias.domain.model.Usuario;
import com.matias.domain.port.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository jpaRepository;
    private final UsuarioMapper usuarioMapper;

    @Override
    public Optional<Usuario> findById(Integer id) {
        return jpaRepository.findById(id).map(usuarioMapper::toDomain);
    }

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioEntity entity = usuarioMapper.toEntity(usuario);
        UsuarioEntity saved = jpaRepository.save(entity);
        return usuarioMapper.toDomain(saved);
    }

    @Override
    public void updateStatus(Integer id, boolean active) {
        jpaRepository.findById(id).ifPresent(entity -> {
            entity.setActivo(active);
            jpaRepository.save(entity);
        });
    }

    @Override
    public void assignRole(Integer userId, Rol rol) {
        jpaRepository.findById(userId).ifPresent(entity -> {
            UsuarioRolEntity rolEntity = new UsuarioRolEntity(entity, rol);
            entity.getUsuarioRoles().add(rolEntity);
            jpaRepository.save(entity);
        });
    }

    @Override
    public void unassignRole(Integer userId, Rol rol) {
        jpaRepository.findById(userId).ifPresent(entity -> {
            boolean removed = entity.getUsuarioRoles().removeIf(ur -> ur.getRol() == rol);
            if (removed) {
                jpaRepository.save(entity);
            }
        });
    }

    @Override
    public boolean existsByUsuarioIdAndRol(Integer userId, Rol rol) {
        return jpaRepository.findById(userId)
                .map(entity -> entity.getUsuarioRoles().stream()
                        .anyMatch(ur -> ur.getRol() == rol))
                .orElse(false);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public long countByActivo(boolean activo) {
        return jpaRepository.countByActivo(activo);
    }

    @Override
    public long countByEmailVerificado(boolean verificado) {
        return jpaRepository.countByEmailVerificado(verificado);
    }

    @Override
    public long countByFechaCreacionAfter(java.time.Instant date) {
        return jpaRepository.countByFechaCreacionAfter(date);
    }
}
