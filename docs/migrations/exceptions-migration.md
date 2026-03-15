# Migración de Excepciones (Exception Handling)

## Fecha
15 de marzo de 2026

## Contexto
Migración del sistema de manejo de excepciones desde el proyecto `seguridad-back` al proyecto `security-backend`, manteniendo la separación de responsabilidades según la arquitectura hexagonal.

## Objetivos
1. Migrar las excepciones de dominio al módulo `domain`
2. Migrar el exception handler al módulo `web`
3. Mantener la separación de responsabilidades entre capas
4. Asegurar que el manejo de errores sea consistente y robusto

---

## Cambios Realizados

### 1. Excepciones de Dominio (Módulo `domain`)

Se crearon las siguientes excepciones en `domain/src/main/java/com/matias/domain/exception/`:

#### `RecursoNoEncontradoException.java` (404 Not Found)
```java
package com.matias.domain.exception;

/**
 * 404 Not Found. Entidades no encontradas en BD.
 */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
```

**Uso**: Cuando una entidad no existe en la base de datos (usuario, producto, etc.)

#### `ConflictoException.java` (409 Conflict)
```java
package com.matias.domain.exception;

/**
 * 409 Conflict. Duplicados, estados conflictivos.
 */
public class ConflictoException extends RuntimeException {
    public ConflictoException(String message) {
        super(message);
    }
}
```

**Uso**: Violaciones de unicidad (email duplicado), estados conflictivos

#### `AccesoDenegadoException.java` (403 Forbidden)
```java
package com.matias.domain.exception;

/**
 * 403 Forbidden. Permisos insuficientes, cuentas deshabilitadas.
 */
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String message) {
        super(message);
    }
}
```

**Uso**: Permisos insuficientes, cuentas deshabilitadas, roles inadecuados

#### `NoAutenticadoException.java` (401 Unauthorized)
```java
package com.matias.domain.exception;

/**
 * 401 Unauthorized. Tokens inválidos, sesión expirada.
 */
public class NoAutenticadoException extends RuntimeException {
    public NoAutenticadoException(String message) {
        super(message);
    }
}
```

**Uso**: Tokens inválidos, sesión expirada, credenciales incorrectas

#### `OperacionNoPermitidaException.java` (400 Bad Request)
```java
package com.matias.domain.exception;

/**
 * 400 Bad Request. Operaciones no permitidas, validaciones de negocio.
 */
public class OperacionNoPermitidaException extends RuntimeException {
    public OperacionNoPermitidaException(String message) {
        super(message);
    }
}
```

**Uso**: Operaciones no permitidas por reglas de negocio, validaciones de dominio

#### `ServicioExternoException.java` (503 Service Unavailable)
```java
package com.matias.domain.exception;

/**
 * 503 Service Unavailable. Errores de servicios externos (email, APIs).
 */
public class ServicioExternoException extends RuntimeException {
    public ServicioExternoException(String message) {
        super(message);
    }
}
```

**Uso**: Errores de servicios externos (email, APIs externas, servicios de terceros)

---

### 2. Exception Handler Global (Módulo `web`)

Se creó `DefaultExceptionHandler.java` en `web/src/main/java/com/matias/web/handler/`:

```java
@Slf4j
@ControllerAdvice
public class DefaultExceptionHandler {
    // Manejo centralizado de todas las excepciones
}
```

#### Excepciones Manejadas

1. **Validación de Bean Validation** (`MethodArgumentNotValidException`)
   - Status: 400 Bad Request
   - Agrupa todos los errores de validación

2. **Credenciales Incorrectas** (`BadCredentialsException`)
   - Status: 401 Unauthorized
   - Para login fallido

3. **Excepciones de Negocio**:
   - `OperacionNoPermitidaException` → 400 Bad Request
   - `NoAutenticadoException` → 401 Unauthorized
   - `AccesoDenegadoException` → 403 Forbidden
   - `RecursoNoEncontradoException` → 404 Not Found
   - `ConflictoException` → 409 Conflict
   - `ServicioExternoException` → 503 Service Unavailable

4. **Errores JWT**:
   - `SignatureException` → Token con firma inválida
   - `MalformedJwtException` → Token malformado
   - `ExpiredJwtException` → Token expirado
   - `UnsupportedJwtException` → Token no soportado
   - Todos retornan 401 Unauthorized con mensajes específicos

5. **Errores de Sistema**:
   - `IllegalStateException` → 500 Internal Server Error
   - `Exception` (fallback) → 500 Internal Server Error

6. **Errores de Endpoint/HTTP**:
   - `NoResourceFoundException` → 404 Not Found (endpoint inexistente)
   - `HttpRequestMethodNotSupportedException` → 405 Method Not Allowed
   - `MethodArgumentTypeMismatchException` → 400 Bad Request (parámetros inválidos)

---

## Arquitectura Hexagonal

### Separación de Responsabilidades

```
┌─────────────────────────────────────────────────────────────┐
│                    MÓDULO WEB (Adaptador)                   │
│  - DefaultExceptionHandler.java                             │
│  - Mapea excepciones a HTTP responses                       │
│  - Formatea respuestas JSON (ErrorResponse)                 │
└─────────────────────────────────────────────────────────────┘
                              ▲
                              │
                              │ throws
                              │
┌─────────────────────────────────────────────────────────────┐
│                 MÓDULO APPLICATION (Casos de Uso)           │
│  - AuthServiceImpl.java                                     │
│  - Lanza excepciones de dominio                             │
│  - Lógica de negocio                                        │
└─────────────────────────────────────────────────────────────┘
                              ▲
                              │
                              │ usa
                              │
┌─────────────────────────────────────────────────────────────┐
│                    MÓDULO DOMAIN (Core)                     │
│  - Excepciones de negocio:                                  │
│    * RecursoNoEncontradoException                           │
│    * ConflictoException                                     │
│    * AccesoDenegadoException                                │
│    * NoAutenticadoException                                 │
│    * OperacionNoPermitidaException                          │
│    * ServicioExternoException                               │
└─────────────────────────────────────────────────────────────┘
```

### Beneficios de esta Arquitectura

1. **Independencia del Dominio**: Las excepciones de negocio no conocen nada sobre HTTP o REST
2. **Reutilización**: Las mismas excepciones pueden usarse en diferentes adaptadores (REST, GraphQL, CLI)
3. **Testabilidad**: Se pueden probar las excepciones de negocio sin mockear HTTP
4. **Claridad**: Cada excepción tiene un propósito semántico claro

---

## Formato de Respuesta de Error

Todas las excepciones retornan un `ErrorResponse` con la siguiente estructura:

```json
{
  "title": "Título del error",
  "details": [
    "Detalle 1",
    "Detalle 2"
  ]
}
```

**Ejemplo de validación fallida:**
```json
{
  "title": "Datos de entrada no válidos",
  "details": [
    "El email no es válido",
    "La contraseña debe tener al menos 8 caracteres"
  ]
}
```

**Ejemplo de recurso no encontrado:**
```json
{
  "title": "Recurso no encontrado",
  "details": [
    "Usuario con id 123 no encontrado"
  ]
}
```

---

## Mapeo de Excepciones a HTTP Status

| Excepción                           | HTTP Status | Descripción                                    |
|-------------------------------------|-------------|------------------------------------------------|
| `OperacionNoPermitidaException`     | 400         | Operaciones no permitidas, validaciones        |
| `MethodArgumentNotValidException`   | 400         | Bean Validation fallida                        |
| `MethodArgumentTypeMismatchException`| 400        | Parámetros con tipo incorrecto                 |
| `NoAutenticadoException`            | 401         | No autenticado, token inválido                 |
| `BadCredentialsException`           | 401         | Credenciales incorrectas                       |
| JWT Exceptions                      | 401         | Problemas con tokens JWT                       |
| `AccesoDenegadoException`           | 403         | Permisos insuficientes                         |
| `RecursoNoEncontradoException`      | 404         | Entidad no encontrada                          |
| `NoResourceFoundException`          | 404         | Endpoint no existe                             |
| `HttpRequestMethodNotSupportedException` | 405    | Método HTTP no soportado                       |
| `ConflictoException`                | 409         | Conflictos (duplicados, estados)               |
| `IllegalStateException`             | 500         | Error interno                                  |
| `Exception` (fallback)              | 500         | Error inesperado                               |
| `ServicioExternoException`          | 503         | Servicio externo no disponible                 |

---

## Ejemplo de Uso

### En AuthServiceImpl

```java
@Override
public RegistroResponse registrarUsuario(RegistroRequest request) {
    // Verificar si el email ya existe
    if (usuarioRepository.existsByEmail(request.email())) {
        throw new ConflictoException("El email ya está registrado");
    }
    
    // Lógica de registro...
}
```

### Respuesta HTTP

El `DefaultExceptionHandler` captura la excepción y retorna:

```http
HTTP/1.1 409 Conflict
Content-Type: application/json

{
  "title": "Conflicto",
  "details": [
    "El email ya está registrado"
  ]
}
```

---

## Implementación en Servicios

### AuthServiceImpl

Se implementaron las siguientes excepciones en `application/src/main/java/com/matias/application/service/impl/AuthServiceImpl.java`:

#### Método `register()`
- **ConflictoException**: Cuando el email ya está registrado
  ```java
  if (usuarioRepository.findByEmail(request.email()).isPresent()) {
      throw new ConflictoException("El email ya está registrado");
  }
  ```

#### Método `login()`
- **NoAutenticadoException**: 
  - Usuario no encontrado (credenciales inválidas)
  - Contraseña incorrecta
  ```java
  Usuario usuario = usuarioRepository.findByEmail(request.email())
      .orElseThrow(() -> new NoAutenticadoException("Credenciales inválidas"));
  
  if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
      throw new NoAutenticadoException("Credenciales inválidas");
  }
  ```

- **AccesoDenegadoException**: Usuario inactivo
  ```java
  if (!usuario.getActivo()) {
      throw new AccesoDenegadoException("Usuario inactivo. Contacte al administrador");
  }
  ```

#### Método `refresh()`
- **NoAutenticadoException**: Token inválido o no es refresh token
  ```java
  if (!tokenService.esRefreshToken(refreshToken)) {
      throw new NoAutenticadoException("Token inválido: no es un refresh token");
  }
  ```

- **RecursoNoEncontradoException**: Usuario no encontrado al refrescar
  ```java
  Usuario usuario = usuarioRepository.findByEmail(email)
      .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
  ```

---

### AdminServiceImpl

Se implementaron las siguientes excepciones en `application/src/main/java/com/matias/application/service/impl/AdminServiceImpl.java`:

#### Método `obtenerDetalleUsuario()`
- **RecursoNoEncontradoException**: Usuario no encontrado por ID
  ```java
  return usuarioRepository.findById(userId)
      .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + userId));
  ```

#### Método `updateUserStatus()`
- **OperacionNoPermitidaException**: Intento de desactivar usuario con roles privilegiados
  ```java
  if (tieneRolesPrivilegiados) {
      throw new OperacionNoPermitidaException(
          "No se puede desactivar un usuario con rol ADMINISTRADOR o MODERADOR");
  }
  ```

#### Método `assignRole()`
- **ConflictoException**: Intento de asignar un rol que ya tiene
  ```java
  if (usuarioRepository.existsByUsuarioIdAndRol(userId, rol)) {
      throw new ConflictoException("El usuario ya tiene el rol asignado");
  }
  ```

#### Método `unassignRole()`
- **OperacionNoPermitidaException**: Intento de remover el rol USUARIO
  ```java
  if (rol == Rol.USUARIO) {
      throw new OperacionNoPermitidaException("El rol USUARIO no puede ser removido");
  }
  ```

---

## Migración Completada

✅ Excepciones de dominio migradas al módulo `domain`  
✅ Exception handler migrado al módulo `web`  
✅ Separación de responsabilidades según arquitectura hexagonal  
✅ **Excepciones implementadas en AuthServiceImpl**  
✅ **Excepciones implementadas en AdminServiceImpl**  
✅ Compilación exitosa (BUILD SUCCESS)  
✅ Respuestas de error consistentes y estructuradas  

---

## Notas Importantes

1. **No usar EntityNotFoundException**: 
   - El módulo `web` no debe depender de `jakarta.persistence`
   - Usar `RecursoNoEncontradoException` del dominio

2. **Logging**:
   - Warnings para errores esperados (400, 401, 403, 404, 409)
   - Errors para problemas del sistema (500, 503)

3. **Mensajes**:
   - Los mensajes deben ser claros para el cliente
   - No exponer detalles técnicos internos en producción

4. **Extensibilidad**:
   - Agregar nuevas excepciones de dominio cuando sea necesario
   - Actualizar el handler para mapearlas correctamente

---

## Referencias

- Proyecto origen: `seguridad-back`
- Archivos migrados:
  - `seguridad-back/src/main/java/com/matias/project/exception/DefaultExceptionHandler.java`
  - Excepciones de `seguridad-back/src/main/java/com/matias/project/exception/*Exception.java`
