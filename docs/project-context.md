# 🚀 Contexto y Planificación: security-backend

## 📌 Visión General
**Nombre del Proyecto:** `security-backend`
**Tecnología Principal:** Java 21, Spring Boot 3.5.x, Maven
**Tipo de Arquitectura:** Monolito Modular en Capas (Arquitectura Hexagonal / Clean Architecture)
**Objetivo Principal:** Proveer un backend robusto enfocado en la seguridad, autenticación, y gestión de usuarios, aislando el núcleo de negocio de detalles de infraestructura y dependencias de frameworks externos (como Spring o JPA).

## 🏗️ Estructura de Módulos (Topología)
El proyecto está estructurado lógicamente en múltiples módulos de Maven bajo un modelo de separación de responsabilidades estricto. La regla de oro es que las dependencias fluyan hacia el interior (`domain`):

1. **`domain` (El Núcleo):** Contiene el corazón de la aplicación (entidades POJO, records, enumeraciones, excepciones y "Puertos"/interfaces). *Regla arquitectónica: No posee dependencias externas, ni frameworks (cero Spring, DB o Lombok).*
2. **`application` (Casos de Uso):** Contiene la lógica orquestada. Depende de `domain`. No conoce detalles de DB, Web o esquemas de cifrado/seguridad.
3. **`database` (Persistencia):** Adaptador JPA/Spring Data. Implementa los repositorios del `domain`.
4. **`security` (Seguridad y Auth):** Aislamiento total de Spring Security, filtros JWT, manejos de sesión/cors. Implementa interfaces de autenticación para uso de `application`.
5. **`email` (Comunicaciones):** Adaptador para el manejo de correos electrónicos (ej. SendGrid o MailHog para desarrollo).
6. **`web` (Presentación / API REST):** Es el punto de entrada (Controladores, DTOs, OpenAPI). Accede únicamente a `application`.
7. **`app-root` (Inicialización):** Módulo de ensamblaje. Contiene `@SpringBootApplication`, propiedades de configuración (application.yml) e importa todos los demás módulos para compilar la aplicación final.

## 🛠️ Reglas Arquitectónicas Críticas
En base al estado actual del proyecto (`docs/architecture.md`), nos regimos bajo los siguientes principios:
- **Flujo de Dependencia Unidireccional:** El núcleo NO llama a los bordes. Los detalles (Web, Base de datos) dependen de las abstracciones (Domain).
- **Protección del Dominio (Pure Java):** `domain` utiliza Java puro. Se evita inyectar herramientas pre-compilables como Lombok para asegurar un modelado universal y portátil, protegido por `maven-enforcer-plugin`.
- **Desacople en la Capa Web:** No interactuar con componentes de `security` ni de persistencia (como constructores de Page u objetos de filtrado) desde los `Controllers`. El adaptador Web está aislado de la base de datos y de la seguridad.

## 📜 Estado Actual (Marzo 2026)
- **Deuda Arquitectónica:** Saneada con éxito. Se corrigieron fugas críticas de *Swagger* a *Application*, *Spring Security* a los casos de uso, e invocaciones cruzadas irrelevantes entre adaptadores. 
- **Verificación:** El código actual demuestra un cumplimiento estricto del patrón.

## 🎯 Plan de Trabajo para Nuevas Funcionalidades
A la hora de implementar futuras tareas, el *workflow* predeterminado para el agente será:
1. **Modelar el Negocio (1er Paso):** Definir entidades y puertos (`ports/in` o `ports/out`) en el módulo `domain`.
2. **Definir la Orquestación (2do Paso):** Implementar la lógica en la capa `application` y orquestar las llamadas a los puertos.
3. **Construir Adaptadores (3er Paso):** Programar el guardado/consulta en `database`, la exposición en la capa `web` (REST) o servicios de terceros (`email`, `security`).
4. **Validación:** Garantizar mediante tests unitarios que la lógica del dominio persiste estable y las capas respetan las inyecciones de dependencias de Spring.
