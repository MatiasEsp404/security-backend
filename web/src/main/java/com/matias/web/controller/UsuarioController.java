package com.matias.web.controller;

import com.matias.application.dto.response.UsuarioResponse;
import com.matias.application.service.UsuarioService;
import com.matias.web.dto.response.UsuarioWebResponse;
import com.matias.web.mapper.UsuarioWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Usuario", description = "Endpoints para gestión de usuarios")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioWebMapper usuarioWebMapper;

    @Operation(
            summary = "Obtener información del usuario actual",
            description = "Retorna la información completa del usuario autenticado, incluyendo roles y estado de la cuenta."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Información obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<UsuarioWebResponse> obtenerInfoUsuarioActual() {
        UsuarioResponse appResponse = usuarioService.obtenerInfoUsuario();
        UsuarioWebResponse webResponse = usuarioWebMapper.toUsuarioWebResponse(appResponse);
        return ResponseEntity.ok(webResponse);
    }
}
