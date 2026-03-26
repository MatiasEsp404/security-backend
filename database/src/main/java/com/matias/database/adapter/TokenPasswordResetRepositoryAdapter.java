package com.matias.database.adapter;

import com.matias.database.entity.TokenPasswordResetEntity;
import com.matias.database.entity.UsuarioEntity;
import com.matias.database.mapper.TokenPasswordResetMapper;
import com.matias.database.mapper.UsuarioEntityMapper;
import com.matias.database.repository.TokenPasswordResetJpaRepository;
import com.matias.domain.model.EstadoTokenVerificacion;
import com.matias.domain.model.TokenPasswordReset;
import com.matias.domain.model.Usuario;
import com.matias.domain.port.TokenPasswordResetRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TokenPasswordResetRepositoryAdapter implements TokenPasswordResetRepositoryPort {

    private final TokenPasswordResetJpaRepository jpaRepository;
    private final TokenPasswordResetMapper mapper;
    private final UsuarioEntityMapper usuarioMapper;

    @Override
    @Transactional
    public TokenPasswordReset save(TokenPasswordReset token) {
        TokenPasswordResetEntity entity = mapper.toEntity(token);
        TokenPasswordResetEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TokenPasswordReset> findByToken(String token) {
        return jpaRepository.findByToken(token)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public int updateEstadoByUsuarioAndEstado(Usuario usuario, EstadoTokenVerificacion estadoActual, EstadoTokenVerificacion nuevoEstado) {
        UsuarioEntity usuarioEntity = usuarioMapper.toEntity(usuario);
        return jpaRepository.updateEstadoByUsuarioAndEstado(usuarioEntity, estadoActual, nuevoEstado);
    }

    @Override
    @Transactional
    public int deleteByExpiracionBeforeAndEstado(Instant expiracion, EstadoTokenVerificacion estado) {
        return jpaRepository.deleteByExpiracionBeforeAndEstado(expiracion, estado);
    }
}
