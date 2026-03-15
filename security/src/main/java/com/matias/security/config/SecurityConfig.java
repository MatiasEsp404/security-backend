package com.matias.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matias.domain.model.Rol;
import com.matias.security.csrf.SpaCsrfTokenRequestHandler;
import com.matias.security.filter.JwtFilter;
import com.matias.security.ratelimit.filter.RateLimitFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${app.front-url:http://localhost:3000}")
    private String frontUrl;

    @Value("${app.back-url:http://localhost:8081}")
    private String backUrl;

    @Value("${app.csrf.enabled:false}")
    private boolean csrfEnabled;

    private final JwtFilter jwtFilter;
    private final UserDetailsService userDetailsService;

    @Autowired(required = false)
    private RateLimitFilter rateLimitFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(this::configureCsrf)
                .sessionManagement(this::configureSessionManagement)
                .authorizeHttpRequests(this::configureAuthorization)
                .exceptionHandling(this::configureExceptionHandling);

        // Agregar Rate Limit Filter si está habilitado
        if (rateLimitFilter != null) {
            http.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);
            log.info("RateLimitFilter agregado a la cadena de seguridad");
        }

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (req, res, authEx) -> {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json");
            Map<String, Object> errorResponse = Map.of(
                    "mensaje", "No autenticado",
                    "errores", List.of("Autenticación requerida"));
            writeJson(res, errorResponse);
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (req, res, accEx) -> {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.setContentType("application/json");
            Map<String, Object> errorResponse = Map.of(
                    "mensaje", "Acceso denegado",
                    "errores", List.of("Permisos insuficientes"));
            writeJson(res, errorResponse);
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(backUrl, frontUrl));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-XSRF-TOKEN"));
        configuration.setExposedHeaders(List.of("X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private void configureAuthorization(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
                // Endpoints públicos
                .requestMatchers(
                        "/swagger/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/actuator/**",
                        "/v1/auth/register",
                        "/v1/auth/verify",
                        "/v1/auth/login",
                        "/v1/auth/resend-verification",
                        "/v1/auth/password-reset/request",
                        "/v1/auth/password-reset/validate",
                        "/v1/auth/password-reset/confirm",
                        "/v1/auth/refresh")
                .permitAll()

                // Endpoints de administración - solo ADMINISTRADOR
                .requestMatchers("/v1/admin/**")
                .hasRole(Rol.ADMINISTRADOR.name())

                // Endpoints de autenticación - usuarios autenticados
                .requestMatchers("/v1/auth/**")
                .hasAnyRole(Rol.USUARIO.name(), Rol.MODERADOR.name(), Rol.ADMINISTRADOR.name())

                // Endpoints de usuario - usuarios autenticados
                .requestMatchers("/v1/usuario/me")
                .hasAnyRole(Rol.USUARIO.name(), Rol.MODERADOR.name(), Rol.ADMINISTRADOR.name())

                // Cualquier otro endpoint requiere autenticación
                .anyRequest().authenticated();
    }

    private void configureCsrf(CsrfConfigurer<HttpSecurity> csrf) {
        if (csrfEnabled) {
            csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                    .ignoringRequestMatchers(
                            "/v1/auth/register",
                            "/v1/auth/login",
                            "/v1/auth/verify",
                            "/v1/auth/resend-verification",
                            "/v1/auth/password-reset/**",
                            "/swagger/**",
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/actuator/**");
            log.info("CSRF activado.");
        } else {
            csrf.disable();
            log.warn("CSRF desactivado.");
        }
    }

    private void configureExceptionHandling(ExceptionHandlingConfigurer<HttpSecurity> ex) {
        ex.authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler());
    }

    private void configureSessionManagement(SessionManagementConfigurer<HttpSecurity> session) {
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    private void writeJson(HttpServletResponse res, Object obj) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        res.getWriter().write(mapper.writeValueAsString(obj));
    }
}
