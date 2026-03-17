package com.matias.database.mapper;

import com.matias.database.entity.TokenInvalidoEntity;
import com.matias.domain.model.TokenInvalido;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre TokenInvalidoEntity (JPA) y TokenInvalido (Domain).
 */
@Component
public class TokenInvalidoMapper {
    
    /**
     * Convierte un modelo de dominio a una entidad JPA.
     *
     * @param domain modelo de dominio
     * @return entidad JPA
     */
    public TokenInvalidoEntity toEntity(TokenInvalido domain) {
        if (domain == null) {
            return null;
        }
        
        TokenInvalidoEntity entity = new TokenInvalidoEntity();
        entity.setId(domain.id());
        entity.setTokenHash(domain.tokenHash());
        entity.setExpiracion(domain.expiracion());
        entity.setFechaInvalidacion(domain.fechaInvalidacion());
        entity.setMotivo(domain.motivo());
        entity.setUsuarioId(domain.usuarioId());
        
        return entity;
    }
    
    /**
     * Convierte una entidad JPA a un modelo de dominio.
     *
     * @param entity entidad JPA
     * @return modelo de dominio
     */
    public TokenInvalido toDomain(TokenInvalidoEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return new TokenInvalido(
            entity.getId(),
            entity.getTokenHash(),
            entity.getExpiracion(),
            entity.getFechaInvalidacion(),
            entity.getMotivo(),
            entity.getUsuarioId()
        );
    }
}
