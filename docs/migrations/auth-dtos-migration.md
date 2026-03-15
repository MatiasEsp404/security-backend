# Auth DTOs Migration

## Overview

This document details the migration of DTOs (Data Transfer Objects) from the legacy `seguridad-back` project to the `security-backend` project, specifically for the `/v1/auth` API endpoints.

**Migration Date:** March 15, 2026  
**Source Project:** `C:\Proyectos\seguridad-back`  
**Target Project:** `C:\Proyectos\security-backend`  
**Scope:** Authentication DTOs and custom validation annotations

## Architecture Compliance

This migration follows the hexagonal architecture principles established in `security-backend`:

- **DTOs Location:** Moved to the `application` module (application layer)
- **Validation Annotations:** Custom validators placed in `application` module
- **Separation of Concerns:** Web layer (`web` module) only handles HTTP concerns
- **Domain Independence:** Application DTOs are independent of web framework specifics

## Migrated Components

### 1. Custom Validation Annotations

#### 1.1 Password Validation

**Location:** `application/src/main/java/com/matias/application/validation/`

- **`@Password`** - Custom annotation for password validation
  - Validates minimum length (8 characters)
  - Requires at least one uppercase letter
  - Requires at least one lowercase letter
  - Requires at least one digit
  - Requires at least one special character (!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?)
  
- **`PasswordValidator`** - Implementation of the password validation logic

**Usage Example:**
```java
@Password
private String password;
```

#### 1.2 Characters with White Spaces Validation

**Location:** `application/src/main/java/com/matias/application/validation/`

- **`@CharactersWithWhiteSpaces`** - Custom annotation for alphanumeric validation with spaces
  - Allows letters (a-z, A-Z)
  - Allows digits (0-9)
  - Allows spaces
  - Minimum length: 1 character
  - Maximum length: 50 characters

- **`CharactersWithWhiteSpacesValidator`** - Implementation of the validation logic

**Usage Example:**
```java
@CharactersWithWhiteSpaces
private String nombre;
```

### 2. Request DTOs

#### 2.1 RegistroRequest (Registration Request)

**Location:** `application/src/main/java/com/matias/application/dto/request/RegistroRequest.java`

**Purpose:** Captures user registration data

**Fields:**
- `nombre` (String) - User's first name
  - Validation: `@NotBlank`, `@CharactersWithWhiteSpaces`
  
- `apellido` (String) - User's last name
  - Validation: `@NotBlank`, `@CharactersWithWhiteSpaces`
  
- `email` (String) - User's email address
  - Validation: `@NotBlank`, `@Email`
  
- `password` (String) - User's password
  - Validation: `@NotBlank`, `@Password`

**OpenAPI Documentation:** Fully annotated with `@Schema` for API documentation

#### 2.2 LogueoRequest (Login Request)

**Location:** `application/src/main/java/com/matias/application/dto/request/LogueoRequest.java`

**Purpose:** Captures user authentication credentials

**Fields:**
- `email` (String) - User's email address
  - Validation: `@NotBlank`, `@Email`
  
- `password` (String) - User's password
  - Validation: `@NotBlank`

**OpenAPI Documentation:** Fully annotated with `@Schema` for API documentation

### 3. Response DTOs

#### 3.1 RegistroResponse (Registration Response)

**Location:** `application/src/main/java/com/matias/application/dto/response/RegistroResponse.java`

**Purpose:** Returns data about the newly registered user

**Fields:**
- `id` (Integer) - User's unique identifier
- `email` (String) - User's email address

**Implementation:** Java Record for immutability

#### 3.2 TokenResponse (Token Response)

**Location:** `application/src/main/java/com/matias/application/dto/response/TokenResponse.java`

**Purpose:** Returns JWT access token to the client

**Fields:**
- `accessToken` (String) - JWT access token (short-lived)

**Implementation:** Java Record for immutability

### 4. Internal DTOs

#### 4.1 TokenInternal

**Location:** `application/src/main/java/com/matias/application/dto/internal/TokenInternal.java`

**Purpose:** Internal DTO for passing both access and refresh tokens between layers

**Fields:**
- `accessToken` (String) - JWT access token
- `refreshToken` (String) - JWT refresh token

**Implementation:** Java Record for immutability  
**Note:** Marked with `@Hidden` to exclude from OpenAPI documentation

## Updated Services and Controllers

### AuthService Interface

**Location:** `application/src/main/java/com/matias/application/service/AuthService.java`

**Updated Method Signatures:**
```java
RegistroResponse register(RegistroRequest request);
TokenInternal login(LogueoRequest request);
TokenInternal refresh(String refreshToken);
void logout(String refreshToken);
void verificarEmail(String token);
void reenviarEmailVerificacion(Object request, String ipOrigen);
void solicitarReseteoPassword(Object request, String ipOrigen);
void validarTokenReset(String token);
void resetearPassword(Object request);
```

**Changes:**
- Updated imports to use DTOs from `application` module
- Return types updated to use new DTOs

### AuthController

**Location:** `web/src/main/java/com/matias/web/controller/AuthController.java`

**Updates:**
- Imports changed from `com.matias.web.dto.*` to `com.matias.application.dto.*`
- All endpoint handlers now use DTOs from the application layer
- Maintains HTTP-specific concerns (cookies, response headers, etc.)

**Endpoint Structure:**
- `POST /v1/auth/register` - User registration
- `POST /v1/auth/login` - User authentication
- `POST /v1/auth/refresh` - Token refresh
- `POST /v1/auth/logout` - User logout
- `POST /v1/auth/verify` - Email verification

## Dependency Updates

### Application Module (application/pom.xml)

**Added Dependencies:**
```xml
<!-- Spring Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- OpenAPI Documentation -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

**Rationale:**
- `spring-boot-starter-validation` - Required for custom validators and Jakarta Validation API
- `springdoc-openapi-starter-webmvc-ui` - Required for `@Schema` annotations on DTOs

## File Structure

### Before Migration
```
web/
└── src/main/java/com/matias/web/
    ├── dto/
    │   ├── request/
    │   │   ├── RegistroRequest.java
    │   │   └── LogueoRequest.java
    │   ├── response/
    │   │   ├── RegistroResponse.java
    │   │   └── TokenResponse.java
    │   └── internal/
    │       └── TokenInternal.java
    └── validation/
        ├── Password.java
        ├── PasswordValidator.java
        ├── CharactersWithWhiteSpaces.java
        └── CharactersWithWhiteSpacesValidator.java
```

### After Migration
```
application/
└── src/main/java/com/matias/application/
    ├── dto/
    │   ├── request/
    │   │   ├── RegistroRequest.java
    │   │   └── LogueoRequest.java
    │   ├── response/
    │   │   ├── RegistroResponse.java
    │   │   └── TokenResponse.java
    │   └── internal/
    │       └── TokenInternal.java
    ├── validation/
    │   ├── Password.java
    │   ├── PasswordValidator.java
    │   ├── CharactersWithWhiteSpaces.java
    │   └── CharactersWithWhiteSpacesValidator.java
    └── service/
        └── AuthService.java

web/
└── src/main/java/com/matias/web/
    ├── dto/
    │   └── ProductDto.java  (preserved - not part of auth migration)
    └── controller/
        └── AuthController.java
```

## Removed Files

The following files were removed from the `web` module after successful migration:

**Validation Annotations:**
- `web/src/main/java/com/matias/web/validation/Password.java`
- `web/src/main/java/com/matias/web/validation/PasswordValidator.java`
- `web/src/main/java/com/matias/web/validation/CharactersWithWhiteSpaces.java`
- `web/src/main/java/com/matias/web/validation/CharactersWithWhiteSpacesValidator.java`

**DTOs:**
- `web/src/main/java/com/matias/web/dto/request/RegistroRequest.java`
- `web/src/main/java/com/matias/web/dto/request/LogueoRequest.java`
- `web/src/main/java/com/matias/web/dto/response/RegistroResponse.java`
- `web/src/main/java/com/matias/web/dto/response/TokenResponse.java`
- `web/src/main/java/com/matias/web/dto/internal/TokenInternal.java`

**Note:** `ProductDto.java` was preserved in the `web` module as it's not part of the auth migration scope.

## Build Verification

The project was successfully compiled with:
- **Java Version:** 21
- **Spring Boot Version:** 3.5.11
- **Build Tool:** Maven

**Build Command:**
```bash
mvn clean compile
```

**Build Result:** ✅ SUCCESS
- All modules compiled without errors
- All dependencies resolved correctly
- No compilation warnings related to the migration

## Benefits of This Migration

1. **Architectural Alignment:** DTOs are now in the correct layer according to hexagonal architecture
2. **Reusability:** Application-layer DTOs can be used by different adapters (not just web)
3. **Domain Independence:** Application logic is isolated from web framework specifics
4. **Testability:** DTOs can be tested independently of the web layer
5. **Maintainability:** Clear separation of concerns makes the codebase easier to maintain
6. **Scalability:** Easier to add new adapters (e.g., gRPC, GraphQL) in the future

## Next Steps

The following features mentioned in `AuthService` still need implementation:

1. **Email Verification Flow**
   - `verificarEmail(String token)` - Verify email with token
   - `reenviarEmailVerificacion(Object request, String ipOrigen)` - Resend verification email

2. **Password Reset Flow**
   - `solicitarReseteoPassword(Object request, String ipOrigen)` - Request password reset
   - `validarTokenReset(String token)` - Validate reset token
   - `resetearPassword(Object request)` - Reset password with token

**Recommendation:** Create specific DTOs for these operations following the same pattern established in this migration.

## References

- [Architecture Documentation](../architecture.md)
- [CRUD Concepts](../crud-concepts.md)
- [Hexagonal Architecture Pattern](https://alistair.cockburn.us/hexagonal-architecture/)

## Migration Checklist

- [x] Analyze DTOs in legacy project
- [x] Create custom validation annotations in `application` module
- [x] Create request DTOs in `application` module
- [x] Create response DTOs in `application` module
- [x] Create internal DTOs in `application` module
- [x] Update `AuthService` interface with new DTOs
- [x] Update `AuthController` imports
- [x] Add required dependencies to `application/pom.xml`
- [x] Remove old files from `web` module
- [x] Verify successful compilation
- [x] Document migration process

---

## Implementation Status Update

### AuthServiceImpl Implementation

**Location:** `application/src/main/java/com/matias/application/service/impl/AuthServiceImpl.java`

**Status:** ✅ COMPLETED

**Implemented Methods:**
- ✅ `register(RegistroRequest request)` - Complete user registration with password encryption
- ✅ `login(LogueoRequest request)` - Authentication with password verification and token generation
- ✅ `refresh(String refreshToken)` - Token refresh functionality
- ⚠️ `logout(String refreshToken)` - Basic structure (TODO: implement token blacklist)
- ⚠️ `verificarEmail(String token)` - Stub implementation (TODO: complete)
- ⚠️ `reenviarEmailVerificacion(...)` - Stub implementation (TODO: complete)
- ⚠️ `solicitarReseteoPassword(...)` - Stub implementation (TODO: complete)
- ⚠️ `validarTokenReset(String token)` - Stub implementation (TODO: complete)
- ⚠️ `resetearPassword(...)` - Stub implementation (TODO: complete)

**Dependencies:**
- `UsuarioRepositoryPort` - For database operations
- `TokenServicePort` - For JWT token generation
- `PasswordEncoder` - For password hashing (BCrypt)

### Infrastructure Updates

#### 1. UsuarioRepositoryPort Enhancement
**Location:** `domain/src/main/java/com/matias/domain/port/UsuarioRepositoryPort.java`

**Added Method:**
```java
Optional<Usuario> findByEmail(String email);
```

#### 2. UsuarioRepositoryAdapter Update
**Location:** `database/src/main/java/com/matias/database/adapter/UsuarioRepositoryAdapter.java`

**Implemented:**
```java
@Override
public Optional<Usuario> findByEmail(String email) {
    return jpaRepository.findByEmail(email)
            .map(usuarioMapper::toDomain);
}
```

#### 3. UsuarioJpaRepository Update
**Location:** `database/src/main/java/com/matias/database/repository/UsuarioJpaRepository.java`

**Added Query Methods:**
```java
Optional<UsuarioEntity> findByEmail(String email);
long countByActivo(boolean activo);
long countByEmailVerificado(boolean verificado);
long countByFechaCreacionAfter(Instant date);
```

#### 4. SecurityConfig Enhancement
**Location:** `security/src/main/java/com/matias/security/config/SecurityConfig.java`

**Added Bean:**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### Application Status

**Build Status:** ✅ SUCCESS  
**Runtime Status:** ✅ RUNNING  
**Port:** 8081  
**Database:** H2 (in-memory)

**Verification Results:**
- Application starts successfully
- All beans are properly configured and injected
- Security configuration is active
- JPA repositories are initialized
- Authentication endpoints are exposed

### Current API Endpoints

**Available:**
- ✅ `POST /v1/auth/register` - User registration (fully functional)
- ✅ `POST /v1/auth/login` - User login (fully functional)
- ✅ `POST /v1/auth/refresh` - Token refresh (fully functional)
- ⚠️ `POST /v1/auth/logout` - Logout (basic implementation, needs token blacklist)
- ⚠️ `POST /v1/auth/verify` - Email verification (endpoint exists, needs implementation)

### Remaining Tasks

**High Priority:**
1. Implement token blacklist for logout functionality
2. Implement email verification flow
3. Implement password reset flow
4. Add comprehensive error handling
5. Add integration tests

**Medium Priority:**
1. Configure JWT token expiration times
2. Add refresh token rotation
3. Implement rate limiting for sensitive endpoints
4. Add audit logging

**Low Priority:**
1. Add metrics and monitoring
2. Optimize database queries
3. Add caching layer
4. Implement account lockout after failed attempts

---

**Document Version:** 1.1  
**Last Updated:** March 15, 2026 (Updated with implementation details)  
**Author:** Migration Team
