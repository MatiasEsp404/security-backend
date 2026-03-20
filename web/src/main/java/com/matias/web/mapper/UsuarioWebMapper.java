package com.matias.web.mapper;

import com.matias.application.dto.response.UsuarioResponse;
import com.matias.web.dto.response.UsuarioWebResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper entre DTOs de la capa web y la capa de aplicación para usuarios.
 * Responsable de la conversión bidireccional entre contratos web y contratos de casos de uso.
 */
@Component
public class UsuarioWebMapper {

    /**
     * Convierte un UsuarioResponse (application) a UsuarioWebResponse (web)
     */
    public UsuarioWebResponse toUsuarioWebResponse(UsuarioResponse appResponse) {
        return new UsuarioWebResponse(
                appResponse.id(),
                appResponse.email(),
                appResponse.nombre(),
                appResponse.apellido(),
                appResponse.roles(),
                appResponse.activo(),
                appResponse.emailVerificado()
        );
    }
}
