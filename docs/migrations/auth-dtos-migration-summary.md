# Migración: DTOs de Autenticación - Resumen Final

## Descripción General

Se han migrado todos los DTOs relacionados con la API de autenticación desde el proyecto `seguridad-back` al proyecto `security-backend`, implementándolos en la capa de aplicación siguiendo los principios de arquitectura hexagonal.

## DTOs Migrados

### 1. Request DTOs

#### RegistroRequest ✅
**Ubicación:** `application/src/main/java/com/matias/application/dto/request/RegistroRequest.java`

**Campos:**
- `email`: Email del usuario (validado con `@Email`)
- `password`: Contraseña (validada con `@Password`)
- `nombre`: Nombre del usuario (validado con `@CharactersWithWhiteSpaces`)
- `apellido`: Apellido del usuario (validado con `@CharactersWithWhiteSpaces`)

**Uso:** Endpoint `POST /v1/auth/register`

#### LogueoRequest ✅
**Ubicación:** `application/src/main/java/com/matias/application/dto/request/LogueoRequest.java`

**Campos:**
- `email`: Email del usuario
- `password`: Contraseña

**Uso:** Endpoint `POST /v1/auth/login`

#### EmailRequest ✅
**Ubicación:** `application/src/main/java/com/matias/application/dto/request/EmailRequest.java`

**Campos:**
- `email`: Email del usuario (validado con `@Email`)

**Uso:** 
- Reenviar email de verificación
- Solicitar reseteo de contraseña

**Estado:** Preparado para futuros endpoints

#### ResetPasswordRequest ✅
**Ubicación:** `application/src/main/java/com/matias/application/dto/request/ResetPasswordRequest.java`

**Campos:**
- `token`: Token de reseteo
- `nuevaPassword`: Nueva contraseña (validada con `@Password`)

**Uso:** Confirmar reseteo de contraseña

**Estado:** Preparado para futuros endpoints

### 2. Response DTOs

#### RegistroResponse ✅
**Ubicación:** `application/src/main/java/com/matias/application/dto/response/RegistroResponse.java`

**Campos:**
- `id`: ID del usuario registrado
- `email`: Email del usuario
- `nombre`: Nombre del usuario
- `apellido`: Apellido del usuario
- `mensaje`: Mensaje informativo

**Uso:** Respuesta del endpoint `POST /v1/auth/register`

#### TokenResponse ✅
**Ubicación:** `application/src/main/java/com/matias/application/dto/response/TokenResponse.java`

**Campos:**
- `accessToken`: Token de acceso JWT

**Uso:** Respuesta de endpoints de autenticación (`login`, `refresh`)

#### ErrorResponse ✅
**Ubicación:** `application/src/main/java/com/matias/application/dto/response/ErrorResponse.java`

**Campos:**
- `timestamp`: Marca de tiempo del error
- `status`: Código HTTP del error
- `error`: Descripción del tipo de error
- `message`: Mensaje descriptivo del error
- `path`: Ruta donde ocurrió el error

**Uso:** Respuesta estándar para todos los errores del sistema

### 3. Internal DTOs

#### TokenInternal ✅
**Ubicación:** `application/src/main/java/com/matias/application/dto/internal/TokenInternal.java`

**Campos:**
- `accessToken`: Token de acceso JWT
- `refreshToken`: Token de refresco

**Uso:** Comunicación interna entre capa de aplicación y capa web

## Validaciones Implementadas

### @Password
Valida que la contraseña cumpla con:
- Al menos una letra mayúscula
- Al menos una letra minúscula
- Al menos un dígito
- Al menos un carácter especial

### @CharactersWithWhiteSpaces
Valida que el campo:
- Solo contenga letras (mayúsculas/minúsculas)
- Permita espacios en blanco
- No permita números ni caracteres especiales

## Endpoints Implementados

### Actualmente Activos

1. **POST /v1/auth/register**
   - Request: `RegistroRequest`
   - Response: `RegistroResponse` (201 Created)
   - Funcionalidad: Registra nuevo usuario y envía email de verificación

2. **POST /v1/auth/login**
   - Request: `LogueoRequest`
   - Response: `TokenResponse` (200 OK)
   - Funcionalidad: Autentica usuario y retorna tokens

3. **POST /v1/auth/refresh**
   - Request: Cookie `refresh`
   - Response: `TokenResponse` (200 OK)
   - Funcionalidad: Renueva tokens usando refresh token

4. **POST /v1/auth/logout**
   - Request: Cookie `refresh`
   - Response: 204 No Content
   - Funcionalidad: Cierra sesión e invalida tokens

5. **GET /v1/auth/verificar-email**
   - Query Param: `token`
   - Response: 204 No Content
   - Funcionalidad: Verifica email del usuario

### Preparados para Implementación Futura

Los siguientes DTOs están listos para cuando se implementen sus endpoints correspondientes:

1. **POST /v1/auth/reenviar-verificacion** (futuro)
   - Request: `EmailRequest`
   - Funcionalidad: Reenvía email de verificación

2. **POST /v1/auth/solicitar-reseteo** (futuro)
   - Request: `EmailRequest`
   - Funcionalidad: Solicita reseteo de contraseña

3. **POST /v1/auth/confirmar-reseteo** (futuro)
   - Request: `ResetPasswordRequest`
   - Funcionalidad: Confirma y ejecuta reseteo de contraseña

## Comparación con seguridad-back

| DTO | seguridad-back | security-backend | Estado |
|-----|----------------|------------------|--------|
| RegistroRequest | ✅ | ✅ | Migrado |
| LogueoRequest | ✅ | ✅ | Migrado |
| EmailRequest | ✅ | ✅ | Migrado |
| ResetPasswordRequest | ✅ | ✅ | Migrado |
| RegistroResponse | ✅ | ✅ | Migrado |
| TokenResponse | ✅ | ✅ | Migrado |
| ErrorResponse | ✅ | ✅ | Migrado |
| TokenInternal | ✅ | ✅ | Migrado |
| UpdateUserStatusRequest | ✅ | ⏳ | Pendiente (Admin API) |
| UsuarioFilterRequest | ✅ | ⏳ | Pendiente (Admin API) |
| PageResponse | ✅ | ⏳ | Pendiente (Admin API) |
| StatsResponse | ✅ | ⏳ | Pendiente (Admin API) |
| UsuarioAuditResponse | ✅ | ⏳ | Pendiente (Admin API) |
| UsuarioListItemResponse | ✅ | ⏳ | Pendiente (Admin API) |
| UsuarioResponse | ✅ | ⏳ | Pendiente (Admin API) |
| UsuarioRolResponse | ✅ | ⏳ | Pendiente (Admin API) |

## Principios Aplicados

### 1. Ubicación de DTOs
Siguiendo la arquitectura hexagonal de security-backend:
- **Request/Response DTOs:** En módulo `application` (capa de aplicación)
- **Internal DTOs:** En módulo `application` para comunicación interna
- **No en web:** La capa web solo usa los DTOs, no los define

### 2. Validaciones
- Validaciones en DTOs usando Bean Validation (JSR-380)
- Validaciones personalizadas con anotaciones custom (`@Password`, `@CharactersWithWhiteSpaces`)
- Validadores en el módulo `application`

### 3. Documentación
- Uso de `@Schema` de OpenAPI para documentación de API
- Comentarios JavaDoc descriptivos
- Ejemplos en anotaciones `@Schema`

### 4. Inmutabilidad
- Uso de `record` de Java para DTOs inmutables
- Sin setters, solo construcción mediante constructor

## Beneficios de la Migración

1. **Separación de Responsabilidades:** DTOs en capa de aplicación, separados de capa web
2. **Validación Consistente:** Todas las validaciones centralizadas y reutilizables
3. **Documentación Mejorada:** OpenAPI integrado para documentación automática
4. **Type Safety:** Uso de records para inmutabilidad y seguridad de tipos
5. **Mantenibilidad:** Estructura clara y predecible siguiendo arquitectura hexagonal

## Próximos Pasos

1. **Implementar funcionalidad de reseteo de contraseña:**
   - Crear modelo de dominio `TokenResetPassword`
   - Implementar servicios de aplicación
   - Crear endpoints en AuthController
   - Los DTOs ya están listos (`EmailRequest`, `ResetPasswordRequest`)

2. **Implementar reenvío de verificación:**
   - Agregar método en `AuthService`
   - Crear endpoint en `AuthController`
   - El DTO ya está listo (`EmailRequest`)

3. **Migrar DTOs de Admin:**
   - Migrar DTOs de gestión de usuarios
   - Implementar API de administración
   - DTOs pendientes listados en la tabla de comparación

## Testing

Para probar los DTOs migrados:

```bash
# Compilar proyecto
mvn clean compile -DskipTests

# Ejecutar tests
mvn test

# Ejecutar aplicación
mvn spring-boot:run -pl app-root
```

Endpoints disponibles para testing:
- Registro: `POST http://localhost:8081/api/v1/auth/register`
- Login: `POST http://localhost:8081/api/v1/auth/login`
- Refresh: `POST http://localhost:8081/api/v1/auth/refresh`
- Logout: `POST http://localhost:8081/api/v1/auth/logout`
- Verificación: `GET http://localhost:8081/api/v1/auth/verificar-email?token={token}`

## Referencias

- Documentación de arquitectura: `docs/architecture.md`
- Migración de email: `docs/migrations/email-migration.md`
- Migración de verificación: `docs/migrations/email-verification-migration.md`
- Migración de excepciones: `docs/migrations/exceptions-migration.md`
- Migración de rate limiting: `docs/migrations/rate-limit-migration.md`
