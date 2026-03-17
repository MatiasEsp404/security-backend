# Migración: Data Normalizer

**Fecha**: 2026-03-17  
**Estado**: ✅ Completado

## Descripción

Migración de la utilidad `DataNormalizer` desde `seguridad-back` para normalizar datos de entrada del usuario (emails, nombres y apellidos). Esta utilidad se usa para garantizar consistencia en los datos almacenados en la base de datos.

## Componentes Migrados

### 1. Módulo Domain

#### **Archivo**: `domain/src/main/java/com/matias/domain/util/DataNormalizer.java`

**Clase Utilitaria Estática** para normalización de datos:

```java
public final class DataNormalizer {
    
    // Normalización de emails
    public static String normalizeEmail(String email)
    
    // Normalización de nombres propios
    public static String normalizeProperName(String name)
    
    // Normalización de apellidos
    public static String normalizeLastName(String lastName)
}
```

**Características**:
- Clase `final` con constructor privado (utility class pattern)
- Métodos estáticos para facilitar su uso
- Validación de entrada null
- Trim de espacios en blanco
- Conversión a lowercase para emails
- Capitalización apropiada para nombres y apellidos

### 2. Integración en Application Layer

#### **Archivo**: `application/src/main/java/com/matias/application/service/impl/AuthServiceImpl.java`

**Métodos Modificados**:

1. **`register()`**: Normaliza email, nombre y apellido antes de crear el usuario
2. **`login()`**: Normaliza email antes de buscar el usuario
3. **`reenviarEmailVerificacion()`**: Normaliza email antes de buscar el usuario
4. **`solicitarResetPassword()`**: Normaliza email antes de validar y buscar el usuario

## Arquitectura Hexagonal

### Ubicación en el Módulo Domain

La clase `DataNormalizer` se ubicó en el módulo **domain** bajo el paquete `util` porque:

1. **Lógica de Negocio Pura**: La normalización de datos es una regla de negocio que define cómo deben representarse los datos en el dominio
2. **Sin Dependencias Externas**: Solo usa Java estándar (String manipulation)
3. **Reutilizable**: Puede ser usada por cualquier capa (application, adapters)
4. **Inmutable**: Clase utilitaria sin estado

### Flujo de Datos

```
Web Layer (DTO) → Application Layer (Normalización) → Domain (Entidades) → Database
```

**Ejemplo**:
```
Input:  "  John.DOE@EXAMPLE.COM  "
↓ normalizeEmail()
Output: "john.doe@example.com"
```

## Beneficios

1. **Consistencia de Datos**: Todos los emails se almacenan en minúsculas, nombres/apellidos capitalizados
2. **Búsquedas Confiables**: Los emails normalizados permiten búsquedas case-insensitive
3. **Prevención de Duplicados**: Evita que "user@email.com" y "USER@EMAIL.COM" se consideren diferentes
4. **Presentación Profesional**: Nombres y apellidos con capitalización correcta
5. **Centralización**: Lógica de normalización en un solo lugar

## Casos de Uso

### Registro de Usuario
```java
// Input del usuario
email: "  Maria@EXAMPLE.com  "
nombre: "  maría josé  "
apellido: "  garcía lópez  "

// Después de normalización
email: "maria@example.com"
nombre: "María José"
apellido: "García López"
```

### Login
```java
// El usuario puede escribir su email de cualquier forma
Input: "USER@EXAMPLE.COM"
↓ normalización
Búsqueda en DB: "user@example.com"
```

## Pruebas Manuales Sugeridas

1. **Registro con email en mayúsculas**: Verificar que se almacene en minúsculas
2. **Login con diferentes capitalizaciones**: Verificar que funcione independientemente de mayúsculas/minúsculas
3. **Nombres con múltiples palabras**: Verificar capitalización correcta (ej: "maría josé" → "María José")
4. **Espacios en blanco**: Verificar que se eliminen espacios al inicio y final

## Compatibilidad con Datos Existentes

**IMPORTANTE**: Si ya existen usuarios en la base de datos con emails sin normalizar:

### Opción 1: Script de Migración de Datos (Recomendado)
```sql
-- Normalizar emails existentes
UPDATE usuarios 
SET email = LOWER(TRIM(email));

-- Normalizar nombres (requiere función personalizada o hacerlo manualmente)
```

### Opción 2: Normalización Gradual
Los datos existentes se normalizarán cuando:
- El usuario haga login (el email se normalizará en la búsqueda)
- Se actualice su información

**Nota**: Para búsquedas, el sistema ahora normaliza el email de búsqueda, por lo que encontrará usuarios existentes independientemente de la capitalización original.

## Diferencias con Implementación Original

La implementación es prácticamente idéntica a `seguridad-back`, con:

1. **Mismo comportamiento**: Lógica de normalización sin cambios
2. **Ubicación arquitectónica**: Ahora en `domain/util` siguiendo arquitectura hexagonal
3. **Package**: `com.matias.domain.util` (antes `com.springboot.app.util`)

## Notas Técnicas

- **Thread-safe**: Clase utilitaria sin estado, segura para uso concurrente
- **Performance**: Operaciones O(n) donde n es la longitud del string
- **Memory**: No mantiene estado, garbage collector friendly
- **Java Version**: Compatible con Java 21

## Próximos Pasos

Considerar integrar DataNormalizer en:

1. **AdminService**: Al actualizar información de usuarios
2. **Validadores Custom**: Para validación de datos en DTOs
3. **Filtros de Búsqueda**: En especificaciones y queries

## Referencias

- Archivo Original: `seguridad-back/src/main/java/com/springboot/app/util/DataNormalizer.java`
- Arquitectura: `docs/architecture.md`
