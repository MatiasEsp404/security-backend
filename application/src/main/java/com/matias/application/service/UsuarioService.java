package com.matias.application.service;

import com.matias.application.dto.response.UsuarioResponse;

public interface UsuarioService {

    /**
     * Obtiene la información completa de un usuario autenticado
     *
     * @param email Email del usuario autenticado
     * @return UsuarioResponse con la información del usuario
     */
    UsuarioResponse obtenerInfoUsuario(String email);
}
