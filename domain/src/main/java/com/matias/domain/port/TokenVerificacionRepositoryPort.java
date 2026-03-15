package com.matias.domain.port;

import com.matias.domain.model.TokenVerificacion;

import java.util.Optional;

public interface TokenVerificacionRepositoryPort {
    
    TokenVerificacion save(TokenVerificacion tokenVerificacion);
    
    Optional<TokenVerificacion> findByToken(String token);
    
    Optional<TokenVerificacion> findByUsuarioIdAndEstadoPendiente(Integer usuarioId);
    
    void deleteById(Integer id);
}
