package com.matias.web.mapper;

import com.matias.application.dto.request.EmailRequest;
import com.matias.application.dto.request.LogueoRequest;
import com.matias.application.dto.request.RegistroRequest;
import com.matias.application.dto.response.RegistroResponse;
import com.matias.application.dto.response.TokenResponse;
import com.matias.web.dto.request.EmailWebRequest;
import com.matias.web.dto.request.LogueoWebRequest;
import com.matias.web.dto.request.RegistroWebRequest;
import com.matias.web.dto.response.RegistroWebResponse;
import com.matias.web.dto.response.TokenWebResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper entre DTOs de la capa web y la capa de aplicación para autenticación.
 * Responsable de la conversión bidireccional entre contratos web y contratos de casos de uso.
 */
@Component
public class AuthWebMapper {

    /**
     * Convierte un RegistroWebRequest (web) a RegistroRequest (application)
     */
    public RegistroRequest toRegistroRequest(RegistroWebRequest webRequest) {
        return new RegistroRequest(
                webRequest.email(),
                webRequest.password(),
                webRequest.nombre(),
                webRequest.apellido()
        );
    }

    /**
     * Convierte un LogueoWebRequest (web) a LogueoRequest (application)
     */
    public LogueoRequest toLogueoRequest(LogueoWebRequest webRequest) {
        return new LogueoRequest(
                webRequest.email(),
                webRequest.password()
        );
    }

    /**
     * Convierte un EmailWebRequest (web) a EmailRequest (application)
     */
    public EmailRequest toEmailRequest(EmailWebRequest webRequest) {
        return new EmailRequest(webRequest.email());
    }

    /**
     * Convierte un RegistroResponse (application) a RegistroWebResponse (web)
     */
    public RegistroWebResponse toRegistroWebResponse(RegistroResponse appResponse) {
        return new RegistroWebResponse(
                appResponse.id(),
                appResponse.email()
        );
    }

    /**
     * Convierte un TokenResponse (application) a TokenWebResponse (web)
     */
    public TokenWebResponse toTokenWebResponse(TokenResponse appResponse) {
        return new TokenWebResponse(appResponse.accessToken());
    }
}
