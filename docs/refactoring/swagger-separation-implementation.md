# Implementación: Separación de Swagger de la Capa de Aplicación

## 📋 Resumen

Este documento detalla la corrección de la **Infracción GRAVE #2** identificada en el análisis de deuda arquitectónica: "Fuga de Swagger/OpenAPI al modelo de Aplicación".

**Fecha de implementación:** 19/03/2026  
**Estado:** ✅ Completado y compilado exitosamente

---

## 🎯 Problema Identificado

### Descripción de la Infracción

El módulo `application` contenía DTOs anotados con `@Schema` de `io.swagger.v3.oas.annotations`, lo que representa conocimiento de presentación API filtrándose hacia la capa de casos de uso. Esto viola los principios de la Arquitectura Hexagonal al acoplar la lógica de aplicación con detalles de documentación web.

### Archivos Afectados (Antes)

- `application/pom.xml` - Dependencia de springdoc-openapi
- `application/src/main/java/com/matias/application/dto/request/`
  - `RegistroRequest.java`
  - `LogueoRequest.java`
  - `EmailRequest.java`
  - `ResetPasswordRequest.java`
- `application/src/main/java/com/matias/application/dto/response/`
  - `UsuarioResponse.java`
  - `TokenResponse.java`
  - `RegistroResponse.java`
  - `ErrorResponse.java`
- `application/src/main/java/com/matias/application/dto/internal/`
  - `TokenInternal.java`

---

## ✅ Solución Implementada

### Estrategia Adoptada

**Patrón Anti-Corruption Layer (ACL):** Crear DTOs específicos para la capa Web que actúen como contratos de API, manteniendo los DTOs de Application limpios de anotaciones de infraestructura.

### Cambios Realizados

#### 1. Limpieza del Módulo Application

**Archivo:** `application/pom.xml`

✅ **Eliminado:**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
</dependency>
```

✅ **DTOs Limpiados:** Removidas todas las anotaciones `@Schema` de:
- Todos los DTOs de request
- Todos los DTOs de response
- DTOs internos (`TokenInternal`, `ErrorResponse`)

**Resultado:** El módulo `application` ahora es completamente agnóstico de la tecnología de documentación API.

#### 2. Creación de DTOs Web con Anotaciones Swagger

**Ubicación:** `web/src/main/java/com/matias/web/dto/`

✅ **Nuevos DTOs de Request:**
- `RegistroWebRequest.java` - Con anotaciones Swagger completas
- `LogueoWebRequest.java` - Con anotaciones Swagger completas
- `EmailWebRequest.java` - Con anotaciones Swagger completas

✅ **Nuevos DTOs de Response:**
- `UsuarioWebResponse.java` - Con anotaciones Swagger completas
- `TokenWebResponse.java` - Con anotaciones Swagger completas
- `RegistroWebResponse.java` - Con anotaciones Swagger completas

**Características:**
- Documentación OpenAPI rica y detallada
- Ejemplos específicos para cada campo
- Descripciones claras orientadas al consumidor de la API
- Completamente desacoplados de la capa de aplicación

#### 3. Creación de Mappers

**Ubicación:** `web/src/main/java/com/matias/web/mapper/`

✅ **AuthWebMapper.java**
- Convierte entre DTOs de Web ↔ Application
- Métodos bidireccionales para requests y responses
- Mapeo explícito sin dependencias mágicas

✅ **UsuarioWebMapper.java**
- Mapea `UsuarioResponse` (app) → `UsuarioWebResponse` (web)
- Transformación de datos limpia y mantenible

#### 4. Actualización de Controladores

✅ **AuthController.java**
- Recibe DTOs de Web (`RegistroWebRequest`, `LogueoWebRequest`)
- Mapea a DTOs de Application usando `AuthWebMapper`
- Invoca servicios de aplicación
- Convierte respuestas de Application a Web
- Retorna DTOs de Web al cliente

✅ **UsuarioController.java**
- Recibe respuestas de Application
- Mapea a DTOs de Web usando `UsuarioWebMapper`
- Retorna DTOs de Web documentados con Swagger

---

## 🏗️ Arquitectura Resultante

### Flujo de Datos

```
Cliente HTTP
    ↓
[AuthController] - Recibe RegistroWebRequest (con @Schema)
    ↓
[AuthWebMapper] - Convierte a RegistroRequest (limpio)
    ↓
[AuthService] - Procesa con DTO de Application
    ↓
[AuthService] - Retorna RegistroResponse (limpio)
    ↓
[AuthWebMapper] - Convierte a RegistroWebResponse (con @Schema)
    ↓
[AuthController] - Retorna al cliente
    ↓
Cliente HTTP + Swagger UI (documentado)
```

### Beneficios de esta Arquitectura

1. **Separación de Concerns:**
   - Application: Lógica de negocio pura
   - Web: Contratos de API y documentación

2. **Inversión de Dependencias Respetada:**
   - Application no conoce detalles de Web
   - Web depende de Application (correcto)

3. **Flexibilidad:**
   - Cambiar documentación API sin tocar casos de uso
   - Agregar nuevos endpoints sin modificar Application
   - Versionado de API simplificado

4. **Testabilidad:**
   - Casos de uso testeables sin infraestructura web
   - DTOs de negocio independientes de frameworks

5. **Mantenibilidad:**
   - Responsabilidades claras
   - Cambios localizados
   - Código más limpio y comprensible

---

## 📊 Estructura de Archivos

### Módulo Application (Limpio)

```
application/
├── pom.xml (sin springdoc)
└── src/main/java/com/matias/application/
    ├── dto/
    │   ├── request/
    │   │   ├── RegistroRequest.java (sin @Schema)
    │   │   ├── LogueoRequest.java (sin @Schema)
    │   │   └── EmailRequest.java (sin @Schema)
    │   ├── response/
    │   │   ├── UsuarioResponse.java (sin @Schema)
    │   │   ├── TokenResponse.java (sin @Schema)
    │   │   └── RegistroResponse.java (sin @Schema)
    │   └── internal/
    │       └── TokenInternal.java (sin @Hidden)
    └── service/
        └── AuthService.java
```

### Módulo Web (Con Swagger)

```
web/
├── pom.xml (con springdoc)
└── src/main/java/com/matias/web/
    ├── dto/
    │   ├── request/
    │   │   ├── RegistroWebRequest.java (con @Schema)
    │   │   ├── LogueoWebRequest.java (con @Schema)
    │   │   └── EmailWebRequest.java (con @Schema)
    │   └── response/
    │       ├── UsuarioWebResponse.java (con @Schema)
    │       ├── TokenWebResponse.java (con @Schema)
    │       └── RegistroWebResponse.java (con @Schema)
    ├── mapper/
    │   ├── AuthWebMapper.java
    │   └── UsuarioWebMapper.java
    └── controller/
        ├── AuthController.java
        └── UsuarioController.java
```

---

## 🧪 Verificación

### Compilación

✅ **Resultado:** `BUILD SUCCESS`

```bash
[INFO] BUILD SUCCESS
[INFO] Total time: 29.571 s
[INFO] Reactor Summary for security-backend 0.0.1-SNAPSHOT:
[INFO] security-backend ................................... SUCCESS
[INFO] domain ............................................. SUCCESS
[INFO] application ........................................ SUCCESS
[INFO] database ........................................... SUCCESS
[INFO] security ........................................... SUCCESS
[INFO] email .............................................. SUCCESS
[INFO] web ................................................ SUCCESS
[INFO] app-root ........................................... SUCCESS
```

### Dependencias

✅ **Módulo Application:** Sin dependencias de Swagger
✅ **Módulo Web:** Mantiene springdoc-openapi para documentación

---

## 📝 Próximos Pasos Recomendados

Aunque esta infracción está corregida, considera estos pasos adicionales:

1. **Testing:** Agregar tests unitarios para los mappers
2. **Validación:** Verificar que Swagger UI funcione correctamente
3. **Documentación:** Actualizar README con la nueva estructura
4. **Code Review:** Validar con el equipo el enfoque adoptado
5. **Abordar otras infracciones:** Continuar con las infracciones restantes del análisis

---

## 🎓 Lecciones Aprendidas

1. **Anti-Corruption Layer es efectivo:** Protege capas internas de detalles externos
2. **El mapeo explícito tiene valor:** Aunque agrega código, aumenta claridad
3. **La inversión de dependencias es crucial:** Mantiene el núcleo limpio
4. **Pequeños pasos iterativos:** Facilita validación y reduce errores

---

## 📚 Referencias

- Documento de análisis: `docs/architectural-debt.md`
- Arquitectura Hexagonal: `docs/architecture.md`
- Patrón Anti-Corruption Layer (DDD)
- Clean Architecture - Robert C. Martin

---

**Implementado por:** Axet Plugin  
**Validado:** Compilación exitosa (BUILD SUCCESS)  
**Estado de la infracción:** ✅ **RESUELTA**
