package com.matias.security.adapter;

import com.matias.domain.port.PasswordHashingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adaptador que implementa el puerto de hashing de contraseñas
 * utilizando Spring Security PasswordEncoder.
 * 
 * Esta implementación encapsula los detalles de Spring Security,
 * manteniendo la capa de aplicación independiente de frameworks específicos.
 */
@Component
@RequiredArgsConstructor
public class SpringSecurityPasswordHashingAdapter implements PasswordHashingPort {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
