package com.matias.application.service;

import com.matias.domain.model.Rol;
import com.matias.domain.model.Usuario;
import com.matias.domain.port.UsuarioRepositoryPort;

import java.util.Map;

public interface AdminService {
    Usuario obtenerDetalleUsuario(Integer userId);
    void updateUserStatus(Integer userId, boolean active);
    void assignRole(Integer userId, Rol rol);
    void unassignRole(Integer userId, Rol rol);
    
    // Nuevos métodos para administración
    Map<String, Object> obtenerEstadisticas();
    UsuarioRepositoryPort.PageResult<Usuario> buscarUsuarios(
            UsuarioRepositoryPort.UsuarioFilter filter,
            UsuarioRepositoryPort.PageRequest pageRequest
    );
}
