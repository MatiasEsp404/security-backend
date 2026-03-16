package com.matias.application.mapper;

import com.matias.application.dto.response.UsuarioResponse;
import com.matias.domain.model.Usuario;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    /**
     * Convierte un Usuario de dominio a UsuarioResponse DTO
     *
     * @param usuario Usuario del dominio
     * @return UsuarioResponse DTO
     */
    UsuarioResponse toResponse(Usuario usuario);
}
