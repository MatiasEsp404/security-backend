package com.matias.application.service.impl;

import com.matias.application.dto.internal.TokenInternal;
import com.matias.application.dto.request.LogueoRequest;
import com.matias.application.dto.request.ReenvioEmailRequest;
import com.matias.application.dto.request.RegistroRequest;
import com.matias.application.dto.response.RegistroResponse;
import com.matias.application.email.VerificationEmailTemplate;
import com.matias.application.service.AuthService;
import com.matias.application.service.PasswordResetService;
import com.matias.application.service.VerificacionEmailService;
import com.matias.domain.exception.AccesoDenegadoException;
import com.matias.domain.exception.ConflictoException;
import com.matias.domain.exception.NoAutenticadoException;
import com.matias.domain.exception.OperacionNoPermitidaException;
import com.matias.domain.exception.RecursoNoEncontradoException;
import com.matias.domain.model.Rol;
import com.matias.domain.model.TokenVerificacion;
import com.matias.domain.model.Usuario;
import com.matias.domain.port.EmailServicePort;
import com.matias.domain.port.TokenInvalidoRepositoryPort;
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
    private final VerificacionEmailService verificacionEmailService;
    private final PasswordResetService passwordResetService;
    private final TokenInvalidoRepositoryPort tokenInvalidoRepository;

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
                          EmailServicePort emailService,
                          VerificacionEmailService verificacionEmailService,
                          PasswordResetService passwordResetService,
                          TokenInvalidoRepositoryPort tokenInvalidoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.tokenVerificacionRepository = tokenVerificacionRepository;
        this.emailService = emailService;
        this.verificacionEmailService = verificacionEmailService;
        this.passwordResetService = passwordResetService;
        this.tokenInvalidoRepository = tokenInvalidoRepository;
    }

    @Override
    public RegistroResponse register(RegistroRequest request) {
        log.info("Iniciando registro de usuario con email: {}", request.email());
        
        // Verificar si el usuario ya existe
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new ConflictoException("El email ya estรก registrado");
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

        // Generar token de verificaciรณn
        String token = UUID.randomUUID().toString();
        Instant expiracion = Instant.now().plus(tokenExpirationHours, ChronoUnit.HOURS);
        
        TokenVerificacion tokenVerificacion = new TokenVerificacion(token, expiracion, usuarioGuardado.getId());
        tokenVerificacionRepository.save(tokenVerificacion);
        
        log.info("Token de verificaciรณn generado para usuario ID: {}", usuarioGuardado.getId());

        // Enviar email de verificaciรณn
        try {
            VerificationEmailTemplate emailTemplate = new VerificationEmailTemplate(
                    usuarioGuardado.getEmail(),
                    usuarioGuardado.getNombre(),
                    token,
                    frontUrl,
                    tokenExpirationHours + " horas"
            );
            emailService.send(emailTemplate);
            log.info("Email de verificaciรณn enviado a: {}", usuarioGuardado.getEmail());
        } catch (Exception e) {
            log.error("Error al enviar email de verificaciรณn: {}", e.getMessage(), e);
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
                .orElseThrow(() -> new NoAutenticadoException("Credenciales invรกlidas"));

        // Verificar contraseรฑa
        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new NoAutenticadoException("Credenciales invรกlidas");
        }

        // Verificar si el usuario estรก activo
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
            throw new NoAutenticadoException("Token invรกlido: no es un refresh token");
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
        
        // Validar que es un refresh token
        if (!tokenService.esRefreshToken(refreshToken)) {
            throw new NoAutenticadoException("Token inválido: no es un refresh token");
        }
        
        // Obtener email del token
        String email = tokenService.extractEmail(refreshToken);
        
        // Buscar usuario
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        
        // Invalidar el refresh token
        tokenService.invalidarToken(
            refreshToken, 
            com.matias.domain.model.MotivoInvalidacionToken.LOGOUT, 
            usuario.getId()
        );
        
        log.info("Logout procesado exitosamente para usuario: {}", email);
    }

    @Override
    public void verificarEmail(String token) {
        log.info("Verificando email con token");
        
        // Buscar token
        TokenVerificacion tokenVerificacion = tokenVerificacionRepository.findByToken(token)
                .orElseThrow(() -> new RecursoNoEncontradoException("Token de verificaciรณn no encontrado"));

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
            throw new OperacionNoPermitidaException("El token no es vรกlido");
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
    public void reenviarEmailVerificacion(ReenvioEmailRequest request, String ipOrigen) {
        log.info("Reenviando email de verificaciรณn para email: {} desde IP: {}", request.email(), ipOrigen);
        
        // Buscar usuario por email
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        // Validar que el usuario puede recibir un reenvรญo (lรณgica anti-abuso)
        verificacionEmailService.validarReenvio(usuario, ipOrigen);

        // Generar nuevo token de verificaciรณn
        String token = verificacionEmailService.generarTokenVerificacion(usuario);
        
        log.info("Nuevo token de verificaciรณn generado para usuario: {}", usuario.getEmail());

        // Enviar email de verificaciรณn
        try {
            VerificationEmailTemplate emailTemplate = new VerificationEmailTemplate(
                    usuario.getEmail(),
                    usuario.getNombre(),
                    token,
                    frontUrl,
                    tokenExpirationHours + " horas"
            );
            emailService.send(emailTemplate);
            log.info("Email de verificaciรณn reenviado a: {}", usuario.getEmail());
        } catch (Exception e) {
            log.error("Error al reenviar email de verificaciรณn: {}", e.getMessage(), e);
            throw new OperacionNoPermitidaException("No se pudo enviar el email de verificaciรณn. Intente mรกs tarde");
        }
    }

    @Override
    public void limpiarDatosObsoletos() {
        log.info("Iniciando limpieza de datos obsoletos");
        
        Instant ahora = Instant.now();
        
        // Limpieza de datos de verificación de email
        int eliminadosVerificacion = verificacionEmailService.limpiarDatosObsoletos();
        log.info("Limpieza de datos de verificación: {} registros eliminados", eliminadosVerificacion);
        
        // Limpieza de datos de reseteo de password
        int eliminadosReset = passwordResetService.limpiarDatosObsoletos();
        log.info("Limpieza de datos de reseteo: {} registros eliminados", eliminadosReset);
        
        // Limpieza de tokens invalidados expirados
        try {
            tokenInvalidoRepository.eliminarTokensExpirados(ahora);
            log.info("Limpieza de tokens invalidados expirados completada");
        } catch (Exception e) {
            log.error("Error al limpiar tokens invalidados: {}", e.getMessage(), e);
        }
        
        log.info("Limpieza de datos obsoletos completada");
    }

    @Override
    public void solicitarResetPassword(String email, String ipOrigen) {
        log.info("Solicitando reseteo de password para email: {} desde IP: {}", email, ipOrigen);
        
        // Validar la solicitud de reset (anti-abuso)
        passwordResetService.validarSolicitudReset(email, ipOrigen);
        
        // Buscar usuario por email
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        // Generar token de reset y enviar email
        passwordResetService.generarTokenReset(usuario);
        
        log.info("Solicitud de reseteo de password completada para email: {}", email);
    }
}
