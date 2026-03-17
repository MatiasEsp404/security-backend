package com.matias.web.dto.request;

import com.matias.domain.model.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Set;

@Schema(description = "Filtros para búsqueda de usuarios")
public record UsuarioFilterRequest(

        @Schema(description = "Búsqueda por email o nombre completo", example = "juan")
        String search,

        @Schema(description = "Filtrar por estado activo", example = "true")
        Boolean activo,

        @Schema(description = "Filtrar por email verificado", example = "true")
        Boolean emailVerificado,

        @Schema(description = "Filtrar por roles (OR - al menos uno)", example = "[\"USUARIO\", \"MODERADOR\"]")
        Set<Rol> roles,

        @Schema(description = "Fecha de creación desde (ISO 8601)", example = "2024-01-01T00:00:00Z")
        Instant fechaDesde,

        @Schema(description = "Fecha de creación hasta (ISO 8601)", example = "2024-12-31T23:59:59Z")
        Instant fechaHasta
) {}
