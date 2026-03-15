package com.matias.application.email;

import com.matias.domain.model.email.ContentTemplate;
import com.matias.domain.model.email.EmailTemplate;

/**
 * Plantilla de email de bienvenida.
 * <p>
 * Se envía cuando un usuario se registra exitosamente en la aplicación.
 */
public record WelcomeEmailTemplate(String toEmail, String userName) implements EmailTemplate, ContentTemplate {

    @Override
    public String getSubject() {
        return "¡Bienvenido a nuestra aplicación!";
    }

    @Override
    public String getTo() {
        return toEmail;
    }

    @Override
    public ContentTemplate getContent() {
        return this;
    }

    @Override
    public String getContentType() {
        return "text/html";
    }

    @Override
    public String getBody() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background-color: #f9f9f9; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>¡Bienvenido!</h1>
                        </div>
                        <div class="content">
                            <p>Hola <strong>%s</strong>,</p>
                            <p>¡Gracias por registrarte en nuestra aplicación!</p>
                            <p>Estamos emocionados de tenerte con nosotros.</p>
                            <p>Si tienes alguna pregunta, no dudes en contactarnos.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2025 MatiasProject. Todos los derechos reservados.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName);
    }
}
