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

## Conclusión

La migración del módulo de administración se completó exitosamente, respetando los principios de arquitectura hexagonal definidos en el proyecto. Se implementó un sistema robusto de gestión de usuarios con capacidades avanzadas de búsqueda, filtrado, paginación y gestión de roles, todo documentado y protegido con seguridad basada en roles.

El código compila sin errores y está listo para pruebas funcionales y desarrollo de tests automatizados.
