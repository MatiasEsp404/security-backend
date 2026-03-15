package com.matias.domain.port;

import com.matias.domain.model.email.EmailTemplate;

/**
 * Puerto para el servicio de envío de emails.
 * <p>
 * Este port define el contrato para enviar emails utilizando diferentes
 * proveedores (SendGrid, MailHog, etc.) sin que el dominio conozca
 * los detalles de implementación.
 */
public interface EmailServicePort {

    /**
     * Envía un email basado en la plantilla proporcionada.
     *
     * @param email plantilla del email a enviar
     */
    void send(EmailTemplate email);
}
