package com.matias.application.service.impl;

import com.matias.application.service.AdminService;
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
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));
    }

    @Override
    public void updateUserStatus(Integer userId, boolean active) {
        Usuario usuario = obtenerDetalleUsuario(userId);
        
        if (!active) {
            boolean tieneRolesPrivilegiados = usuario.getRoles().stream()
                    .anyMatch(rol -> rol == Rol.ADMINISTRADOR || rol == Rol.MODERADOR);
            if (tieneRolesPrivilegiados) {
                throw new RuntimeException("No se puede desactivar un usuario con rol ADMINISTRADOR o MODERADOR");
            }
        }
        
        usuarioRepository.updateStatus(userId, active);
    }

    @Override
    public void assignRole(Integer userId, Rol rol) {
        Usuario usuario = obtenerDetalleUsuario(userId);
        if (usuarioRepository.existsByUsuarioIdAndRol(userId, rol)) {
            throw new RuntimeException("El usuario ya tiene el rol asignado");
        }
        usuarioRepository.assignRole(userId, rol);
    }

    @Override
    public void unassignRole(Integer userId, Rol rol) {
        if (rol == Rol.USUARIO) {
            throw new RuntimeException("El rol USUARIO no puede ser removido");
        }
        usuarioRepository.unassignRole(userId, rol);
    }
}
