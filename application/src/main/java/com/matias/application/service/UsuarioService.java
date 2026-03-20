package com.matias.application.service;

import com.matias.application.dto.response.UsuarioResponse;

public interface UsuarioService {

    /**
     * Obtiene la información completa del usuario actualmente autenticado
     *
     * @return UsuarioResponse con la información del usuario
     */
    UsuarioResponse obtenerInfoUsuario();
}
