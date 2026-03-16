package com.matias.application.service;

import com.matias.application.dto.request.LogueoRequest;
import com.matias.application.dto.request.ReenvioEmailRequest;
import com.matias.application.dto.request.RegistroRequest;
import com.matias.application.dto.response.RegistroResponse;
import com.matias.application.dto.internal.TokenInternal;

public interface AuthService {
    RegistroResponse register(RegistroRequest request);
    TokenInternal login(LogueoRequest request);
    TokenInternal refresh(String refreshToken);
    void logout(String refreshToken);
    void verificarEmail(String token);
    void reenviarEmailVerificacion(ReenvioEmailRequest request, String ipOrigen);
    void solicitarResetPassword(String email, String ipOrigen);
}
