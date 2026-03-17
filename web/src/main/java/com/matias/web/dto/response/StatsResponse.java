package com.matias.web.dto.response;

import com.matias.domain.model.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Estadísticas generales del sistema")
public record StatsResponse(

        @Schema(description = "Estadísticas de usuarios")
        UsuariosStats usuarios,

        @Schema(description = "Estadísticas de verificación de email")
        VerificacionStats verificacion,

        @Schema(description = "Estadísticas de crecimiento")
        CrecimientoStats crecimiento,

        @Schema(description = "Cantidad de usuarios por rol")
        Map<Rol, Long> usuariosPorRol

) {

    @Schema(description = "Estadísticas de usuarios")
    public record UsuariosStats(
            @Schema(description = "Total de usuarios registrados", example = "1523")
            Long total,

            @Schema(description = "Usuarios con cuenta activa", example = "1450")
            Long activos,

            @Schema(description = "Usuarios con cuenta inactiva", example = "73")
            Long inactivos
    ) {}

    @Schema(description = "Estadísticas de verificación")
    public record VerificacionStats(
            @Schema(description = "Usuarios con email verificado", example = "1400")
            Long verificados,

            @Schema(description = "Usuarios pendientes de verificación", example = "123")
            Long pendientes
    ) {}

    @Schema(description = "Estadísticas de crecimiento")
    public record CrecimientoStats(
            @Schema(description = "Usuarios registrados este mes", example = "45")
            Long esteMes,

            @Schema(description = "Usuarios registrados hoy", example = "3")
            Long hoy
    ) {}
}
