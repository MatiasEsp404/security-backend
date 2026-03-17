# Migración: Documentación API con Swagger/OpenAPI

## 📋 Resumen

Se ha integrado **SpringDoc OpenAPI 3** (Swagger) para documentar automáticamente la API REST del sistema `security-backend`, proporcionando una interfaz interactiva para explorar y probar los endpoints.

**Fecha:** 17/03/2026  
**Estado:** ✅ Completado  
**Módulos afectados:** `web`, `application`, `app-root`

---

## 🎯 Objetivos

1. Proporcionar documentación automática y actualizada de la API
2. Facilitar el testing manual de endpoints mediante Swagger UI
3. Generar especificación OpenAPI estándar para integración con herramientas externas
4. Documentar schemas de DTOs con ejemplos claros
5. Incluir soporte para autenticación JWT en la documentación

---

## 📦 Dependencias Agregadas

### Módulo `web/pom.xml`

```xml
<!-- SpringDoc OpenAPI (Swagger UI) -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.7.0</version>
</dependency>
```

**Nota:** La dependencia ya existía en el módulo `web`, solo se verificó su presencia.

---

## 🔧 Archivos Creados/Modificados

### 1. Configuración de Swagger

**Archivo:** `web/src/main/java/com/matias/web/config/SwaggerConfig.java`

```java
@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Security Backend API")
                        .version("1.0.0")
                        .description("API REST para sistema de seguridad...")
                        .contact(new Contact()
                                .name("Matias")
                                .email("contact@example.com")
                                .url("https://github.com/MatiasEsp404/security-backend"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingrese el token JWT...")));
    }
}
```

**Características:**
- Configura información general de la API (título, versión, descripción)
- Define esquema de autenticación JWT Bearer
- Incluye información de contacto y licencia
- Aplica seguridad global a todos los endpoints

### 2. Propiedades de SpringDoc

**Archivo:** `app-root/src/main/resources/application.properties`

```properties
# SpringDoc OpenAPI Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.tryItOutEnabled=true
springdoc.swagger-ui.displayRequestDuration=true
springdoc.swagger-ui.persistAuthorization=true
```

**Configuración aplicada:**
- **api-docs.path:** Ruta de la especificación OpenAPI JSON
- **swagger-ui.path:** Ruta de la interfaz Swagger UI
- **operationsSorter:** Ordena operaciones por método HTTP
- **tagsSorter:** Ordena tags alfabéticamente
- **tryItOutEnabled:** Habilita el botón "Try it out"
- **displayRequestDuration:** Muestra tiempo de respuesta
- **persistAuthorization:** Mantiene el token JWT entre recargas

### 3. Anotaciones en Controllers

Todos los controllers fueron anotados con documentación Swagger:

#### AuthController
```java
@Tag(name = "Autenticación", description = "Endpoints para registro, login, verificación...")
@RestController
@RequestMapping("/v1/auth")
public class AuthController {
    
    @Operation(summary = "Registrar usuario", description = "Crea un usuario...")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registro exitoso"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Email ya registrado")
    })
    @PostMapping("/register")
    public ResponseEntity<RegistroResponse> register(@Valid @RequestBody RegistroRequest request) {
        // ...
    }
}
```

#### UsuarioController
```java
@Tag(name = "Usuario", description = "Endpoints para gestión de usuarios")
@RestController
@RequestMapping("/v1/usuario")
public class UsuarioController {
    
    @Operation(summary = "Obtener información del usuario actual", ...)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Información obtenida"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> obtenerInfoUsuarioActual(...) {
        // ...
    }
}
```

#### ProductController
```java
@Tag(name = "Productos", description = "Endpoints para gestión de productos (ejemplo CRUD)")
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @Operation(summary = "Crear producto", description = "Crea un nuevo producto...")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(...) {
        // ...
    }
}
```

#### SchedulerController
```java
@Tag(name = "Scheduler", description = "Ejecución manual de tareas programadas")
@RestController
@RequestMapping("/v1/scheduler")
public class SchedulerController {
    
    @Operation(summary = "Ejecutar limpieza de datos", ...)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Limpieza ejecutada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PostMapping("/cleanup")
    public ResponseEntity<Void> limpiarDatosObsoletos() {
        // ...
    }
}
```

### 4. Anotaciones en DTOs

Todos los DTOs principales fueron documentados con `@Schema`:

#### DTOs de Response

```java
@Schema(description = "Token JWT generado para el usuario")
public record TokenResponse(
    @Schema(description = "Access Token JWT", example = "eyJhbGci...")
    String accessToken
) {}

@Schema(description = "Información del usuario autenticado")
public record UsuarioResponse(
    @Schema(description = "ID del usuario", example = "1")
    Long id,
    
    @Schema(description = "Email del usuario", example = "usuario@example.com")
    String email,
    
    @Schema(description = "Roles asignados", example = "[\"USUARIO\"]")
    Set<Rol> roles,
    // ...
) {}
```

#### DTOs de Request

```java
@Schema(description = "Datos de registro de un nuevo usuario")
public record RegistroRequest(
    @Schema(description = "Email del usuario", example = "usuario@example.com")
    @NotBlank @Email
    String email,
    
    @Schema(description = "Contraseña", example = "MiPassword123!")
    @NotBlank @Size(min = 8, max = 32) @Password
    String password,
    // ...
) {}

@Schema(description = "Credenciales de acceso")
public record LogueoRequest(
    @Schema(description = "Email del usuario", example = "usuario@example.com")
    @Email @NotBlank
    String email,
    
    @Schema(description = "Contraseña", example = "MiPassword123!")
    @NotBlank
    String password
) {}
```

#### ProductDto

```java
@Schema(description = "Información de un producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    
    @Schema(description = "ID del producto", example = "1")
    private Long id;
    
    @Schema(description = "Nombre del producto", example = "Laptop")
    private String name;
    
    @Schema(description = "Descripción del producto", example = "Laptop de alta gama")
    private String description;
    
    @Schema(description = "Precio del producto", example = "999.99")
    private Double price;
}
```

---

## 🌐 URLs de Acceso

Una vez iniciada la aplicación:

- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **OpenAPI Spec (JSON):** http://localhost:8081/api-docs
- **OpenAPI Spec (YAML):** http://localhost:8081/api-docs.yaml

---

## 🔐 Uso de Autenticación JWT en Swagger

Para probar endpoints protegidos:

1. **Registrar/Login:** Usar endpoint `/v1/auth/login` para obtener token
2. **Autorizar:** Hacer clic en el botón "Authorize" (candado) en la parte superior
3. **Ingresar Token:** Pegar el `accessToken` en el campo "Value"
4. **Probar:** Los siguientes requests incluirán automáticamente el header `Authorization: Bearer <token>`

El token persiste entre recargas gracias a la configuración `persistAuthorization=true`.

---

## 📊 Organización de la Documentación

Los endpoints están organizados por tags:

1. **Autenticación:** Registro, login, verificación email, reset password
2. **Usuario:** Información del usuario actual
3. **Productos:** CRUD de productos (ejemplo)
4. **Scheduler:** Tareas programadas

Cada endpoint incluye:
- Summary y descripción detallada
- Parámetros con tipos y validaciones
- Schemas de request/response con ejemplos
- Códigos de respuesta HTTP con descripciones
- Información sobre autenticación requerida

---

## ✅ Validación

### Compilación
```bash
mvn clean compile
```

**Resultado:** ✅ BUILD SUCCESS

### Verificación Manual
1. Iniciar aplicación: `mvn spring-boot:run -pl app-root`
2. Acceder a: http://localhost:8081/swagger-ui.html
3. Verificar que todos los endpoints están documentados
4. Probar autenticación JWT
5. Ejecutar algunos endpoints de prueba

---

## 🏗️ Alineación con Arquitectura Hexagonal

La integración de Swagger respeta los principios de la arquitectura:

- ✅ **Configuración en capa externa (web):** `SwaggerConfig` está en el módulo `web`
- ✅ **Sin acoplamiento en domain:** El módulo `domain` no conoce Swagger
- ✅ **Anotaciones pragmáticas:** Anotaciones en DTOs (application) y controllers (web)
- ✅ **Sin modificación de lógica:** Solo documentación, sin cambios en reglas de negocio
- ✅ **Dependencias controladas:** SpringDoc solo en módulo `web`

---

## 📝 Notas Adicionales

### Beneficios de SpringDoc vs Springfox
- Compatible con Spring Boot 3.x y Jakarta EE
- No requiere anotaciones @EnableSwagger2
- Configuración más simple y moderna
- Mejor integración con Spring Security
- Soporte activo y actualizaciones regulares

### Personalización Futura
Se puede extender la configuración para:
- Agregar servidores múltiples (dev, staging, prod)
- Incluir términos de servicio
- Agregar más esquemas de seguridad (OAuth2, API Keys)
- Personalizar el tema de Swagger UI
- Generar documentación en múltiples idiomas

---

## 🔗 Referencias

- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI Specification 3.0](https://swagger.io/specification/)
- [Swagger UI](https://swagger.io/tools/swagger-ui/)

---

## 📌 Siguiente Paso

Ver `docs/migrations/pending-features-migration-plan.md` para planificar las siguientes funcionalidades a migrar.
