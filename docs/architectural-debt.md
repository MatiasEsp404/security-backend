# Análisis de Deuda Arquitectónica (Arquitectura Hexagonal)

Este documento detalla los hallazgos tras la revisión exhaustiva de dependencias de módulos y ubicación de clases en el proyecto `security-backend`, aplicando los principios estables de Arquitectura Hexagonal.

---

## 🛑 Infracciones CRÍTICAS
*(Violaciones al flujo de dependencia, ej: domain dependiendo de base de datos o web)*

**¡Excelente noticia!** No se encontraron infracciones Críticas. El flujo principal de dependencias es respetado a nivel de módulos y de importaciones Java. El núcleo de negocio (el módulo `domain`) no depende de ningún otro módulo del proyecto y sirve de base universalmente. No hay dependencias circulares detectadas.

---

## 🔴 Infracciones GRAVES
*(Fuga de detalles de implementación a los módulos core - `domain` y `application`)*

**1. Fuga de Spring Security a Casos de Uso**
- **Archivo:** `c:\Projects\security-backend\application\pom.xml` y Servicios (ej. `AuthServiceImpl.java`, `PasswordResetServiceImpl.java`).
- **Descripción de la infracción:** El módulo `application` importa `spring-boot-starter-security` y utiliza directamente componentes como el bean `PasswordEncoder` en los servicios de negocio/aplicación. Esta dependencia inyecta detalles específicos de cifrado y el marco conceptual de Spring Security de infraestructura en los casos de uso, rompiendo la inversión de dependencia.
- **Sugerencia de corrección:** Declarar un puerto `PasswordHashingPort` en `domain` (o en `application`), utilizar dicha abstracción en los servicios de aplicación (`AuthServiceImpl`) e implementar la lógica de Spring Security `PasswordEncoder` en un adaptador dedicado en el módulo de infraestructura de seguridad (`security`).

**2. Fuga de Swagger/OpenAPI al modelo de Aplicación**
- **Archivo:** `c:\Projects\security-backend\application\pom.xml` y DTOs (`UsuarioResponse.java`, `RegistroRequest.java`, `LogueoRequest.java`, etc.).
- **Descripción de la infracción:** El módulo `application` contiene múltiples clases DTO anotadas con `@Schema` perteneciente a `io.swagger.v3` (springdoc-openapi). Esto es conocimiento puro de presentación y API web goteando hacia la capa de casos de uso (Application layer), acoplando las operaciones interinas con documentación de Web.
- **Sugerencia de corrección:** Eliminar la dependencia de `springdoc` del módulo `application`. Crear DTOs específicos de Request y Response web en el módulo `web` (que serían los portadores de anotaciones OpenAPI) e implementar ahí el mapeo hacia o desde comandos/respuestas de la capa de aplicación.

---

## 🟠 Infracciones MODERADAS
*(Lógica de negocio fuera del core o acoplamientos innecesarios)*

**1. Acoplamiento entre Adaptadores de Infraestructura**
- **Archivo:** `c:\Projects\security-backend\web\pom.xml`
- **Descripción de la infracción:** Según el POM, el adaptador `web` depende directamente del adaptador `security`. La arquitectura hexagonal exige que los adaptadores se ignoren mutuamente (cada adaptador primario o secundario se enlaza directa y únicamente a los puertos). Conectar dos adaptadores entrelaza capas de salida de red y filtros de seguridad.
- **Sugerencia de corrección:** Remover la dependencia de `security` sobre `web`. Cualquier información o contexto que los controladores de `web` requieran provenientes del token de seguridad (e.g., obtener el ID del usuario) debería pasarse o inyectarse sin depender compilatoriamente de los artefactos de `security` sino en el `domain`/`application`, y la configuración centralizada de arranque debe ir en el módulo `app-root`.

**2. Fuga de estructura de Repositorio a la capa Web**
- **Archivo:** `c:\Projects\security-backend\web\src\main\java\com\matias\web\controller\AdminController.java`
- **Descripción de la infracción:** El controlador `AdminController`, aunque llama a `AdminService` y no inyecta bibliotecas JPA directas, importa los objetos `UsuarioRepositoryPort.UsuarioFilter` o `UsuarioRepositoryPort.PageRequest` definidos directamente en el interior del puerto de base de datos. Esto significa que el adaptador de interfaz de usuario sabe de "filtros de repositorio" destinados a adaptadores secundarios, violando la separación de intenciones.
- **Sugerencia de corrección:** Re-definir comandos o consultas limpios en `application` (por ejemplo: `SearchUsersQuery`), y proveer en su lugar de los objetos de filtro/paginación dedicados exclusivamente a inyectar información al `AdminService`, impidiendo que el controlador deba armar los objetos internos estipulados para el servicio de repositorios.

---

## 🟡 Infracciones LEVES
*(Inconsistencias en el nombrado de puertos o adaptadores)*

**1. Dependencias semi-externas en Domain (Lombok)**
- **Archivo:** `c:\Projects\security-backend\domain\pom.xml` (y clases POJO como `Usuario.java`).
- **Descripción de la infracción:** Se encontró el uso de metadatos `lombok.*` dentro del núcleo (`domain`). Bajo el purismo estricto de la Arquitectura Hexagonal, el modelaje de negocio debe sobrevivir si se desplaza entre contextos, y herramientas como Lombok son dependencias instrumentales de construcción en tiempo de compilación.
- **Sugerencia de corrección:** Generalmente clasificado como *enfoque pragmático* válido, se aconseja simplemente dejar asentadas las razones en la documentación por las cuales Lombok constituye una excepción tolerada en las directrices de pureza del Dominio, de lo contrario reemplácelo con constructores y accesores puros.
