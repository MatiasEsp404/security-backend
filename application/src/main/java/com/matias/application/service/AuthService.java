package com.matias.application.service;

public interface AuthService {
    Object register(Object request);
    Object login(Object request);
    Object refresh(String refreshToken);
    void logout(String refreshToken);
    void verificarEmail(String token);
    void reenviarEmailVerificacion(Object request, String ipOrigen);
    void solicitarReseteoPassword(Object request, String ipOrigen);
    void validarTokenReset(String token);
    void resetearPassword(Object request);
}
