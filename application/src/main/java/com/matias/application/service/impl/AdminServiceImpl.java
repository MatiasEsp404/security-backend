package com.matias.application.service.impl;

import com.matias.application.dto.internal.AuditPageQuery;
import com.matias.application.dto.internal.PageQuery;
import com.matias.application.dto.internal.PageResult;
import com.matias.application.dto.internal.SearchUsersQuery;
import com.matias.application.service.AdminService;
import com.matias.domain.exception.ConflictoException;
import com.matias.domain.exception.OperacionNoPermitidaException;
import com.matias.domain.exception.RecursoNoEncontradoException;
import com.matias.domain.model.Rol;
import com.matias.domain.model.Usuario;
import com.matias.domain.model.UsuarioAudit;
import com.matias.domain.port.UsuarioAuditRepositoryPort;
import com.matias.domain.port.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    private final UsuarioRepositoryPort usuarioRepository;
    private final UsuarioAuditRepositoryPort usuarioAuditRepository;

    public AdminServiceImpl(UsuarioRepositoryPort usuarioRepository,
                           UsuarioAuditRepositoryPort usuarioAuditRepository) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioAuditRepository = usuarioAuditRepository;
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
    public PageResult<Usuario> buscarUsuarios(SearchUsersQuery query, PageQuery pageQuery) {
        // Mapear query de aplicación a filtro de repositorio
        UsuarioRepositoryPort.UsuarioFilter filter = new UsuarioRepositoryPort.UsuarioFilter(
            query.search(),
            query.activo(),
            query.emailVerificado(),
            query.roles(),
            query.fechaDesde(),
            query.fechaHasta()
        );

        // Mapear paginación de aplicación a paginación de repositorio
        UsuarioRepositoryPort.PageRequest pageRequest = new UsuarioRepositoryPort.PageRequest(
            pageQuery.page(),
            pageQuery.size(),
            pageQuery.sortBy(),
            pageQuery.direction() == PageQuery.SortDirection.ASC 
                ? UsuarioRepositoryPort.SortDirection.ASC 
                : UsuarioRepositoryPort.SortDirection.DESC
        );

        // Ejecutar consulta
        UsuarioRepositoryPort.PageResult<Usuario> result = usuarioRepository.findAllWithFilters(filter, pageRequest);

        // Mapear resultado de repositorio a resultado de aplicación
        return new PageResult<>(
            result.content(),
            result.pageNumber(),
            result.pageSize(),
            result.totalElements(),
            result.totalPages(),
            result.isFirst(),
            result.isLast()
        );
    }

    @Override
    public PageResult<UsuarioAudit> obtenerHistorialUsuario(Integer userId, AuditPageQuery pageQuery) {
        // Verificar que el usuario existe
        obtenerDetalleUsuario(userId);

        // Mapear paginación de aplicación a paginación de repositorio
        UsuarioAuditRepositoryPort.PageRequest pageRequest = new UsuarioAuditRepositoryPort.PageRequest(
            pageQuery.page(),
            pageQuery.size(),
            pageQuery.direction() == AuditPageQuery.SortDirection.ASC
                ? UsuarioAuditRepositoryPort.SortDirection.ASC
                : UsuarioAuditRepositoryPort.SortDirection.DESC
        );

        // Ejecutar consulta
        UsuarioAuditRepositoryPort.PageResult<UsuarioAudit> result = 
            usuarioAuditRepository.findAuditByUsuarioId(userId, pageRequest);

        // Mapear resultado de repositorio a resultado de aplicación
        return new PageResult<>(
            result.content(),
            result.pageNumber(),
            result.pageSize(),
            result.totalElements(),
            result.totalPages(),
            result.isFirst(),
            result.isLast()
        );
    }
}
