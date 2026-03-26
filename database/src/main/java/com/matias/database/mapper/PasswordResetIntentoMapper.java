package com.matias.database.mapper;

import com.matias.database.entity.PasswordResetIntentoEntity;
import com.matias.domain.model.PasswordResetIntento;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UsuarioEntityMapper.class})
public interface PasswordResetIntentoMapper {

    PasswordResetIntento toDomain(PasswordResetIntentoEntity entity);

    PasswordResetIntentoEntity toEntity(PasswordResetIntento domain);
}
