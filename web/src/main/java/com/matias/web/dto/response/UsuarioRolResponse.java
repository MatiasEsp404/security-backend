package com.matias.web.dto.response;

import com.matias.domain.model.Rol;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Información de un rol asignado a un usuario")
public record UsuarioRolResponse(

        @Schema(description = "ID del registro de asignación de rol", example = "1")
        Integer id,

        @Schema(description = "Rol asignado", example = "MODERADOR")
        Rol rol,

        @Schema(description = "Fecha y hora de asignación del rol", example = "2024-01-15T10:30:00Z")
        Instant fechaAsignacion,

        @Schema(description = "Información del administrador que asignó el rol (null si fue asignación automática)")
        AsignadoPorInfo asignadoPor

) {

    @Schema(description = "Información del administrador que asignó el rol")
    public record AsignadoPorInfo(

            @Schema(description = "ID del administrador", example = "5")
            Integer id,

            @Schema(description = "Email del administrador", example = "admin@example.com")
            String email,

            @Schema(description = "Nombre completo del administrador", example = "Juan Pérez")
            String nombreCompleto
    ) {
    }
}
