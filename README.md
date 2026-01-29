# Financial Planner Forecast Ledger - React/Java
Financial Planner Forecasted Ledger with a React frontend, Java backend and PostgreSQL database

Absolutely, Rick — here is a **fully expanded, architecture‑rich, diagram‑ready README.md** that you can paste directly into your repository.  
It’s written in clean GitHub‑flavored Markdown, uses ASCII‑safe diagrams, and reflects everything you’ve built into **API‑JDK** so far.

---

# **API‑JDK — Modular Java API Platform**

A production‑ready, multi‑module Java API built with Spring Boot 3, Maven, and domain‑driven design principles.  
The project emphasizes clarity, maintainability, modularity, and a frictionless developer workflow.

---

## **📚 Table of Contents**

- [Overview](#overview)  
- [Architecture](#architecture)  
  - [High‑Level System Diagram](#high-level-system-diagram)  
  - [Module Dependency Graph](#module-dependency-graph)  
  - [DDD Package Structure](#ddd-package-structure)  
- [Modules](#modules)  
- [Key Features](#key-features)  
- [Exception Strategy](#exception-strategy)  
- [DTO Mapping Strategy](#dto-mapping-strategy)  
- [Developer Experience](#developer-experience)  
- [Build & Run](#build--run)  
- [Project Layout](#project-layout)  
- [Onboarding Notes](#onboarding-notes)

---

# **Overview**

API‑JDK is a clean, modular Java API designed for enterprise environments.  
It uses a multi‑module Maven structure to enforce boundaries and keep domain logic isolated from API concerns.

The architecture follows:

- **Domain‑Driven Design (DDD)**
- **Hexagonal / Clean Architecture influences**
- **Strict module boundaries**
- **Predictable error contracts**
- **Swagger/OpenAPI‑driven documentation**

---

# **Architecture**

## **High‑Level System Diagram**

```
                   ┌──────────────────────────────┐
                   │          module-api           │
                   │  - Controllers                │
                   │  - Request/Response Models    │
                   │  - Swagger/OpenAPI            │
                   └───────────────┬──────────────┘
                                   │
                                   ▼
         ┌────────────────────────────────────────────────────┐
         │                    Business Components              │
         │                                                    │
         │  module-items-bc      module-display-bc            │
         │  - Item domain        - Display domain             │
         │  - Validation         - Formatting rules           │
         │  - Services           - Domain logic               │
         └───────────────┬───────────────────────┬──────────┘
                         │                       │
                         ▼                       ▼
                 ┌──────────────┐        ┌──────────────┐
                 │ module-common │        │  Shared Utils │
                 │ - Shared DTOs │        │  - Exceptions │
                 │ - Base types  │        │  - Helpers    │
                 └──────────────┘        └──────────────┘
```

---

## **Module Dependency Graph**

```
module-api
   │
   ├──> module-items-bc
   │         │
   │         └──> module-common-bc
   │
   └──> module-display-bc
             │
             └──> module-common-bc
```

**Rules enforced:**

- Business components never depend on each other.
- All shared logic flows downward into `module-common-bc`.
- `module-api` is the only module exposed to the outside world.

---

## **DDD Package Structure**

```
module-items-bc
 └── src/main/java
      └── com.example.items
           ├── domain
           │     ├── model
           │     ├── rules
           │     └── validation
           ├── application
           │     └── services
           └── infrastructure
                 └── persistence

module-api
 └── com.example.api
       ├── controllers
       ├── dto
       ├── mappers
       └── config
```

This structure keeps domain logic pure and isolated from API concerns.

---

# **Modules**

### **module-api**
- Spring Boot application entry point  
- REST controllers  
- Request/response DTOs  
- OpenAPI/Swagger configuration  
- Global exception handling  

### **module-common-bc**
- Shared domain types  
- Base exceptions  
- Utility classes  
- Cross‑cutting helpers  

### **module-items-bc**
- Item domain model  
- Domain validation  
- Sanitizer logic  
- Services and business rules  
- JPA/Hibernate persistence  

### **module-display-bc**
- Display formatting logic  
- Presentation‑oriented domain rules  
- Reusable display utilities  

---

# **Key Features**

### **Modular Architecture**
- Clear separation of concerns  
- Independent versioning and testing  
- Easy onboarding for new developers  

### **Spring Boot 3**
- Fast startup  
- Native support for Java 21  
- Auto‑configuration for controllers and services  

### **OpenAPI / Swagger**
- Automatic documentation  
- Interactive UI for testing endpoints  
- Clean grouping and metadata  

### **Clean Build Workflow**
- Maven multi‑module build  
- Reproducible Docker builds  
- No duplicate images or stale containers  

---

# **Exception Strategy**

A dedicated, predictable exception hierarchy:

```
SanitizerException
   ├── MissingFieldException
   ├── InvalidFormatException
   └── OutOfRangeException

DomainValidationException
   ├── BusinessRuleViolation
   └── ReferentialIntegrityException
```

Mapped through a centralized `@ControllerAdvice`:

- Consistent HTTP status codes  
- Scanner‑friendly JSON error payloads  
- Clear separation between **sanitization** and **domain validation**  

---

# **DTO Mapping Strategy**

DTO → Entity mapping uses:

- A dedicated mapper layer  
- `getReferenceById()` for foreign key resolution  
- No manual repository lookups in controllers  
- Domain logic stays inside business components  

Example flow:

```
Controller DTO
     │
     ▼
Mapper (resolves FK IDs)
     │
     ▼
Domain Model
     │
     ▼
Service (business rules)
```

---

# **Developer Experience**

- IntelliJ‑optimized multi‑module workflow  
- Clean Docker/Compose setup  
- One‑click run configuration  
- AI‑assisted JavaDoc templates with consistent separators  
- Architecture notes and onboarding docs included  

---

# **Build & Run**

```bash
# Build all modules
mvn clean install

# Run the API
java -jar module-api/target/module-api.jar

# Swagger UI
http://localhost:8080/swagger-ui.html
```

---

# **Project Layout**

```
api-jdk/
 ├── module-api/
 │     ├── src/main/java/com.example.api
 │     └── resources/
 ├── module-common-bc/
 ├── module-items-bc/
 ├── module-display-bc/
 ├── pom.xml
 └── README.md
```

---

# **Onboarding Notes**

- All domain logic lives in business components, not the API module.  
- Controllers must remain thin and delegate to services.  
- New business domains should be added as new modules.  
- Shared logic belongs in `module-common-bc`.  
- Keep exception types scanner‑friendly and predictable.  


