# Directrices para Agentes de IA en `security-backend`

Este documento contiene las reglas fundamentales que todo Agente de IA (como Claude, OpenCode, Cursor, etc.) debe respetar y priorizar al momento de sugerir, refactorizar o generar código para este proyecto.

## 🎯 Objetivo Principal
El proyecto `security-backend` está desarrollado en **Java 21** y **Spring Boot 3.5**, rigiéndose estrictamente bajo los principios de **Arquitectura Hexagonal (Clean Architecture)**. El objetivo primordial es mantener un núcleo (`domain`) puro, agnóstico y libre de dependencias tecnológicas.

## 🏗️ Topología del Proyecto y Reglas de Dependencia
El código está dividido en múltiples módulos Maven. La regla inquebrantable es que **la dependencia siempre fluye hacia el núcleo (adentro)**:
- **`domain`:** El núcleo puro. **Prohibido** usar dependencias de Spring, Jakarta, JPA, Jackson o Lombok. Todo debe ser Java puro (POJOs, Records, Interfaces/Puertos).
- **`application`:** Casos de uso e interfaces de servicio. Solo depende de `domain` y `spring-context`. **Prohibido** usar Spring Security, metadatos web (Swagger) o JPA en esta capa. Maneja la orquestación.
- **`database`:** Adaptador secundario (JPA). Implementa los repositorios (puertos) del dominio. Solo este módulo conoce sobre entidades `@Entity`, repositorios Spring Data y de base de datos.
- **`security`:** Adaptador transversal y especializado para Autenticación/Autorización (Spring Security, JWT).
- **`web`:** Adaptador primario (REST Controllers). Solo depende de `application`. **Prohibido** invocar o importar directamente clases de `security` o `database` desde aquí.
- **`app-root`:** Orquestador global (Inicializador Spring Boot).

## 🛑 Restricciones Críticas (No negociables)
1. **Sin Lombok en el Dominio**: Totalmente prohibido. El módulo `domain` está custodiado por `maven-enforcer-plugin`. Todo POJO en `domain` debe escribirse con código Java nativo explícito (Constructores, Getters, y métodos `equals`/`hashCode`).
2. **Cero Fugas de Seguridad**: El esquema de autenticación no debe filtrarse. El usuario autenticado se obtiene siempre en la capa `application` invocando el puerto `AuthenticationFacadePort`, **NO** inyectando objetos `Authentication` o anotaciones `@PreAuthorize` en los `@RestController` de `web`. Las reglas se definen en `SecurityConfig`.
3. **Paginación Agóstica**: La capa `web` y `application` **NO** deben utilizar objetos intrínsecos de bases de datos como `PageRequest` o `Page<T>` de Spring Data. Deben usar DTOs y queries construidos explícitamente en la capa de `application`, los cuales luego se mapean por el servicio a las interfaces de persistencia correspondientes.
4. **Mapeo de Datos Aislado**: Los DTOs pertenecen a la capa `web`, las Entities a la capa `database`. Se debe devolver y trabajar siempre con objetos de Dominio puros a través de la capa `application`. Para transformaciones (mapping), se emplea MapStruct.

## 🧩 Protocolo de Resolución para IA
Cuando se solicite agregar una nueva funcionalidad, estructurar el pensamiento y el código en este orden:
1. **Diseño de Dominio (Core):** Define siempre primero en `domain` los modelos, excepciones y los puertos requeridos (ej. `UsuarioRepositoryPort`).
2. **Lógica Aplicativa (Orquestación):** Construye los casos de uso en `application` implementando los servicios. Inyecta los puertos a través de constructores estándar en lugar de `@Autowired` en los campos.
3. **Desarrollo de Adaptadores (Bordes):** Codifica las implementaciones de base de datos en `database` y los controladores/DTOs en `web`, cumpliendo de forma estricta con los contratos definidos por el dominio y las interfaces de la aplicación.
4. **Resguardo de Calidad:** Siempre debes contemplar, o dejar listo para la implementación, los tests unitarios en la capa de `application` y `domain` usando JUnit 5 (Jupiter) y Mockito, sin levantar un contexto completo de Spring de no ser necesario.

> **Regla de Oro:** Antes de proponer un cambio en código o escribir un `import`, realiza la trazabilidad completa en tu contexto. Verifica no estar rompiendo la arquitectura al acoplar dos adaptadores primarios o secundarios (ej: capa `web` pidiendo datos a la clase `security`). En caso de dudas, consulta los archivos `project-context.md` y `architecture.md`.
