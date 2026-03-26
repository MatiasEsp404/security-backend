package com.matias.application.service;

import com.matias.application.dto.internal.AuditPageQuery;
import com.matias.application.dto.internal.PageQuery;
import com.matias.application.dto.internal.PageResult;
import com.matias.application.dto.internal.SearchUsersQuery;
import com.matias.domain.model.Rol;
import com.matias.domain.model.Usuario;
import com.matias.domain.model.UsuarioAudit;

import java.util.Map;

public interface AdminService {
    Usuario obtenerDetalleUsuario(Integer userId);
    void updateUserStatus(Integer userId, boolean active);
    void assignRole(Integer userId, Rol rol);
    void unassignRole(Integer userId, Rol rol);
    
    // Nuevos métodos para administración
    Map<String, Object> obtenerEstadisticas();
    PageResult<Usuario> buscarUsuarios(SearchUsersQuery query, PageQuery pageQuery);
    
    // Auditoría
    PageResult<UsuarioAudit> obtenerHistorialUsuario(Integer userId, AuditPageQuery pageQuery);
}
