# Theoretical CRUD Concepts Guide

This document serves as a guide for future developers and Generative AIs to understand the underlying theory and design patterns used when creating a CRUD (Create, Read, Update, Delete) operation in this system.

## 1. Classes vs. Interfaces

In our architecture, we strictly separate *what* needs to be done from *how* it is done. 

*   **Interfaces (The "What"):** An interface defines a contract. It states the methods that are available (e.g., `findById(Long id)`) without specifying how those methods should accomplish their task. Interfaces are crucial for decoupling modules. For example, the `domain` module defines repository ports, which determine the required persistence operations. It does not care if the data is saved in PostgreSQL, MongoDB, or a simple text file.
*   **Classes (The "How"):** A class provides the concrete implementation of an interface. The `application` module provides service classes which implement service interfaces, dictating the actual business logic that occurs upon creation or retrieval.

## 2. Ports and Adapters (Hexagonal Architecture Concept)

Our system uses the concepts of Ports and Adapters to protect the core `domain` logic from external dependencies (like databases, web frameworks, or messaging queues).

*   **Ports:** These are simply **Interfaces** defined inside the `domain` module. They act as "sockets" that external modules can plug into. They are categorized into:
    *   *Driving Ports (Inbound):* Interfaces that the outside world uses to interact with the application (e.g., Use cases).
    *   *Driven Ports (Outbound):* Interfaces that the application uses to interact with the outside world (e.g., a Generic `RepositoryPort`).
*   **Adapters:** These are concrete **Classes** provided by external modules (`web`, `database`) that implement the Ports. For instance, a generic `RepositoryAdapter` in the `database` module implements a `RepositoryPort` to translate our agnostic save operation into a Spring Data JPA call.

## 3. Domain Model (POJOs) vs. JPA Entities

A common pitfall in simpler architectures is using the same class for the core business logic and database persistence. We intentionally separate them:

*   **Domain Model (e.g., a theoretical *BusinessObject*):** This is a pure Java object (POJO) representing the core business concept. It only knows about Java and its own business rules. It is clean, easily testable, and ignorant of how it is serialized or stored.
*   **JPA Entity (e.g., a theoretical *BusinessObjectEntity*):** This class lives exclusively in the `database` layer. It is heavily annotated (`@Entity`, `@Table`, `@Id`) to map directly to a database table. It is structural and bound to the persistence framework.

*Why the separation?*
If we ever change our database from SQL to NoSQL, our `domain` logic (which uses the Domain Model) remains 100% untouched. The adapter is responsible for bridging these two worlds by mapping the Domain Model to the JPA Entity when saving, and vice versa when retrieving.

## 4. The Pragmatic Approach

While we rely heavily on Clean Architecture concepts (like isolating the Domain and using Ports/Adapters), we are programmatic developers. 

We accept the use of framework-specific annotations within internal layers to increase development speed and leverage the ecosystem's power. For example:
*   We use the `@Service` annotation in the `application` layer classes. A purist would argue that `application` should not know about Spring. However, doing so avoids creating verbose configuration classes in `app-root` just to register beans manually. 
*   We accept using `spring-context` to make dependency injection (`@Autowired` / Constructor Injection) seamless across the board. 

This hybrid approach allows us to maintain a clean boundary around the `domain` while still remaining highly productive.
