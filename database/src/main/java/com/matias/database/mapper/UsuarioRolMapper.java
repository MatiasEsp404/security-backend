package com.matias.database.mapper;

import com.matias.database.entity.UsuarioRolEntity;
import com.matias.domain.model.UsuarioRol;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper para convertir entre UsuarioRolEntity (capa database) y UsuarioRol (capa domain).
 * Utiliza MapStruct para generar implementaciones automáticas.
 */
@Mapper(componentModel = "spring")
public interface UsuarioRolMapper {

    /**
     * Convierte una entidad JPA a modelo de dominio.
     *
     * @param entity Entidad JPA
     * @return Modelo de dominio
     */
    @Mapping(target = "usuarioId", source = "usuario.id")
    UsuarioRol toDomain(UsuarioRolEntity entity);

    /**
     * Convierte un modelo de dominio a entidad JPA.
     * El usuario debe ser establecido manualmente después de la conversión.
     *
     * @param domain Modelo de dominio
     * @return Entidad JPA
     */
    @Mapping(target = "usuario", ignore = true)
    UsuarioRolEntity toEntity(UsuarioRol domain);
}
