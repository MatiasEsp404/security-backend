# Migración de DTOs de Autenticación

## Resumen
Este documento describe la migración completa de los DTOs de autenticación desde el proyecto `seguridad-back` hacia `security-backend`, siguiendo los principios de arquitectura hexagonal del proyecto.

## Fecha de Migración
15 de Marzo de 2026

## 1. DTOs Migrados

### 1.1 DTOs de Request (application/dto/request)
- **RegistroRequest**: DTO para registro de usuarios
  - Validaciones: `@Email`, `@NotBlank`, `@Password`, `@CharactersWithWhiteSpaces`
  - Campos: nombre, apellido, telefono, email, password
  
- **LogueoRequest**: DTO para inicio de sesión
  - Validaciones: `@Email`, `@NotBlank`
  - Campos: email, password

### 1.2 DTOs de Response (application/dto/response)
- **RegistroResponse**: Respuesta del registro exitoso
  - Campos: id, nombre, apellido, email, createdAt
  
- **TokenResponse**: Respuesta con token de autenticación
  - Campos: token, expirationTime

### 1.3 DTOs Internos (application/dto/internal)
- **TokenInternal**: DTO interno para gestión de tokens
  - Campos: token, expirationTime

## 2. Validadores Personalizados

### 2.1 @Password
- **Ubicación**: `application/validation/Password.java` y `PasswordValidator.java`
- **Propósito**: Validar que la contraseña cumpla con los requisitos de seguridad
- **Reglas**:
  - Mínimo 8 caracteres
  - Al menos una letra mayúscula
  - Al menos una letra minúscula
  - Al menos un dígito
  - Al menos un carácter especial (@$!%*?&)
  - Solo caracteres alfanuméricos y especiales permitidos

### 2.2 @CharactersWithWhiteSpaces
- **Ubicación**: `application/validation/CharactersWithWhiteSpaces.java` y `CharactersWithWhiteSpacesValidator.java`
- **Propósito**: Validar que el texto solo contenga letras y espacios
- **Reglas**: Solo permite letras (a-z, A-Z, áéíóúÁÉÍÓÚñÑ) y espacios

## 3. Servicios Implementados

### 3.1 AuthService y AuthServiceImpl
- **Ubicación**: 
  - Interface: `application/service/AuthService.java`
  - Implementación: `application/service/impl/AuthServiceImpl.java`
- **Métodos**:
  - `registrar(RegistroRequest)`: Registra un nuevo usuario
  - `loguear(LogueoRequest)`: Autentica un usuario y genera token

### 3.2 Implementación de AuthService
- Utiliza `UsuarioRepositoryPort` para acceso a datos
- Utiliza `TokenServicePort` para generación de tokens
- Utiliza `PasswordEncoder` para cifrado de contraseñas
- Manejo de excepciones para casos de error

## 4. Controlador REST

### 4.1 AuthController
- **Ubicación**: `web/controller/AuthController.java`
- **Base Path**: `/v1/auth`
- **Endpoints**:
  - `POST /v1/auth/registro`: Registra un nuevo usuario
    - Request: `RegistroRequest`
    - Response: `RegistroResponse` (201 Created)
  - `POST /v1/auth/login`: Autentica un usuario
    - Request: `LogueoRequest`
    - Response: `TokenResponse` (200 OK)

## 5. Migración de Componentes de Seguridad

### 5.1 TokenServiceImpl
- **Ubicación**: `security/jwt/TokenServiceImpl.java`
- **Propósito**: Implementa `TokenServicePort` para generación y validación de JWT
- **Funcionalidades**:
  - Generación de tokens JWT
  - Validación de tokens
  - Extracción de claims
  - Configuración mediante properties

### 5.2 JwtFilter
- **Ubicación**: `security/filter/JwtFilter.java`
- **Propósito**: Filtro para validar JWT en cada request
- **Funcionamiento**:
  - Extrae token del header Authorization
  - Valida el token
  - Establece autenticación en SecurityContext

### 5.3 SpaCsrfTokenRequestHandler
- **Ubicación**: `security/csrf/SpaCsrfTokenRequestHandler.java`
- **Propósito**: Manejador de tokens CSRF para Single Page Applications
- **Características**:
  - Resuelve y carga tokens CSRF
  - Compatible con aplicaciones SPA

### 5.4 UserDetailsServiceImpl
- **Ubicación**: `security/service/UserDetailsServiceImpl.java`
- **Propósito**: Implementa `UserDetailsService` de Spring Security
- **Funcionalidades**:
  - Carga usuarios desde el repositorio
  - Carga roles asociados con JOIN FETCH
  - Convierte a `SecurityUser` para Spring Security

### 5.5 SecurityConfig
- **Ubicación**: `security/config/SecurityConfig.java`
- **Propósito**: Configuración central de Spring Security
- **Configuraciones**:
  - Endpoints públicos y protegidos
  - Configuración CSRF con handler personalizado
  - Integración del JwtFilter
  - CORS habilitado
  - Session management STATELESS
  - PasswordEncoder (BCrypt)

## 6. Configuración de Propiedades

### 6.1 application.properties
- **Ubicación**: `app-root/src/main/resources/application.properties`
- **Propiedades JWT**:
  ```properties
  jwt.secret=your_secret_key_here_should_be_at_least_256_bits
  jwt.expiration-time=86400000
  ```

## 7. Actualizaciones en Base de Datos

### 7.1 UsuarioRepositoryPort
- **Nuevo método**: `findByEmailWithRoles(String email)`
- **Propósito**: Cargar usuario con roles en una sola consulta

### 7.2 UsuarioRepositoryAdapter
- **Implementación** del método `findByEmailWithRoles`

### 7.3 UsuarioJpaRepository
- **Query JPQL**:
  ```java
  @Query("SELECT u FROM UsuarioEntity u LEFT JOIN FETCH u.usuarioRoles WHERE u.email = :email")
  Optional<UsuarioEntity> findByEmailWithRoles(@Param("email") String email);
  ```

## 8. Dependencias Agregadas

### 8.1 Módulo security (security/pom.xml)
```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>

<!-- Web y Transaccional -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-tx</artifactId>
</dependency>
```

### 8.2 Módulo application (application/pom.xml)
```xml
<!-- Validación -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### 8.3 Módulo web (web/pom.xml)
```xml
<!-- Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

## 9. Arquitectura Hexagonal

La migración respeta los principios de arquitectura hexagonal:

### 9.1 Capa de Dominio (domain)
- **Modelo**: `Usuario`, `Rol`
- **Puertos**: `UsuarioRepositoryPort`, `TokenServicePort`
- Sin dependencias de frameworks

### 9.2 Capa de Aplicación (application)
- **DTOs**: Request, Response, Internal
- **Validadores**: Validaciones de negocio personalizadas
- **Servicios**: `AuthService` con lógica de negocio
- Depende solo del dominio

### 9.3 Capa de Infraestructura
- **database**: Adaptadores JPA y entidades
- **security**: Implementación de seguridad con Spring Security y JWT
- **web**: Controllers REST
- Implementan los puertos del dominio

## 10. Flujo de Autenticación

### 10.1 Registro de Usuario
1. Cliente envía `POST /v1/auth/registro` con `RegistroRequest`
2. `AuthController` valida y delega a `AuthService`
3. `AuthService` cifra password y guarda usuario
4. Retorna `RegistroResponse` con datos del usuario

### 10.2 Login de Usuario
1. Cliente envía `POST /v1/auth/login` con `LogueoRequest`
2. `AuthController` delega a `AuthService`
3. `AuthService` valida credenciales
4. `TokenServiceImpl` genera JWT
5. Retorna `TokenResponse` con token y expiración

### 10.3 Autenticación de Requests
1. `JwtFilter` intercepta el request
2. Extrae token del header Authorization
3. Valida token con `TokenServiceImpl`
4. Establece autenticación en `SecurityContext`
5. Request continúa a su destino

## 11. Testing y Compilación

### 11.1 Resultado de Compilación
```
[INFO] BUILD SUCCESS
[INFO] Total time: 17.754 s
```

Todos los módulos compilaron exitosamente:
- domain ✓
- application ✓
- database ✓
- security ✓
- web ✓
- app-root ✓

## 12. Próximos Pasos

1. **Testing**: Crear tests unitarios y de integración para:
   - Validadores personalizados
   - AuthService
   - AuthController
   - Componentes de seguridad

2. **Documentación API**: Agregar Swagger/OpenAPI para documentar endpoints

3. **Migración adicional**: Continuar con otros módulos de seguridad-back si es necesario

4. **Configuración de Base de Datos**: Agregar scripts de inicialización si se requieren

5. **Variables de Entorno**: Externalizar configuraciones sensibles (jwt.secret)

## 13. Notas Importantes

- Todos los DTOs incluyen validaciones de Bean Validation
- Las contraseñas se cifran con BCrypt
- Los tokens JWT tienen tiempo de expiración configurable
- La configuración CSRF está adaptada para SPAs
- El filtro JWT solo procesa requests con token Bearer
- Los endpoints de autenticación son públicos (no requieren token)

## 14. Referencias

- Documentación de arquitectura: `docs/architecture.md`
- Conceptos CRUD: `docs/crud-concepts.md`
- Código fuente del proyecto original: `C:\Proyectos\seguridad-back`
