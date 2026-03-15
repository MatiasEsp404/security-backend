package com.matias.database.adapter;

import com.matias.database.entity.TokenVerificacionEntity;
import com.matias.database.entity.UsuarioEntity;
import com.matias.database.mapper.TokenVerificacionMapper;
import com.matias.database.repository.TokenVerificacionJpaRepository;
import com.matias.database.repository.UsuarioJpaRepository;
import com.matias.domain.model.EstadoTokenVerificacion;
import com.matias.domain.model.TokenVerificacion;
import com.matias.domain.port.TokenVerificacionRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TokenVerificacionRepositoryAdapter implements TokenVerificacionRepositoryPort {

    private final TokenVerificacionJpaRepository jpaRepository;
    private final UsuarioJpaRepository usuarioJpaRepository;
    private final TokenVerificacionMapper mapper;

    public TokenVerificacionRepositoryAdapter(
            TokenVerificacionJpaRepository jpaRepository,
            UsuarioJpaRepository usuarioJpaRepository,
            TokenVerificacionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public TokenVerificacion save(TokenVerificacion tokenVerificacion) {
        TokenVerificacionEntity entity = mapper.toEntity(tokenVerificacion);
        
        // Obtener y asignar el usuario
        if (tokenVerificacion.getUsuarioId() != null) {
            UsuarioEntity usuario = usuarioJpaRepository.findById(tokenVerificacion.getUsuarioId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            entity.setUsuario(usuario);
        }
        
        TokenVerificacionEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<TokenVerificacion> findByToken(String token) {
        return jpaRepository.findByToken(token)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<TokenVerificacion> findByUsuarioIdAndEstadoPendiente(Integer usuarioId) {
        return jpaRepository.findByUsuario_IdAndEstado(usuarioId, EstadoTokenVerificacion.PENDIENTE)
                .map(mapper::toDomain);
    }

    @Override
    public void deleteById(Integer id) {
        jpaRepository.deleteById(id);
    }
}
