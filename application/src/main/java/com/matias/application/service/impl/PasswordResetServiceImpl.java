package com.matias.application.service.impl;

import com.matias.application.email.PasswordResetEmailTemplate;
import com.matias.application.service.PasswordResetService;
import com.matias.domain.exception.OperacionNoPermitidaException;
import com.matias.domain.exception.RecursoNoEncontradoException;
import com.matias.domain.model.*;
import com.matias.domain.port.EmailServicePort;
import com.matias.domain.port.PasswordResetIntentoRepositoryPort;
import com.matias.domain.port.TokenPasswordResetRepositoryPort;
import com.matias.domain.port.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    @Value("${security.password-reset.expiration:PT1H}")
    private Duration expiracionToken;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    private static final Duration TIEMPO_ENTRE_SOLICITUDES = Duration.ofMinutes(5);
    private static final int MAX_INTENTOS_DIARIOS = 5;

    private final TokenPasswordResetRepositoryPort tokenPasswordResetRepositoryPort;
    private final PasswordResetIntentoRepositoryPort intentoRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final EmailServicePort emailServicePort;

    @Override
    @Transactional
    public String generarTokenReset(Usuario usuario) {
        // Se invalidan tokens previos para garantizar que solo uno quede activo.
        invalidarTokensPendientes(usuario);

        String token = UUID.randomUUID().toString();
        Instant expiracion = Instant.now().plus(expiracionToken);

        TokenPasswordReset tokenEntity = new TokenPasswordReset(token, expiracion, usuario);
        tokenPasswordResetRepositoryPort.save(tokenEntity);

        // Enviar email con el token
        enviarEmailResetPassword(usuario, token);

        log.info("Token de reseteo de contraseña generado para usuario: {}", usuario.getEmail());
        return token;
    }

    @Override
    @Transactional
    public void resetearPassword(String token, String nuevaPassword) {

        // Validación del token: existencia, estado y expiración
        TokenPasswordReset tokenEntity = tokenPasswordResetRepositoryPort.findByToken(token)
                .orElseThrow(() -> new OperacionNoPermitidaException("Token de reseteo no encontrado"));

        // Validación del token: estado
        if (tokenEntity.getEstado() != EstadoTokenVerificacion.PENDIENTE) {
            String mensaje = switch (tokenEntity.getEstado()) {
                case USADO -> "El token ya ha sido utilizado";
                case EXPIRADO -> "El token ha expirado. Solicita un nuevo enlace de recuperación";
                default -> "El token no es válido";
            };
            throw new OperacionNoPermitidaException(mensaje);
        }

        // Validación del token: expiración
        if (tokenEntity.estaExpirado()) {
            tokenEntity.marcarComoExpirado();
            tokenPasswordResetRepositoryPort.save(tokenEntity);
            throw new OperacionNoPermitidaException(
                    "El token ha expirado. Solicita un nuevo enlace de recuperación");
        }

        // Si es válido, se marca como usado y se actualiza la contraseña.
        tokenEntity.marcarComoUsado();
        tokenPasswordResetRepositoryPort.save(tokenEntity);

        Usuario usuario = tokenEntity.getUsuario();
        String passwordCifrada = passwordEncoder.encode(nuevaPassword);
        usuario.setPassword(passwordCifrada);
        usuarioRepositoryPort.save(usuario);

        log.info("Contraseña restablecida exitosamente para usuario: {}", usuario.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public void validarToken(String token) {
        TokenPasswordReset tokenEntity = tokenPasswordResetRepositoryPort.findByToken(token)
                .orElseThrow(() -> new OperacionNoPermitidaException("Token de reseteo no encontrado"));

        if (tokenEntity.getEstado() != EstadoTokenVerificacion.PENDIENTE) {
            throw new OperacionNoPermitidaException("El token no es válido");
        }

        if (tokenEntity.estaExpirado()) {
            throw new OperacionNoPermitidaException("El token ha expirado");
        }
    }

    @Override
    @Transactional
    public void validarSolicitudReset(String email, String ipOrigen) {
        Usuario usuario = usuarioRepositoryPort.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        // Lógica anti-abuso: Tiempo mínimo entre solicitudes
        intentoRepositoryPort.findUltimoIntentoByUsuario(usuario).ifPresent(ultimoIntento -> {
            Instant proximaSolicitudPermitida = ultimoIntento.getFechaIntento().plus(TIEMPO_ENTRE_SOLICITUDES);
            if (Instant.now().isBefore(proximaSolicitudPermitida)) {
                long segundosRestantes = Duration.between(Instant.now(), proximaSolicitudPermitida).getSeconds();
                throw new OperacionNoPermitidaException(
                        String.format("Debes esperar %d segundos antes de solicitar otro reseteo", segundosRestantes));
            }
        });

        // Lógica anti-abuso: Máximo de solicitudes por día
        Instant inicioDelDia = Instant.now().minus(Duration.ofHours(24));
        long intentosHoy = intentoRepositoryPort.countByUsuarioAndFechaIntentoAfter(usuario, inicioDelDia);

        if (intentosHoy >= MAX_INTENTOS_DIARIOS) {
            throw new OperacionNoPermitidaException(
                    "Has excedido el límite de " + MAX_INTENTOS_DIARIOS + " solicitudes por día. Intenta mañana");
        }

        PasswordResetIntento intento = new PasswordResetIntento(usuario, ipOrigen);
        intentoRepositoryPort.save(intento);

        log.info("Validación de solicitud de reseteo exitosa para usuario: {}", usuario.getEmail());
    }

    private void invalidarTokensPendientes(Usuario usuario) {
        int actualizados = tokenPasswordResetRepositoryPort.updateEstadoByUsuarioAndEstado(
                usuario,
                EstadoTokenVerificacion.PENDIENTE,
                EstadoTokenVerificacion.EXPIRADO);
        if (actualizados > 0) {
            log.debug("Invalidados {} tokens de reseteo pendientes para usuario: {}", actualizados, usuario.getEmail());
        }
    }

    @Override
    @Transactional
    public int limpiarDatosObsoletos() {
        Instant ahora = Instant.now();

        // Limpia tokens expirados
        int tokensEliminados = tokenPasswordResetRepositoryPort.deleteByExpiracionBeforeAndEstado(ahora,
                EstadoTokenVerificacion.EXPIRADO);

        // Limpia registros de intentos antiguos
        Instant fechaLimiteIntentos = ahora.minus(Duration.ofDays(30));
        int intentosEliminados = intentoRepositoryPort.deleteByFechaIntentoBefore(fechaLimiteIntentos);

        log.info("Limpieza de datos de reseteo: {} tokens expirados, {} intentos antiguos eliminados", tokensEliminados,
                intentosEliminados);

        return tokensEliminados + intentosEliminados;
    }

    @Override
    public Duration getExpiracionToken() {
        return expiracionToken;
    }

    private void enviarEmailResetPassword(Usuario usuario, String token) {
        String expirationTime = formatearDuracion(expiracionToken);
        PasswordResetEmailTemplate emailTemplate = new PasswordResetEmailTemplate(
                usuario.getEmail(),
                usuario.getNombre(),
                token,
                frontendUrl,
                expirationTime
        );
        emailServicePort.send(emailTemplate);
        log.info("Email de reseteo de contraseña enviado a: {}", usuario.getEmail());
    }

    private String formatearDuracion(Duration duracion) {
        long horas = duracion.toHours();
        if (horas > 0) {
            return horas + (horas == 1 ? " hora" : " horas");
        }
        long minutos = duracion.toMinutes();
        return minutos + (minutos == 1 ? " minuto" : " minutos");
    }
}
