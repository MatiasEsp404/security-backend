package com.matias.database.adapter;

import com.matias.database.entity.UsuarioEntity;
import com.matias.database.entity.UsuarioRolEntity;
import com.matias.database.mapper.UsuarioEntityMapper;
import com.matias.database.repository.UsuarioJpaRepository;
import com.matias.database.specification.UsuarioSpecification;
import com.matias.domain.model.Rol;
import com.matias.domain.model.Usuario;
import com.matias.domain.port.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository jpaRepository;
    private final UsuarioEntityMapper usuarioMapper;

    @Override
    public Optional<Usuario> findById(Integer id) {
        return jpaRepository.findById(id).map(usuarioMapper::toDomain);
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(usuarioMapper::toDomain);
    }

    @Override
    public Optional<Usuario> findByEmailWithRoles(String email) {
        return jpaRepository.findByEmailWithRoles(email)
                .map(usuarioMapper::toDomain);
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
    public long countByFechaCreacionAfter(Instant date) {
        return jpaRepository.countByFechaCreacionAfter(date);
    }

    @Override
    public Map<Rol, Long> countUsuariosPorRol() {
        List<Object[]> results = jpaRepository.countUsuariosPorRol();
        Map<Rol, Long> countMap = new HashMap<>();
        for (Object[] result : results) {
            Rol rol = (Rol) result[0];
            Long count = (Long) result[1];
            countMap.put(rol, count);
        }
        return countMap;
    }

    @Override
    public PageResult<Usuario> findAllWithFilters(UsuarioFilter filter, UsuarioRepositoryPort.PageRequest pageRequest) {
        // Convertir filtros a Specification
        Specification<UsuarioEntity> spec = UsuarioSpecification.withFilters(
                filter.search(),
                filter.activo(),
                filter.emailVerificado(),
                filter.roles(),
                filter.fechaDesde(),
                filter.fechaHasta()
        );

        // Convertir PageRequest a Spring PageRequest con Sort
        Sort sort = pageRequest.direction() == UsuarioRepositoryPort.SortDirection.ASC
                ? Sort.by(Sort.Direction.ASC, pageRequest.sortBy())
                : Sort.by(Sort.Direction.DESC, pageRequest.sortBy());

        org.springframework.data.domain.PageRequest springPageRequest = 
                org.springframework.data.domain.PageRequest.of(
                        pageRequest.page(),
                        pageRequest.size(),
                        sort
                );

        // Ejecutar consulta
        Page<UsuarioEntity> page = jpaRepository.findAll(spec, springPageRequest);

        // Convertir a dominio
        List<Usuario> usuarios = page.getContent().stream()
                .map(usuarioMapper::toDomain)
                .collect(Collectors.toList());

        // Retornar PageResult
        return new PageResult<>(
                usuarios,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
