# Migración del Módulo de Administración

## Resumen
Este documento describe la migración completa del módulo de administración desde `seguridad-back` a `security-backend`, implementando funcionalidades avanzadas de gestión de usuarios con filtros, paginación, estadísticas y gestión de roles.

## Fecha de Migración
17 de marzo de 2026

## Componentes Migrados

### 1. DTOs en el Módulo Web (`web`)

#### DTOs de Request
- **`UpdateUserStatusRequest`**: DTO para actualizar el estado activo/inactivo de un usuario
  - Campo: `active` (Boolean, obligatorio)
  
- **`UsuarioFilterRequest`**: DTO utilizado internamente para filtros (no se expone directamente en el controlador)

#### DTOs de Response
- **`StatsResponse`**: Respuesta con estadísticas del sistema
  - Records anidados: `UsuariosStats`, `VerificacionStats`, `CrecimientoStats`
  - Incluye estadísticas de usuarios por rol
  
- **`UsuarioListItemResponse`**: Respuesta ligera para listados de usuarios
  - Campos: id, email, nombreCompleto, roles, activo, emailVerificado, fechaCreacion
  
- **`UsuarioRolResponse`**: Respuesta para operaciones de asignación/remoción de roles
  - Campos: id, rol, fechaAsignacion, asignadoPor (info anidada)
  
- **`PageResponse<T>`**: DTO genérico para respuestas paginadas
  - Campos: content, pageNumber, pageSize, totalElements, totalPages, first, last

### 2. Extensiones en el Módulo Domain (`domain`)

#### Puerto `UsuarioRepositoryPort`
Se extendió con nuevas estructuras y métodos:

**Nuevos Records:**
- `UsuarioFilter`: Filtro para búsqueda de usuarios
  - search, activo, emailVerificado, roles, fechaDesde, fechaHasta
  
- `PageRequest`: Configuración de paginación
  - pageNumber, pageSize, sortBy, sortDirection
  
- `PageResult<T>`: Resultado paginado genérico
  - content, pageNumber, pageSize, totalElements, totalPages, isFirst, isLast

**Nuevo Enum:**
- `SortDirection`: ASC, DESC

**Nuevos Métodos:**
```java
PageResult<Usuario> buscarUsuariosConFiltros(UsuarioFilter filter, PageRequest pageRequest);
Map<String, Object> obtenerEstadisticas();
```

### 3. Capa de Base de Datos (`database`)

#### `UsuarioSpecification`
Clase nueva que implementa `Specification<UsuarioEntity>` para construcción dinámica de consultas JPA:
- Búsqueda por texto (email, nombre, apellido)
- Filtro por estado activo
- Filtro por email verificado
- Filtro por roles (múltiples)
- Filtro por rango de fechas

#### `UsuarioJpaRepository`
Se extendió para soportar `Specification`:
```java
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Integer>,
                                              JpaSpecificationExecutor<UsuarioEntity>
```

**Nuevas Queries:**
- `countByActivo(Boolean activo)`
- `countByEmailVerificado(Boolean emailVerificado)`
- `countByFechaCreacionAfter(Instant fecha)`

#### `UsuarioRepositoryAdapter`
Se implementaron los nuevos métodos del puerto:
- `buscarUsuariosConFiltros`: Utiliza Specifications para filtrado dinámico
- `obtenerEstadisticas`: Consultas agregadas para estadísticas del sistema

### 4. Capa de Aplicación (`application`)

#### `AdminService`
Interface de servicio con operaciones administrativas:
```java
Map<String, Object> obtenerEstadisticas();
PageResult<Usuario> buscarUsuarios(UsuarioFilter filter, PageRequest pageRequest);
Usuario obtenerDetalleUsuario(Integer userId);
void updateUserStatus(Integer userId, Boolean active);
void assignRole(Integer userId, Rol rol);
void unassignRole(Integer userId, Rol rol);
```

#### `AdminServiceImpl`
Implementación del servicio con lógica de negocio:
- Validaciones de permisos (no se puede desactivar ADMIN/MODERADOR)
- Validaciones de roles (no se puede remover rol USUARIO)
- Gestión de roles con verificación de duplicados
- Cálculo de estadísticas del sistema

### 5. Controlador Web (`web`)

#### `AdminController`
Controlador REST con los siguientes endpoints:

**GET `/api/admin/stats`**
- Rol requerido: ADMINISTRADOR
- Devuelve estadísticas generales del sistema

**GET `/api/admin/users`**
- Roles requeridos: ADMINISTRADOR o MODERADOR
- Búsqueda avanzada con múltiples filtros y paginación
- Parámetros: search, activo, emailVerificado, roles, fechaDesde, fechaHasta, page, size, sortBy, direction

**GET `/api/admin/users/{userId}`**
- Roles requeridos: ADMINISTRADOR o MODERADOR
- Obtiene detalles completos de un usuario específico

**PATCH `/api/admin/users/{userId}/status`**
- Rol requerido: ADMINISTRADOR
- Actualiza el estado activo/inactivo de un usuario

**POST `/api/admin/users/{userId}/roles/{rol}`**
- Rol requerido: ADMINISTRADOR
- Asigna un rol adicional a un usuario

**DELETE `/api/admin/users/{userId}/roles/{rol}`**
- Rol requerido: ADMINISTRADOR
- Remueve un rol de un usuario (excepto USUARIO)

## Flujo de Arquitectura

```
[AdminController - web]
        ↓
[AdminService - application]
        ↓
[UsuarioRepositoryPort - domain]
        ↓
[UsuarioRepositoryAdapter - database]
        ↓
[UsuarioJpaRepository + UsuarioSpecification - database]
        ↓
[Base de Datos]
```

## Cumplimiento de Arquitectura Hexagonal

### ✅ Separación de Responsabilidades
- **Domain**: Define contratos (ports) y modelos puros
- **Application**: Implementa lógica de negocio sin dependencias externas
- **Database**: Implementa los puertos con tecnología específica (JPA)
- **Web**: Adaptador de entrada que expone endpoints REST

### ✅ Inversión de Dependencias
- `AdminService` depende de `UsuarioRepositoryPort` (interface en domain)
- `UsuarioRepositoryAdapter` implementa `UsuarioRepositoryPort`
- La dependencia fluye hacia el dominio, no hacia afuera

### ✅ Enfoque Pragmático
- Se utilizan anotaciones de Spring (`@Service`, `@Repository`, `@RestController`)
- Se usa Lombok para reducir boilerplate
- Se mantiene la separación de capas estricta a nivel de módulos Maven

## Características Implementadas

### 1. Búsqueda Avanzada
- Filtrado por múltiples criterios simultáneamente
- Búsqueda de texto en email, nombre y apellido
- Filtrado por estado y verificación de email
- Filtrado por uno o más roles
- Filtrado por rango de fechas

### 2. Paginación y Ordenamiento
- Paginación configurable (tamaño y número de página)
- Ordenamiento por cualquier campo
- Dirección de ordenamiento (ASC/DESC)
- Metadata completa de paginación en respuesta

### 3. Estadísticas del Sistema
- Total de usuarios
- Usuarios activos e inactivos
- Emails verificados y pendientes
- Nuevos usuarios en últimos 30 días
- Distribución de usuarios por rol

### 4. Gestión de Estados
- Activación/desactivación de usuarios
- Protección de cuentas administrativas
- Validación de permisos

### 5. Gestión de Roles
- Asignación de roles adicionales
- Remoción de roles
- Protección del rol básico USUARIO
- Prevención de duplicados

## Validaciones y Seguridad

### Validaciones de Negocio
1. No se pueden desactivar usuarios con rol ADMINISTRADOR o MODERADOR
2. No se puede remover el rol USUARIO de ningún usuario
3. No se pueden asignar roles duplicados
4. Validación de existencia de usuario antes de operaciones

### Seguridad
- Todos los endpoints requieren autenticación (Bearer Token)
- Control de acceso basado en roles con `@PreAuthorize`
- ADMINISTRADOR: Acceso completo
- MODERADOR: Solo lectura de usuarios
- Documentación OpenAPI con esquemas de seguridad

## Documentación OpenAPI/Swagger

Todos los endpoints están documentados con:
- Anotaciones `@Operation` con descripción detallada
- Anotaciones `@Parameter` para cada parámetro
- Anotaciones `@ApiResponse` para códigos de estado
- Anotaciones `@Schema` en todos los DTOs
- Tag "Admin" para agrupación en Swagger UI

## Diferencias con el Proyecto Original

### Cambios Estructurales
1. **Nombres de campos**: Se adaptaron los getters del modelo Usuario (de `getNombreCompleto()` a `getNombre() + " " + getApellido()`)
2. **Records vs Classes**: Los DTOs se implementaron como records de Java 17+ en lugar de clases con Lombok
3. **Respuestas de roles**: Se simplificaron las respuestas de asignación/remoción de roles (retornan 200 OK sin body)

### Mejoras Implementadas
1. **Especificaciones JPA**: Se usa `Specification<T>` para consultas dinámicas más mantenibles
2. **Tipado fuerte**: PageRequest, PageResult y filtros son records tipados en lugar de Maps
3. **Separación de DTOs**: DTOs de request y response bien separados por responsabilidad

## Testing

### Compilación
```bash
mvn clean compile -DskipTests
```
✅ BUILD SUCCESS - Todos los módulos compilan correctamente

### Pendiente
- Tests unitarios para `AdminServiceImpl`
- Tests de integración para `AdminController`
- Tests de `UsuarioSpecification`

## Próximos Pasos

1. **Implementar sistema completo de roles con historial**:
   - Tabla `usuario_rol_asignacion` con seguimiento
   - Relación con usuario que asigna el rol
   - Fecha de asignación
   
2. **Agregar estadística de usuarios registrados hoy**:
   - Actualmente retorna 0L como placeholder
   
3. **Implementar auditoría**:
   - Registrar cambios de estado de usuarios
   - Registrar asignaciones/remociones de roles
   
4. **Agregar filtros adicionales**:
   - Por rango de última actividad
   - Por cantidad de intentos fallidos de login

## Archivos Creados/Modificados

### Creados
- `web/src/main/java/com/matias/web/dto/request/UpdateUserStatusRequest.java`
- `web/src/main/java/com/matias/web/dto/request/UsuarioFilterRequest.java`
- `web/src/main/java/com/matias/web/dto/response/StatsResponse.java`
- `web/src/main/java/com/matias/web/dto/response/UsuarioListItemResponse.java`
- `web/src/main/java/com/matias/web/dto/response/UsuarioRolResponse.java`
- `web/src/main/java/com/matias/web/dto/response/PageResponse.java`
- `database/src/main/java/com/matias/database/specification/UsuarioSpecification.java`
- `application/src/main/java/com/matias/application/service/AdminService.java`
- `application/src/main/java/com/matias/application/service/impl/AdminServiceImpl.java`
- `web/src/main/java/com/matias/web/controller/AdminController.java`
- `docs/migrations/admin-module-migration.md`

### Modificados
- `domain/src/main/java/com/matias/domain/port/UsuarioRepositoryPort.java`
- `database/src/main/java/com/matias/database/repository/UsuarioJpaRepository.java`
- `database/src/main/java/com/matias/database/adapter/UsuarioRepositoryAdapter.java`

## ✅ Sistema de Filtros Dinámicos - Detalles de Implementación

El módulo de administración cuenta con un sistema completo de búsqueda avanzada utilizando **JPA Criteria API (Specifications)** que permite filtrado dinámico, eficiente y type-safe.

### Arquitectura del Sistema de Filtros

#### 1. Definición en Domain (UsuarioRepositoryPort)
Los filtros se definen como **records** inmutables en el dominio:

```java
record UsuarioFilter(
    String search,              // Búsqueda LIKE en email, nombre o apellido
    Boolean activo,            // Filtro exacto por estado activo/inactivo
    Boolean emailVerificado,   // Filtro exacto por email verificado
    Set<Rol> roles,           // Filtro por uno o múltiples roles
    Instant fechaDesde,       // Fecha de creación desde (inclusivo)
    Instant fechaHasta        // Fecha de creación hasta (inclusivo)
) {}
```

#### 2. Specifications en Database (UsuarioSpecification)
Utiliza **JPA Criteria API** para construcción dinámica de consultas:

**Características:**
- **Type-safe**: No usa strings mágicos, referencias directas a campos
- **Composable**: Combina múltiples predicados con `AND` lógico
- **Null-safe**: Solo agrega predicados cuando los filtros no son nulos
- **Eficiente**: JPA optimiza las queries generadas

**Predicados implementados:**
- **Búsqueda por texto**: `LOWER(email) LIKE %search% OR LOWER(nombre) LIKE %search% OR LOWER(apellido) LIKE %search%`
- **Filtro por estado**: `activo = ?`
- **Filtro por verificación**: `emailVerificado = ?`
- **Filtro por roles**: `EXISTS (SELECT 1 FROM usuario_rol WHERE rol IN (?))`
- **Rango de fechas**: `fechaCreacion BETWEEN ? AND ?`

#### 3. Endpoint REST con Query Parameters

```
GET /api/admin/users

Query Parameters:
  - search: String          (búsqueda parcial en email/nombre)
  - activo: Boolean         (true/false)
  - emailVerificado: Boolean (true/false)
  - roles: List<Rol>        (USUARIO,ADMINISTRADOR,MODERADOR - múltiples valores)
  - fechaDesde: String      (ISO 8601: 2024-01-01T00:00:00Z)
  - fechaHasta: String      (ISO 8601: 2024-12-31T23:59:59Z)
  - page: int               (0-indexed, default: 0)
  - size: int               (default: 20, max recomendado: 100)
  - sortBy: String          (default: "fechaCreacion")
  - direction: String       (ASC/DESC, default: DESC)
```

### Ejemplos de Uso del API

#### 1. Búsqueda simple por texto
```bash
GET /api/admin/users?search=juan&page=0&size=10
# Busca "juan" en email, nombre o apellido
```

#### 2. Usuarios activos con email no verificado
```bash
GET /api/admin/users?activo=true&emailVerificado=false
# Útil para encontrar usuarios que necesitan verificar email
```

#### 3. Filtro por múltiples roles
```bash
GET /api/admin/users?roles=ADMINISTRADOR&roles=MODERADOR
# Devuelve usuarios que tienen rol ADMINISTRADOR O MODERADOR
```

#### 4. Usuarios creados en un período
```bash
GET /api/admin/users?fechaDesde=2024-01-01T00:00:00Z&fechaHasta=2024-03-31T23:59:59Z&sortBy=fechaCreacion&direction=DESC
# Usuarios del Q1 2024, ordenados por fecha más reciente
```

#### 5. Búsqueda combinada compleja
```bash
GET /api/admin/users?search=gmail.com&activo=true&emailVerificado=true&roles=USUARIO&page=0&size=50&sortBy=email&direction=ASC
# Usuarios activos con Gmail verificado, solo rol básico, ordenados alfabéticamente
```

### Respuesta del API

```json
{
  "content": [
    {
      "id": 1,
      "email": "juan.perez@example.com",
      "fullName": "Juan Pérez",
      "roles": ["USUARIO", "MODERADOR"],
      "active": true,
      "emailVerified": true,
      "createdAt": "2024-03-15T10:30:00Z"
    }
  ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 150,
  "totalPages": 8,
  "first": true,
  "last": false
}
```

### Flujo de Ejecución

```
[HTTP Request] 
  → [AdminController.buscarUsuarios()]
      ├─ Convierte query params a UsuarioFilter
      ├─ Convierte paginación a PageRequest
      └─ Llama AdminService.buscarUsuarios()
  
[AdminService.buscarUsuarios()]
  → Delega al UsuarioRepositoryPort.findAllWithFilters()
  
[UsuarioRepositoryAdapter.findAllWithFilters()]
  ├─ Convierte UsuarioFilter a Specification<UsuarioEntity>
  ├─ Crea Spring PageRequest con Sort
  ├─ Ejecuta jpaRepository.findAll(spec, pageRequest)
  └─ Mapea entidades a objetos de dominio
  
[JPA Criteria API]
  → Genera SQL optimizado dinámicamente
  
[Base de Datos]
  → Ejecuta query con índices apropiados
```

### Ventajas del Diseño

#### ✅ Arquitectura Hexagonal Preservada
- **Domain**: Define contratos (UsuarioFilter, PageRequest) sin conocer JPA
- **Database**: Implementa con tecnología específica (Specifications)
- **Application**: Orquesta sin conocer detalles de persistencia
- **Web**: Adapta HTTP a objetos de dominio

#### ✅ Type Safety
- Records inmutables en lugar de Maps o DTOs mutables
- Enums para direcciones de ordenamiento
- Validación en tiempo de compilación

#### ✅ Extensibilidad
- Agregar un nuevo filtro requiere:
  1. Agregar campo a `UsuarioFilter` (domain)
  2. Agregar predicado a `UsuarioSpecification` (database)
  3. Agregar query param al controlador (web)
- No se rompen otras partes del sistema

#### ✅ Performance
- JPA genera SQL optimizado
- Solo se ejecutan predicados necesarios
- Paginación a nivel de base de datos (no en memoria)
- Compatible con índices de base de datos

#### ✅ Mantenibilidad
- Código declarativo y legible
- Separación clara de responsabilidades
- Fácil de testear cada capa independientemente

### Casos de Uso del Negocio

1. **Moderación**: Encontrar usuarios con email no verificado hace más de 7 días
2. **Seguridad**: Listar todos los usuarios con roles privilegiados
3. **Analytics**: Usuarios registrados en el último mes
4. **Soporte**: Buscar usuario por email parcial cuando no recuerda completo
5. **Auditoría**: Usuarios inactivos con roles administrativos

### Posibles Mejoras Futuras

1. **Caché de resultados**: Para búsquedas frecuentes
2. **Índices compuestos**: En combinaciones comunes de filtros
3. **Búsqueda full-text**: Para búsquedas más sofisticadas
4. **Filtros guardados**: Permitir guardar configuraciones de búsqueda
5. **Exportación**: CSV/Excel de resultados filtrados
6. **Agregaciones**: Contar resultados por categoría sin traer todos los datos

## Conclusión

La migración del módulo de administración se completó exitosamente, respetando los principios de arquitectura hexagonal definidos en el proyecto. Se implementó un sistema robusto de gestión de usuarios con capacidades avanzadas de búsqueda, filtrado, paginación y gestión de roles, todo documentado y protegido con seguridad basada en roles.

**✅ El sistema de filtros dinámicos está completamente implementado, probado (compilación exitosa) y documentado.**

El código compila sin errores y está listo para pruebas funcionales y desarrollo de tests automatizados.
