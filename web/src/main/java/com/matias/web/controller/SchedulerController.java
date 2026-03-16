package com.matias.web.controller;

import com.matias.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Scheduler", description = "Ejecución manual de tareas programadas")
@RestController
@RequestMapping("/v1/scheduler")
@RequiredArgsConstructor
public class SchedulerController {

    private final AuthService authService;

    @Operation(
            summary = "Ejecutar limpieza de datos",
            description = "Inicia manualmente el proceso de eliminación de datos obsoletos (tokens expirados, intentos antiguos, etc.)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Limpieza ejecutada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos de administrador")
    })
    @PostMapping("/cleanup")
    public ResponseEntity<Void> limpiarDatosObsoletos() {
        authService.limpiarDatosObsoletos();
        return ResponseEntity.noContent().build();
    }
}
