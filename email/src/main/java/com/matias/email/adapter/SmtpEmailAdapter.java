package com.matias.email.adapter;

import com.matias.domain.exception.ServicioExternoException;
import com.matias.domain.model.email.EmailTemplate;
import com.matias.domain.port.EmailServicePort;
import com.matias.email.properties.EmailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * Adaptador para enviar emails usando SMTP (MailHog para desarrollo).
 * <p>
 * Se activa cuando la propiedad email.provider=mailhog.
 * Utiliza JavaMailSender de Spring para enviar emails vía SMTP.
 */
@Component
@ConditionalOnProperty(name = "email.provider", havingValue = "mailhog", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailAdapter implements EmailServicePort {

    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;

    @Override
    public void send(EmailTemplate email) {
        try {
            log.info("Enviando email a {} vía SMTP", email.getTo());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(
                    emailProperties.getSmtp().getFromEmail(),
                    emailProperties.getSmtp().getFromName()
            );
            helper.setTo(email.getTo());
            helper.setSubject(email.getSubject());

            boolean isHtml = "text/html".equalsIgnoreCase(email.getContent().getContentType());
            helper.setText(email.getContent().getBody(), isHtml);

            mailSender.send(message);

            log.info("Email enviado exitosamente a {} vía SMTP", email.getTo());

        } catch (MessagingException e) {
            log.error("Error al crear el mensaje de email para {}", email.getTo(), e);
            throw new ServicioExternoException("Error al crear el mensaje de email: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error al enviar email vía SMTP a {}", email.getTo(), e);
            throw new ServicioExternoException("Error al enviar email vía SMTP: " + e.getMessage());
        }
    }
}
