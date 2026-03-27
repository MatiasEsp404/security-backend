package com.matias.web.controller;

import com.matias.application.dto.internal.AuditPageQuery;
import com.matias.application.dto.internal.PageQuery;
import com.matias.application.dto.internal.PageResult;
import com.matias.application.dto.internal.SearchUsersQuery;
import com.matias.application.service.AdminService;
import com.matias.domain.model.Rol;
import com.matias.domain.model.Usuario;
import com.matias.domain.model.UsuarioAudit;
import com.matias.web.dto.request.UpdateUserStatusRequest;
import com.matias.web.dto.request.UsuarioFilterRequest;
import com.matias.web.dto.response.PageResponse;
import com.matias.web.dto.response.StatsResponse;
import com.matias.web.dto.response.UsuarioAuditResponse;
import com.matias.web.dto.response.UsuarioListItemResponse;
import com.matias.web.dto.response.UsuarioRolResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Endpoints de administración del sistema")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    @Operation(
        summary = "Obtener estadísticas del sistema",
        description = "Devuelve estadísticas generales sobre usuarios, roles y actividad"
    )
    @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente")
    public ResponseEntity<StatsResponse> obtenerEstadisticas() {
        Map<String, Object> stats = adminService.obtenerEstadisticas();
        
        StatsResponse response = new StatsResponse(
            new StatsResponse.UsuariosStats(
                ((Number) stats.get("totalUsuarios")).longValue(),
                ((Number) stats.get("usuariosActivos")).longValue(),
                ((Number) stats.get("usuariosInactivos")).longValue()
            ),
            new StatsResponse.VerificacionStats(
                ((Number) stats.get("emailsVerificados")).longValue(),
                ((Number) stats.get("emailsPendientes")).longValue()
            ),
            new StatsResponse.CrecimientoStats(
                ((Number) stats.get("nuevosUsuariosUltimos30Dias")).longValue(),
                0L  // Hoy - no implementado aún
            ),
            (Map<Rol, Long>) stats.get("usuariosPorRol")
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    @Operation(
        summary = "Buscar usuarios con filtros",
        description = "Permite buscar usuarios aplicando múltiples filtros y paginación"
    )
    @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente")
    public ResponseEntity<PageResponse<UsuarioListItemResponse>> buscarUsuarios(
            @Parameter(description = "Búsqueda por email o nombre") @RequestParam(required = false) String search,
            @Parameter(description = "Filtrar por estado activo") @RequestParam(required = false) Boolean activo,
            @Parameter(description = "Filtrar por email verificado") @RequestParam(required = false) Boolean emailVerificado,
            @Parameter(description = "Filtrar por roles (múltiples valores separados por coma)") 
            @RequestParam(required = false) List<Rol> roles,
            @Parameter(description = "Fecha desde (ISO 8601)") @RequestParam(required = false) String fechaDesde,
            @Parameter(description = "Fecha hasta (ISO 8601)") @RequestParam(required = false) String fechaHasta,
            @Parameter(description = "Número de página (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo de ordenamiento") @RequestParam(defaultValue = "fechaCreacion") String sortBy,
            @Parameter(description = "Dirección de ordenamiento") @RequestParam(defaultValue = "DESC") String direction
    ) {
        // Construir query de búsqueda
        SearchUsersQuery searchQuery = SearchUsersQuery.builder()
            .search(search)
            .activo(activo)
            .emailVerificado(emailVerificado)
            .roles(roles != null ? roles.stream().collect(Collectors.toSet()) : null)
            .fechaDesde(fechaDesde != null ? java.time.Instant.parse(fechaDesde) : null)
            .fechaHasta(fechaHasta != null ? java.time.Instant.parse(fechaHasta) : null)
            .build();

        // Construir query de paginación
        PageQuery pageQuery = PageQuery.builder()
            .page(page)
            .size(size)
            .sortBy(sortBy)
            .direction("ASC".equalsIgnoreCase(direction) 
                ? PageQuery.SortDirection.ASC 
                : PageQuery.SortDirection.DESC)
            .build();

        // Ejecutar búsqueda
        PageResult<Usuario> result = adminService.buscarUsuarios(searchQuery, pageQuery);

        // Convertir a DTO
        List<UsuarioListItemResponse> content = result.content().stream()
            .map(usuario -> new UsuarioListItemResponse(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombre() + " " + usuario.getApellido(),
                usuario.getRoles(),
                usuario.getActivo(),
                usuario.getEmailVerificado(),
                usuario.getFechaCreacion()
            ))
            .collect(Collectors.toList());

        PageResponse<UsuarioListItemResponse> response = new PageResponse<>(
            content,
            result.pageNumber(),
            result.pageSize(),
            result.totalElements(),
            result.totalPages(),
            result.isFirst(),
            result.isLast()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}")
    @Operation(
        summary = "Obtener detalles de un usuario",
        description = "Devuelve información completa de un usuario específico"
    )
    @ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    public ResponseEntity<UsuarioListItemResponse> obtenerDetalleUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Integer userId
    ) {
        Usuario usuario = adminService.obtenerDetalleUsuario(userId);
        
        UsuarioListItemResponse response = new UsuarioListItemResponse(
            usuario.getId(),
            usuario.getEmail(),
            usuario.getNombre() + " " + usuario.getApellido(),
            usuario.getRoles(),
            usuario.getActivo(),
            usuario.getEmailVerificado(),
            usuario.getFechaCreacion()
        );
        
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/users/{userId}/status")
    @Operation(
        summary = "Actualizar estado de un usuario",
        description = "Activa o desactiva un usuario. No se pueden desactivar usuarios con rol ADMINISTRADOR o MODERADOR"
    )
    @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente")
    @ApiResponse(responseCode = "400", description = "Operación no permitida")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    public ResponseEntity<Void> actualizarEstadoUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Integer userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        adminService.updateUserStatus(userId, request.active());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/roles/{rol}")
    @Operation(
        summary = "Asignar rol a un usuario",
        description = "Asigna un rol adicional a un usuario existente"
    )
    @ApiResponse(responseCode = "200", description = "Rol asignado exitosamente")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    @ApiResponse(responseCode = "409", description = "El usuario ya tiene el rol asignado")
    public ResponseEntity<UsuarioRolResponse> asignarRol(
            @Parameter(description = "ID del usuario") @PathVariable Integer userId,
            @Parameter(description = "Rol a asignar") @PathVariable Rol rol
    ) {
        adminService.assignRole(userId, rol);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{userId}/roles/{rol}")
    @Operation(
        summary = "Remover rol de un usuario",
        description = "Remueve un rol de un usuario. El rol USUARIO no puede ser removido"
    )
    @ApiResponse(responseCode = "200", description = "Rol removido exitosamente")
    @ApiResponse(responseCode = "400", description = "El rol USUARIO no puede ser removido")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    public ResponseEntity<UsuarioRolResponse> removerRol(
            @Parameter(description = "ID del usuario") @PathVariable Integer userId,
            @Parameter(description = "Rol a remover") @PathVariable Rol rol
    ) {
        adminService.unassignRole(userId, rol);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{userId}/audit")
    @Operation(
        summary = "Obtener historial de auditoría de un usuario",
        description = "Devuelve el historial completo de cambios realizados sobre un usuario"
    )
    @ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    public ResponseEntity<PageResponse<UsuarioAuditResponse>> obtenerHistorialUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Integer userId,
            @Parameter(description = "Número de página (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Dirección de ordenamiento") @RequestParam(defaultValue = "DESC") String direction
    ) {
        // Construir query de paginación para auditoría
        AuditPageQuery pageQuery = AuditPageQuery.builder()
            .page(page)
            .size(size)
            .direction("ASC".equalsIgnoreCase(direction) 
                ? AuditPageQuery.SortDirection.ASC 
                : AuditPageQuery.SortDirection.DESC)
            .build();
        
        // Ejecutar consulta
        PageResult<UsuarioAudit> auditPage = adminService.obtenerHistorialUsuario(userId, pageQuery);
        
        // Convertir a DTO
        List<UsuarioAuditResponse> content = auditPage.content().stream()
            .map(audit -> new UsuarioAuditResponse(
                audit.usuarioId(),
                audit.email(),
                audit.nombre(),
                audit.apellido(),
                audit.activo(),
                audit.emailVerificado(),
                audit.revision(),
                audit.fechaRevision(),
                audit.usuarioModificador(),
                audit.tipoRevision()
            ))
            .collect(Collectors.toList());

        PageResponse<UsuarioAuditResponse> response = new PageResponse<>(
            content,
            auditPage.pageNumber(),
            auditPage.pageSize(),
            auditPage.totalElements(),
            auditPage.totalPages(),
            auditPage.isFirst(),
            auditPage.isLast()
        );

        return ResponseEntity.ok(response);
    }
}
