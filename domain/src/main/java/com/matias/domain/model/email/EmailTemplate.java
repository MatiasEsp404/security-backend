package com.matias.domain.model.email;

/**
 * Plantilla básica de un email.
 * <p>
 * Define el contrato para cualquier tipo de email que se envíe
 * en el sistema.
 */
public interface EmailTemplate {

    /**
     * Asunto del email.
     *
     * @return asunto
     */
    String getSubject();

    /**
     * Destinatario del email.
     *
     * @return dirección del destinatario
     */
    String getTo();

    /**
     * Contenido del email.
     *
     * @return contenido asociado
     */
    ContentTemplate getContent();
}
