package com.matias.web.controller;

import com.matias.application.service.AuthService;
import com.matias.application.service.PasswordResetService;
import com.matias.application.dto.internal.TokenInternal;
import com.matias.web.dto.request.ConfirmarResetPasswordRequest;
import com.matias.application.dto.request.LogueoRequest;
import com.matias.application.dto.request.ReenvioEmailRequest;
import com.matias.application.dto.request.RegistroRequest;
import com.matias.web.dto.request.SolicitudResetPasswordRequest;
import com.matias.application.dto.response.RegistroResponse;
import com.matias.application.dto.response.TokenResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    @Value("${app.environment:dev}")
    private String environment;

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistroResponse> register(@Valid @RequestBody RegistroRequest request) {
        RegistroResponse response = authService.register(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/usuario/me")
                .build()
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LogueoRequest request, HttpServletResponse response) {
        TokenInternal tokens = authService.login(request);
        addRefreshTokenCookie(response, tokens.refreshToken(), Duration.ofDays(7));
        return ResponseEntity.ok(new TokenResponse(tokens.accessToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            throw new RuntimeException("Refresh token no encontrado");
        }
        TokenInternal tokens = authService.refresh(refreshToken);
        addRefreshTokenCookie(response, tokens.refreshToken(), Duration.ofDays(7));
        return ResponseEntity.ok(new TokenResponse(tokens.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        clearRefreshTokenCookie(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/verificar-email")
    public ResponseEntity<Void> verificarEmail(@RequestParam String token) {
        authService.verificarEmail(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reenviar-email-verificacion")
    public ResponseEntity<Void> reenviarEmailVerificacion(
            @Valid @RequestBody ReenvioEmailRequest request,
            HttpServletRequest httpServletRequest) {
        String ipOrigen = obtenerIpCliente(httpServletRequest);
        authService.reenviarEmailVerificacion(request, ipOrigen);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/solicitar-reset-password")
    public ResponseEntity<Void> solicitarResetPassword(
            @Valid @RequestBody SolicitudResetPasswordRequest request,
            HttpServletRequest httpServletRequest) {
        String ipOrigen = obtenerIpCliente(httpServletRequest);
        authService.solicitarResetPassword(request.getEmail(), ipOrigen);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validar-token-reset")
    public ResponseEntity<Void> validarTokenReset(@RequestParam String token) {
        passwordResetService.validarToken(token);
        return ResponseEntity.noContent().build();
    }

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
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
