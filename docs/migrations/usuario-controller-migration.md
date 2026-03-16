# Migración de UsuarioController

## Resumen

Se ha migrado exitosamente la funcionalidad del `UsuarioController` desde el proyecto `seguridad-back` a `security-backend`, siguiendo estrictamente los principios de arquitectura hexagonal definidos en `docs/architecture.md`.

## Componentes Migrados

### 1. **UsuarioService** (application/service)

**Ubicación:** 
- Interface: `application/src/main/java/com/matias/application/service/UsuarioService.java`
- Implementación: `application/src/main/java/com/matias/application/service/impl/UsuarioServiceImpl.java`

**Responsabilidad:** Orquesta la lógica de negocio relacionada con la obtención de información del usuario.

```java
public interface UsuarioService {
    UsuarioResponse obtenerInfoUsuario(String email);
}
```

La implementación utiliza el `UsuarioRepositoryPort` para buscar el usuario por email, y luego lo mapea a `UsuarioResponse` usando MapStruct.

### 2. **UsuarioResponse** (application/dto/response)

**Ubicación:** `application/src/main/java/com/matias/application/dto/response/UsuarioResponse.java`

**Responsabilidad:** DTO (Data Transfer Object) que representa la respuesta con la información del usuario para el cliente.

```java
public record UsuarioResponse(
    Long id,
    String email,
    String nombre,
    String apellido,
    Set<Rol> roles,
    Boolean activo,
    Boolean emailVerificado
) {}
```

**Nota:** Se cambió el tipo de `id` de `Integer` a `Long` para coincidir con el modelo de dominio en `security-backend`.

### 3. **UsuarioMapper** (application/mapper)

**Ubicación:** `application/src/main/java/com/matias/application/mapper/UsuarioMapper.java`

**Responsabilidad:** Mapper de MapStruct que convierte objetos de dominio (`Usuario`) a DTOs de respuesta (`UsuarioResponse`).

```java
@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    UsuarioResponse toResponse(Usuario usuario);
}
```

### 4. **UsuarioController** (web/controller)

**Ubicación:** `web/src/main/java/com/matias/web/controller/UsuarioController.java`

**Responsabilidad:** Controlador REST que expone los endpoints para gestión de usuarios.

**Endpoints:**

#### GET /v1/usuario/me

Obtiene la información del usuario actualmente autenticado.

- **Autenticación:** Requerida (JWT Bearer Token)
- **Respuesta exitosa:** `200 OK` con `UsuarioResponse`
- **Respuestas de error:**
  - `401 Unauthorized`: Usuario no autenticado
  - `404 Not Found`: Usuario no encontrado

**Ejemplo de respuesta:**
```json
{
  "id": 1,
  "email": "usuario@example.com",
  "nombre": "Juan",
  "apellido": "Pérez",
  "roles": ["USUARIO"],
  "activo": true,
  "emailVerificado": true
}
```

## Arquitectura Implementada

La migración sigue **estrictamente** el flujo de dependencias de la arquitectura hexagonal:

```
┌─────────────────────────────────────────────────────────┐
│  web (Capa de Presentación)                             │
│  └─ UsuarioController (@RestController)                 │
│      └─ GET /v1/usuario/me                              │
│          └─ Recibe Authentication                       │
│          └─ Extrae email (Authentication.getName())     │
└────────────────┬────────────────────────────────────────┘
                 │
                 │ Llama a UsuarioService
                 ▼
┌─────────────────────────────────────────────────────────┐
│  application (Capa de Aplicación)                       │
│  ├─ UsuarioService                                      │
│  │   └─ obtenerInfoUsuario(String email)               │
│  │       → UsuarioResponse                              │
│  │                                                       │
│  ├─ UsuarioMapper (MapStruct)                           │
│  │   └─ Usuario → UsuarioResponse                       │
│  │                                                       │
│  └─ UsuarioResponse (DTO)                               │
└────────────────┬────────────────────────────────────────┘
                 │
                 │ Usa UsuarioRepositoryPort
                 ▼
┌─────────────────────────────────────────────────────────┐
│  domain (Núcleo)                                        │
│  ├─ Usuario (Modelo de dominio)                         │
│  │   ├─ id, email, nombre, apellido                     │
│  │   ├─ roles (Set<Rol>)                                │
│  │   ├─ activo, emailVerificado                         │
│  │   └─ password, fechaCreacion                         │
│  │                                                       │
│  └─ UsuarioRepositoryPort (Interface)                   │
│      └─ findByEmail(String) → Optional<Usuario>         │
└────────────────┬────────────────────────────────────────┘
                 │
                 │ Implementado por
                 ▼
┌─────────────────────────────────────────────────────────┐
│  database (Adaptador de Persistencia)                   │
│  └─ UsuarioRepositoryAdapter                            │
│      └─ Implementa UsuarioRepositoryPort                │
└─────────────────────────────────────────────────────────┘
```

## Flujo de Ejecución

1. **Request:** El cliente hace una petición `GET /v1/usuario/me` con un JWT token válido
2. **Security:** Spring Security intercepta la petición y valida el JWT
3. **Controller:** El `UsuarioController` recibe el objeto `Authentication` de Spring Security
4. **Extracción del email:** Se extrae el email mediante `authentication.getName()`
5. **Service:** Se llama a `UsuarioService.obtenerInfoUsuario(email)`
6. **Repository:** El servicio busca el usuario en la base de datos usando `UsuarioRepositoryPort.findByEmail(email)`
7. **Mapper:** Se mapea el `Usuario` del dominio a `UsuarioResponse` DTO usando MapStruct
8. **Response:** Se retorna la respuesta `200 OK` con el DTO

## Diferencias con seguridad-back

| Aspecto | seguridad-back | security-backend |
|---------|----------------|------------------|
| **Tipo ID Usuario** | `Integer` | `Long` |
| **Inyección en Controller** | `@AuthenticationPrincipal UsuarioEntity` | `Authentication` (Spring Security estándar) |
| **Parámetro del Service** | `Usuario` (objeto completo) | `String email` (solo email) |
| **Ubicación del DTO** | Mismo módulo que controller | `application` (módulo de casos de uso) |
| **Ubicación del Mapper** | Mismo módulo que controller | `application` (módulo de casos de uso) |
| **Dependencia del módulo web** | Directa a entidades JPA | Solo a `application` (sin SecurityUser) |
| **Arquitectura** | MVC tradicional | Hexagonal/Clean Architecture estricta |
| **Módulos** | Monolítico | Multi-módulo (domain, application, web, database, security) |

## Principios Arquitectónicos Aplicados

### 1. Separación de Responsabilidades

- **domain:** Define el modelo `Usuario` (entidad pura de negocio) y los Ports (interfaces)
- **application:** Contiene la lógica de negocio (`UsuarioService`), DTOs de respuesta/request, y Mappers
- **database:** Implementa los Ports del dominio (adaptador de persistencia)
- **security:** Maneja la autenticación y autorización (módulo independiente)
- **web:** Maneja **solo** la presentación (Controllers HTTP, routing)

### 2. Flujo de Dependencias (Dependency Rule)

```
web → application → domain ← database
```

Las dependencias siempre apuntan hacia adentro (hacia el dominio), nunca al revés.

**Importante:** El módulo `web` **NO** depende de `security`. Ambos son capas externas que dependen de `application`.

### 3. Inversión de Dependencias

- El `UsuarioController` (web) depende de la abstracción `UsuarioService` (interface en application)
- La implementación concreta `UsuarioServiceImpl` se inyecta en runtime por Spring

### 4. Uso Pragmático de Spring

Siguiendo el enfoque pragmático descrito en `docs/architecture.md`:
- Usamos `@Service` en la capa de aplicación
- Usamos `@Transactional` para gestión de transacciones
- Aprovechamos la inyección de dependencias de Spring

## Uso del Endpoint

### Con cURL

```bash
curl -X GET http://localhost:8080/v1/usuario/me \
  -H "Authorization: Bearer {tu-jwt-token}"
```

### Respuesta Exitosa (200 OK)

```json
{
  "id": 1,
  "email": "juan.perez@example.com",
  "nombre": "Juan",
  "apellido": "Pérez",
  "roles": ["USUARIO"],
  "activo": true,
  "emailVerificado": true
}
```

### Respuesta de Error (401 Unauthorized)

```json
{
  "timestamp": "2026-03-16T12:30:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token inválido o expirado",
  "path": "/v1/usuario/me"
}
```

## Testing

Para probar el endpoint:

1. Obtén un JWT token válido mediante el endpoint de login: `POST /v1/auth/login`
2. Usa el access token en el header `Authorization: Bearer {token}`
3. Realiza la petición a `GET /v1/usuario/me`

## Documentación API (Swagger)

El endpoint está documentado con Swagger/OpenAPI y puede consultarse en:
- **URL:** `http://localhost:8080/swagger-ui.html`
- **Tag:** Usuario
- **Operación:** Obtener información del usuario actual

## Notas Técnicas

1. **Authentication:** Interfaz estándar de Spring Security, evita dependencias entre `web` y `security`
2. **authentication.getName():** Retorna el "principal" (en nuestro caso, el email del usuario)
3. **MapStruct:** Genera automáticamente la implementación del mapper en tiempo de compilación
4. **@Transactional(readOnly = true):** Optimiza la consulta a la base de datos como solo lectura
5. **DTOs en application:** Los DTOs de respuesta viven en `application` porque son parte del contrato del caso de uso

## ¿Por qué esta arquitectura respeta los lineamientos?

Según `docs/architecture.md`:

### ✅ Cumplimiento de Principios

1. **El módulo `web` NO conoce `security`:**
   - El controller recibe `Authentication` (interfaz estándar de Spring)
   - No importa `SecurityUser` ni ninguna clase del módulo `security`
   
2. **El módulo `web` NO conoce `domain` directamente:**
   - El controller solo trabaja con `UsuarioResponse` (DTO de `application`)
   - No importa `Usuario` del dominio
   
3. **La lógica de negocio está en `application`:**
   - El servicio busca el usuario en el repositorio
   - El servicio realiza el mapeo de dominio a DTO
   
4. **Los DTOs pertenecen a `application`:**
   - `UsuarioResponse` está en `application/dto/response`
   - Esto sigue el patrón de `TokenResponse`, `RegistroResponse`, etc.

5. **El flujo de dependencias es correcto:**
   ```
   web → application → domain ← database
   ```
   - `web` solo depende de `application`
   - `application` solo depende de `domain`
   - `database` implementa interfaces de `domain`

## Futuras Mejoras

1. Agregar endpoint para actualización de perfil de usuario
2. Implementar endpoint para cambio de contraseña
3. Agregar caché para información de usuario frecuentemente consultada
4. Implementar paginación si se agrega listado de usuarios (para admin)
5. Agregar más información al perfil (avatar, preferencias, etc.)

## Compilación

El proyecto compila exitosamente con todos los cambios:

```bash
mvn clean compile
# BUILD SUCCESS
```

Todos los módulos se compilan correctamente y MapStruct genera las implementaciones de los mappers automáticamente.
