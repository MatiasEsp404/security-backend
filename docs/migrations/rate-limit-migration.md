# Migración de Rate Limit de seguridad-back a security-backend

## Fecha de Migración
15 de marzo de 2026

## Descripción
Se migró el sistema de Rate Limiting desde el proyecto `seguridad-back` al proyecto `security-backend`, adaptándolo a la arquitectura hexagonal y a los principios de diseño del proyecto.

## Componentes Migrados

### 1. Storage (Almacenamiento)
**Ubicación**: `security/src/main/java/com/matias/security/ratelimit/storage/`

#### RateLimitStorage (Interface)
- Define el contrato para almacenar y recuperar buckets de rate-limit
- Método: `resolveBucket(String key, BucketConfiguration config)`

#### InMemoryRateLimitStorage (Implementación)
- Almacenamiento en memoria usando `ConcurrentHashMap`
- Thread-safe para uso concurrente
- Ideal para desarrollo y aplicaciones de pequeña escala
- **Nota**: Se puede extender con implementaciones basadas en Redis para producción

### 2. Properties (Configuración)
**Ubicación**: `security/src/main/java/com/matias/security/ratelimit/properties/`

#### RateLimitProperties
- Clase de configuración con `@ConfigurationProperties`
- Permite habilitar/deshabilitar el rate-limit mediante properties
- Prefijo: `app.rate-limit`
- Property clave: `app.rate-limit.enabled=true`

### 3. Config (Configuración de Beans)
**Ubicación**: `security/src/main/java/com/matias/security/ratelimit/config/`

#### RateLimitConfig
- Configura el bean de `RateLimitStorage`
- Usa `@ConditionalOnProperty` para activación condicional
- Habilita las properties con `@EnableConfigurationProperties`
- Log de información al iniciarse

### 4. Filter (Filtro HTTP)
**Ubicación**: `security/src/main/java/com/matias/security/ratelimit/filter/`

#### RateLimitFilter
- Extiende `OncePerRequestFilter` de Spring
- Se ejecuta antes del filtro de autenticación en la cadena de seguridad
- Implementa lógica de rate-limiting por IP y endpoint
- Estrategia fail-open: permite requests en caso de error
- Responde con HTTP 429 (Too Many Requests) cuando se excede el límite

**Configuraciones por endpoint**:
- `/auth/login`: 5 intentos por minuto
- `/auth/registro`: 3 registros cada 5 minutos
- `/auth/resend-verification`: 3 reenvíos cada 15 minutos
- `/auth/refresh`: 20 refreshes por minuto
- `/auth/password-reset/request`: 3 solicitudes cada 15 minutos
- Default: 30 requests por minuto

## Cambios en la Arquitectura

### Adaptaciones Realizadas

1. **Módulo de Ubicación**: Se colocó en el módulo `security` ya que:
   - Es una preocupación de seguridad
   - Actúa como filtro en la cadena de seguridad
   - No tiene lógica de negocio del dominio

2. **ErrorResponse**: 
   - Se movió de `web.dto` a `application.dto.response`
   - Permite que el módulo `security` use DTOs sin depender de `web`
   - Mantiene la arquitectura en capas correcta

3. **Integración con SecurityConfig**:
   - Se inyecta `RateLimitFilter` de forma opcional (`@Autowired(required = false)`)
   - Se agrega condicionalmente a la cadena de filtros antes de `UsernamePasswordAuthenticationFilter`
   - Log de información cuando se activa

## Dependencias Agregadas

### security/pom.xml
```xml
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.10.1</version>
</dependency>
```

## Configuración

### application.properties
```properties
# Rate Limit Configuration
app.rate-limit.enabled=true
```

Para deshabilitar el rate-limit en desarrollo:
```properties
app.rate-limit.enabled=false
```

## Principios de Diseño Aplicados

1. **Inversión de Dependencias**:
   - Interface `RateLimitStorage` permite cambiar la implementación
   - Fácil migrar a Redis sin cambiar el filtro

2. **Configuración Condicional**:
   - `@ConditionalOnProperty` permite activar/desactivar features
   - Útil para diferentes ambientes (dev, test, prod)

3. **Fail-Safe**:
   - Si el storage falla, se permite el request (fail-open)
   - Evita que problemas de rate-limit bloqueen la aplicación completamente

4. **Thread-Safety**:
   - Uso de `ConcurrentHashMap` para almacenamiento thread-safe
   - Bucket4j maneja la concurrencia internamente

5. **Logging Apropiado**:
   - Info: cuando se activan componentes
   - Warn: cuando se excede el rate-limit
   - Error: cuando hay problemas con el storage (pero se permite el request)

## Decisiones de Diseño

### ¿Por qué en el módulo `security`?
- Es una preocupación de seguridad, no de negocio
- Se integra directamente con Spring Security
- No requiere acceso al dominio

### ¿Por qué Bucket4j?
- Librería madura y bien mantenida
- Implementa algoritmo Token Bucket
- Soporte para diferentes storages (memoria, Redis, Hazelcast, etc.)
- Thread-safe por diseño

### ¿Por qué fail-open?
- Prioriza disponibilidad sobre seguridad estricta
- Evita que problemas de infraestructura bloqueen usuarios legítimos
- Se puede cambiar a fail-close si se requiere mayor seguridad

## Próximos Pasos (Opcionales)

1. **Implementación Redis**:
   - Crear `RedisRateLimitStorage` implementando `RateLimitStorage`
   - Usar `@ConditionalOnProperty` para elegir implementación
   - Necesario para clusters o múltiples instancias

2. **Métricas**:
   - Agregar métricas de Micrometer
   - Monitorear requests bloqueados por endpoint
   - Alertas cuando se superan umbrales

3. **Configuración Dinámica**:
   - Permitir configurar límites por properties
   - Crear DTOs para configuración de endpoints
   - Endpoint de administración para ajustar límites en runtime

4. **Headers de Rate Limit**:
   - Agregar headers estándar: `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`
   - Ayuda a clientes a manejar rate-limiting proactivamente

## Testing

Para probar el rate-limit:

1. Iniciar la aplicación
2. Hacer múltiples requests al mismo endpoint desde la misma IP
3. Verificar que se recibe HTTP 429 después de exceder el límite
4. Esperar el tiempo de reset y verificar que se permite nuevamente

Ejemplo con curl:
```bash
# Hacer 6 requests rápidos al login (límite: 5/min)
for i in {1..6}; do
  curl -X POST http://localhost:8081/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@example.com","password":"test123"}'
  echo ""
done
```

El sexto request debería recibir:
```json
{
  "mensaje": "Demasiadas solicitudes",
  "detalles": ["Intenta nuevamente más tarde."]
}
```

## Referencias

- [Bucket4j Documentation](https://bucket4j.com/)
- [Token Bucket Algorithm](https://en.wikipedia.org/wiki/Token_bucket)
- [Rate Limiting Best Practices](https://cloud.google.com/architecture/rate-limiting-strategies-techniques)
