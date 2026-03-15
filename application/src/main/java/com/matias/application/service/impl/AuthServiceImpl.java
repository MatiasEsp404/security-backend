package com.matias.application.service.impl;

import com.matias.application.dto.internal.TokenInternal;
import com.matias.application.dto.request.LogueoRequest;
import com.matias.application.dto.request.RegistroRequest;
import com.matias.application.dto.response.RegistroResponse;
import com.matias.application.service.AuthService;
import com.matias.domain.exception.AccesoDenegadoException;
import com.matias.domain.exception.ConflictoException;
import com.matias.domain.exception.NoAutenticadoException;
import com.matias.domain.exception.RecursoNoEncontradoException;
import com.matias.domain.model.Rol;
import com.matias.domain.model.Usuario;
import com.matias.domain.port.TokenServicePort;
import com.matias.domain.port.UsuarioRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepositoryPort usuarioRepository;
    private final TokenServicePort tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UsuarioRepositoryPort usuarioRepository, 
                          TokenServicePort tokenService,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public RegistroResponse register(RegistroRequest request) {
        log.info("Iniciando registro de usuario con email: {}", request.email());
        
        // Verificar si el usuario ya existe
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new ConflictoException("El email ya está registrado");
        }

        // Crear usuario
        Usuario usuario = Usuario.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nombre(request.nombre())
                .apellido(request.apellido())
                .fechaCreacion(Instant.now())
                .activo(true)
                .emailVerificado(false)
                .roles(Set.of(Rol.USUARIO))
                .build();

        // Guardar usuario
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        
        log.info("Usuario registrado exitosamente con ID: {}", usuarioGuardado.getId());
        
        return new RegistroResponse(
                usuarioGuardado.getId(),
                usuarioGuardado.getEmail()
        );
    }

    @Override
    public TokenInternal login(LogueoRequest request) {
        log.info("Intento de login para email: {}", request.email());
        
        // Buscar usuario
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new NoAutenticadoException("Credenciales inválidas"));

        // Verificar contraseña
        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new NoAutenticadoException("Credenciales inválidas");
        }

        // Verificar si el usuario está activo
        if (!usuario.getActivo()) {
            throw new AccesoDenegadoException("Usuario inactivo. Contacte al administrador");
        }

        // Generar refresh token primero
        String refreshToken = tokenService.generateRefreshToken(usuario.getEmail());
        
        // Generar access token con el refresh token
        String accessToken = tokenService.generateAccessToken(usuario.getEmail(), usuario.getRoles(), refreshToken);

        log.info("Login exitoso para usuario: {}", usuario.getEmail());
        
        return new TokenInternal(accessToken, refreshToken);
    }

    @Override
    public TokenInternal refresh(String refreshToken) {
        log.info("Intento de refresh token");
        
        // Validar que es un refresh token
        if (!tokenService.esRefreshToken(refreshToken)) {
            throw new NoAutenticadoException("Token inválido: no es un refresh token");
        }
        
        // Obtener email del token
        String email = tokenService.extractEmail(refreshToken);
        
        // Buscar usuario
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        // Generar nuevos tokens
        String newRefreshToken = tokenService.generateRefreshToken(usuario.getEmail());
        String newAccessToken = tokenService.generateAccessToken(usuario.getEmail(), usuario.getRoles(), newRefreshToken);

        log.info("Tokens refrescados exitosamente para usuario: {}", email);
        
        return new TokenInternal(newAccessToken, newRefreshToken);
    }

    @Override
    public void logout(String refreshToken) {
        log.info("Intento de logout");
        // TODO: Implementar lógica de logout (blacklist de tokens, etc.)
        log.info("Logout procesado");
    }

    @Override
    public void verificarEmail(String token) {
        log.info("Verificando email con token");
        // TODO: Implementar lógica de verificación de email
        throw new UnsupportedOperationException("Funcionalidad no implementada aún");
    }

    @Override
    public void reenviarEmailVerificacion(Object request, String ipOrigen) {
        log.info("Reenviando email de verificación desde IP: {}", ipOrigen);
        // TODO: Implementar lógica de reenvío de email
        throw new UnsupportedOperationException("Funcionalidad no implementada aún");
    }

    @Override
    public void solicitarReseteoPassword(Object request, String ipOrigen) {
        log.info("Solicitando reseteo de password desde IP: {}", ipOrigen);
        // TODO: Implementar lógica de solicitud de reseteo
        throw new UnsupportedOperationException("Funcionalidad no implementada aún");
    }

    @Override
    public void validarTokenReset(String token) {
        log.info("Validando token de reset");
        // TODO: Implementar lógica de validación de token
        throw new UnsupportedOperationException("Funcionalidad no implementada aún");
    }

    @Override
    public void resetearPassword(Object request) {
        log.info("Reseteando password");
        // TODO: Implementar lógica de reseteo de password
        throw new UnsupportedOperationException("Funcionalidad no implementada aún");
    }
}
