# Migración del Sistema de Auditoría con Spring Data Envers

## 📋 Resumen

Migración del sistema de auditoría basado en **Spring Data Envers** (que usa Hibernate Envers internamente) desde `seguridad-back` hacia `security-backend`. Este sistema registra automáticamente todos los cambios realizados sobre entidades (INSERT, UPDATE, DELETE) incluyendo información sobre qué usuario realizó cada operación.

---

## 🔍 Análisis del Sistema Existente

### Componentes en seguridad-back

El proyecto `seguridad-back` utiliza **Hibernate Envers** directamente. Sin embargo, en `security-backend` migraremos a **Spring Data Envers** para una mejor integración con el ecosistema Spring.

#### Componentes Identificados:

1. **Dependencia**: `hibernate-envers`
2. **AuditRevisionEntity**: Entidad que almacena información de cada revisión
3. **AuditRevisionListener**: Intercepta operaciones y registra el usuario que las realizó
4. **UsuarioAuditService**: Servicio para consultar historial de auditoría
5. **UsuarioAuditResponse**: DTO para devolver información de auditoría
6. **Entidades Auditadas**: `UsuarioEntity` y `UsuarioRolEntity` con `@Audited`
7. **Endpoint**: `GET /admin/users/{userId}/audit-log`

---

## 🏗️ Plan de Migración (Arquitectura Hexagonal)

### Fase 1: Configurar Spring Data Envers

#### 1.1. Agregar Spring Data Envers al módulo `database`
**Archivo**: `database/pom.xml`

```xml
<!-- Spring Data Envers para auditoría -->
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-envers</artifactId>
</dependency>
```

**NOTA**: No necesitamos agregar `hibernate-envers` explícitamente, ya que `spring-data-envers` lo incluye como dependencia transitiva.

#### 1.2. Crear `AuditRevisionEntity` en el módulo `database`
**Archivo**: `database/src/main/java/com/matias/database/audit/AuditRevisionEntity.java`

```java
package com.matias.database.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

@Getter
@Setter
@Entity
@Table(name = "revision_info")
@RevisionEntity(AuditRevisionListener.class)
public class AuditRevisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    private Integer id;

    @RevisionTimestamp
    private Long timestamp;

    @Column(name = "usuario_email")
    private String usuarioEmail;

    @Column(name = "usuario_id")
    private Integer usuarioId;
}
```

**Justificación del módulo**:
- Pertenece al módulo `database` porque es una entidad JPA que maneja persistencia
- Forma parte de la infraestructura de base de datos, no del dominio

#### 1.3. Crear Puerto `AuditUserProvider` en el módulo `domain`

Para respetar la arquitectura hexagonal y evitar que `database` dependa de `security`, creamos un puerto en `domain`:

**Archivo**: `domain/src/main/java/com/matias/domain/port/AuditUserProvider.java`

```java
package com.matias.domain.port;

/**
 * Puerto para obtener información del usuario actual en contextos de auditoría.
 * Implementado por el módulo de seguridad.
 */
public interface AuditUserProvider {
    
    /**
     * Obtiene el email del usuario autenticado actual
     * 
     * @return Email del usuario, o "SYSTEM" si no hay usuario autenticado
     */
    String getCurrentUserEmail();
    
    /**
     * Obtiene el ID del usuario autenticado actual
     * 
     * @return ID del usuario, o null si no hay usuario autenticado
     */
    Integer getCurrentUserId();
}
```

**Justificación**:
- El puerto está en `domain` (capa más interna)
- Define el contrato sin conocer detalles de implementación
- Permite que `database` obtenga información del usuario sin conocer Spring Security

#### 1.4. Implementar el Puerto en el módulo `security`

**Archivo**: `security/src/main/java/com/matias/security/service/AuditUserProviderImpl.java`

```java
package com.matias.security.service;

import com.matias.domain.port.AuditUserProvider;
import com.matias.security.model.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuditUserProviderImpl implements AuditUserProvider {

    @Override
    public String getCurrentUserEmail() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth != null && auth.isAuthenticated() && 
                !auth.getPrincipal().equals("anonymousUser") &&
                auth.getPrincipal() instanceof SecurityUser securityUser) {
                return securityUser.getUsername();
            }
        } catch (Exception e) {
            // Log si es necesario
        }
        return "SYSTEM";
    }

    @Override
    public Integer getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth != null && auth.isAuthenticated() && 
                !auth.getPrincipal().equals("anonymousUser") &&
                auth.getPrincipal() instanceof SecurityUser securityUser) {
                return securityUser.getId();
            }
        } catch (Exception e) {
            // Log si es necesario
        }
        return null;
    }
}
```

**Justificación**:
- El módulo `security` tiene acceso a `SecurityContextHolder` y `SecurityUser`
- Implementa el puerto definido en `domain`
- Maneja casos edge (usuario anónimo, sin autenticación)

#### 1.5. Crear `AuditRevisionListener` en el módulo `database`

Ahora el listener puede acceder al usuario a través del puerto, sin depender directamente de `security`:

**Archivo**: `database/src/main/java/com/matias/database/audit/AuditRevisionListener.java`

```java
package com.matias.database.audit;

import com.matias.domain.port.AuditUserProvider;
import org.hibernate.envers.RevisionListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Listener de Envers que captura el usuario autenticado actual.
 * Usa ApplicationContextAware para obtener el bean AuditUserProvider
 * ya que Hibernate instancia este listener directamente.
 */
@Component
public class AuditRevisionListener implements RevisionListener, ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    @Override
    public void newRevision(Object revisionEntity) {
        AuditRevisionEntity revision = (AuditRevisionEntity) revisionEntity;

        try {
            if (applicationContext != null) {
                AuditUserProvider auditUserProvider = applicationContext.getBean(AuditUserProvider.class);
                revision.setUsuarioEmail(auditUserProvider.getCurrentUserEmail());
                revision.setUsuarioId(auditUserProvider.getCurrentUserId());
            } else {
                revision.setUsuarioEmail("SYSTEM");
            }
        } catch (Exception e) {
            revision.setUsuarioEmail("SYSTEM");
        }
    }
}
```

**⚠️ SOLUCIÓN ARQUITECTÓNICA CLAVE**:
- **Problema**: Hibernate instancia `RevisionListener` directamente, sin pasar por Spring IoC
- **Solución**: Usar `ApplicationContextAware` para acceder al contexto de Spring de forma programática
- **Beneficio**: El módulo `database` NO depende del módulo `security`
- **Inversión de Dependencias**: `database` depende del puerto en `domain`, `security` implementa el puerto
- **Respeta Arquitectura Hexagonal**: Las dependencias fluyen hacia el dominio

#### 1.6. **IMPORTANTE: NO agregar dependencia a `security` en `database/pom.xml`**

El módulo `database` **NO** debe tener dependencia sobre `security`. Solo necesita:

```xml
<!-- Dependencia al dominio (donde está el puerto AuditUserProvider) -->
<dependency>
    <groupId>com.matias</groupId>
    <artifactId>domain</artifactId>
    <version>${project.version}</version>
</dependency>
```

Esta dependencia ya existe, por lo que no hay que agregar nada nuevo en el `pom.xml` de `database`.

---

### Fase 2: Anotar Entidades como Auditadas

#### 2.1. Anotar `UsuarioEntity` con `@Audited`
**Archivo**: `database/src/main/java/com/matias/database/entity/UsuarioEntity.java`

```java
import org.hibernate.envers.Audited;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited  // <- Agregar esta anotación
@Table(name = "usuarios")
public class UsuarioEntity {
    // ... resto del código
}
```

#### 2.2. Anotar `UsuarioRolEntity` con `@Audited`
**Archivo**: `database/src/main/java/com/matias/database/entity/UsuarioRolEntity.java`

```java
import org.hibernate.envers.Audited;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited  // <- Agregar esta anotación
@Table(name = "usuario_rol")
public class UsuarioRolEntity {
    // ... resto del código
}
```

**IMPORTANTE**: Con solo agregar `@Audited`, Spring Data Envers automáticamente:
- Crea tablas de auditoría (`usuarios_aud`, `usuario_rol_aud`, `revision_info`)
- Registra todos los cambios (INSERT, UPDATE, DELETE)
- Asocia cada cambio con una revisión en `revision_info`

---

### Fase 3: Crear Modelos de Dominio para Auditoría

#### 3.1. Crear modelo `FieldChange` en `domain`
**Archivo**: `domain/src/main/java/com/matias/domain/model/audit/FieldChange.java`

```java
package com.matias.domain.model.audit;

public record FieldChange(
    Object oldValue,
    Object newValue
) {}
```

#### 3.2. Crear modelo `RoleAuditLog` en `domain`
**Archivo**: `domain/src/main/java/com/matias/domain/model/audit/RoleAuditLog.java`

```java
package com.matias.domain.model.audit;

import java.time.Instant;

public record RoleAuditLog(
    Integer revisionId,
    Instant timestamp,
    String operationType,
    String modifiedBy,
    String role,
    String assignedBy,
    Instant assignmentDate
) {}
```

#### 3.3. Crear modelo `UsuarioAuditLog` en `domain`
**Archivo**: `domain/src/main/java/com/matias/domain/model/audit/UsuarioAuditLog.java`

```java
package com.matias.domain.model.audit;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record UsuarioAuditLog(
    Integer revisionId,
    Instant timestamp,
    String operationType,
    String modifiedBy,
    Integer modifiedById,
    Map<String, FieldChange> changes,
    List<RoleAuditLog> roleChanges
) {}
```

**Justificación**:
- Usamos `record` de Java 21 para inmutabilidad y simplicidad
- Estos modelos representan conceptos del dominio de auditoría
- Son agnósticos de la tecnología (no saben de Spring Data Envers)

---

### Fase 4: Crear Puerto para Auditoría

#### 4.1. Crear `UsuarioAuditRepositoryPort` en `domain`
**Archivo**: `domain/src/main/java/com/matias/domain/port/UsuarioAuditRepositoryPort.java`

```java
package com.matias.domain.port;

import com.matias.domain.model.audit.UsuarioAuditLog;
import java.util.List;

public interface UsuarioAuditRepositoryPort {
    
    /**
     * Obtiene el historial completo de auditoría de un usuario
     * 
     * @param usuarioId ID del usuario
     * @return Lista de registros de auditoría ordenados cronológicamente
     */
    List<UsuarioAuditLog> findAuditLogByUsuarioId(Integer usuarioId);
}
```

**Justificación**:
- Este puerto define el contrato para acceder a datos de auditoría
- El dominio no sabe cómo se implementa (Spring Data Envers, otro sistema, etc.)
- Inversión de dependencias: el dominio define, la infraestructura implementa

---

### Fase 5: Implementar Adaptador de Auditoría con Spring Data Envers

#### 5.1. Crear `UsuarioAuditRepositoryAdapter` en `database`
**Archivo**: `database/src/main/java/com/matias/database/adapter/UsuarioAuditRepositoryAdapter.java`

```java
package com.matias.database.adapter;

import com.matias.database.audit.AuditRevisionEntity;
import com.matias.database.entity.UsuarioEntity;
import com.matias.database.entity.UsuarioRolEntity;
import com.matias.domain.model.audit.FieldChange;
import com.matias.domain.model.audit.RoleAuditLog;
import com.matias.domain.model.audit.UsuarioAuditLog;
import com.matias.domain.port.UsuarioAuditRepositoryPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UsuarioAuditRepositoryAdapter implements UsuarioAuditRepositoryPort {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioAuditLog> findAuditLogByUsuarioId(Integer usuarioId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        List<UsuarioAuditLog> auditLog = new ArrayList<>();

        List<Number> revisions = auditReader.getRevisions(UsuarioEntity.class, usuarioId);

        for (int i = 0; i < revisions.size(); i++) {
            Number revisionNumber = revisions.get(i);

            UsuarioEntity currentEntity = auditReader.find(UsuarioEntity.class, usuarioId, revisionNumber);
            AuditRevisionEntity revisionEntity = auditReader.findRevision(AuditRevisionEntity.class, revisionNumber);

            List<?> revisionResults = auditReader.createQuery()
                    .forRevisionsOfEntity(UsuarioEntity.class, false, true)
                    .add(AuditEntity.id().eq(usuarioId))
                    .add(AuditEntity.revisionNumber().eq(revisionNumber))
                    .getResultList();

            RevisionType revisionType = revisionResults.isEmpty()
                    ? RevisionType.MOD
                    : (RevisionType) ((Object[]) revisionResults.get(0))[2];

            UsuarioEntity previousEntity = null;
            if (i > 0) {
                Number previousRevision = revisions.get(i - 1);
                previousEntity = auditReader.find(UsuarioEntity.class, usuarioId, previousRevision);
            }

            UsuarioAuditLog auditDTO = buildAuditLog(
                    revisionEntity,
                    revisionType,
                    currentEntity,
                    previousEntity
            );

            List<RoleAuditLog> roleChanges = getRoleChangesForRevision(
                    auditReader,
                    usuarioId,
                    revisionNumber,
                    revisionEntity
            );

            // Crear nuevo UsuarioAuditLog con roleChanges
            auditLog.add(new UsuarioAuditLog(
                    auditDTO.revisionId(),
                    auditDTO.timestamp(),
                    auditDTO.operationType(),
                    auditDTO.modifiedBy(),
                    auditDTO.modifiedById(),
                    auditDTO.changes(),
                    roleChanges
            ));
        }

        return auditLog;
    }

    private UsuarioAuditLog buildAuditLog(
            AuditRevisionEntity revisionEntity,
            RevisionType revisionType,
            UsuarioEntity currentEntity,
            UsuarioEntity previousEntity) {

        Map<String, FieldChange> changes = new HashMap<>();

        if (previousEntity != null && currentEntity != null) {
            compareAndAddChange(changes, "email", previousEntity.getEmail(), currentEntity.getEmail());
            compareAndAddChange(changes, "nombre", previousEntity.getNombre(), currentEntity.getNombre());
            compareAndAddChange(changes, "apellido", previousEntity.getApellido(), currentEntity.getApellido());
            compareAndAddChange(changes, "activo", previousEntity.getActivo(), currentEntity.getActivo());
            compareAndAddChange(changes, "emailVerificado", previousEntity.getEmailVerificado(), currentEntity.getEmailVerificado());

            if (!previousEntity.getPassword().equals(currentEntity.getPassword())) {
                changes.put("password", new FieldChange("CHANGED", "CHANGED"));
            }
        }

        return new UsuarioAuditLog(
                revisionEntity.getId(),
                Instant.ofEpochMilli(revisionEntity.getTimestamp()),
                getOperationTypeName(revisionType),
                revisionEntity.getUsuarioEmail(),
                revisionEntity.getUsuarioId(),
                changes.isEmpty() ? null : changes,
                null  // roleChanges se agregarán después
        );
    }

    private List<RoleAuditLog> getRoleChangesForRevision(
            AuditReader auditReader,
            Integer usuarioId,
            Number revisionNumber,
            AuditRevisionEntity revisionEntity) {

        List<RoleAuditLog> roleChanges = new ArrayList<>();

        List<?> roleRevisions = auditReader.createQuery()
                .forRevisionsOfEntity(UsuarioRolEntity.class, false, true)
                .add(AuditEntity.relatedId("usuario").eq(usuarioId))
                .add(AuditEntity.revisionNumber().eq(revisionNumber))
                .getResultList();

        for (Object result : roleRevisions) {
            Object[] resultArray = (Object[]) result;
            UsuarioRolEntity roleEntity = (UsuarioRolEntity) resultArray[0];
            RevisionType revType = (RevisionType) resultArray[2];

            roleChanges.add(new RoleAuditLog(
                    revisionEntity.getId(),
                    Instant.ofEpochMilli(revisionEntity.getTimestamp()),
                    getOperationTypeName(revType),
                    revisionEntity.getUsuarioEmail(),
                    roleEntity.getRol().name(),
                    roleEntity.getAsignadoPor() != null ? roleEntity.getAsignadoPor().getEmail() : "SYSTEM",
                    roleEntity.getFechaAsignacion()
            ));
        }

        return roleChanges;
    }

    private void compareAndAddChange(
            Map<String, FieldChange> changes,
            String fieldName,
            Object oldValue,
            Object newValue) {

        if ((oldValue == null && newValue != null) ||
                (oldValue != null && !oldValue.equals(newValue))) {
            changes.put(fieldName, new FieldChange(oldValue, newValue));
        }
    }

    private String getOperationTypeName(RevisionType revisionType) {
        return switch (revisionType) {
            case ADD -> "INSERT";
            case MOD -> "UPDATE";
            case DEL -> "DELETE";
        };
    }
}
```

**Justificación**:
- Implementa `UsuarioAuditRepositoryPort` del dominio
- Usa `AuditReader` de Hibernate Envers (incluido en Spring Data Envers)
- Compara estado anterior vs actual para identificar cambios específicos
- Oculta contraseñas mostrando solo "CHANGED"
- Mapea de entidades JPA a modelos de dominio (records)

---

### Fase 6: Crear Servicio de Aplicación

#### 6.1. Crear `UsuarioAuditService` en `application`
**Archivo**: `application/src/main/java/com/matias/application/service/UsuarioAuditService.java`

```java
package com.matias.application.service;

import com.matias.domain.model.audit.UsuarioAuditLog;
import java.util.List;

public interface UsuarioAuditService {
    
    /**
     * Obtiene el historial de auditoría completo de un usuario
     * 
     * @param usuarioId ID del usuario
     * @return Lista de registros de auditoría
     */
    List<UsuarioAuditLog> getUsuarioAuditLog(Integer usuarioId);
}
```

#### 6.2. Implementar `UsuarioAuditServiceImpl` en `application`
**Archivo**: `application/src/main/java/com/matias/application/service/impl/UsuarioAuditServiceImpl.java`

```java
package com.matias.application.service.impl;

import com.matias.application.service.UsuarioAuditService;
import com.matias.domain.model.audit.UsuarioAuditLog;
import com.matias.domain.port.UsuarioAuditRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioAuditServiceImpl implements UsuarioAuditService {

    private final UsuarioAuditRepositoryPort usuarioAuditRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioAuditLog> getUsuarioAuditLog(Integer usuarioId) {
        return usuarioAuditRepository.findAuditLogByUsuarioId(usuarioId);
    }
}
```

**Justificación**:
- Capa de aplicación delgada: solo delega al puerto
- Podría agregar lógica adicional como validaciones o autorizaciones si fuera necesario
- Mantiene la separación de responsabilidades

---

### Fase 7: Crear DTOs de Respuesta en el módulo `web`

#### 7.1. Crear `UsuarioAuditResponse` en `web`
**Archivo**: `web/src/main/java/com/matias/web/dto/response/UsuarioAuditResponse.java`

```java
package com.matias.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Registro de auditoría de un usuario")
public record UsuarioAuditResponse(
        @Schema(description = "ID de la revisión", example = "1")
        Integer revisionId,

        @Schema(description = "Fecha y hora del cambio")
        Instant timestamp,

        @Schema(description = "Tipo de operación", example = "UPDATE", allowableValues = {"INSERT", "UPDATE", "DELETE"})
        String operationType,

        @Schema(description = "Usuario que realizó la modificación", example = "admin@example.com")
        String modifiedBy,

        @Schema(description = "ID del usuario que realizó la modificación", example = "5")
        Integer modifiedById,

        @Schema(description = "Mapa de campos modificados con valores anterior y nuevo")
        Map<String, FieldChangeResponse> changes,

        @Schema(description = "Lista de cambios en roles del usuario")
        List<RoleAuditResponse> roleChanges
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Cambio en un campo específico")
    public record FieldChangeResponse(
            @Schema(description = "Valor anterior del campo")
            Object oldValue,

            @Schema(description = "Valor nuevo del campo")
            Object newValue
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Registro de auditoría de cambios en roles")
    public record RoleAuditResponse(
            @Schema(description = "ID de la revisión", example = "3")
            Integer revisionId,

            @Schema(description = "Fecha y hora del cambio")
            Instant timestamp,

            @Schema(description = "Tipo de operación", example = "INSERT", allowableValues = {"INSERT", "UPDATE", "DELETE"})
            String operationType,

            @Schema(description = "Usuario que realizó la modificación", example = "admin@example.com")
            String modifiedBy,

            @Schema(description = "Rol asignado/modificado/eliminado", example = "USER")
            String role,

            @Schema(description = "Usuario que asignó el rol", example = "admin@example.com")
            String assignedBy,

            @Schema(description = "Fecha de asignación del rol")
            Instant assignmentDate
    ) {}
}
```

**Justificación**:
- DTOs específicos para la capa web (JSON)
- Incluye anotaciones Swagger para documentación de API
- Usa records para simplicidad
- Anotaciones Jackson para serialización JSON

---

### Fase 8: Actualizar AdminController con Endpoint de Auditoría

#### 8.1. Agregar endpoint de auditoría en `AdminController`
**Archivo**: `web/src/main/java/com/matias/web/controller/AdminController.java`

Agregar estas líneas al controlador existente:

```java
import com.matias.application.service.UsuarioAuditService;
import com.matias.web.dto.response.UsuarioAuditResponse;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UsuarioAuditService usuarioAuditService;  // <- Agregar

    // ... métodos existentes ...

    @GetMapping("/users/{userId:[1-9][0-9]*}/audit-log")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Obtener historial de auditoría de usuario",
        description = "Retorna el historial completo de cambios realizados sobre un usuario específico"
    )
    @ApiResponse(responseCode = "200", description = "Historial de auditoría obtenido exitosamente")
    @ApiResponse(responseCode = "403", description = "No tiene permisos para acceder a este recurso")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    public ResponseEntity<List<UsuarioAuditResponse>> getAuditLog(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Integer userId) {

        List<UsuarioAuditLog> auditLog = usuarioAuditService.getUsuarioAuditLog(userId);

        // Mapear de modelo de dominio a DTO de respuesta
        List<UsuarioAuditResponse> response = auditLog.stream()
                .map(this::mapToAuditResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    private UsuarioAuditResponse mapToAuditResponse(UsuarioAuditLog auditLog) {
        Map<String, UsuarioAuditResponse.FieldChangeResponse> changesDto = null;
        if (auditLog.changes() != null) {
            changesDto = auditLog.changes().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> new UsuarioAuditResponse.FieldChangeResponse(
                                    entry.getValue().oldValue(),
                                    entry.getValue().newValue()
                            )
                    ));
        }

        List<UsuarioAuditResponse.RoleAuditResponse> roleChangesDto = null;
        if (auditLog.roleChanges() != null) {
            roleChangesDto = auditLog.roleChanges().stream()
                    .map(roleAudit -> new UsuarioAuditResponse.RoleAuditResponse(
                            roleAudit.revisionId(),
                            roleAudit.timestamp(),
                            roleAudit.operationType(),
                            roleAudit.modifiedBy(),
                            roleAudit.role(),
                            roleAudit.assignedBy(),
                            roleAudit.assignmentDate()
                    ))
                    .toList();
        }

        return new UsuarioAuditResponse(
                auditLog.revisionId(),
                auditLog.timestamp(),
                auditLog.operationType(),
                auditLog.modifiedBy(),
                auditLog.modifiedById(),
                changesDto,
                roleChangesDto
        );
    }
}
```

---

### Fase 9: Configuración de Propiedades (Opcional)

#### 9.1. Agregar propiedades de Envers (si es necesario)
**Archivo**: `app-root/src/main/resources/application.properties`

```properties
# Configuración de Hibernate Envers (opcional, estos son los valores por defecto)
spring.jpa.properties.org.hibernate.envers.audit_table_suffix=_aud
spring.jpa.properties.org.hibernate.envers.revision_field_name=rev
spring.jpa.properties.org.hibernate.envers.revision_type_field_name=revtype
spring.jpa.properties.org.hibernate.envers.store_data_at_delete=true
```

**NOTA**: Estas propiedades son opcionales. Envers funciona con valores predeterminados razonables.

---

## 📊 Estructura de Tablas Generadas por Envers

Cuando ejecutes la aplicación, Spring Data Envers creará automáticamente estas tablas:

### 1. `revision_info`
Almacena información sobre cada revisión (transacción que modifica datos auditados).

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | INTEGER | ID único de la revisión (PK) |
| timestamp | BIGINT | Timestamp en milisegundos |
| usuario_email | VARCHAR | Email del usuario que hizo el cambio |
| usuario_id | INTEGER | ID del usuario que hizo el cambio |

### 2. `usuarios_aud`
Almacena el estado histórico de la tabla `usuarios`.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | INTEGER | ID del usuario (PK junto con rev) |
| rev | INTEGER | ID de la revisión (PK, FK a revision_info) |
| revtype | SMALLINT | Tipo de operación (0=INSERT, 1=UPDATE, 2=DELETE) |
| email | VARCHAR | Email del usuario en esta revisión |
| password | VARCHAR | Password del usuario en esta revisión |
| nombre | VARCHAR | Nombre del usuario en esta revisión |
| apellido | VARCHAR | Apellido del usuario en esta revisión |
| activo | BOOLEAN | Estado activo en esta revisión |
| email_verificado | BOOLEAN | Estado de verificación en esta revisión |
| ... | ... | Todos los demás campos de usuarios |

### 3. `usuario_rol_aud`
Almacena el estado histórico de la tabla `usuario_rol`.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | INTEGER | ID del usuario_rol (PK junto con rev) |
| rev | INTEGER | ID de la revisión (PK, FK a revision_info) |
| revtype | SMALLINT | Tipo de operación (0=INSERT, 1=UPDATE, 2=DELETE) |
| usuario_id | INTEGER | ID del usuario |
| rol | VARCHAR | Rol asignado |
| asignado_por_id | INTEGER | ID del usuario que asignó el rol |
| fecha_asignacion | TIMESTAMP | Fecha de asignación |

---

## ✅ Verificación de la Migración

### Checklist de Implementación:

- [ ] **Fase 1**: Agregar dependencia `spring-data-envers` en `database/pom.xml`
- [ ] **Fase 1**: Agregar dependencia al módulo `security` en `database/pom.xml`
- [ ] **Fase 1**: Crear `AuditRevisionEntity` en `database/src/.../audit/`
- [ ] **Fase 1**: Crear `AuditRevisionListener` en `database/src/.../audit/`
- [ ] **Fase 2**: Anotar `UsuarioEntity` con `@Audited`
- [ ] **Fase 2**: Anotar `UsuarioRolEntity` con `@Audited`
- [ ] **Fase 3**: Crear `FieldChange` en `domain/src/.../model/audit/`
- [ ] **Fase 3**: Crear `RoleAuditLog` en `domain/src/.../model/audit/`
- [ ] **Fase 3**: Crear `UsuarioAuditLog` en `domain/src/.../model/audit/`
- [ ] **Fase 4**: Crear `UsuarioAuditRepositoryPort` en `domain/src/.../port/`
- [ ] **Fase 5**: Crear `UsuarioAuditRepositoryAdapter` en `database/src/.../adapter/`
- [ ] **Fase 6**: Crear `UsuarioAuditService` en `application/src/.../service/`
- [ ] **Fase 6**: Crear `UsuarioAuditServiceImpl` en `application/src/.../service/impl/`
- [ ] **Fase 7**: Crear `UsuarioAuditResponse` en `web/src/.../dto/response/`
- [ ] **Fase 8**: Actualizar `AdminController` con endpoint de auditoría
- [ ] **Fase 9**: Configurar propiedades (opcional)
- [ ] **Compilar**: Ejecutar `mvn clean install`
- [ ] **Probar**: Iniciar aplicación y verificar tablas de auditoría

### Pruebas de Funcionamiento:

#### 1. Verificar Creación de Tablas

Al iniciar la aplicación, verifica que se crearon las siguientes tablas:
```sql
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
  AND table_name IN ('revision_info', 'usuarios_aud', 'usuario_rol_aud');
```

#### 2. Probar Auditoría de Usuario

```bash
# Crear un usuario
curl -X POST http://localhost:8080/auth/registro \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!",
    "nombre": "Juan",
    "apellido": "Pérez"
  }'

# Actualizar el usuario (como admin)
curl -X PATCH http://localhost:8080/admin/users/1/status \
  -H "Authorization: Bearer {ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"activo": false}'

# Consultar historial de auditoría
curl -X GET http://localhost:8080/admin/users/1/audit-log \
  -H "Authorization: Bearer {ADMIN_TOKEN}"
```

#### 3. Respuesta Esperada

```json
[
  {
    "revisionId": 1,
    "timestamp": "2026-03-17T13:30:00Z",
    "operationType": "INSERT",
    "modifiedBy": "SYSTEM",
    "modifiedById": null,
    "changes": null,
    "roleChanges": [
      {
        "revisionId": 1,
        "timestamp": "2026-03-17T13:30:00Z",
        "operationType": "INSERT",
        "modifiedBy": "SYSTEM",
        "role": "USER",
        "assignedBy": "SYSTEM",
        "assignmentDate": "2026-03-17T13:30:00Z"
      }
    ]
  },
  {
    "revisionId": 2,
    "timestamp": "2026-03-17T13:35:00Z",
    "operationType": "UPDATE",
    "modifiedBy": "admin@example.com",
    "modifiedById": 1,
    "changes": {
      "activo": {
        "oldValue": true,
        "newValue": false
      }
    },
    "roleChanges": null
  }
]
```

---

## 🎯 Beneficios del Sistema de Auditoría

1. **Trazabilidad Completa**: Cada cambio queda registrado con su autor y fecha
2. **Cumplimiento Normativo**: Ayuda a cumplir con regulaciones como GDPR, SOC2, ISO 27001
3. **Detección de Anomalías**: Facilita la identificación de actividades sospechosas
4. **Resolución de Conflictos**: Historial completo para entender el estado anterior
5. **Accountability**: Los usuarios saben que sus acciones quedan registradas
6. **Debugging**: Útil para diagnosticar problemas en producción

---

## 📝 Consideraciones Adicionales

### Performance

- Las tablas de auditoría crecen rápidamente. Considera:
  - **Particionamiento** de tablas por fecha
  - **Archivado** de auditorías antiguas
  - **Índices** en campos frecuentemente consultados (timestamp, usuario_id)

### Auditoría Selectiva

No todas las entidades necesitan ser auditadas. Evalúa cuidadosamente:
- ✅ Auditar: Usuarios, roles, configuraciones críticas
- ❌ No auditar: Logs, métricas, datos temporales

### Privacidad de Datos

- Las contraseñas en `usuarios_aud` están hasheadas (seguras)
- Considera si datos sensibles deben ser auditados
- Implementa políticas de retención de datos

### Extensibilidad

Para auditar otras entidades en el futuro:
1. Agregar `@Audited` a la entidad
2. Spring Data Envers automáticamente creará la tabla `_aud`
3. Si necesitas consultar el historial, extender `UsuarioAuditRepositoryAdapter`

---

## 🔗 Referencias

- [Spring Data Envers Documentation](https://docs.spring.io/spring-data/envers/docs/current/reference/html/)
- [Hibernate Envers User Guide](https://hibernate.org/orm/envers/)
- [Arquitectura Hexagonal](../../docs/architecture.md)

---

## 📅 Estado de la Migración

**Estado**: ✅ COMPLETADO  
**Fecha de Finalización**: 17/03/2026  
**Autor**: Claudio Matías Correa Espínola
