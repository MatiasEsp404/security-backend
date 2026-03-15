# Migración del Sistema de Emails

## Resumen

Se ha implementado el sistema de envío de emails en security-backend, migrando la funcionalidad desde seguridad-back y siguiendo los principios de arquitectura hexagonal del proyecto.

## Arquitectura

El sistema de emails sigue la arquitectura hexagonal del proyecto:

### Domain (Puerto)
- **EmailServicePort**: Define el contrato para enviar emails
- **EmailTemplate**: Interfaz para plantillas de email
- **ContentTemplate**: Interfaz para el contenido del email

### Email Module (Adaptador)
Nuevo módulo que contiene las implementaciones concretas para diferentes proveedores:

#### Adaptadores
- **SendGridEmailAdapter**: Implementación usando SendGrid para producción
- **SmtpEmailAdapter**: Implementación usando SMTP/MailHog para desarrollo

#### Configuración
- **EmailProperties**: Propiedades de configuración desde application.properties
- **MailConfig**: Configuración del JavaMailSender para SMTP

### Application (Plantillas)
Plantillas concretas de email en el módulo application:
- **WelcomeEmailTemplate**: Email de bienvenida al registrarse
- **VerificationEmailTemplate**: Email de verificación de cuenta

## Estructura de Archivos

```
security-backend/
├── domain/
│   └── src/main/java/com/matias/domain/
│       ├── port/
│       │   └── EmailServicePort.java
│       └── model/email/
│           ├── EmailTemplate.java
│           └── ContentTemplate.java
├── email/                                    [NUEVO MÓDULO]
│   ├── pom.xml
│   └── src/main/java/com/matias/email/
│       ├── adapter/
│       │   ├── SendGridEmailAdapter.java
│       │   └── SmtpEmailAdapter.java
│       ├── config/
│       │   └── MailConfig.java
│       └── properties/
│           └── EmailProperties.java
└── application/
    └── src/main/java/com/matias/application/
        └── email/                            [NUEVAS PLANTILLAS]
            ├── WelcomeEmailTemplate.java
            └── VerificationEmailTemplate.java
```

## Configuración

### Dependencias Maven

En `pom.xml` raíz:
```xml
<properties>
    <sendgrid.version>4.10.3</sendgrid.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.sendgrid</groupId>
            <artifactId>sendgrid-java</artifactId>
            <version>${sendgrid.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>

<modules>
    <module>email</module>  <!-- Nuevo módulo -->
</modules>
```

### Propiedades de Aplicación

En `application.properties`:
```properties
# Email Configuration
email.provider=mailhog

# MailHog/SMTP Configuration (for development)
email.smtp.host=localhost
email.smtp.port=1025
email.smtp.from-email=noreply@localhost
email.smtp.from-name=MatiasProject
email.smtp.auth=false
email.smtp.starttls-enable=false

# SendGrid Configuration (for production)
# email.provider=sendgrid
# email.sendgrid.api-key=${SENDGRID_API_KEY}
# email.sendgrid.from-email=noreply@example.com
# email.sendgrid.from-name=MatiasProject
```

## Uso

### Enviar Email de Bienvenida

```java
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final EmailServicePort emailService;
    
    public void registrar(RegistroRequest request) {
        // ... lógica de registro ...
        
        // Enviar email de bienvenida
        WelcomeEmailTemplate email = new WelcomeEmailTemplate(
            usuario.getEmail(),
            usuario.getNombreUsuario()
        );
        emailService.send(email);
    }
}
```

### Enviar Email de Verificación

```java
VerificationEmailTemplate email = new VerificationEmailTemplate(
    usuario.getEmail(),
    usuario.getNombreUsuario(),
    verificationToken,
    "http://localhost:3000",
    "24 horas"
);
emailService.send(email);
```

## Proveedores Disponibles

### MailHog (Desarrollo)
- Servidor SMTP local para testing
- No requiere configuración externa
- Por defecto: localhost:1025
- Ver emails en: http://localhost:8025

### SendGrid (Producción)
- Servicio de email profesional
- Requiere API Key
- Configurar en propiedades o variables de entorno

## Selección de Proveedor

El proveedor se selecciona mediante la propiedad `email.provider`:
- `mailhog` (default): Usa SmtpEmailAdapter
- `sendgrid`: Usa SendGridEmailAdapter

Spring Boot activa automáticamente el adapter correcto mediante `@ConditionalOnProperty`.

## Ventajas de la Arquitectura

1. **Inversión de Dependencias**: La lógica de negocio no depende de implementaciones concretas
2. **Fácil Testing**: Se puede mockear EmailServicePort
3. **Intercambiable**: Cambiar entre proveedores modificando solo configuración
4. **Extensible**: Agregar nuevos proveedores implementando EmailServicePort
5. **Separación de Responsabilidades**: Plantillas en application, adaptadores en email module

## Diferencias con seguridad-back

| Aspecto | seguridad-back | security-backend |
|---------|----------------|------------------|
| Arquitectura | Monolítica | Hexagonal (módulos separados) |
| Proveedores | Solo SendGrid | SendGrid + MailHog |
| Configuración | Hardcoded | Desde properties |
| Plantillas | Clases separadas | Records que implementan interfaces |
| Testing | Difícil | Fácil (port mockeable) |

## Próximos Pasos

1. Integrar envío de emails en el flujo de registro
2. Implementar verificación de email
3. Agregar email de recuperación de contraseña
4. Implementar sistema de plantillas más avanzado (Thymeleaf)
5. Agregar colas de mensajes para envíos asíncronos (RabbitMQ/Kafka)

## Testing en Desarrollo

Para probar emails en desarrollo:

1. Levantar MailHog:
   ```bash
   docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog
   ```

2. Configurar `email.provider=mailhog` en application.properties

3. Ver emails en http://localhost:8025

## Testing en Producción

Para usar SendGrid en producción:

1. Obtener API Key de SendGrid
2. Configurar variable de entorno: `SENDGRID_API_KEY`
3. Actualizar `application.properties`:
   ```properties
   email.provider=sendgrid
   email.sendgrid.api-key=${SENDGRID_API_KEY}
   email.sendgrid.from-email=tu-email@dominio.com
   email.sendgrid.from-name=TuApp
   ```

## Migración Completada

✅ Port EmailServicePort creado en domain  
✅ Modelos EmailTemplate y ContentTemplate creados  
✅ Módulo email con adaptadores implementado  
✅ Plantillas de email en application  
✅ Configuración en application.properties  
✅ Dependencias Maven configuradas  
✅ Compilación exitosa verificada  
✅ Documentación completada
