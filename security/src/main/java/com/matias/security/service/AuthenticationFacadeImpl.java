package com.matias.security.service;

import com.matias.domain.exception.NoAutenticadoException;
import com.matias.domain.model.Usuario;
import com.matias.domain.port.AuthenticationFacadePort;
import com.matias.domain.port.UsuarioRepositoryPort;
import com.matias.security.model.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Implementación del puerto AuthenticationFacadePort que abstrae el acceso
 * al usuario autenticado usando Spring Security.
 * <p>
 * Esta implementación centraliza la lógica de obtención del usuario autenticado,
 * eliminando la necesidad de que los servicios de aplicación conozcan detalles
 * de Spring Security (SecurityContextHolder, SecurityUser, etc.).
 * </p>
 * <p>
 * Flujo de ejecución:
 * <ol>
 *   <li>Obtiene el Authentication del SecurityContext</li>
 *   <li>Extrae el SecurityUser del Principal</li>
 *   <li>Usa el email para buscar el Usuario completo del dominio</li>
 *   <li>Retorna el Usuario o lanza excepción si no está autenticado</li>
 * </ol>
 * </p>
 *
 * @see AuthenticationFacadePort
 * @see SecurityUser
 */
@Service
public class AuthenticationFacadeImpl implements AuthenticationFacadePort {

    private final UsuarioRepositoryPort usuarioRepository;

    public AuthenticationFacadeImpl(UsuarioRepositoryPort usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() ||
                auth.getPrincipal().equals("anonymousUser")) {
            throw new NoAutenticadoException("No hay usuario autenticado");
        }

        SecurityUser securityUser = (SecurityUser) auth.getPrincipal();
        return usuarioRepository.findByEmail(securityUser.getUsername())
                .orElseThrow(() -> new NoAutenticadoException("Usuario no encontrado en el sistema"));
    }

    @Override
    public String getEmailUsuarioAutenticado() {
        return getUsuarioAutenticado().getEmail();
    }

    @Override
    public Integer getIdUsuarioAutenticado() {
        return getUsuarioAutenticado().getId();
    }
}
