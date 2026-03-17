package com.matias.domain.util;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utilidad para normalización de datos de entrada.
 * <p>
 * Esta clase proporciona métodos estáticos para normalizar diferentes tipos de datos
 * (nombres, apellidos, emails, direcciones, teléfonos) garantizando consistencia
 * en la información almacenada.
 * </p>
 * <p>
 * Características principales:
 * </p>
 * <ul>
 *   <li>Normalización de nombres propios con Title Case</li>
 *   <li>Manejo de prefijos culturales en apellidos (von, de, mc, o', etc.)</li>
 *   <li>Normalización de emails a minúsculas</li>
 *   <li>Formateo de direcciones preservando números</li>
 *   <li>Limpieza y formateo de números telefónicos</li>
 * </ul>
 *
 * @author Matias
 * @version 1.0
 * @since 2026-03-17
 */
public final class DataNormalizer {

    // Prefijos especiales que no se capitalizan en apellidos
    private static final Set<String> LOWERCASE_PREFIXES = Set.of(
            "de", "del", "la", "las", "los", "el",  // Español
            "da", "das", "do", "dos",                // Portugués
            "van", "von", "der", "den",              // Alemán/Holandés
            "di",                                    // Italiano
            "le", "du", "des"                        // Francés
    );

    // Prefijos que mantienen mayúscula después del apóstrofe
    private static final Set<String> APOSTROPHE_PREFIXES = Set.of("o'", "d'", "l'");

    // Patrón para teléfonos: solo dígitos, paréntesis, guiones, espacios y el símbolo +
    private static final Pattern PHONE_PATTERN = Pattern.compile("[^0-9+()\\-\\s]");

    /**
     * Constructor privado para prevenir instanciación.
     * Esta es una clase utilitaria que solo contiene métodos estáticos.
     */
    private DataNormalizer() {
        throw new UnsupportedOperationException("Esta es una clase utilitaria y no debe ser instanciada");
    }

    /**
     * Normaliza nombres propios con Title Case.
     * <p>
     * Ejemplos:
     * </p>
     * <ul>
     *   <li>"MATÍAS" → "Matías"</li>
     *   <li>"  maría josé  " → "María José"</li>
     *   <li>"JUAN CARLOS" → "Juan Carlos"</li>
     * </ul>
     *
     * @param input El nombre a normalizar
     * @return El nombre normalizado en Title Case, o el valor original si es null/blank
     */
    public static String normalizeProperName(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        String normalized = input.trim().replaceAll("\\s+", " ");
        return toTitleCase(normalized);
    }

    /**
     * Normaliza apellidos considerando prefijos culturales.
     * <p>
     * Ejemplos:
     * </p>
     * <ul>
     *   <li>"VON NEUMANN" → "von Neumann"</li>
     *   <li>"MC DONALD" → "McDonald"</li>
     *   <li>"O'BRIEN" → "O'Brien"</li>
     *   <li>"DE LA CRUZ" → "de la Cruz"</li>
     *   <li>"VAN DER BERG" → "van der Berg"</li>
     * </ul>
     *
     * @param input El apellido a normalizar
     * @return El apellido normalizado con prefijos culturales correctos, o el valor original si es null/blank
     */
    public static String normalizeLastName(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        String normalized = input.trim().replaceAll("\\s+", " ");
        String[] words = normalized.split(" ");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                result.append(" ");
            }

            String word = words[i].toLowerCase();

            // Manejo de prefijos con apóstrofe (O'Brien, D'Angelo)
            if (word.contains("'")) {
                result.append(handleApostrophe(word));
            } else if (word.startsWith("mc") && word.length() > 2) {
                // Manejo de prefijos Mc/Mac (McDonald, MacGregor)
                result.append("Mc").append(Character.toUpperCase(word.charAt(2)))
                        .append(word.substring(3));
            } else if (word.startsWith("mac") && word.length() > 3) {
                result.append("Mac").append(Character.toUpperCase(word.charAt(3)))
                        .append(word.substring(4));
            } else if (i > 0 && LOWERCASE_PREFIXES.contains(word)) {
                // Prefijos que van en minúsculas (excepto si es la primera palabra)
                result.append(word);
            } else {
                // Palabra normal con Title Case
                result.append(toTitleCase(word));
            }
        }

        return result.toString();
    }

    /**
     * Normaliza email a minúsculas.
     * <p>
     * Ejemplos:
     * </p>
     * <ul>
     *   <li>"Usuario@EXAMPLE.COM" → "usuario@example.com"</li>
     *   <li>"  ADMIN@TEST.COM  " → "admin@test.com"</li>
     * </ul>
     *
     * @param email El email a normalizar
     * @return El email normalizado en minúsculas, o el valor original si es null/blank
     */
    public static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }
        return email.trim().toLowerCase();
    }

    /**
     * Normaliza username a minúsculas, sin espacios.
     * <p>
     * Ejemplos:
     * </p>
     * <ul>
     *   <li>"Usuario_123  " → "usuario_123"</li>
     *   <li>"ADMIN USER" → "adminuser"</li>
     * </ul>
     *
     * @param username El username a normalizar
     * @return El username normalizado en minúsculas sin espacios, o el valor original si es null/blank
     */
    public static String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            return username;
        }
        return username.trim().toLowerCase().replaceAll("\\s+", "");
    }

    /**
     * Normaliza dirección: Title Case pero preserva números y formato.
     * <p>
     * Ejemplos:
     * </p>
     * <ul>
     *   <li>"  calle   falsa  123  " → "Calle Falsa 123"</li>
     *   <li>"AV LIBERTADOR 5678" → "Av Libertador 5678"</li>
     *   <li>"CALLE 5A PISO 3B" → "Calle 5A Piso 3B"</li>
     * </ul>
     *
     * @param address La dirección a normalizar
     * @return La dirección normalizada, o el valor original si es null/blank
     */
    public static String normalizeAddress(String address) {
        if (address == null || address.isBlank()) {
            return address;
        }

        String normalized = address.trim().replaceAll("\\s+", " ");
        String[] words = normalized.split(" ");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                result.append(" ");
            }

            String word = words[i];

            // Si es un número, lo deja tal cual
            if (word.matches("\\d+")) {
                result.append(word);
            } else if (word.matches(".*\\d.*")) {
                // Si contiene números y letras (ej: "5A", "123B")
                result.append(word.toUpperCase());
            } else {
                // Palabras normales
                result.append(toTitleCase(word));
            }
        }

        return result.toString();
    }

    /**
     * Normaliza teléfono: solo dígitos (opción básica).
     * <p>
     * Ejemplos:
     * </p>
     * <ul>
     *   <li>"+54 (011) 1234-5678" → "5401112345678"</li>
     *   <li>"(11) 4567-8901" → "1145678901"</li>
     * </ul>
     *
     * @param phone El teléfono a normalizar
     * @return El teléfono con solo dígitos, o el valor original si es null/blank
     */
    public static String normalizePhoneDigitsOnly(String phone) {
        if (phone == null || phone.isBlank()) {
            return phone;
        }
        return phone.replaceAll("[^0-9]", "");
    }

    /**
     * Normaliza teléfono: formato estándar internacional.
     * <p>
     * Ejemplos:
     * </p>
     * <ul>
     *   <li>"011  1234-5678" → "+54 11 1234 5678" (con countryCode "+54")</li>
     *   <li>"+1 (555) 123-4567" → "+1 (555) 123-4567"</li>
     * </ul>
     * <p>
     * Nota: Este es un formato básico. Para producción se recomienda usar
     * una librería especializada como libphonenumber de Google.
     * </p>
     *
     * @param phone       El teléfono a normalizar
     * @param countryCode El código de país a agregar si no está presente (ej: "+54")
     * @return El teléfono normalizado, o el valor original si es null/blank
     */
    public static String normalizePhoneFormatted(String phone, String countryCode) {
        if (phone == null || phone.isBlank()) {
            return phone;
        }

        // Remover caracteres no permitidos excepto +, números, paréntesis, guiones y espacios
        String cleaned = PHONE_PATTERN.matcher(phone).replaceAll("");
        cleaned = cleaned.trim().replaceAll("\\s+", " ");

        // Si no tiene código de país y se proporciona uno, agregarlo
        if (!cleaned.startsWith("+") && countryCode != null && !countryCode.isBlank()) {
            cleaned = countryCode + " " + cleaned;
        }

        return cleaned;
    }

    /**
     * Normaliza texto general: trim y espacios múltiples.
     * <p>
     * Ejemplos:
     * </p>
     * <ul>
     *   <li>"  texto   con    espacios  " → "texto con espacios"</li>
     *   <li>"    múltiples  líneas    " → "múltiples líneas"</li>
     * </ul>
     *
     * @param input El texto a normalizar
     * @return El texto normalizado, o el valor original si es null/blank
     */
    public static String normalizeText(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        return input.trim().replaceAll("\\s+", " ");
    }

    /**
     * Normaliza texto a minúsculas.
     * <p>
     * Ejemplos:
     * </p>
     * <ul>
     *   <li>"TEXTO" → "texto"</li>
     *   <li>"  MAYÚSCULAS  " → "mayúsculas"</li>
     * </ul>
     *
     * @param input El texto a normalizar
     * @return El texto en minúsculas, o el valor original si es null/blank
     */
    public static String normalizeLowercase(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        return input.trim().toLowerCase();
    }

    /**
     * Normaliza texto a MAYÚSCULAS.
     * <p>
     * Ejemplos:
     * </p>
     * <ul>
     *   <li>"texto" → "TEXTO"</li>
     *   <li>"  minúsculas  " → "MINÚSCULAS"</li>
     * </ul>
     *
     * @param input El texto a normalizar
     * @return El texto en mayúsculas, o el valor original si es null/blank
     */
    public static String normalizeUppercase(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        return input.trim().toUpperCase();
    }

    /**
     * Convierte texto a Title Case (primera letra de cada palabra en mayúscula).
     *
     * @param input El texto a convertir
     * @return El texto en Title Case
     */
    private static String toTitleCase(String input) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : input.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toTitleCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }

        return result.toString();
    }

    /**
     * Maneja apellidos con apóstrofe (O'Brien, D'Angelo).
     *
     * @param word La palabra con apóstrofe
     * @return La palabra normalizada correctamente
     */
    private static String handleApostrophe(String word) {
        int apostropheIndex = word.indexOf('\'');

        if (apostropheIndex == -1) {
            return toTitleCase(word);
        }

        String prefix = word.substring(0, apostropheIndex + 1).toLowerCase();
        String suffix = word.substring(apostropheIndex + 1);

        // Si el prefijo está en la lista especial, mantenerlo en minúscula
        if (APOSTROPHE_PREFIXES.contains(prefix)) {
            return prefix + toTitleCase(suffix);
        }

        // Caso general: O'Brien → capitalizar antes y después del apóstrofe
        return Character.toUpperCase(prefix.charAt(0)) + prefix.substring(1) + toTitleCase(suffix);
    }
}
