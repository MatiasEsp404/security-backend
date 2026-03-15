package com.matias.email.adapter;

import com.matias.domain.exception.ServicioExternoException;
import com.matias.domain.model.email.EmailTemplate;
import com.matias.domain.port.EmailServicePort;
import com.matias.email.properties.EmailProperties;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Adaptador para enviar emails usando SendGrid.
 * <p>
 * Se activa cuando la propiedad email.provider=sendgrid.
 */
@Component
@ConditionalOnProperty(name = "email.provider", havingValue = "sendgrid")
@RequiredArgsConstructor
@Slf4j
public class SendGridEmailAdapter implements EmailServicePort {

    private final EmailProperties emailProperties;

    @Override
    public void send(EmailTemplate email) {
        try {
            log.info("Enviando email a {} con SendGrid", email.getTo());

            SendGrid sg = new SendGrid(emailProperties.getSendgrid().getApiKey());

            Email from = new Email(
                    emailProperties.getSendgrid().getFromEmail(),
                    emailProperties.getSendgrid().getFromName()
            );
            Email to = new Email(email.getTo());
            Content content = new Content(
                    email.getContent().getContentType(),
                    email.getContent().getBody()
            );

            Mail mail = new Mail(from, email.getSubject(), to, content);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Email enviado exitosamente a {} vía SendGrid", email.getTo());
            } else {
                log.error("Error al enviar email vía SendGrid. Status: {}, Body: {}",
                        response.getStatusCode(), response.getBody());
                throw new ServicioExternoException(
                        "Error al enviar email vía SendGrid: " + response.getBody()
                );
            }

        } catch (IOException e) {
            log.error("Error de comunicación con SendGrid al enviar email a {}", email.getTo(), e);
            throw new ServicioExternoException("Error al comunicarse con SendGrid: " + e.getMessage());
        }
    }
}
