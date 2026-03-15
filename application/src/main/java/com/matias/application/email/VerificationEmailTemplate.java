package com.matias.application.email;

import com.matias.domain.model.email.ContentTemplate;
import com.matias.domain.model.email.EmailTemplate;

/**
 * Plantilla de email de verificación de cuenta.
 * <p>
 * Se envía cuando un usuario necesita verificar su email.
 */
public record VerificationEmailTemplate(String toEmail, String userName, String token, String frontendUrl,
                                        String expirationTime) implements EmailTemplate, ContentTemplate {

    @Override
    public String getSubject() {
        return "Verifica tu cuenta";
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
        String verificationUrl = frontendUrl + "/verify-email?token=" + token;

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background-color: #f9f9f9; }
                        .button { display: inline-block; padding: 12px 24px; background-color: #2196F3;
                                 color: white; text-decoration: none; border-radius: 4px; margin: 20px 0; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Verifica tu cuenta</h1>
                        </div>
                        <div class="content">
                            <p>Hola <strong>%s</strong>,</p>
                            <p>Por favor, verifica tu dirección de email haciendo clic en el siguiente botón:</p>
                            <center>
                                <a href="%s" class="button">Verificar mi cuenta</a>
                            </center>
                            <p>O copia y pega este enlace en tu navegador:</p>
                            <p style="word-break: break-all;">%s</p>
                            <p>Este enlace expirará en %s.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2025 MatiasProject. Todos los derechos reservados.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName, verificationUrl, verificationUrl, expirationTime);
    }
}
