package com.matias.database.mapper;

import com.matias.database.entity.UsuarioEntity;
import com.matias.database.entity.UsuarioRolEntity;
import com.matias.domain.model.Rol;
import com.matias.domain.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "roles", source = "usuarioRoles", qualifiedByName = "mapRolesToDomain")
    Usuario toDomain(UsuarioEntity entity);

    @Mapping(target = "usuarioRoles", ignore = true)
    UsuarioEntity toEntity(Usuario domain);

    @Named("mapRolesToDomain")
    default Set<Rol> mapRolesToDomain(Set<UsuarioRolEntity> usuarioRoles) {
        if (usuarioRoles == null) {
            return null;
        }
        return usuarioRoles.stream()
                .map(UsuarioRolEntity::getRol)
                .collect(Collectors.toSet());
    }
}
