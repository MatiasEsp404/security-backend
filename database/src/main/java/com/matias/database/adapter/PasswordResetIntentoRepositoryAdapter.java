package com.matias.database.adapter;

import com.matias.database.entity.UsuarioEntity;
import com.matias.database.mapper.PasswordResetIntentoMapper;
import com.matias.database.mapper.UsuarioMapper;
import com.matias.database.repository.PasswordResetIntentoJpaRepository;
import com.matias.domain.model.PasswordResetIntento;
import com.matias.domain.model.Usuario;
import com.matias.domain.port.PasswordResetIntentoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PasswordResetIntentoRepositoryAdapter implements PasswordResetIntentoRepositoryPort {

    private final PasswordResetIntentoJpaRepository jpaRepository;
    private final PasswordResetIntentoMapper mapper;
    private final UsuarioMapper usuarioMapper;

    @Override
    @Transactional
    public PasswordResetIntento save(PasswordResetIntento intento) {
        var entity = mapper.toEntity(intento);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PasswordResetIntento> findUltimoIntentoByUsuario(Usuario usuario) {
        UsuarioEntity usuarioEntity = usuarioMapper.toEntity(usuario);
        return jpaRepository.findUltimoIntentoByUsuario(usuarioEntity)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByUsuarioAndFechaIntentoAfter(Usuario usuario, Instant fechaIntento) {
        UsuarioEntity usuarioEntity = usuarioMapper.toEntity(usuario);
        return jpaRepository.countByUsuarioAndFechaIntentoAfter(usuarioEntity, fechaIntento);
    }

    @Override
    @Transactional
    public int deleteByFechaIntentoBefore(Instant instant) {
        return jpaRepository.deleteByFechaIntentoBefore(instant);
    }
}
