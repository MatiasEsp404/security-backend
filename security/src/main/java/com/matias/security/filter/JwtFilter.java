package com.matias.security.filter;

import com.matias.domain.port.TokenServicePort;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final TokenServicePort tokenService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = obtenerToken(request);
        if (token != null) {
            try {
                if (tokenService.esRefreshToken(token)) {
                    SecurityContextHolder.clearContext();
                    log.debug("Se intentó usar un refresh token como access token. Acceso denegado.");
                    filterChain.doFilter(request, response);
                    return;
                }
                String email = tokenService.extractEmail(token);
                if (email != null && tokenService.esTokenValido(token, email)) {
                    UserDetails user = userDetailsService.loadUserByUsername(email);
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.debug("Usuario autenticado por JWT: {}", email);
                } else {
                    SecurityContextHolder.clearContext();
                    log.debug("Token inválido o expirado. Se continúa como anónimo.");
                }
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
                log.debug("No se pudo validar el token JWT: {}", e.getMessage());
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                log.debug("No se pudo cargar el usuario desde JWT: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }

    private String obtenerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7);
    }

}
