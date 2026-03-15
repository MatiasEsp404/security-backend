package com.matias.database.mapper;

import com.matias.database.entity.TokenVerificacionEntity;
import com.matias.domain.model.TokenVerificacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TokenVerificacionMapper {

    @Mapping(target = "usuarioId", source = "usuario.id")
    TokenVerificacion toDomain(TokenVerificacionEntity entity);

    @Mapping(target = "usuario", ignore = true)
    TokenVerificacionEntity toEntity(TokenVerificacion domain);
}
