package com.matias.domain.port;

import com.matias.domain.model.EmailVerificacionIntento;

import java.time.Instant;
import java.util.Optional;

public interface EmailVerificacionIntentoRepositoryPort {
    EmailVerificacionIntento save(EmailVerificacionIntento intento);
    Optional<EmailVerificacionIntento> findUltimoIntentoByUsuarioId(Integer usuarioId);
    long countByUsuarioIdAndFechaIntentoAfter(Integer usuarioId, Instant fechaLimite);
    int deleteByFechaIntentoBefore(Instant fechaLimite);
}
