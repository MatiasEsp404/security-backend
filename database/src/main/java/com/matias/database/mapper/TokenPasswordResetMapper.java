package com.matias.database.mapper;

import com.matias.database.entity.TokenPasswordResetEntity;
import com.matias.domain.model.TokenPasswordReset;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UsuarioMapper.class})
public interface TokenPasswordResetMapper {

    TokenPasswordReset toDomain(TokenPasswordResetEntity entity);

    TokenPasswordResetEntity toEntity(TokenPasswordReset domain);
}
