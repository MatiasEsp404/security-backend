package com.matias.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Datos para actualizar el estado de un usuario")
public record UpdateUserStatusRequest(

    @NotNull(message = "El campo 'active' es obligatorio")
    @Schema(
        description = "Indica si el usuario está activo",
        example = "false"
    )
    Boolean active
) {}
