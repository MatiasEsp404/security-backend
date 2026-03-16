package com.matias.database.mapper;

import com.matias.database.entity.EmailVerificacionIntentoEntity;
import com.matias.domain.model.EmailVerificacionIntento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmailVerificacionIntentoMapper {

    @Mapping(target = "usuarioId", source = "usuario.id")
    EmailVerificacionIntento toDomain(EmailVerificacionIntentoEntity entity);

    @Mapping(target = "usuario", ignore = true)
    EmailVerificacionIntentoEntity toEntity(EmailVerificacionIntento domain);
}
