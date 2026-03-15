# Migración: Verificación de Email

## Descripción General

Se ha implementado la funcionalidad completa de verificación de email en el proceso de registro. Cuando un usuario se registra, se le envía un email con un enlace de verificación que debe usar para confirmar su dirección de email.

## Componentes Implementados

### 1. Dominio (`domain/`)

#### TokenVerificacion (Modelo de dominio)
```java
domain/src/main/java/com/matias/domain/model/TokenVerificacion.java
domain/src/main/java/com/matias/domain/model/EstadoTokenVerificacion.java
```

Representa un token de verificación con:
- Token único (UUID)
- Fecha de expiración
- Estado (PENDIENTE, USADO, EXPIRADO)
- ID del usuario asociado

#### TokenVerificacionRepositoryPort
```java
domain/src/main/java/com/matias/domain/port/TokenVerificacionRepositoryPort.java
```

Puerto para operaciones de persistencia de tokens.

### 2. Base de Datos (`database/`)

#### TokenVerificacionEntity
```java
database/src/main/java/com/matias/database/entity/TokenVerificacionEntity.java
```

Entidad JPA que mapea la tabla de tokens de verificación.

#### TokenVerificacionJpaRepository
```java
database/src/main/java/com/matias/database/repository/TokenVerificacionJpaRepository.java
```

Repositorio JPA con queries personalizadas:
- Buscar por token
- Encontrar tokens expirados y no usados
- Eliminar tokens expirados

#### TokenVerificacionMapper
```java
database/src/main/java/com/matias/database/mapper/TokenVerificacionMapper.java
```

Mapper con MapStruct para conversión entre entidad y modelo de dominio.

#### TokenVerificacionRepositoryAdapter
```java
database/src/main/java/com/matias/database/adapter/TokenVerificacionRepositoryAdapter.java
```

Adaptador que implementa el port del dominio.

### 3. Aplicación (`application/`)

#### AuthService
Se actualizó la interfaz para incluir:
```java
void verificarEmail(String token);
```

#### AuthServiceImpl
Se implementó la lógica de negocio:

**En el registro (`register`)**:
1. Se crea el usuario con `emailVerificado=false`
2. Se genera un token UUID único
3. Se calcula fecha de expiración (configurable, por defecto 24 horas)
4. Se guarda el token en la base de datos
5. Se envía email de verificación con el enlace

**En la verificación (`verificarEmail`)**:
1. Se busca el token en la base de datos
2. Se valida que no esté expirado
3. Se valida que no haya sido usado
4. Se marca el email del usuario como verificado
5. Se marca el token como usado

### 4. Web (`web/`)

#### AuthController
Se agregó el endpoint:
```java
GET /v1/auth/verificar-email?token={token}
```

Retorna:
- `204 No Content` si la verificación es exitosa
- `404 Not Found` si el token no existe
- `400 Bad Request` si el token está expirado o ya fue usado

## Configuración

### application.properties

```properties
# URLs de la aplicación
app.front-url=http://localhost:3000
app.back-url=http://localhost:8081

# Configuración de verificación de email
app.verification.token.expiration-hours=24
```

## Flujo de Usuario

1. Usuario se registra mediante `POST /v1/auth/register`
2. Sistema crea cuenta con `emailVerificado=false`
3. Sistema genera token de verificación
4. Sistema envía email con enlace: `{frontUrl}/verify-email?token={token}`
5. Usuario hace clic en el enlace en su email
6. Frontend redirige a: `GET /v1/auth/verificar-email?token={token}`
7. Sistema valida y marca email como verificado
8. Frontend muestra mensaje de éxito

## Consideraciones de Seguridad

1. **Tokens únicos**: Se usa UUID v4 para generar tokens únicos e impredecibles
2. **Expiración**: Los tokens expiran después de 24 horas (configurable)
3. **Uso único**: Un token solo puede usarse una vez
4. **Estados claros**: Los tokens tienen estados bien definidos (PENDIENTE, USADO, EXPIRADO)
5. **Validaciones**: Se validan todas las condiciones antes de verificar el email

## Limpieza de Datos

Para implementar limpieza automática de tokens expirados, se puede usar el query del repositorio:
```java
tokenVerificacionRepository.deleteExpiredTokens(Instant.now());
```

Se recomienda ejecutar esto periódicamente mediante un job programado (ej: diariamente).

## Mejoras Futuras

1. **Reenvío de email**: Implementar endpoint para reenviar email de verificación
2. **Rate limiting**: Limitar cantidad de reenvíos por usuario
3. **Plantilla HTML mejorada**: Mejorar diseño del email
4. **Notificaciones**: Notificar al usuario cuando su email ha sido verificado
5. **Limpieza automática**: Job programado para eliminar tokens expirados
6. **Métricas**: Tracking de tasa de verificación de emails

## Testing

Para probar la funcionalidad:

1. Iniciar MailHog (servidor SMTP de desarrollo):
   ```bash
   docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog
   ```

2. Registrar un usuario:
   ```bash
   POST http://localhost:8081/api/v1/auth/register
   {
     "email": "test@example.com",
     "password": "Password123!",
     "nombre": "Test",
     "apellido": "User"
   }
   ```

3. Ver el email en MailHog: http://localhost:8025

4. Copiar el token del enlace y verificar:
   ```bash
   GET http://localhost:8081/api/v1/auth/verificar-email?token={token}
   ```

## Migración desde seguridad-back

Esta implementación reemplaza la funcionalidad de `TokenVerificacionEntity` del proyecto anterior, con las siguientes mejoras:

1. **Arquitectura hexagonal**: Separación clara entre dominio, aplicación e infraestructura
2. **Mejores validaciones**: Lógica de validación en el modelo de dominio
3. **Manejo de errores**: Excepciones personalizadas del dominio
4. **Configuración flexible**: Propiedades configurables para personalización
5. **Mejor separación de responsabilidades**: El email se envía desde el servicio de aplicación
