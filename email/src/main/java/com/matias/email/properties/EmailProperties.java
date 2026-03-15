package com.matias.email.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propiedades de configuración para el servicio de email.
 * <p>
 * Estas propiedades se cargan desde application.properties con el prefijo "email".
 */
@Configuration
@ConfigurationProperties(prefix = "email")
@Getter
@Setter
public class EmailProperties {

    /**
     * Proveedor de email a utilizar (sendgrid, mailhog).
     */
    private String provider = "mailhog";

    /**
     * Configuración específica para SendGrid.
     */
    private SendGrid sendgrid = new SendGrid();

    /**
     * Configuración específica para MailHog (SMTP).
     */
    private Smtp smtp = new Smtp();

    @Getter
    @Setter
    public static class SendGrid {
        /**
         * API Key de SendGrid.
         */
        private String apiKey;

        /**
         * Email del remitente.
         */
        private String fromEmail;

        /**
         * Nombre del remitente.
         */
        private String fromName;
    }

    @Getter
    @Setter
    public static class Smtp {
        /**
         * Host del servidor SMTP.
         */
        private String host = "localhost";

        /**
         * Puerto del servidor SMTP.
         */
        private int port = 1025;

        /**
         * Usuario para autenticación SMTP.
         */
        private String username;

        /**
         * Contraseña para autenticación SMTP.
         */
        private String password;

        /**
         * Email del remitente.
         */
        private String fromEmail = "noreply@localhost";

        /**
         * Nombre del remitente.
         */
        private String fromName = "MatiasProject";

        /**
         * Habilitar autenticación SMTP.
         */
        private boolean auth = false;

        /**
         * Habilitar STARTTLS.
         */
        private boolean starttlsEnable = false;
    }
}
