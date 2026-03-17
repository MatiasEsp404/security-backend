package com.matias.database.mapper;

import com.matias.database.entity.UsuarioEntity;
import com.matias.database.entity.UsuarioRolEntity;
import com.matias.domain.model.UsuarioRol;
import com.matias.domain.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {UsuarioRolMapper.class})
public interface UsuarioMapper {

    @Mapping(target = "usuarioRoles", source = "usuarioRoles", qualifiedByName = "mapUsuarioRolesToDomain")
    Usuario toDomain(UsuarioEntity entity);

    @Mapping(target = "usuarioRoles", ignore = true)
    UsuarioEntity toEntity(Usuario domain);

    @Named("mapUsuarioRolesToDomain")
    default Set<UsuarioRol> mapUsuarioRolesToDomain(Set<UsuarioRolEntity> usuarioRoles) {
        if (usuarioRoles == null) {
            return null;
        }
        return usuarioRoles.stream()
                .map(rolEntity -> UsuarioRol.builder()
                        .id(rolEntity.getId())
                        .usuarioId(rolEntity.getUsuario().getId())
                        .rol(rolEntity.getRol())
                        .fechaAsignacion(rolEntity.getFechaAsignacion())
                        .build())
                .collect(Collectors.toSet());
    }
}
