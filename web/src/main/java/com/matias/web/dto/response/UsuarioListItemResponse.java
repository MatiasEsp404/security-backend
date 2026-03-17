package com.matias.web.dto.response;

import com.matias.domain.model.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Set;

@Schema(description = "Usuario en listado (versión ligera)")
public record UsuarioListItemResponse(

        @Schema(description = "ID del usuario", example = "1")
        Integer id,

        @Schema(description = "Email del usuario", example = "usuario@example.com")
        String email,

        @Schema(description = "Nombre completo del usuario", example = "Juan Pérez")
        String nombreCompleto,

        @Schema(description = "Roles asignados al usuario", example = "[\"USUARIO\", \"MODERADOR\"]")
        Set<Rol> roles,

        @Schema(description = "Indica si la cuenta está activa", example = "true")
        Boolean activo,

        @Schema(description = "Indica si el email ha sido verificado", example = "true")
        Boolean emailVerificado,

        @Schema(description = "Fecha de creación del usuario", example = "2024-01-15T10:30:00Z")
        Instant fechaCreacion
) {}
