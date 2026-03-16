package com.matias.database.adapter;

import com.matias.database.entity.EmailVerificacionIntentoEntity;
import com.matias.database.entity.UsuarioEntity;
import com.matias.database.mapper.EmailVerificacionIntentoMapper;
import com.matias.database.repository.EmailVerificacionIntentoJpaRepository;
import com.matias.database.repository.UsuarioJpaRepository;
import com.matias.domain.model.EmailVerificacionIntento;
import com.matias.domain.port.EmailVerificacionIntentoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmailVerificacionIntentoRepositoryAdapter implements EmailVerificacionIntentoRepositoryPort {

    private final EmailVerificacionIntentoJpaRepository jpaRepository;
    private final UsuarioJpaRepository usuarioJpaRepository;
    private final EmailVerificacionIntentoMapper mapper;

    @Override
    public EmailVerificacionIntento save(EmailVerificacionIntento intento) {
        UsuarioEntity usuario = usuarioJpaRepository.findById(intento.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        EmailVerificacionIntentoEntity entity = mapper.toEntity(intento);
        entity.setUsuario(usuario);
        
        EmailVerificacionIntentoEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<EmailVerificacionIntento> findUltimoIntentoByUsuarioId(Integer usuarioId) {
        return jpaRepository.findUltimoIntentoByUsuarioId(usuarioId)
                .map(mapper::toDomain);
    }

    @Override
    public long countByUsuarioIdAndFechaIntentoAfter(Integer usuarioId, Instant fechaLimite) {
        return jpaRepository.countByUsuarioIdAndFechaIntentoAfter(usuarioId, fechaLimite);
    }

    @Override
    public int deleteByFechaIntentoBefore(Instant fechaLimite) {
        return jpaRepository.deleteByFechaIntentoBefore(fechaLimite);
    }
}
