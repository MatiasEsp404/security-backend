package com.matias.domain.port;

/**
 * Puerto para operaciones de hashing de contraseñas.
 * Abstrae el algoritmo de cifrado de contraseñas del núcleo de la aplicación.
 */
public interface PasswordHashingPort {
    
    /**
     * Codifica una contraseña en texto plano.
     * 
     * @param rawPassword Contraseña en texto plano
     * @return Contraseña codificada/hasheada
     */
    String encode(String rawPassword);
    
    /**
     * Verifica si una contraseña en texto plano coincide con una contraseña hasheada.
     * 
     * @param rawPassword Contraseña en texto plano a verificar
     * @param encodedPassword Contraseña hasheada almacenada
     * @return true si la contraseña coincide, false en caso contrario
     */
    boolean matches(String rawPassword, String encodedPassword);
}
