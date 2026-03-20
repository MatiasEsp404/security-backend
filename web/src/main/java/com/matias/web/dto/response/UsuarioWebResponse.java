package com.matias.web.dto.response;

import com.matias.domain.model.Rol;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

/**
 * DTO de la capa web con información del usuario.
 * Contiene anotaciones de documentación OpenAPI/Swagger.
 */
@Schema(description = "Información del usuario autenticado")
public record UsuarioWebResponse(
    @Schema(description = "ID del usuario", example = "1")
    Long id,

    @Schema(description = "Email del usuario", example = "usuario@example.com")
    String email,

    @Schema(description = "Nombre del usuario", example = "Juan")
    String nombre,

    @Schema(description = "Apellido del usuario", example = "Pérez")
    String apellido,

    @Schema(description = "Roles asignados al usuario", example = "[\"USUARIO\", \"ADMIN\"]")
    Set<Rol> roles,

    @Schema(description = "Indica si la cuenta está activa", example = "true")
    Boolean activo,

    @Schema(description = "Indica si el email ha sido verificado", example = "true")
    Boolean emailVerificado
) {
}
