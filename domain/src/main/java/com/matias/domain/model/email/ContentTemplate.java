package com.matias.domain.model.email;

/**
 * Contenido de un email.
 * <p>
 * Define el cuerpo del mensaje y su tipo MIME.
 */
public interface ContentTemplate {

    /**
     * Cuerpo del mensaje.
     *
     * @return contenido del cuerpo
     */
    String getBody();

    /**
     * Tipo MIME del contenido.
     *
     * @return tipo de contenido (ej: "text/html", "text/plain")
     */
    String getContentType();
}
