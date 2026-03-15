package com.matias.application.service.impl;

import com.matias.application.dto.internal.TokenInternal;
import com.matias.application.dto.request.LogueoRequest;
import com.matias.application.dto.request.RegistroRequest;
import com.matias.application.dto.response.RegistroResponse;
import com.matias.application.email.VerificationEmailTemplate;
import com.matias.application.service.AuthService;
import com.matias.domain.exception.AccesoDenegadoException;
import com.matias.domain.exception.ConflictoException;
import com.matias.domain.exception.NoAutenticadoException;
import com.matias.domain.exception.OperacionNoPermitidaException;
import com.matias.domain.exception.RecursoNoEncontradoException;
import com.matias.domain.model.Rol;
import com.matias.domain.model.TokenVerificacion;
import com.matias.domain.model.Usuario;
import com.matias.domain.port.EmailServicePort;
import com.matias.domain.port.TokenServicePort;
import com.matias.domain.port.TokenVerificacionRepositoryPort;
import com.matias.domain.port.UsuarioRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepositoryPort usuarioRepository;
    private final TokenServicePort tokenService;
    private final PasswordEncoder passwordEncoder;
    private final TokenVerificacionRepositoryPort tokenVerificacionRepository;
    private final EmailServicePort emailService;

    @Value("${app.back-url}")
    private String backUrl;

    @Value("${app.front-url}")
    private String frontUrl;

    @Value("${app.verification.token.expiration-hours:24}")
    private int tokenExpirationHours;

    public AuthServiceImpl(UsuarioRepositoryPort usuarioRepository, 
                          TokenServicePort tokenService,
                          PasswordEncoder passwordEncoder,
                          TokenVerificacionRepositoryPort tokenVerificacionRepository,
                          EmailServicePort emailService) {
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.tokenVerificacionRepository = tokenVerificacionRepository;
        this.emailService = emailService;
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

        // Generar token de verificación
        String token = UUID.randomUUID().toString();
        Instant expiracion = Instant.now().plus(tokenExpirationHours, ChronoUnit.HOURS);
        
        TokenVerificacion tokenVerificacion = new TokenVerificacion(token, expiracion, usuarioGuardado.getId());
        tokenVerificacionRepository.save(tokenVerificacion);
        
        log.info("Token de verificación generado para usuario ID: {}", usuarioGuardado.getId());

        // Enviar email de verificación
        try {
            VerificationEmailTemplate emailTemplate = new VerificationEmailTemplate(
                    usuarioGuardado.getEmail(),
                    usuarioGuardado.getNombre(),
                    token,
                    frontUrl,
                    tokenExpirationHours + " horas"
            );
            emailService.send(emailTemplate);
            log.info("Email de verificación enviado a: {}", usuarioGuardado.getEmail());
        } catch (Exception e) {
            log.error("Error al enviar email de verificación: {}", e.getMessage(), e);
            // No fallar el registro si el email no se puede enviar
        }
        
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
        
        // Buscar token
        TokenVerificacion tokenVerificacion = tokenVerificacionRepository.findByToken(token)
                .orElseThrow(() -> new RecursoNoEncontradoException("Token de verificación no encontrado"));

        // Validar token
        if (tokenVerificacion.estaExpirado()) {
            tokenVerificacion.marcarComoExpirado();
            tokenVerificacionRepository.save(tokenVerificacion);
            throw new OperacionNoPermitidaException("El token ha expirado. Solicite uno nuevo");
        }

        if (tokenVerificacion.estaUsado()) {
            throw new OperacionNoPermitidaException("El token ya ha sido utilizado");
        }

        if (!tokenVerificacion.esValido()) {
            throw new OperacionNoPermitidaException("El token no es válido");
        }

        // Buscar usuario
        Usuario usuario = usuarioRepository.findById(tokenVerificacion.getUsuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        // Verificar email del usuario
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        // Marcar token como usado
        tokenVerificacion.marcarComoUsado();
        tokenVerificacionRepository.save(tokenVerificacion);

        log.info("Email verificado exitosamente para usuario ID: {}", usuario.getId());
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
