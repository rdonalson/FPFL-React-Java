# Financial Planner: Forecast Ledger — API JDK

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/rdonalson/FPFL-React-Java/actions)
[![Maven Central](https://img.shields.io/badge/maven-available-blue)](https://search.maven.org/)
[![License](https://img.shields.io/badge/license-MIT-lightgrey)](../LICENSE)

A clean, enterprise‑grade, multi‑module Java platform built with **Java 25**, **Spring Boot 4.x**, and **Maven**. The system follows Domain‑Driven Design and is organized into bounded contexts for clarity, testability, and long‑term maintainability.

---

## Overview

**Purpose**  
Provide a modular backend API that stores, manages, and projects user financial planning items (recurring payments, incomes, and scheduled events). The **api** module exposes REST endpoints and is the single runnable entrypoint; domain logic and persistence live in separate bounded contexts.

**Key technologies**  
Java 25 · Spring Boot 4.x · Spring Data JPA · Maven · PostgreSQL 16+

---

## Quick links

- **API module**: `api/` (runnable Spring Boot app)  
- **Docs**: `API-JDK/docs/` (ER diagrams, screenshots)  
- **License**: `../LICENSE` (MIT)

---

## Project structure

Each module is packaged as a **JAR** and built under a unified parent POM.

### API-JDK/

- ├── api/        # REST API, controllers, DTOs, app bootstrap
- ├── auth/       # Authentication and authorization (module-auth)
- ├── items/      # Domain + persistence for source data
- ├── display/    # Read models, projections, query handlers
- ├── common/     # Shared utilities, exceptions, config
- └── pom.xml     # Parent POM (dependencyManagement + modules)

---

## Module responsibilities

### **api**

- Exposes REST endpoints and OpenAPI/Swagger contracts
- Defines DTOs and client‑facing contracts
- Delegates write operations to **items**
- Delegates read operations to **display**
- Owns Spring Boot application bootstrap, datasource, and JPA configuration

### **auth** (new)

- Centralized authentication and authorization concerns
- JWT token issuance and validation
- Security filters, authentication providers, and role/permission mapping
- Integrates with `api` to secure endpoints and with `items`/`display` for user-scoped data access
- Keeps security logic separated from domain logic

### **items**

- Owns the write‑side domain model for financial items
- Handles CRUD operations and domain invariants
- Implements repositories (Spring Data JPA)
- Contains domain services and aggregates

### **display**

- Provides read‑optimized models and projections
- Query handlers and view models shaped for UI consumption
- Keeps read concerns separate from write models for performance and simplicity

### **common**

- Shared utilities, custom exceptions, reusable config, and annotations
- Use sparingly — only for true cross‑cutting concerns

---

## Dependency rules

- No cross‑context leakage: domain logic must remain in its owning bounded context.
- No circular dependencies.
- `api` is the only runnable module.
- `items`, `display`, and `auth` may depend on `common` where appropriate.
- `api` depends on `auth`, `items`, `display`, and `common`.

---

## 🧭 Architecture Overview

![Modular Dependency Diagram](docs/FPFL-API-JDK.png)

---

# 🗄️ PostgreSQL Database Structure

The API module connects to a PostgreSQL database that stores financial planning items, their types, and their recurrence periods.  
Below is a summary of the schema represented in the ER diagram.

---

## **📌 Tables Overview**

### **items**

Stores all user‑defined financial items, including scheduling metadata.

| **Column** | **Description** |
| --- | --- |
| **id** | Primary key |
| **user_id** | Owner of the item (UUID referencing ``users.userID``) |
| **name** | Item name |
| **amount** | Monetary value |
| **fk_item_type** | Foreign key → ``item_types.id`` |
| **fk_time_period** | Foreign key → ``time_periods.id`` |
| **begin_date** | Start date |
| **end_date** | End date |
| **weekly_dow** | Weekly day of week (0–6 or 1–7 depending on app convention) |
| **every_other_week_dow** | Bi‑weekly day of week |
| **bi_monthly_day_1** | First bi‑monthly day |
| **bi_monthly_day_2** | Second bi‑monthly day |
| **monthly_dom** | Monthly day of month |
| **quarterly_1_month** | Q1 month |
| **quarterly_1_day** | Q1 day |
| **quarterly_2_month** | Q2 month |
| **quarterly_2_day** | Q2 day |
| **quarterly_3_month** | Q3 month |
| **quarterly_3_day** | Q3 day |
| **quarterly_4_month** | Q4 month |
| **quarterly_4_day** | Q4 day |
| **semi_annual_1_month** | First semi‑annual month |
| **semi_annual_1_day** | First semi‑annual day |
| **semi_annual_2_month** | Second semi‑annual month |
| **semi_annual_2_day** | Second semi‑annual day |
| **annual_moy** | Annual month of year |
| **annual_dom** | Annual day of month |
| **date_range_req** | Whether a date range is required (flag or descriptor) |

---

### **item_types**

Defines the type/category of an item.

| **Column** | **Description** |
| --- | --- |
| **id** | Primary key |
| **name** | Type name (e.g., rent, salary, subscription) |

---

### **time_periods**

Defines the recurrence period for an item.

| **Column** | **Description** |
| --- | --- |
| **id** | Primary key |
| **name** | Type name (e.g., rent, salary, subscription) |

---

### **users**

Stores application user accounts used by the auth module and referenced by items.

| **Column** | **Description** |
| --- | --- |
| **id** | Primary key (bigserial) used for relational joins |
| **userID** | Application UUID for the user (used as ``items.user_id``) |
| **email** | Unique username or email address |
| **password_hash** | Hashed password (store only salted hashes) |
| **first** | First name |
| **last** | Last name |
| **created_at** | Account creation timestamp |
| **last_login** | Last successful login timestamp |

---

### **roles**

Defines named roles for authorization.

| **Column** | **Description** |
| --- | --- |
| **id** | Primary key (bigserial) |
| **name** | Role name (e.g., ROLE_USER, ROLE_ADMIN) |

---

### **user_roles**

Join table mapping users to roles.

| **Column** | **Description** |
| --- | --- |
| **user_id** | FK → ``users.id`` (links a user to a role) |
| **role_id** | FK → ``roles.id`` (links a role to a user) |

---

### **refresh_tokens**

Tracks refresh tokens for session management and revocation.

| **Column** | **Description** |
| --- | --- |
| **id** | Primary key (bigserial) |
| **token** | Stored token value or token identifier (store hashed token where possible) |
| **user_id** | FK → ``users.id`` (owner of the token) |
| **expiry_date** | Token expiration timestamp |
| **revoked** | Optional boolean flag indicating token revocation (if implemented) |

---

## **🔗 Relationships**

---

## item_types (1) ────< (many) items >──── (1) time_periods

- `items.fk_item_type` → `item_types.id`
- `items.fk_time_period` → `time_periods.id`
- `items.user_id` → `users.userID` (user ownership; userID is UUID)

## users (1) ────< (many) user_roles >──── (1) roles

- `user_roles.user_id` → `users.id`
- `user_roles.role_id` → `roles.id`

## users (1) ────< (many) refresh_tokens

- `refresh_tokens.user_id` → `users.id`

---

![Modular Dependency Diagram](docs/FPFL-JDK-API-ER.png)

The API module exposes endpoints that operate on these tables through the domain logic in the `items` bounded context.

---

## 🚀 Getting Started

### Prerequisites

- Java 25
- Maven 3.9+
- Git
- PostgreSQL 16+

Ensure your `application.yml` contains valid datasource credentials.

---

## Build the entire system

```bash
mvn clean install
```

This compiles, tests, and packages all modules into JAR files.

---

## Run the API module

From the `api` directory:

```bash
mvn spring-boot:run
```

---

Or from the project root:

```bash
mvn -pl api spring-boot:run
```

---

Or run the packaged JAR:


```bash
java -jar target/api-0.0.1-SNAPSHOT.jar
```

---

The API starts on port **8000** by default.

---

## 🧪 Running Tests

```bash
mvn test
```

---

Each module contains its own isolated test suite.

---

## 📦 Packaging

Each bounded context produces a JAR:

---

api/target/api-VERSION.jar
items/target/items-VERSION.jar
display/target/display-VERSION.jar
common/target/common-VERSION.jar

---

The **API module is the only runnable entrypoint**.

---

## 🧰 Development Notes

- Use `application-local.yml` for local overrides (ignored by Git)
- Keep domain logic inside the bounded context that owns it
- Avoid placing business logic in controllers
- Use `common` sparingly — only for true cross‑cutting concerns

---

## 📄 License

This project is licensed under the [MIT License](../LICENSE).

---

---

## 🔒 Patch AssertJ CVE Across Maven Modules

This document outlines the controlled process for identifying, standardizing, and remediating CVE findings related to `org.assertj:assertj-core` across the API‑JDK multi‑module Maven project.

## 📌 Plan Overview

The goal is to baseline current AssertJ CVE findings, align all module versions, apply a patched version centrally, and verify the fix through CVE validation and a full reactor build.

## 🧭 Steps

### 1. Capture Baseline CVE Findings

Run:

```bash
validate_cves_for_java
```

Record all findings related to:

```bash
org.assertj:assertj-core
```

This establishes the before‑state for comparison.

---

### 2. Locate All AssertJ Declarations

Inspect the following module POMs:

- `module-auth/pom.xml`
- `module-common-bc/pom.xml`
- `module-display-bc/pom.xml`
- `module-items-bc/pom.xml`

Identify:

- Direct `assertj-core` declarations
- Any inherited versions coming from `dependencyManagement`

---

### 3. Confirm Effective Versions

Use:

```bash
mvn help:effective-pom
```

Verify:

- Which modules inherit the parent version
- Which modules override it
- Whether any transitive dependencies introduce older AssertJ versions

---

### 4. Standardize the AssertJ Version

Choose a single patched version and apply it consistently.

**Preferred approach:**  
✔ Add the version once in the root `dependencyManagement`  
✔ Remove module‑level overrides

This ensures:

- Version consistency
- Easier future upgrades
- Cleaner module POMs

---

### 5. Re‑Run CVE Validation

Run:

```bash
validate_cves_for_java
```

Compare before/after results to confirm the AssertJ CVE is resolved.

---

### 6. Verify Build & Test Stability

Execute a full reactor build from the project root:

```bash
mvn clean verify
```

Confirm:

- All modules compile
- Tests pass
- No regressions introduced by the dependency update

---

## 🧩 Further Considerations

### ✔ Which patched version should we target?

Options include:

- **Latest stable AssertJ release**
- **Version aligned with Spring Boot BOM**
- **Security‑team mandated minimum version**

### ✔ Where should the version be controlled?

- **Option A (recommended):** Centralize in parent `dependencyManagement`
- **Option B:** Explicitly pin per module (only if module isolation is required)

### ✔ What if the CVE persists transitively?

Evaluate:

- Adding exclusions
- Upgrading the Spring BOM
- Upgrading the parent dependency strategy
