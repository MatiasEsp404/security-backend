package com.matias.application.service;

import com.matias.domain.model.Rol;
import com.matias.domain.model.Usuario;

public interface AdminService {
    Usuario obtenerDetalleUsuario(Integer userId);
    void updateUserStatus(Integer userId, boolean active);
    void assignRole(Integer userId, Rol rol);
    void unassignRole(Integer userId, Rol rol);
}
