package com.matias.web.controller;

import com.matias.application.service.AuthService;
import com.matias.application.service.PasswordResetService;
import com.matias.application.dto.internal.TokenInternal;
import com.matias.application.dto.request.ReenvioEmailRequest;
import com.matias.web.dto.request.ConfirmarResetPasswordRequest;
import com.matias.web.dto.request.LogueoWebRequest;
import com.matias.web.dto.request.RegistroWebRequest;
import com.matias.web.dto.request.SolicitudResetPasswordRequest;
import com.matias.web.dto.response.RegistroWebResponse;
import com.matias.web.dto.response.TokenWebResponse;
import com.matias.web.mapper.AuthWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;

@Slf4j
@Tag(name = "Autenticación", description = "Endpoints para registro, login, verificación de email y reseteo de contraseña.")
@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    @Value("${app.environment:dev}")
    private String environment;

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final AuthWebMapper authWebMapper;

    public AuthController(AuthService authService, PasswordResetService passwordResetService, AuthWebMapper authWebMapper) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
        this.authWebMapper = authWebMapper;
    }

    @Operation(summary = "Registrar usuario", description = "Crea un usuario y envía un email para verificar la cuenta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registro exitoso"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email ya registrado", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<RegistroWebResponse> register(@Valid @RequestBody RegistroWebRequest webRequest) {
        var appRequest = authWebMapper.toRegistroRequest(webRequest);
        var appResponse = authService.register(appRequest);
        var webResponse = authWebMapper.toRegistroWebResponse(appResponse);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/usuario/me")
                .build()
                .toUri();
        return ResponseEntity.created(location).body(webResponse);
    }

    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario y devuelve tokens JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas", content = @Content),
            @ApiResponse(responseCode = "403", description = "Email no verificado", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<TokenWebResponse> login(@Valid @RequestBody LogueoWebRequest webRequest, HttpServletResponse response) {
        var appRequest = authWebMapper.toLogueoRequest(webRequest);
        TokenInternal tokens = authService.login(appRequest);
        addRefreshTokenCookie(response, tokens.refreshToken(), Duration.ofDays(7));
        var webResponse = authWebMapper.toTokenWebResponse(new com.matias.application.dto.response.TokenResponse(tokens.accessToken()));
        return ResponseEntity.ok(webResponse);
    }

    @Operation(summary = "Refrescar tokens", description = "Genera un nuevo access token usando un refresh token válido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens refrescados"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Token no encontrado", content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenWebResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            String ipOrigen = obtenerIpCliente(request);
            log.warn("[SECURITY] Intento de refresh sin cookie de refresh token. IP: {}, User-Agent: {}",
                    ipOrigen, request.getHeader("User-Agent"));
            throw new RuntimeException("Refresh token no encontrado");
        }
        TokenInternal tokens = authService.refresh(refreshToken);
        addRefreshTokenCookie(response, tokens.refreshToken(), Duration.ofDays(7));
        var webResponse = authWebMapper.toTokenWebResponse(new com.matias.application.dto.response.TokenResponse(tokens.accessToken()));
        return ResponseEntity.ok(webResponse);
    }

    @Operation(summary = "Cerrar sesión", description = "Invalida el refresh token del usuario.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Sesión cerrada"),
            @ApiResponse(responseCode = "400", description = "Token inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Token no encontrado", content = @Content)
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            authService.logout(refreshToken);
        } else {
            String ipOrigen = obtenerIpCliente(request);
            log.debug("[SECURITY] Intento de logout sin cookie de refresh token. IP: {}", ipOrigen);
        }
        clearRefreshTokenCookie(response);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Verificar email", description = "Confirma el email usando el token enviado por correo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Email verificado"),
            @ApiResponse(responseCode = "400", description = "Token inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Token no encontrado", content = @Content)
    })
    @GetMapping("/verificar-email")
    public ResponseEntity<Void> verificarEmail(
            @Parameter(description = "Token de verificación", required = true) @RequestParam String token) {
        authService.verificarEmail(token);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reenviar verificación", description = "Envía nuevamente el email de verificación.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Email reenviado"),
            @ApiResponse(responseCode = "400", description = "Email inválido o usuario ya verificado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    @PostMapping("/reenviar-email-verificacion")
    public ResponseEntity<Void> reenviarEmailVerificacion(
            @Valid @RequestBody ReenvioEmailRequest request,
            HttpServletRequest httpServletRequest) {
        String ipOrigen = obtenerIpCliente(httpServletRequest);
        authService.reenviarEmailVerificacion(request, ipOrigen);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Solicitar reseteo de contraseña", description = "Envía un email con un token para resetear la contraseña.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Email enviado"),
            @ApiResponse(responseCode = "400", description = "Email inválido o límite de solicitudes excedido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    @PostMapping("/solicitar-reset-password")
    public ResponseEntity<Void> solicitarResetPassword(
            @Valid @RequestBody SolicitudResetPasswordRequest request,
            HttpServletRequest httpServletRequest) {
        String ipOrigen = obtenerIpCliente(httpServletRequest);
        authService.solicitarResetPassword(request.getEmail(), ipOrigen);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Validar token de reseteo", description = "Comprueba si el token de reseteo es válido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Token válido"),
            @ApiResponse(responseCode = "400", description = "Token inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Token no encontrado", content = @Content)
    })
    @GetMapping("/validar-token-reset")
    public ResponseEntity<Void> validarTokenReset(
            @Parameter(description = "Token de reseteo", required = true) @RequestParam String token) {
        passwordResetService.validarToken(token);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Confirmar reseteo de contraseña", description = "Actualiza la contraseña usando un token válido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Contraseña actualizada"),
            @ApiResponse(responseCode = "400", description = "Token inválido, expirado o contraseña inválida", content = @Content),
            @ApiResponse(responseCode = "404", description = "Token no encontrado", content = @Content)
    })
    @PostMapping("/confirmar-reset-password")
    public ResponseEntity<Void> confirmarResetPassword(@Valid @RequestBody ConfirmarResetPasswordRequest request) {
        passwordResetService.resetearPassword(request.getToken(), request.getNuevaPassword());
        return ResponseEntity.noContent().build();
    }

    private boolean isProduction() {
        return "prod".equals(environment);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> "refresh".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken, Duration maxAge) {
        ResponseCookie cookie = buildRefreshTokenCookie(refreshToken, maxAge);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private ResponseCookie buildRefreshTokenCookie(String value, Duration maxAge) {
        return ResponseCookie.from("refresh", value)
                .httpOnly(true)
                .secure(isProduction())
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(maxAge)
                .build();
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = buildRefreshTokenCookie("", Duration.ZERO);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
