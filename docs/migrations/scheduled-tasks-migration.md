# Migración de Tareas Programadas (Scheduled Tasks)

## Resumen

Se ha migrado exitosamente la funcionalidad de tareas programadas desde el proyecto `seguridad-back` a `security-backend`, siguiendo los principios de arquitectura hexagonal definidos en el proyecto.

## Componentes Migrados

### 1. **LimpiezaDatosScheduler** (application/scheduled)

**Ubicación:** `application/src/main/java/com/matias/application/scheduled/LimpiezaDatosScheduler.java`

Clase que ejecuta automáticamente la limpieza de datos obsoletos:
- **Frecuencia:** Todos los días a las 2:00 AM
- **Cron Expression:** `0 0 2 * * *`
- **Función:** Elimina tokens expirados, intentos de verificación antiguos y datos temporales obsoletos

```java
@Scheduled(cron = "0 0 2 * * *")
public void limpiarDatosObsoletos()
```

### 2. **SchedulerController** (web/controller)

**Ubicación:** `web/src/main/java/com/matias/web/controller/SchedulerController.java`

Controlador REST que permite la ejecución manual de tareas programadas:
- **Endpoint:** `POST /v1/scheduler/cleanup`
- **Respuesta:** 204 No Content
- **Uso:** Permite ejecutar la limpieza de datos bajo demanda sin esperar a la ejecución automática

### 3. **Método limpiarDatosObsoletos()** (application/service)

**Ubicación:** 
- Interface: `application/src/main/java/com/matias/application/service/AuthService.java`
- Implementación: `application/src/main/java/com/matias/application/service/impl/AuthServiceImpl.java`

Lógica de negocio que coordina la limpieza de datos:
1. Limpia tokens de verificación de email expirados
2. Limpia tokens de reseteo de password expirados
3. Registra estadísticas de limpieza en los logs

## Configuración

### @EnableScheduling

Se habilitó el soporte para tareas programadas en la clase principal:

**Ubicación:** `app-root/src/main/java/com/matias/SecurityBackendApplication.java`

```java
@EnableScheduling
public class SecurityBackendApplication { ... }
```

## Arquitectura

La implementación sigue los principios de arquitectura hexagonal del proyecto:

```
┌─────────────────────────────────────────────────────────────┐
│  web (Capa de Presentación)                                 │
│  ├─ SchedulerController (@RestController)                   │
│  │   └─ POST /v1/scheduler/cleanup                          │
│  │       └─ Permite ejecución manual de limpieza            │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────┐
│  application (Capa de Aplicación)                           │
│  ├─ scheduled/                                               │
│  │   └─ LimpiezaDatosScheduler (@Component)                 │
│  │       └─ @Scheduled(cron = "0 0 2 * * *")                │
│  │           └─ Ejecución automática diaria                 │
│  │                                                            │
│  ├─ service/                                                 │
│  │   └─ AuthService.limpiarDatosObsoletos()                 │
│  │       ├─ Coordina la limpieza de datos                   │
│  │       ├─ Llama a VerificacionEmailService                │
│  │       └─ Llama a PasswordResetService                    │
└─────────────────────────────────────────────────────────────┘
```

## Uso

### Ejecución Automática

La tarea se ejecuta automáticamente todos los días a las 2:00 AM sin intervención manual.

### Ejecución Manual

Para ejecutar la limpieza manualmente, realiza una petición POST:

```bash
curl -X POST http://localhost:8080/v1/scheduler/cleanup \
  -H "Authorization: Bearer {token}"
```

**Respuesta esperada:** 
- Status: `204 No Content` (limpieza exitosa)

## Logs

Durante la ejecución, se registran los siguientes logs:

```
INFO  - Iniciando tarea programada de limpieza de datos obsoletos
INFO  - Iniciando limpieza de datos obsoletos
INFO  - Limpieza de datos de verificación: X registros eliminados
INFO  - Limpieza de datos de reseteo: Y registros eliminados
INFO  - Limpieza de datos obsoletos completada
INFO  - Tarea programada de limpieza completada exitosamente
```

En caso de error:
```
ERROR - Error durante la tarea programada de limpieza: {mensaje}
```

## Diferencias con seguridad-back

| Aspecto | seguridad-back | security-backend |
|---------|----------------|------------------|
| **Ubicación Scheduler** | `controller/SchedulerController.java` | `application/scheduled/LimpiezaDatosScheduler.java` |
| **Módulo** | Monolítico | Separado por capas (application, web) |
| **Arquitectura** | MVC tradicional | Hexagonal/Clean Architecture |
| **Limpieza de tokens de sesión** | Sí (TokenInvalidoRepository) | No implementado aún |

## Notas

- El proyecto no tiene habilitado `@PreAuthorize` en la configuración de Spring Security, por lo que el endpoint `/v1/scheduler/cleanup` no tiene restricciones de rol
- Se recomienda proteger el endpoint en producción mediante configuración de seguridad o restricciones a nivel de red
- La tarea programada tiene manejo de excepciones para evitar que un error detenga el scheduler

## Futuras Mejoras

1. Implementar limpieza de tokens de sesión invalidados cuando se migre la funcionalidad de logout
2. Agregar métricas y monitoreo de las limpiezas realizadas
3. Configurar la expresión cron mediante properties para mayor flexibilidad
4. Agregar más tareas programadas según sea necesario (reportes, notificaciones, etc.)
