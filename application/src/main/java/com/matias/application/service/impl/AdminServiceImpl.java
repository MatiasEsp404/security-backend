package com.matias.application.service.impl;

import com.matias.application.service.AdminService;
import com.matias.domain.exception.ConflictoException;
import com.matias.domain.exception.OperacionNoPermitidaException;
import com.matias.domain.exception.RecursoNoEncontradoException;
import com.matias.domain.model.Rol;
import com.matias.domain.model.Usuario;
import com.matias.domain.port.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

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

    @Override
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();
        
        // Contadores generales
        long totalUsuarios = usuarioRepository.count();
        long usuariosActivos = usuarioRepository.countByActivo(true);
        long usuariosInactivos = usuarioRepository.countByActivo(false);
        long emailsVerificados = usuarioRepository.countByEmailVerificado(true);
        long emailsPendientes = usuarioRepository.countByEmailVerificado(false);
        
        // Usuarios nuevos (últimos 30 días)
        Instant hace30Dias = Instant.now().minus(30, ChronoUnit.DAYS);
        long nuevosUsuarios = usuarioRepository.countByFechaCreacionAfter(hace30Dias);
        
        // Conteo por roles
        Map<Rol, Long> usuariosPorRol = usuarioRepository.countUsuariosPorRol();
        
        // Construir respuesta
        stats.put("totalUsuarios", totalUsuarios);
        stats.put("usuariosActivos", usuariosActivos);
        stats.put("usuariosInactivos", usuariosInactivos);
        stats.put("emailsVerificados", emailsVerificados);
        stats.put("emailsPendientes", emailsPendientes);
        stats.put("nuevosUsuariosUltimos30Dias", nuevosUsuarios);
        stats.put("usuariosPorRol", usuariosPorRol);
        
        return stats;
    }

    @Override
    public UsuarioRepositoryPort.PageResult<Usuario> buscarUsuarios(
            UsuarioRepositoryPort.UsuarioFilter filter,
            UsuarioRepositoryPort.PageRequest pageRequest) {
        return usuarioRepository.findAllWithFilters(filter, pageRequest);
    }
}
