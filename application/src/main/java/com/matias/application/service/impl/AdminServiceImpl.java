package com.matias.application.service.impl;

import com.matias.application.service.AdminService;
import com.matias.domain.exception.ConflictoException;
import com.matias.domain.exception.OperacionNoPermitidaException;
import com.matias.domain.exception.RecursoNoEncontradoException;
import com.matias.domain.model.Rol;
import com.matias.domain.model.Usuario;
import com.matias.domain.port.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    private final UsuarioRepositoryPort usuarioRepository;

    public AdminServiceImpl(UsuarioRepositoryPort usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario obtenerDetalleUsuario(Integer userId) {
        return usuarioRepository.findById(userId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + userId));
    }

    @Override
    public void updateUserStatus(Integer userId, boolean active) {
        Usuario usuario = obtenerDetalleUsuario(userId);
        
        if (!active) {
            boolean tieneRolesPrivilegiados = usuario.getRoles().stream()
                    .anyMatch(rol -> rol == Rol.ADMINISTRADOR || rol == Rol.MODERADOR);
            if (tieneRolesPrivilegiados) {
                throw new OperacionNoPermitidaException("No se puede desactivar un usuario con rol ADMINISTRADOR o MODERADOR");
            }
        }
        
        usuarioRepository.updateStatus(userId, active);
    }

    @Override
    public void assignRole(Integer userId, Rol rol) {
        Usuario usuario = obtenerDetalleUsuario(userId);
        if (usuarioRepository.existsByUsuarioIdAndRol(userId, rol)) {
            throw new ConflictoException("El usuario ya tiene el rol asignado");
        }
        usuarioRepository.assignRole(userId, rol);
    }

    @Override
    public void unassignRole(Integer userId, Rol rol) {
        if (rol == Rol.USUARIO) {
            throw new OperacionNoPermitidaException("El rol USUARIO no puede ser removido");
        }
        usuarioRepository.unassignRole(userId, rol);
    }
}
