package com.matias.database.adapter;

import com.matias.database.mapper.TokenInvalidoMapper;
import com.matias.database.repository.TokenInvalidoJpaRepository;
import com.matias.domain.model.TokenInvalido;
import com.matias.domain.port.TokenInvalidoRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Adaptador que implementa el puerto TokenInvalidoRepositoryPort.
 * Conecta la capa de dominio con la infraestructura de base de datos.
 */
@Repository
public class TokenInvalidoRepositoryAdapter implements TokenInvalidoRepositoryPort {
    
    private final TokenInvalidoJpaRepository jpaRepository;
    private final TokenInvalidoMapper mapper;
    
    public TokenInvalidoRepositoryAdapter(
            TokenInvalidoJpaRepository jpaRepository,
            TokenInvalidoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    @Transactional
    public TokenInvalido invalidar(TokenInvalido tokenInvalido) {
        var entity = mapper.toEntity(tokenInvalido);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existeTokenInvalido(String tokenHash) {
        return jpaRepository.existsByTokenHash(tokenHash);
    }
    
    @Override
    @Transactional
    public void eliminarTokensExpirados(Instant fecha) {
        jpaRepository.deleteByExpiracionBefore(fecha);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TokenInvalido> buscarPorUsuario(Integer usuarioId) {
        return jpaRepository.findByUsuarioId(usuarioId).stream()
            .map(mapper::toDomain)
            .toList();
    }
}
