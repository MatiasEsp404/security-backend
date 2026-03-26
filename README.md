# 🛡️ security-backend

El proyecto `security-backend` es una aplicación monolítica modular construida con Spring Boot, diseñada para proveer una base robusta para la autenticación de usuarios, autorización y administración del sistema. Implementa una arquitectura en capas inspirada firmemente en los principios de la **Arquitectura Hexagonal (Clean Architecture)**, garantizando una estricta separación entre la lógica de negocio y las preocupaciones de infraestructura.

## 🎯 Propósito Principal
El objetivo primordial de esta base de código es servir como una plantilla backend segura, escalable y mantenible que se encargue de toda la "fontanería" de la seguridad (registro, inicio de sesión, recuperación de contraseñas, verificación de correos electrónicos) para que los desarrolladores puedan enfocarse directamente en las características específicas de su dominio en lugar de reinventar la rueda de la autenticación en cada proyecto.

## ✨ Características Clave
- Autenticación basada en **JWT** con rotación de tokens (Access / Refresh Tokens).
- Control de Accesos Basado en Roles (**RBAC**).
- Auditoría automatizada utilizando **Hibernate Envers**.
- Soporte para mensajería mediante múltiples proveedores de correo electrónico (MailHog/SMTP para dev, SendGrid para prod).
- Documentación automatizada de la API vía **Swagger / OpenAPI 3**.

## 🏗️ Estructura de Módulos (Topología)
El proyecto está dividido en 7 módulos Maven, cada uno con una responsabilidad específica y un flujo de dependencias estrictamente unidireccional hacia el núcleo.

| Módulo | Responsabilidad | Dependencias Base |
|---|---|---|
| **`domain`** | El Core de la aplicación. Entidades puras, reglas de negocio y Puertos. | **Ninguna**. 100% Java puro. Sin dependencias externas. |
| **`application`** | Orquestación de casos de uso y lógica de aplicación. | `domain` |
| **`database`** | Adaptador Secundario (Persistencia). Implementa los repositorios con JPA. | `domain`, H2/BD |
| **`security`** | Filtros de Seguridad, JWT y Auth transversal. | `domain`, `application` |
| **`email`** | Adaptador de Comunicaciones (SendGrid, MailHog). | `domain` |
| **`web`** | Adaptador Primario (API REST). Controladores, DTOs, validaciones web. | `application` |
| **`app-root`** | Ensamblador y configurador Global de Spring Boot. | Depende de **todos**. Punto de entrada ejecutable. |

## 🛠️ Stack Tecnológico
- **Lenguaje:** Java 21
- **Framework:** Spring Boot 3.5.x
- **Seguridad:** Spring Security con JWT
- **Bases de datos:** H2 (Entorno de Desarrollo) / JPA con Hibernate
- **Documentación API:** OpenAPI 3 / Swagger UI
- **Utilidades:** MapStruct (para mapeo DTO-Entity), Lombok (para reducción de boilerplate *solo en capas externas*).

## 🚀 Empezando (Getting Started)

Para correr la aplicación de manera local, la configuración predeterminada en `app-root/src/main/resources/application.properties` se encarga de utilizar H2 como base de datos en memoria y MailHog para interceptar los correos electrónicos.

1. **Construir el proyecto:** 
   Ejecuta el siguiente comando en la raíz del proyecto para descargar las dependencias y compilar los módulos:
   ```bash
   mvn clean install
   ```
2. **Ejecutar la aplicación:** 
   Corre la clase principal `SecurityBackendApplication` ubicada dentro del módulo `app-root`.
3. **Explorar la API (Swagger UI):** 
   Una vez que la aplicación esté corriendo, puedes acceder a la interfaz de documentación interactiva en: 
   👉 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---
> 💡 *Para una documentación más extensa sobre la arquitectura o configuración del entorno, por favor consulta la documentación oficial en [DeepWiki](https://deepwiki.com/MatiasEsp404/security-backend).*
