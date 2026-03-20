package com.matias.application.service.impl;

import com.matias.application.dto.response.UsuarioResponse;
import com.matias.application.mapper.UsuarioMapper;
import com.matias.application.service.UsuarioService;
import com.matias.domain.model.Usuario;
import com.matias.domain.port.AuthenticationFacadePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final AuthenticationFacadePort authenticationFacadePort;
    private final UsuarioMapper usuarioMapper;

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerInfoUsuario() {
        log.debug("Obteniendo información del usuario autenticado");
        
        Usuario usuario = authenticationFacadePort.getUsuarioAutenticado();
        
        log.debug("Usuario obtenido: {}", usuario.getEmail());
        
        return usuarioMapper.toResponse(usuario);
    }
}
