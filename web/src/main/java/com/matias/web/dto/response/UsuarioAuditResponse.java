package com.matias.web.dto.response;

import com.matias.domain.model.TipoRevision;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Respuesta con datos de auditoría de un usuario")
public record UsuarioAuditResponse(
    @Schema(description = "ID del usuario", example = "1")
    Integer usuarioId,
    
    @Schema(description = "Email del usuario en esta revisión", example = "usuario@ejemplo.com")
    String email,
    
    @Schema(description = "Nombre del usuario en esta revisión", example = "Juan")
    String nombre,
    
    @Schema(description = "Apellido del usuario en esta revisión", example = "Pérez")
    String apellido,
    
    @Schema(description = "Estado de activación en esta revisión", example = "true")
    Boolean activo,
    
    @Schema(description = "Estado de verificación de email en esta revisión", example = "true")
    Boolean emailVerificado,
    
    @Schema(description = "Número de revisión", example = "5")
    Integer revision,
    
    @Schema(description = "Fecha de la revisión")
    Instant fechaRevision,
    
    @Schema(description = "Email del usuario que realizó la modificación", example = "admin@ejemplo.com")
    String usuarioModificador,
    
    @Schema(description = "Tipo de operación realizada", example = "MOD")
    TipoRevision tipoRevision
) {
}
