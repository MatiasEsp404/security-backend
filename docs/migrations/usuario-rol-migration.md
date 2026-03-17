# Migración: Modelo UsuarioRol

## Fecha
17 de marzo de 2026

## Objetivo
Migrar la relación entre Usuario y Rol para incluir información adicional sobre la asignación del rol, específicamente la fecha de asignación. Esto permite rastrear cuándo se asignó cada rol a un usuario.

## Cambios Realizados

### 1. Domain Layer

#### Nuevo Modelo: `UsuarioRol`
- **Ubicación**: `domain/src/main/java/com/matias/domain/model/UsuarioRol.java`
- **Descripción**: Modelo de dominio que representa la asignación de un rol a un usuario
- **Campos**:
  - `id`: Identificador único
  - `usuarioId`: ID del usuario al que se asigna el rol
  - `rol`: El rol asignado (enum `Rol`)
  - `fechaAsignacion`: Timestamp de cuando se asignó el rol
- **Constructor simplificado**: Permite crear asignaciones con fecha automática

#### Modificación: `Usuario`
- **Ubicación**: `domain/src/main/java/com/matias/domain/model/Usuario.java`
- **Cambios**:
  - Campo `roles` (tipo `Set<Rol>`) reemplazado por `usuarioRoles` (tipo `Set<UsuarioRol>`)
  - Nuevo método `getRoles()`: Método helper que extrae solo los roles desde `usuarioRoles` para mantener compatibilidad con código existente

### 2. Database Layer

#### Nuevo Repository: `UsuarioRolJpaRepository`
- **Ubicación**: `database/src/main/java/com/matias/database/repository/UsuarioRolJpaRepository.java`
- **Descripción**: Repositorio JPA para gestionar las asignaciones de roles
- **Métodos**:
  - `findByUsuarioId(Integer usuarioId)`: Busca todos los roles de un usuario
  - `findByUsuarioIdAndRol(Integer usuarioId, Rol rol)`: Busca una asignación específica
  - `existsByUsuarioIdAndRol(Integer usuarioId, Rol rol)`: Verifica si existe una asignación
  - `deleteByUsuarioId(Integer usuarioId)`: Elimina todas las asignaciones de un usuario
  - `deleteByUsuarioIdAndRol(Integer usuarioId, Rol rol)`: Elimina una asignación específica

#### Nuevo Mapper: `UsuarioRolMapper`
- **Ubicación**: `database/src/main/java/com/matias/database/mapper/UsuarioRolMapper.java`
- **Descripción**: Mapper MapStruct para conversión entre `UsuarioRolEntity` y `UsuarioRol`
- **Métodos**:
  - `toDomain(UsuarioRolEntity entity)`: Convierte entidad a modelo de dominio
  - `toEntity(UsuarioRol domain)`: Convierte modelo de dominio a entidad

#### Modificación: `UsuarioMapper`
- **Ubicación**: `database/src/main/java/com/matias/database/mapper/UsuarioMapper.java`
- **Cambios**:
  - Usa `UsuarioRolMapper` como dependencia
  - Método `mapUsuarioRolesToDomain`: Mapea `Set<UsuarioRolEntity>` a `Set<UsuarioRol>` incluyendo toda la información de asignación

### 3. Application Layer

#### Modificación: `AuthServiceImpl`
- **Ubicación**: `application/src/main/java/com/matias/application/service/impl/AuthServiceImpl.java`
- **Cambios en `register()`**:
  - El usuario se crea sin roles inicialmente
  - Después de guardar el usuario (para obtener su ID), se crea un `UsuarioRol` con el rol `USUARIO` por defecto
  - Se establece en el usuario y se guarda nuevamente

```java
// Crear usuario sin roles primero
Usuario usuario = Usuario.builder()
        .email(emailNormalizado)
        .password(passwordEncoder.encode(request.password()))
        .nombre(nombreNormalizado)
        .apellido(apellidoNormalizado)
        .fechaCreacion(Instant.now())
        .activo(true)
        .emailVerificado(false)
        .build();

// Guardar usuario
Usuario usuarioGuardado = usuarioRepository.save(usuario);

// Asignar rol por defecto después de tener el ID del usuario
UsuarioRol usuarioRol = new UsuarioRol(
        usuarioGuardado.getId(),
        Rol.USUARIO
);
usuarioGuardado.setUsuarioRoles(Set.of(usuarioRol));
usuarioGuardado = usuarioRepository.save(usuarioGuardado);
```

## Compatibilidad Retroactiva

Para mantener la compatibilidad con el código existente que solo necesita los roles sin información de asignación:

- El modelo `Usuario` expone un método `getRoles()` que retorna `Set<Rol>`
- Este método extrae automáticamente los roles desde `usuarioRoles`
- El código que usaba `usuario.getRoles()` sigue funcionando sin cambios

## Beneficios

1. **Auditoría**: Se puede rastrear cuándo se asignó cada rol a cada usuario
2. **Flexibilidad**: Estructura preparada para futuros requisitos (ej: roles temporales, quien asignó el rol, etc.)
3. **Integridad**: Relación explícita entre usuario y rol con metadata
4. **Compatibilidad**: El código existente sigue funcionando gracias al método helper `getRoles()`

## Arquitectura Hexagonal

Esta migración respeta los principios de la arquitectura hexagonal:

- **Domain**: Modelos puros sin dependencias externas
- **Database**: Adaptadores que implementan las interfaces del dominio
- **Application**: Lógica de negocio que usa los puertos del dominio
- **Sin acoplamiento**: Cada capa solo conoce las capas internas, nunca las externas

## Estado

✅ **Completado** - La compilación es exitosa y todos los módulos están actualizados.

## Notas Técnicas

- La entidad `UsuarioRolEntity` ya existía en la base de datos con la estructura necesaria
- MapStruct genera automáticamente las implementaciones de los mappers en tiempo de compilación
- La fecha de asignación se establece automáticamente al momento de crear una nueva asignación
