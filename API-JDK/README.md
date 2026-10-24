# Financial Planner: Forecast Ledger — API JDK

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/rdonalson/FPFL-React-Java/actions)
[![Java](https://img.shields.io/badge/java-25-orange)](https://openjdk.org/projects/jdk/25/)
[![Spring Boot](https://img.shields.io/badge/spring%20boot-4.0.6-brightgreen)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-lightgrey)](../LICENSE)

A clean, enterprise‑grade, multi‑module Java platform built with **Java 25**, **Spring Boot 4.0.x**, and **Maven**. The system follows Domain‑Driven Design and is organized into bounded contexts for clarity, testability, and long‑term maintainability.

---

## Overview

**Purpose**
Provide a modular backend API that stores, manages, and projects user financial planning items (recurring payments, incomes, and scheduled events). The **module-api** module exposes REST endpoints and is the single runnable entrypoint; authentication, domain logic, projections, and persistence live in separate bounded contexts.

**Key technologies**
Java 25 · Spring Boot 4.0.6 · Spring Security · Spring Data JPA · springdoc‑openapi · JJWT · Lombok · Maven · PostgreSQL 16+

**Deployed as**
A multi‑stage Docker image (`fpflapijdkimg`) pushed to Azure Container Registry and rolled out to AKS by GitHub Actions. Live at <https://api-jdk.ledger-finance.com>.

---

## Quick links

- **Runnable module**: `module-api/` (Spring Boot app, the only executable JAR)
- **Docs**: [`docs/`](docs) — architecture and ER diagrams, screenshots, [troubleshooting](docs/TROUBLESHOOTING.md), [security reports](docs/security)
- **CI/CD workflow**: [`../.github/workflows/build-push-api.yml`](../.github/workflows/build-push-api.yml)
- **Kubernetes manifests**: [`../k8s/API`](../k8s/API)
- **License**: [`../LICENSE`](../LICENSE) (MIT)

---

## Project structure

Each module is packaged as a **JAR** and built under a unified parent POM (`com.Financial-Planner:FPFL-V2-Microservice:1.0.0`).

```
API-JDK/
 ├── module-api/          # REST API, controllers, DTOs, security filters, app bootstrap
 ├── module-auth/         # Authentication and authorization domain
 ├── module-items-bc/     # Write-side domain + persistence for financial items
 ├── module-display-bc/   # Read models, recurrence expansion, ledger projections
 ├── module-common-bc/    # Shared utilities, exceptions, sanitization, converters
 ├── docs/                # Diagrams, screenshots, troubleshooting, security reports
 ├── Dockerfile           # Multi-stage build (Maven + Temurin 25 → JRE 25)
 ├── docker-compose.yml   # Local container run
 └── pom.xml              # Parent POM (dependencyManagement + modules)
```

Reactor build order: `module-common-bc` → `module-items-bc` → `module-display-bc` → `module-auth` → `module-api`.

---

## Module responsibilities

### **module-api**

- Exposes REST endpoints and OpenAPI/Swagger contracts (`springdoc-openapi`)
- Defines DTOs, mappers, and the `ApiResponse` envelope for client‑facing contracts
- Owns the Spring Security filter chain, JWT issue and validation (`JwtAuthFilter`, `JwtServiceImpl`), `CustomUserDetails`, and `CallerAccess` (limits each signed‑in user to their own data; admins may act on anyone's)
- Delegates write operations to **module-items-bc** and read operations to **module-display-bc**
- Owns Spring Boot application bootstrap, datasource, JPA, and Actuator configuration
- Centralizes error translation in `GlobalExceptionHandler`: domain exceptions, validation failures, malformed JSON, bad path values, unknown routes and wrong methods all answer 4xx in the `ApiResponse` envelope, never 500

**Controllers:** `AuthController`, `UserRolesController`, `ItemController`, `ItemTypeController`, `TimePeriodController`, `InitialAmountController`, `DisplayController`, `DebtController`, `ClientLogController`

### **module-auth**

- Centralized authentication and authorization domain
- Users, roles, user‑role assignments, and persisted refresh tokens
- JWT signing/parsing via **JJWT**; password hashing via `PasswordEncoderConfig` (`spring-security-crypto`)
- Token signing key and lifetimes configured through `TokenProperties` (`security.jwt.*`)
- Keeps security *domain* logic out of `module-api`, which owns only the filter chain

### **module-items-bc**

- Owns the write‑side domain model for financial items
- Handles CRUD operations and domain invariants for `Item`, `ItemType`, and `TimePeriod`
- Implements repositories through a ports‑and‑adapters split: domain `*Repository` interfaces, `*RepositoryImpl` adapters, and Spring Data `Jpa*Repository` interfaces (plus `JpaItemRepositoryCustom` for hand‑written queries)
- Debt Paydown: `Debt` / `DebtAllocation` entities, `DebtService` and `DebtRules` (only your own debits and credits; a credit can be at most 100% allocated across all of your debts)

### **module-display-bc**

- Provides read‑optimized models and projections shaped for UI consumption
- Expands each item's recurrence metadata into concrete dated occurrences — one expander per pattern (one‑time, daily, weekly, bi‑weekly, monthly, bi‑monthly, quarterly, semi‑annual, annual, nth‑weekday). `Weekdays` accepts Sunday as 0 (what the UI saves) or 7 and skips out‑of‑range values instead of failing the whole forecast
- `LedgerReadoutService` composes occurrences with the user's initial amount into a running‑balance ledger
- `PaydownCalculator` (pure, unit‑tested) projects each tracked debt day by day — the debit's own payment, the allocated share of each credit, monthly interest (APR ÷ 12 at month end) — up to 30 years out for the payoff date. Allocations appear on the main ledger as extra `Debt Paydown` debits
- Depends on `module-items-bc` for the item model; keeps read concerns separate from write models

### **module-common-bc**

- Shared exceptions (`DomainException`, `DomainValidationException`, `ItemNotFoundException`, `DuplicateItemException`, `RepositoryException`, `SanitizationException`, `InvalidCredentialsException`, `ForbiddenOperationException`)
- Input sanitization (`SanitizerImpl` with `@StrictText`, `@LenientText`, `@NoSanitize` annotations)
- `ErrorLogger` and JPA converters (`BooleanToBitConverter`)
- Use sparingly — only for true cross‑cutting concerns

---

## Dependency rules

- No cross‑context leakage: domain logic must remain in its owning bounded context.
- No circular dependencies.
- `module-api` is the only runnable module and the only one with the Spring Boot repackage plugin.
- `module-auth`, `module-items-bc`, and `module-common-bc` are Spring‑agnostic apart from Spring Data JPA — no web layer.
- Actual dependency edges:

```
module-api  ──► module-auth ──► module-common-bc
     │                              ▲
     ├──────► module-display-bc ────┤
     │              │               │
     │              ▼               │
     └──────► module-items-bc ──────┘
```

---

## 🧭 Architecture Overview

![Modular Dependency Diagram](docs/FPFL-API-JDK.png)

---

# 🗄️ PostgreSQL Database Structure

The API module connects to a PostgreSQL database that stores financial planning items, their types, their recurrence periods, and the authentication tables backing `module-auth`.
Below is a summary of the schema represented in the ER diagram.

Schema management runs through Hibernate (`ddl-auto: update`) against the configured default schema.

---

## **📌 Tables Overview**

### **items**

Stores all user‑defined financial items, including scheduling metadata. The recurrence columns are mutually exclusive — only the set matching the item's `fk_time_period` is populated, and `module-display-bc` reads exactly that set when expanding occurrences.

| **Column**               | **Description**                                             |
| ------------------------ | ----------------------------------------------------------- |
| **id**                   | Primary key                                                 |
| **user_id**              | Owner of the item (UUID referencing `users.userID`)         |
| **name**                 | Item name                                                   |
| **amount**               | Monetary value                                              |
| **fk_item_type**         | Foreign key → `item_types.id`                               |
| **fk_time_period**       | Foreign key → `time_periods.id`                             |
| **begin_date**           | Start date                                                  |
| **end_date**             | End date                                                    |
| **weekly_dow**           | Weekly day of week (0–6 or 1–7 depending on app convention) |
| **every_other_week_dow** | Bi‑weekly day of week                                       |
| **bi_monthly_day_1**     | First bi‑monthly day                                        |
| **bi_monthly_day_2**     | Second bi‑monthly day                                       |
| **monthly_dom**          | Monthly day of month                                        |
| **quarterly_1_month**    | Q1 month                                                    |
| **quarterly_1_day**      | Q1 day                                                      |
| **quarterly_2_month**    | Q2 month                                                    |
| **quarterly_2_day**      | Q2 day                                                      |
| **quarterly_3_month**    | Q3 month                                                    |
| **quarterly_3_day**      | Q3 day                                                      |
| **quarterly_4_month**    | Q4 month                                                    |
| **quarterly_4_day**      | Q4 day                                                      |
| **semi_annual_1_month**  | First semi‑annual month                                     |
| **semi_annual_1_day**    | First semi‑annual day                                       |
| **semi_annual_2_month**  | Second semi‑annual month                                    |
| **semi_annual_2_day**    | Second semi‑annual day                                      |
| **annual_moy**           | Annual month of year                                        |
| **annual_dom**           | Annual day of month                                         |
| **date_range_req**       | Whether a date range is required (flag or descriptor)       |

---

### **item_types**

Defines the type/category of an item — the credit/debit classification applied to the ledger.

| **Column** | **Description**                              |
| ---------- | -------------------------------------------- |
| **id**     | Primary key                                  |
| **name**   | Type name (e.g., rent, salary, subscription) |

---

### **time_periods**

Defines the recurrence period for an item. The chosen period determines which recurrence columns on `items` are used.

| **Column** | **Description**                                                        |
| ---------- | ---------------------------------------------------------------------- |
| **id**     | Primary key                                                            |
| **name**   | Period name (e.g., one‑time, daily, weekly, monthly, quarterly, annual) |

---

### **users**

Stores application user accounts used by the auth module and referenced by items.

| **Column**        | **Description**                                         |
| ----------------- | ------------------------------------------------------- |
| **id**            | Primary key (bigserial) used for relational joins       |
| **userID**        | Application UUID for the user (used as `items.user_id`) |
| **email**         | Unique username or email address                        |
| **password_hash** | Hashed password (store only salted hashes)              |
| **first**         | First name                                              |
| **last**          | Last name                                               |
| **created_at**    | Account creation timestamp                              |
| **last_login**    | Last successful login timestamp                         |

---

### **roles**

Defines named roles for authorization.

| **Column** | **Description**                         |
| ---------- | --------------------------------------- |
| **id**     | Primary key (bigserial)                 |
| **name**   | Role name (e.g., ROLE_USER, ROLE_ADMIN) |

---

### **user_roles**

Join table mapping users to roles. Mapped in `module-auth` by `UserRoles` with the composite key `UserRolesId`.

| **Column**  | **Description**                          |
| ----------- | ---------------------------------------- |
| **user_id** | FK → `users.id` (links a user to a role) |
| **role_id** | FK → `roles.id` (links a role to a user) |

---

### **refresh_tokens**

Tracks refresh tokens for session management and revocation.

| **Column**      | **Description**                                                            |
| --------------- | -------------------------------------------------------------------------- |
| **id**          | Primary key (bigserial)                                                    |
| **token**       | Stored token value or token identifier (store hashed token where possible) |
| **user_id**     | FK → `users.id` (owner of the token)                                       |
| **expiry_date** | Token expiration timestamp                                                 |
| **revoked**     | Optional boolean flag indicating token revocation (if implemented)         |

---

## **🔗 Relationships**

### item_types (1) ────< (many) items >──── (1) time_periods

- `items.fk_item_type` → `item_types.id`
- `items.fk_time_period` → `time_periods.id`
- `items.user_id` → `users.userID` (user ownership; userID is UUID)

### users (1) ────< (many) user_roles >──── (1) roles

- `user_roles.user_id` → `users.id`
- `user_roles.role_id` → `roles.id`

### users (1) ────< (many) refresh_tokens

- `refresh_tokens.user_id` → `users.id`

### items (1) ──── (0..1) debts ────< (many) debt_allocations >──── (1) items

- `debts.fk_item` → `items.id` (the debit being paid down; unique, `ON DELETE CASCADE`)
- `debt_allocations.fk_debt` → `debts.id` (`ON DELETE CASCADE`)
- `debt_allocations.fk_credit_item` → `items.id` (the credit; `ON DELETE CASCADE`)

---

## 💳 Debt Paydown

Track paying down a debit (e.g. a credit card payment) by sending a percentage of individual credits to it.

| Table | Columns |
| ----- | ------- |
| **debts** | `id`, `user_id` (owner), `fk_item` (the debit), `balance` numeric(14,2), `apr` numeric(6,3), `balance_date`, `created_at` |
| **debt_allocations** | `id`, `fk_debt`, `fk_credit_item`, `percent` numeric(5,2); unique (`fk_debt`, `fk_credit_item`) |

Hibernate (`ddl-auto: update`) creates both tables; they match API-NET's `db/001_debt_paydown.sql`, so the two APIs share them.

| Method | Path | Notes |
| ------ | ---- | ----- |
| `GET` | `/debts` | The caller's tracked debts with allocations |
| `GET` | `/debts/{id}` | One debt (someone else's answers 404 unless admin) |
| `POST` | `/debts` | `{ itemId, balance, apr?, balanceDate?, allocations: [{ creditItemId, percent }] }` |
| `PUT` | `/debts/{id}` | Same body; the debit can't change |
| `DELETE` | `/debts/{id}` | Stops tracking (the debit stays) |
| `POST` | `/display/paydown` | Same body as `/display/ledger`: per‑debt daily balances, events, totals, payoff date |

All require a JWT; the owner always comes from the token, and only an admin may ask for another user's paydown or ledger. `/display/ledger` now includes allocations as `Debt Paydown` debits (period shows e.g. `25% of Paycheck`).

---

![Entity Relationship Diagram](docs/FPFL-JDK-API-ER.png)

The API module exposes endpoints that operate on these tables through the domain logic in the `module-items-bc` and `module-auth` bounded contexts.

---

## 🚀 Getting Started

### Prerequisites

- Java 25
- Maven 3.9+
- Git
- PostgreSQL 16+
- Docker (optional, for containerized runs)

### Configuration

Runtime configuration lives in [`module-api/src/main/resources/application.yaml`](module-api/src/main/resources/application.yaml), which reads its datasource values from environment variables. **spring-dotenv** loads them from a `.env` file at the `API-JDK` root, so local setup is a matter of creating one:

```dotenv
DB_URL=jdbc:postgresql://localhost:5432/FPFL-V2
DB_USERNAME=postgres
DB_PASSWORD=your-password
```

| File | Used by |
| ---- | ------- |
| `.env` | Local development |
| `.env.docker` | Local Docker Compose runs |
| `.env.prod` | Production‑shaped container runs |

All three are Git‑ignored. In AKS the same three values come from the `api-db-secret` Kubernetes secret instead.

The token signing key is read from `JWT_SECRET` (Base64, at least 256 bits). Locally you can leave it unset and a development-only default is used (the API logs a warning); in AKS it comes from the `api-jwt-secret` Kubernetes secret, which must exist before the deployment will start:

```bash
kubectl create secret generic api-jwt-secret -n fpfl --from-literal=JWT_SECRET="$(openssl rand -base64 48)"
```

A missing or too-short secret stops the API at startup rather than signing weak tokens. Changing the secret signs everyone out.

**Other settings** (`application.yaml`):

| Setting | Value |
| ------- | ----- |
| `spring.jpa.hibernate.ddl-auto` | `update` |
| Hibernate dialect | `PostgreSQLDialect` |
| Actuator endpoints exposed | `health`, `info` (with full details and components) |
| `security.jwt.access-token-expiration-ms` | `900000` (15 minutes) |
| `security.jwt.refresh-token-expiration-ms` | `900000` (15 minutes) |

| `security.jwt.secret` | `${JWT_SECRET}` (dev-only default when unset) |

---

## Build the entire system

From the `API-JDK` directory:

```bash
mvn clean install
```

This compiles, tests, and packages all five modules into JARs.

---

## Run the API module

From the `API-JDK` root:

```bash
mvn -pl module-api spring-boot:run
```

Or from the `module-api` directory:

```bash
mvn spring-boot:run
```

Or run the packaged JAR:

```bash
java -jar module-api/target/module-api-1.0.0.jar
```

The API starts on port **8080** (Spring's default — no `server.port` override is set; the Docker and Kubernetes runtimes pass `SERVER_PORT=8080` explicitly).

| Endpoint | URL |
| -------- | --- |
| Welcome page | <http://localhost:8080/> |
| Swagger UI | <http://localhost:8080/swagger-ui.html> |
| Actuator health | <http://localhost:8080/actuator/health> |

![Swagger UI](docs/API_Swagger_Page.png)

---

## 🐳 Docker

The [`Dockerfile`](Dockerfile) is a two‑stage build: `maven:3.9.11-eclipse-temurin-25` copies the parent and module POMs first so `dependency:go-offline` caches cleanly, builds the fat JAR, then `eclipse-temurin:25-jre` runs it. The runtime image exposes **8080**.

```bash
docker compose up --build
```

`docker-compose.yml` reads `.env.prod` and maps `9100:8080` (API at <http://localhost:9100>).

---

## 🔄 CI/CD

Pushes to `main` that touch `API-JDK/**` or `k8s/API/*.yaml` trigger [`build-push-api.yml`](../.github/workflows/build-push-api.yml):

1. Build and push `fpflacr.azurecr.io/fpflapijdkimg` tagged `:${{ github.sha }}` and `:latest`, with GitHub Actions layer caching for the Maven stage
2. Azure login → set AKS context (`fpfl-cluster`) → `kubectl apply` the service, deployment, and ingress
3. `kubectl rollout restart deployment/api -n fpfl` and wait for `rollout status`

Full pipeline details are in the [root README](../README.md#-cicd-pipeline).

---

## 🧪 Running Tests

```bash
mvn test
```

Each module contains its own isolated test suite. The bounded contexts test with **JUnit Jupiter** and **Mockito**; `module-api` additionally pulls Spring Boot 4's modular test starters (`spring-boot-starter-webmvc-test`, `-data-jpa-test`, `-validation-test`, `-actuator-test`).

```bash
mvn test -pl module-display-bc -am          # one module (plus what it depends on)
mvn test -Dtest=PaydownCalculatorTest -Dsurefire.failIfNoSpecifiedTests=false
```

Current suites (JUnit 5 + Mockito, no database; the web tests use MockMvc and the security test boots only `SecurityConfig` — the whole run takes a few seconds). About 250 tests:

| Module | What's covered |
| ------ | -------------- |
| module-common-bc | `SanitizerImpl` (strict / lenient / URI rules through POJOs, records, collections, maps, arrays, cycles), `BooleanToBitConverter`, `ErrorLogger`, the domain exceptions |
| module-auth | `AuthServiceImpl` (register, login, change password with BCrypt), `UserServiceImpl`, `RoleServiceImpl`, `UserRolesServiceImpl`, `RefreshTokenServiceImpl` (expiry, uniqueness), the repository adapters and the batched role replacement, `PasswordEncoderConfig`, `TokenProperties` |
| module-items-bc | `ItemServiceImpl`, `ItemTypeServiceImpl`, `TimePeriodServiceImpl`, the repository adapters' error translation (409 / 404 / 500), `ItemEntityMapper`, the criteria queries; Debt Paydown's `DebtRules` and `DebtServiceImpl` |
| module-display-bc | Every recurrence pattern (`RecurrenceExpandersTest`, month-end and leap-day clamping, time frames, Sunday as 0 or 7), `RecurrenceRange`, the ledger build (initial amount, credits, debits, running total), every-two-weeks anchoring; Debt Paydown's `PaydownCalculator` and ledger deductions |
| module-api | Every controller over HTTP (status codes, JSON, `Location` headers, validation errors), `GlobalExceptionHandler`, `ApiResponseFactory`, request-record and Bean Validation rules, the mappers, `JwtServiceImpl`, `JwtAuthFilter`, `SecurityConfig` (public vs. protected vs. admin-only paths, CORS), each user limited to their own items, initial amount, forecast and password, `ItemDraftTranslator` |

The Docker build packages with `-DskipTests`, so run `mvn test` before pushing.

---

## 📦 Packaging

Each module produces a JAR under its own `target/`:

- `module-api/target/module-api-1.0.0.jar` — **the only runnable entrypoint** (Spring Boot fat JAR)
- `module-auth/target/module-auth-1.0.0.jar`
- `module-items-bc/target/module-items-bc-1.0.0.jar`
- `module-display-bc/target/module-display-bc-1.0.0.jar`
- `module-common-bc/target/module-common-bc-1.0.0.jar`

---

## 🔒 Security & Dependency Management

**Who can do what**

| Paths | Access |
| ----- | ------ |
| `/auth/login`, `/auth/register`, `/auth/refresh`, `/actuator/health`, `/client-logs`, Swagger | Public |
| `GET /item-types/**`, `GET /time-periods/**` | Public |
| Changing `/item-types/**`, `/time-periods/**`; all of `/users/**` (roles) | `ROLE_ADMIN` |
| `/auth/change-password`, `/items/**`, `/initial-amount/**`, `/display/**`, `/debts/**` | Signed in |

Signed-in users only reach their own data; `CallerAccess` makes the check in each controller and admins may act on anyone's. Another user's item answers 404 (so ids can't be probed), another user's initial amount, forecast or password answers 403, and `GET /items` (every user's items) is admin-only.


- Dependency versions are pinned centrally in the parent POM's `dependencyManagement` — modules declare artifacts without versions so upgrades happen in one place.
- CVE findings, remediations, and triaged false positives are tracked under [`docs/security/`](docs/security):
  - [CVE-VALIDATION-REPORT.md](docs/security/CVE-VALIDATION-REPORT.md)
  - [VULNERABILITY-FIX-SUMMARY.md](docs/security/VULNERABILITY-FIX-SUMMARY.md)
  - [assertj-cve-remediation.md](docs/security/assertj-cve-remediation.md) — the AssertJ standardization runbook
  - [false-positives/](docs/security/false-positives)

---

## 🧰 Development Notes

- Keep domain logic inside the bounded context that owns it
- Avoid placing business logic in controllers — they delegate to services and map through DTOs
- Use `module-common-bc` sparingly — only for true cross‑cutting concerns
- Sanitize user‑supplied strings with the `@StrictText` / `@LenientText` annotations rather than ad‑hoc cleanup
- Never commit `.env` files or a real JWT secret
- See [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) for common local setup problems

---

## 📄 License

This project is licensed under the [MIT License](../LICENSE).
