package com.matias.domain.port;

import com.matias.domain.model.Usuario;

/**
 * Puerto (interfaz) para abstraer el acceso al usuario autenticado actual.
 * <p>
 * Este puerto centraliza la obtención de información del usuario autenticado,
 * desacoplando la capa de aplicación de los detalles de implementación de Spring Security.
 * </p>
 * <p>
 * Beneficios:
 * <ul>
 *   <li>Desacoplamiento: Los servicios no conocen Spring Security</li>
 *   <li>Testabilidad: Fácil de mockear en tests</li>
 *   <li>Consistencia: Un solo punto de acceso al usuario autenticado</li>
 *   <li>Mantenibilidad: Cambios en autenticación centralizados</li>
 * </ul>
 * </p>
 *
 * @see Usuario
 */
public interface AuthenticationFacadePort {

    /**
     * Obtiene el usuario completo actualmente autenticado.
     *
     * @return el usuario autenticado del dominio
     * @throws com.matias.domain.exception.NoAutenticadoException si no hay usuario autenticado
     */
    Usuario getUsuarioAutenticado();

    /**
     * Obtiene el email del usuario actualmente autenticado.
     * <p>
     * Método de conveniencia que evita obtener el usuario completo
     * cuando solo se necesita el email.
     * </p>
     *
     * @return el email del usuario autenticado
     * @throws com.matias.domain.exception.NoAutenticadoException si no hay usuario autenticado
     */
    String getEmailUsuarioAutenticado();

    /**
     * Obtiene el ID del usuario actualmente autenticado.
     * <p>
     * Método de conveniencia que evita obtener el usuario completo
     * cuando solo se necesita el ID.
     * </p>
     *
     * @return el ID del usuario autenticado
     * @throws com.matias.domain.exception.NoAutenticadoException si no hay usuario autenticado
     */
    Integer getIdUsuarioAutenticado();
}
