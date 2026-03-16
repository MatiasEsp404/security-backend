package com.matias.application.service.impl;

import com.matias.application.service.VerificacionEmailService;
import com.matias.domain.exception.ConflictoException;
import com.matias.domain.exception.OperacionNoPermitidaException;
import com.matias.domain.model.EmailVerificacionIntento;
import com.matias.domain.model.EstadoTokenVerificacion;
import com.matias.domain.model.TokenVerificacion;
import com.matias.domain.model.Usuario;
import com.matias.domain.port.EmailVerificacionIntentoRepositoryPort;
import com.matias.domain.port.TokenVerificacionRepositoryPort;
import com.matias.domain.port.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificacionEmailServiceImpl implements VerificacionEmailService {

    private final TokenVerificacionRepositoryPort tokenVerificacionRepository;
    private final EmailVerificacionIntentoRepositoryPort intentoRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    @Value("${app.verification.token.expiration-hours:24}")
    private int tokenExpirationHours;

    private static final Duration TIEMPO_ENTRE_REENVIOS = Duration.ofMinutes(2);
    private static final int MAX_INTENTOS_DIARIOS = 3;

    @Override
    @Transactional
    public String generarTokenVerificacion(Usuario usuario) {
        invalidarTokensPendientes(usuario.getId());

        String token = UUID.randomUUID().toString();
        Instant expiracion = Instant.now().plus(tokenExpirationHours, ChronoUnit.HOURS);

        TokenVerificacion tokenVerificacion = new TokenVerificacion(token, expiracion, usuario.getId());
        tokenVerificacionRepository.save(tokenVerificacion);

        log.info("Token de verificación generado para usuario: {}", usuario.getEmail());
        return token;
    }

    @Override
    @Transactional
    public Usuario verificarEmail(String token) {
        TokenVerificacion tokenVerificacion = tokenVerificacionRepository.findByToken(token)
                .orElseThrow(() -> new OperacionNoPermitidaException("Token de verificación no encontrado"));

        Usuario usuario = usuarioRepository.findById(tokenVerificacion.getUsuarioId())
                .orElseThrow(() -> new OperacionNoPermitidaException("Usuario no encontrado"));

        // Validación del usuario: ya verificado
        if (Boolean.TRUE.equals(usuario.getEmailVerificado())) {
            throw new OperacionNoPermitidaException("El email ya fue verificado previamente");
        }

        // Validación del token: Debe estar en estado PENDIENTE
        if (tokenVerificacion.getEstado() != EstadoTokenVerificacion.PENDIENTE) {
            String mensaje = switch (tokenVerificacion.getEstado()) {
                case USADO -> "El token ya ha sido utilizado";
                case EXPIRADO -> "El token ha expirado. Solicita un nuevo email de verificación";
                default -> "El token no es válido";
            };
            throw new OperacionNoPermitidaException(mensaje);
        }

        // Validación del token: No debe estar expirado
        if (tokenVerificacion.estaExpirado()) {
            tokenVerificacion.marcarComoExpirado();
            tokenVerificacionRepository.save(tokenVerificacion);
            throw new OperacionNoPermitidaException(
                    "El token ha expirado. Solicita un nuevo email de verificación");
        }

        // Si es válido, se marca como usado
        tokenVerificacion.marcarComoUsado();
        tokenVerificacionRepository.save(tokenVerificacion);

        // Verifica el email del usuario
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        log.info("Email verificado exitosamente para usuario: {}", usuario.getEmail());
        return usuario;
    }

    @Override
    @Transactional
    public void validarReenvio(Usuario usuario, String ipOrigen) {
        if (Boolean.TRUE.equals(usuario.getEmailVerificado())) {
            throw new ConflictoException("El email ya está verificado");
        }

        // Lógica anti-abuso: Retardo mínimo entre reenvíos
        intentoRepository.findUltimoIntentoByUsuarioId(usuario.getId()).ifPresent(ultimoIntento -> {
            Instant proximoReenvioPermitido = ultimoIntento.getFechaIntento().plus(TIEMPO_ENTRE_REENVIOS);
            if (Instant.now().isBefore(proximoReenvioPermitido)) {
                long segundosRestantes = Duration.between(Instant.now(), proximoReenvioPermitido).getSeconds();
                throw new OperacionNoPermitidaException(
                        String.format("Debes esperar %d segundos antes de solicitar un nuevo email", segundosRestantes));
            }
        });

        // Lógica anti-abuso: Límite diario de reenvíos
        Instant inicioDelDia = Instant.now().minus(Duration.ofHours(24));
        long intentosHoy = intentoRepository.countByUsuarioIdAndFechaIntentoAfter(usuario.getId(), inicioDelDia);
        if (intentosHoy >= MAX_INTENTOS_DIARIOS) {
            throw new OperacionNoPermitidaException(
                    "Has excedido el límite de " + MAX_INTENTOS_DIARIOS + " reenvíos por día. Intenta mañana");
        }

        EmailVerificacionIntento intento = new EmailVerificacionIntento(usuario.getId(), ipOrigen);
        intentoRepository.save(intento);

        log.info("Validación de reenvío exitosa para usuario: {}", usuario.getEmail());
    }

    @Override
    @Transactional
    public int limpiarDatosObsoletos() {
        Instant ahora = Instant.now();

        // Limpia tokens expirados
        int tokensEliminados = tokenVerificacionRepository.deleteByExpiracionBeforeAndEstado(
                ahora, EstadoTokenVerificacion.EXPIRADO);

        // Limpia registros de intentos antiguos
        Instant fechaLimiteIntentos = ahora.minus(Duration.ofDays(30));
        int intentosEliminados = intentoRepository.deleteByFechaIntentoBefore(fechaLimiteIntentos);

        log.info("Limpieza de datos de verificación: {} tokens expirados, {} intentos antiguos eliminados",
                tokensEliminados, intentosEliminados);

        return tokensEliminados + intentosEliminados;
    }

    @Override
    public Duration getExpiracionToken() {
        return Duration.ofHours(tokenExpirationHours);
    }

    private void invalidarTokensPendientes(Integer usuarioId) {
        int actualizados = tokenVerificacionRepository.updateEstadoByUsuarioIdAndEstado(
                usuarioId,
                EstadoTokenVerificacion.PENDIENTE,
                EstadoTokenVerificacion.EXPIRADO);
        if (actualizados > 0) {
            log.debug("Invalidados {} tokens pendientes para usuario ID: {}", actualizados, usuarioId);
        }
    }
}
