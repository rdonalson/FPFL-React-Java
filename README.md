# **FPFL‑React‑Java — Financial Planner / Forecasted Ledger**

A full‑stack financial planning system built with a **React 19 / TypeScript UI**, a **Java 25 / Spring Boot 4 API**, and a **PostgreSQL** database — containerized with Docker, deployed to **Azure Kubernetes Service** through **GitHub Actions**.

The project is organized into two top‑level applications plus shared infrastructure:

- **[UI-TSX](./UI-TSX/README.md)** → React + TypeScript front‑end (Vite)
- **[API-JDK](./API-JDK/README.md)** → Modular Java API (multi‑module, DDD‑based)
- **[k8s](./k8s)** → Kubernetes manifests for both applications
- **[.github/workflows](./.github/workflows)** → CI/CD pipelines

This repository serves as the **parent project**, containing both applications, the deployment manifests, and their shared development workflow.

### 🌐 Live Environment

| Surface | URL |
| ------- | --- |
| UI | <https://ui-tsx.ledger-finance.com> |
| API | <https://api-jdk.ledger-finance.com> |

---

## **📁 Repository Structure**

```
FPFL-React-Java/
 ├── .github/
 │    └── workflows/
 │         ├── build-push-api.yml   # CI/CD: API-JDK → ACR → AKS
 │         └── build-push-ui.yml    # CI/CD: UI-TSX  → ACR → AKS
 ├── UI-TSX/         # React + TypeScript front-end application
 ├── API-JDK/        # Java backend (multi-module API)
 ├── k8s/
 │    ├── API/       # api-deployment / api-service / api-ingress
 │    ├── UI/        # ui-deployment / ui-service / ui-ingress
 │    └── cluster-issuer.yaml   # cert-manager Let's Encrypt issuer
 ├── LICENSE
 └── README.md       # (this file)
```

### 🔗 Direct Links

- **React UI** → [UI-TSX](./UI-TSX)
- **Java API** → [API-JDK](./API-JDK)
- **Kubernetes manifests** → [k8s](./k8s)

Each subproject contains its own dedicated README with deeper details.

---

# **🧭 System Overview**

The FPFL system is designed as a **modular, maintainable, enterprise‑grade full‑stack application**:

### **Frontend (UI‑TSX)**

- **React 19** + **TypeScript** built with **Vite 7** (React Compiler enabled)
- **PrimeReact** component library with **Tailwind CSS** and SCSS overrides
- **TanStack Query** for server state, **TanStack Table** for data grids, **Zustand** for session/theme state
- **React Hook Form + Zod** for form handling and validation, **Axios** for transport, **Chart.js** for ledger charting
- JWT session handling with refresh‑token rotation, expiry warning dialog, and role‑aware routing
- Organized by feature with a command/query split:
  - `features/catalog-command` — items, initial amount, and admin maintenance (item types, time periods)
  - `features/catalog-query` — forecasted ledger, date range selection, charting, Debt Paydown chart and ledger tabs
  - `features/debt-paydown` — track a debt and send a percentage of income items to it
  - `app/` — layout (Sakai‑style sidebar/top menu), routing and guards, auth, providers, state

### **Backend (API‑JDK)**

- Built with **Java 25**, **Spring Boot 4.0.x**, and **Maven**
- Organized into five Maven modules / bounded contexts:

| Module | Role |
| ------ | ---- |
| `module-api` | REST controllers, DTOs, mappers, security filters, OpenAPI, global exception handling |
| `module-items-bc` | Write‑side domain logic & persistence for items, item types, time periods, debts and allocations |
| `module-display-bc` | Read‑side projections — recurrence expansion into the forecasted ledger, debt payoff projections |
| `module-auth` | Users, roles, authentication, refresh tokens |
| `module-common-bc` | Shared converters, exceptions, sanitization, error logging |

- Connects to **PostgreSQL** (schema `fpfl`)
- Exposes a clean REST API documented with **springdoc‑openapi** (Swagger UI)
- Health exposed via **Spring Boot Actuator**, consumed by the UI's admin `/status` page

For full backend details, see the API README:
👉 [API-JDK/README.md](./API-JDK/README.md)

---

# **🔐 Authentication & Security**

- **Spring Security** with a stateless JWT filter chain (`JwtAuthFilter`, `JwtServiceImpl`, `SecurityConfig`)
- **JJWT** (HS256) for access tokens, paired with persisted **refresh tokens**; the UI warns before a session expires and offers to extend it
- **The signing key is never in the repository.** It comes from `JWT_SECRET` (the `api-jwt-secret` Kubernetes secret in production), and the API refuses to start with a missing or weak (< 256‑bit) key. Token lifetime is configuration (`security.jwt.access-token-expiration-ms`, 15 minutes)
- Password hashing via `PasswordEncoderConfig` (BCrypt)
- **Three levels of access**, enforced by the API and mirrored by the UI's route guards and menus:

| Who | Can reach |
| --- | --------- |
| Anyone | Sign in / register / refresh, health, reading item types and time periods |
| Signed‑in user | **Only their own** items, initial amount, forecast, debts and password |
| `ROLE_ADMIN` | Everything above for any user, plus item type / time period maintenance, role management and the `/status` page |

- **Data ownership** is checked in every controller through `CallerAccess`; another user's record answers 404, so ids can't be probed
- **Input and output hygiene** — Bean Validation and record invariants on every request; `SanitizerImpl` cleans text on the way out
- **Consistent errors** — `GlobalExceptionHandler` maps every failure to the right status (400 / 401 / 403 / 404 / 405 / 409) in one `ApiResponse` envelope with a correlation id, and logs it; malformed requests and bad tokens never surface as 500s
- CORS limited to the UI's own origins

---

# **🗄️ Database Overview (PostgreSQL)**

The backend uses a PostgreSQL schema (`fpfl`) centered around **items**, their **classification**, and **authentication**.

### **Domain Entities**

- `Item` — user‑defined financial items with amount, date bounds, and recurrence metadata
- `ItemType` — categories/types of items (credit / debit classification)
- `TimePeriod` — recurrence periods (daily, weekly, bi‑weekly, monthly, quarterly, annual, etc.)

### **Auth Entities**

- `User` — application accounts
- `Role` / `UserRoles` — role assignments (composite key via `UserRolesId`)
- `RefreshToken` — persisted refresh tokens for session renewal

Schema management runs through Hibernate (`ddl-auto=update`) with the default schema set to `fpfl`.

A full database breakdown is included in the API README.

---

# **📈 Forecasted Ledger Engine**

The `module-display-bc` context turns stored items into a date‑ranged, running‑balance ledger. Each recurrence pattern has a dedicated expander:

| Expander | Pattern |
| -------- | ------- |
| `OneTimeOccurrenceExpander` | Single‑date item |
| `DailyRecurrenceExpander` | Every day in range |
| `WeeklyRecurrenceExpander` | Weekly on a chosen weekday |
| `BiWeeklyRecurrenceExpander` | Every other week |
| `MonthlyRecurrenceExpander` | Monthly on a chosen day |
| `BiMonthlyRecurrenceExpander` | Two days per month |
| `QuarterlyRecurrenceExpander` | Quarterly |
| `SemiAnnualRecurrenceExpander` | Twice yearly |
| `AnnualRecurrenceExpander` | Yearly |
| `NthWeekdayRecurrenceExpander` | "Nth weekday of the month" patterns |

`LedgerReadoutService` composes the expanded occurrences with the user's initial amount and returns the ledger the UI renders as both a table and a chart.

### **💳 Debt Paydown**

A user picks a debit (say, a credit card payment), enters what's owed (as of a date, with an optional APR), and sends a percentage of any credits — a paycheck, for example — toward it. `DebtRules` keeps each credit at or under 100% allocated across all debts. `PaydownCalculator` then projects every tracked debt day by day — its own payment, its share of each credit, monthly interest — to a payoff date, and the forecast shows the allocations as extra `Debt Paydown` debits. The UI adds a **Paydown Chart** and **Paydown Ledger** tab to the Display page.

---

# **🧪 Testing**

| App | Tests | Stack | Run |
| --- | ----- | ----- | --- |
| UI-TSX | ≈345 | Vitest, Testing Library, jsdom | `cd UI-TSX && npm test` |
| API-JDK | ≈250 | JUnit 6, Mockito, MockMvc | `cd API-JDK && mvn clean test` |

- **UI** — the API layer, session and auth, layout and routing guards, admin tables, every credit/debit period form and page, the Display page and Debt Paydown. Tests mock the API modules (never the network) through shared helpers in `src/test/`.
- **API** — every module: the sanitizer and exceptions; auth services and refresh tokens; item and debt services, rules and repository adapters; every recurrence pattern (month‑end and leap‑day clamping), the ledger build and payoff math; and every controller over HTTP, including the security rules (public vs. signed‑in vs. admin, and that no one can reach another user's data).
- Neither suite needs a database, and each runs in seconds. The Docker builds skip tests, so run both before pushing.

---

# 🗂️ Portfolio Samples

A guided tour of the codebase. All links are relative to the repository root. The **Highlights** are the best place to start: the files that show how the system is designed, secured and tested. The full map follows them.

## ⭐ Highlights

### Back end — design and domain logic

| File | What it shows |
| ---- | ------------- |
| [LedgerReadoutServiceImpl](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/service/LedgerReadoutServiceImpl.java) | The forecast engine: expands every item's recurrence into dated occurrences and folds them into a running-balance ledger |
| [NthWeekdayRecurrenceExpander](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/recurrence/NthWeekdayRecurrenceExpander.java) · [BiWeeklyRecurrenceExpander](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/recurrence/BiWeeklyRecurrenceExpander.java) | Calendar edge cases: "3rd Friday", "last Monday", every-other-week anchoring, one strategy class per pattern |
| [PaydownCalculator](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/paydown/PaydownCalculator.java) | Pure, side-effect-free payoff projection, day by day: the debt's own payment, its share of each paycheck or other credit, monthly APR interest, and the payoff date (up to 30 years out) |
| [DebtRules](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/domain/debt/DebtRules.java) · [DebtServiceImpl](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/application/service/DebtServiceImpl.java) | Domain invariants kept out of controllers: only your own items, and no credit allocated past 100% |
| [ItemRepositoryImpl](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/adapter/ItemRepositoryImpl.java) | Ports-and-adapters persistence: domain interface, Spring Data underneath, database errors translated into domain exceptions |
| [SanitizerImpl](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/sanitizer/SanitizerImpl.java) | Reflection-based output sanitizing driven by `@StrictText` / `@LenientText` / `@NoSanitize` annotations, safe for records, collections and cycles |
| [GlobalExceptionHandler](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/exception/GlobalExceptionHandler.java) · [ApiResponseFactory](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/response/ApiResponseFactory.java) | One response envelope with correlation ids; every failure mapped to the right status (400 / 401 / 403 / 404 / 405 / 409) instead of a 500 |

### Back end — security

| File | What it shows |
| ---- | ------------- |
| [SecurityConfig](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/security/SecurityConfig.java) | Stateless filter chain: public, signed-in and admin-only routes; CORS locked to the UI's origins |
| [CallerAccess](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/security/CallerAccess.java) · [ItemController](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/controllers/ItemController.java) | Per-user data ownership: users only reach their own data, and another user's record answers 404 so ids can't be probed |
| [JwtServiceImpl](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/security/JwtServiceImpl.java) · [JwtAuthFilter](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/security/JwtAuthFilter.java) | Signing key and lifetime from configuration (Kubernetes secret in production); fails fast on a weak key; forged or expired tokens are rejected cleanly |

### Back end — tests (≈250, JUnit 6 + Mockito)

| File | What it shows |
| ---- | ------------- |
| [SecurityConfigTest](API-JDK/module-api/src/test/java/com/financialplanner/moduleapi/security/SecurityConfigTest.java) | Boots the real filter chain and proves who gets in where, including forged tokens and admin-only paths |
| [ItemAndInitialAmountControllersTest](API-JDK/module-api/src/test/java/com/financialplanner/moduleapi/controllers/ItemAndInitialAmountControllersTest.java) · [ApiTestSupport](API-JDK/module-api/src/test/java/com/financialplanner/moduleapi/support/ApiTestSupport.java) | Controllers tested over HTTP (MockMvc) with the real error handler: status codes, validation messages, "can't touch someone else's data" |
| [PaydownCalculatorTest](API-JDK/module-display-bc/src/test/java/com/financialplanner/moduledisplaybc/paydown/PaydownCalculatorTest.java) · [RecurrenceExpandersTest](API-JDK/module-display-bc/src/test/java/com/financialplanner/moduledisplaybc/recurrence/RecurrenceExpandersTest.java) | Financial math and calendar rules pinned down: interest, payoff dates, month-end and leap-day clamping |

### Front end — React 19 / TypeScript

| File | What it shows |
| ---- | ------------- |
| [client.ts](UI-TSX/src/api/client.ts) | One Axios instance: interceptors add the bearer token and turn every failure into one error shape (status, message, correlation id) for logging and the UI |
| [useTokenWatcher](UI-TSX/src/app/auth/hooks/useTokenWatcher.ts) · [SessionExpireDialog](UI-TSX/src/app/auth/components/SessionExpireDialog.tsx) | Reads the token's expiry and offers to extend the session before it lapses |
| [AdminRouteGuard](UI-TSX/src/app/router/AdminRouteGuard.tsx) · [filterMenu](UI-TSX/src/app/layout/model/filterMenu.ts) | Role-aware routing and menus (the API enforces the same rules) |
| [DebtPaydownPage](UI-TSX/src/features/debt-paydown/components/DebtPaydownPage.tsx) · [paydown.ts](UI-TSX/src/features/catalog-query/paydown/paydown.ts) · [PaydownChartPanel](UI-TSX/src/features/catalog-query/paydown/PaydownChartPanel.tsx) | A full feature end to end: form with validation, server state with TanStack Query, payoff chart |
| [render.tsx](UI-TSX/src/test/render.tsx) · [DebtPaydownPage.test.tsx](UI-TSX/src/features/debt-paydown/components/DebtPaydownPage.test.tsx) | Test harness (router + React Query) and a component test that drives the page like a user (≈345 tests, Vitest + Testing Library) |

### DevOps

| File | What it shows |
| ---- | ------------- |
| [build-push-api.yml](.github/workflows/build-push-api.yml) | Path-filtered CI/CD: build, push to ACR, apply manifests, roll out to AKS and fail the run if it isn't healthy |
| [api-deployment.yaml](k8s/API/api-deployment.yaml) · [api-ingress.yaml](k8s/API/api-ingress.yaml) | Secrets from Kubernetes (never in the repo), resource limits, TLS via cert-manager |
| [API-JDK/Dockerfile](API-JDK/Dockerfile) | Multi-stage build with dependency-layer caching, JRE-only runtime image |

---

## Infrastructure

- [.github/workflows/build-push-api.yml](.github/workflows/build-push-api.yml) · [build-push-ui.yml](.github/workflows/build-push-ui.yml)
- [k8s/API/api-deployment.yaml](k8s/API/api-deployment.yaml) · [api-service.yaml](k8s/API/api-service.yaml) · [api-ingress.yaml](k8s/API/api-ingress.yaml)
- [k8s/UI/ui-deployment.yaml](k8s/UI/ui-deployment.yaml) · [ui-service.yaml](k8s/UI/ui-service.yaml) · [ui-ingress.yaml](k8s/UI/ui-ingress.yaml)
- [k8s/cluster-issuer.yaml](k8s/cluster-issuer.yaml)

---

## Front End — React 19 / TypeScript

- [UI-TSX](UI-TSX) · [package.json](UI-TSX/package.json) · [vite.config.ts](UI-TSX/vite.config.ts) · [vitest.config.ts](UI-TSX/vitest.config.ts) · [Dockerfile](UI-TSX/Dockerfile)

### App Shell, Routing & Guards

- [App.tsx](UI-TSX/src/app/App.tsx) · [AppRouter](UI-TSX/src/app/router/AppRouter.tsx)
- [AuthGate](UI-TSX/src/app/router/AuthGate.tsx) · [AdminRouteGuard](UI-TSX/src/app/router/AdminRouteGuard.tsx)
- [AppLayout](UI-TSX/src/app/layout/AppLayout.tsx) · [AppSidebar](UI-TSX/src/app/layout/AppSidebar.tsx) · [AppMenu](UI-TSX/src/app/layout/AppMenu.tsx) · [menuModel](UI-TSX/src/app/layout/model/menuModel.ts) · [filterMenu](UI-TSX/src/app/layout/model/filterMenu.ts)

### Authentication & Session

- [authApi](UI-TSX/src/app/auth/api/authApi.ts) · [useAuth](UI-TSX/src/app/auth/hooks/useAuth.ts) · [useTokenWatcher](UI-TSX/src/app/auth/hooks/useTokenWatcher.ts) · [sessionStore](UI-TSX/src/app/state/sessionStore.ts)
- [LoginForm](UI-TSX/src/app/auth/components/LoginForm.tsx) · [RegisterForm](UI-TSX/src/app/auth/components/RegisterForm.tsx) · [ChangePasswordForm](UI-TSX/src/app/auth/components/ChangePasswordForm.tsx) · [SessionExpireDialog](UI-TSX/src/app/auth/components/SessionExpireDialog.tsx)

### API Layer

- [client.ts](UI-TSX/src/api/client.ts) — Axios instance; interceptors add the token and normalize errors
- [generated clients](UI-TSX/src/api/generated) — [AuthClient](UI-TSX/src/api/generated/AuthClient.ts) · [ItemClient](UI-TSX/src/api/generated/ItemClient.ts) · [DisplayClient](UI-TSX/src/api/generated/DisplayClient.ts) · [DebtClient](UI-TSX/src/api/generated/DebtClient.ts)
- [appConfig](UI-TSX/src/config/appConfig.ts) · [healthApi](UI-TSX/src/api/status/healthApi.ts)

### Feature — Forecasted Ledger (query side)

- [DisplayPage](UI-TSX/src/features/catalog-query/display/DisplayPage.tsx) · [LedgerPanel](UI-TSX/src/features/catalog-query/ledger/LedgerPanel.tsx) · [ChartPanel](UI-TSX/src/features/catalog-query/chart/ChartPanel.tsx) · [DateRangePanel](UI-TSX/src/features/catalog-query/range/DateRangePanel.tsx)
- **Paydown tabs** — [PaydownChartPanel](UI-TSX/src/features/catalog-query/paydown/PaydownChartPanel.tsx) · [PaydownLedgerPanel](UI-TSX/src/features/catalog-query/paydown/PaydownLedgerPanel.tsx) · [paydown.ts](UI-TSX/src/features/catalog-query/paydown/paydown.ts)

### Feature — Debt Paydown

- [DebtPaydownPage](UI-TSX/src/features/debt-paydown/components/DebtPaydownPage.tsx) · [debtApi](UI-TSX/src/features/debt-paydown/api/debtApi.ts) · [Debt types and rules](UI-TSX/src/features/debt-paydown/types/Debt.ts)

### Feature — Items & Admin (command side)

- [InitialAmountPage](UI-TSX/src/features/catalog-command/transactions/components/initial-amount/InitialAmountPage.tsx)
- [TimeFrameSelector](UI-TSX/src/features/catalog-command/transactions/components/common/TimeFrameSelector.tsx) · [MonthDayRow](UI-TSX/src/features/catalog-command/transactions/components/common/MonthDayRow.tsx) · [WeekdayRadioGroup](UI-TSX/src/features/catalog-command/transactions/components/common/WeekdayRadioGroup.tsx)
- [ItemTypeTablePage](UI-TSX/src/features/catalog-command/admin/itemType/components/ItemTypeTablePage.tsx) · [TimePeriodTablePage](UI-TSX/src/features/catalog-command/admin/timePeriod/components/TimePeriodTablePage.tsx)

### Pages & Diagnostics

- [HomePage](UI-TSX/src/app/components/HomePage.tsx) · [StatusPage](UI-TSX/src/app/components/StatusPage.tsx) — admin-only UI + API health, 30s refresh
- [AppErrorBoundary](UI-TSX/src/app/providers/error/AppErrorBoundary.tsx) · [ApiErrorBoundary](UI-TSX/src/shared/components/ApiErrorBoundary.tsx)

### Tests (Vitest + Testing Library)

- **Harness** — [setup.ts](UI-TSX/src/test/setup.ts) · [render.tsx](UI-TSX/src/test/render.tsx) · [session.ts](UI-TSX/src/test/session.ts)
- **API & session** — [client.test.ts](UI-TSX/src/api/client.test.ts) · [sessionStore.test.ts](UI-TSX/src/app/state/sessionStore.test.ts) · [hooks.test.tsx](UI-TSX/src/app/auth/hooks/hooks.test.tsx)
- **Routing** — [guards.test.tsx](UI-TSX/src/app/router/guards.test.tsx) · [menu.test.ts](UI-TSX/src/app/layout/model/menu.test.ts)
- **Features** — [occurrenceForms.test.tsx](UI-TSX/src/features/catalog-command/transactions/components/items/occurrence/occurrenceForms.test.tsx) · [display.test.tsx](UI-TSX/src/features/catalog-query/display.test.tsx) · [DebtPaydownPage.test.tsx](UI-TSX/src/features/debt-paydown/components/DebtPaydownPage.test.tsx) · [paydown.test.ts](UI-TSX/src/features/catalog-query/paydown/paydown.test.ts)

---

## Back End — Java 25 / Spring Boot 4 (Maven multi‑module)

- [API-JDK/pom.xml](API-JDK/pom.xml) — parent POM, module list, dependency management
- [API-JDK/Dockerfile](API-JDK/Dockerfile) · [docker-compose.yml](API-JDK/docker-compose.yml)

### module-api

- [ModuleApiApplication](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/ModuleApiApplication.java)
- **Controllers** — [AuthController](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/controllers/AuthController.java) · [ItemController](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/controllers/ItemController.java) · [InitialAmountController](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/controllers/InitialAmountController.java) · [DisplayController](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/controllers/DisplayController.java) · [DebtController](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/controllers/DebtController.java) · [ItemTypeController](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/controllers/ItemTypeController.java) · [TimePeriodController](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/controllers/TimePeriodController.java) · [UserRolesController](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/controllers/UserRolesController.java) · [ClientLogController](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/controllers/ClientLogController.java)
- **Security** — [SecurityConfig](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/security/SecurityConfig.java) · [JwtAuthFilter](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/security/JwtAuthFilter.java) · [JwtServiceImpl](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/security/JwtServiceImpl.java) · [CallerAccess](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/security/CallerAccess.java) · [CustomUserDetails](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/security/CustomUserDetails.java)
- **DTOs with validation** — [ItemRequest](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/item/ItemRequest.java) · [DebtRequest](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/debt/DebtRequest.java) · [RegisterRequest](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/auth/RegisterRequest.java) · [ChangePasswordRequest](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/auth/ChangePasswordRequest.java) · [all DTOs](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos)
- **Mappers** — [ItemMapper](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/mappers/ItemMapper.java) · [DebtMapper](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/mappers/DebtMapper.java) · [ItemDraftTranslator](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/mappers/ItemDraftTranslator.java)
- **Responses, config & errors** — [ApiResponse](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/response/ApiResponse.java) · [ApiResponseFactory](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/response/ApiResponseFactory.java) · [GlobalExceptionHandler](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/exception/GlobalExceptionHandler.java) · [OpenApiConfig](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/config/OpenApiConfig.java) · [application.yaml](API-JDK/module-api/src/main/resources/application.yaml)
- **Tests** — [SecurityConfigTest](API-JDK/module-api/src/test/java/com/financialplanner/moduleapi/security/SecurityConfigTest.java) · [JwtServiceImplTest](API-JDK/module-api/src/test/java/com/financialplanner/moduleapi/security/JwtServiceImplTest.java) · [JwtAuthFilterTest](API-JDK/module-api/src/test/java/com/financialplanner/moduleapi/security/JwtAuthFilterTest.java) · [ItemAndInitialAmountControllersTest](API-JDK/module-api/src/test/java/com/financialplanner/moduleapi/controllers/ItemAndInitialAmountControllersTest.java) · [AuthControllerTest](API-JDK/module-api/src/test/java/com/financialplanner/moduleapi/controllers/AuthControllerTest.java) · [GlobalExceptionHandlerTest](API-JDK/module-api/src/test/java/com/financialplanner/moduleapi/exception/GlobalExceptionHandlerTest.java) · [ApiTestSupport](API-JDK/module-api/src/test/java/com/financialplanner/moduleapi/support/ApiTestSupport.java)

### module-items-bc

- **Domain contracts** — [ItemService](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/domain/service/ItemService.java) · [DebtService](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/domain/service/DebtService.java) · [ItemRepository](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/domain/repository/ItemRepository.java) · [DebtRepository](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/domain/repository/DebtRepository.java)
- **Domain rules** — [DebtRules](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/domain/debt/DebtRules.java) · [DebtDraft](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/domain/debt/DebtDraft.java)
- **Application services** — [ItemServiceImpl](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/application/service/ItemServiceImpl.java) · [DebtServiceImpl](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/application/service/DebtServiceImpl.java) · [ItemTypeServiceImpl](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/application/service/ItemTypeServiceImpl.java) · [TimePeriodServiceImpl](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/application/service/TimePeriodServiceImpl.java)
- **Persistence** — [ItemRepositoryImpl](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/adapter/ItemRepositoryImpl.java) · [DebtRepositoryImpl](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/adapter/DebtRepositoryImpl.java) · [JpaItemRepositoryCustomImpl](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/repository/custom/JpaItemRepositoryCustomImpl.java) (Criteria API) · [ItemEntityMapper](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/mapper/ItemEntityMapper.java)
- **Entities** — [Item](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/entity/Item.java) · [Debt](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/entity/Debt.java) · [DebtAllocation](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/entity/DebtAllocation.java) · [ItemType](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/entity/ItemType.java) · [TimePeriod](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/entity/TimePeriod.java)
- **Tests** — [DebtRulesTest](API-JDK/module-items-bc/src/test/java/com/financialplanner/moduleitemsbc/domain/debt/DebtRulesTest.java) · [DebtServiceImplTest](API-JDK/module-items-bc/src/test/java/com/financialplanner/moduleitemsbc/application/service/DebtServiceImplTest.java) · [ItemServiceImplTest](API-JDK/module-items-bc/src/test/java/com/financialplanner/moduleitemsbc/application/service/ItemServiceImplTest.java) · [RepositoryAdaptersTest](API-JDK/module-items-bc/src/test/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/adapter/RepositoryAdaptersTest.java)

### module-display-bc

- **Ledger** — [LedgerReadoutService](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/service/LedgerReadoutService.java) · [LedgerReadoutServiceImpl](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/service/LedgerReadoutServiceImpl.java) · [RecurrenceRange](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/utility/RecurrenceRange.java) · [LedgerDto](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/model/LedgerDto.java)
- **Recurrence expanders** — [all ten](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/recurrence) · [WeeklyRecurrenceExpander](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/recurrence/WeeklyRecurrenceExpander.java) · [BiWeeklyRecurrenceExpander](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/recurrence/BiWeeklyRecurrenceExpander.java) · [MonthlyRecurrenceExpander](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/recurrence/MonthlyRecurrenceExpander.java) · [NthWeekdayRecurrenceExpander](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/recurrence/NthWeekdayRecurrenceExpander.java) · [Weekdays](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/recurrence/Weekdays.java)
- **Debt Paydown** — [PaydownCalculator](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/paydown/PaydownCalculator.java) · [PaydownModels](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/paydown/PaydownModels.java)
- **Tests** — [RecurrenceExpandersTest](API-JDK/module-display-bc/src/test/java/com/financialplanner/moduledisplaybc/recurrence/RecurrenceExpandersTest.java) · [LedgerBuildTest](API-JDK/module-display-bc/src/test/java/com/financialplanner/moduledisplaybc/service/LedgerBuildTest.java) · [PaydownCalculatorTest](API-JDK/module-display-bc/src/test/java/com/financialplanner/moduledisplaybc/paydown/PaydownCalculatorTest.java)

### module-auth

- **Services** — [AuthServiceImpl](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/application/service/AuthServiceImpl.java) · [UserRolesServiceImpl](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/application/service/UserRolesServiceImpl.java) · [RefreshTokenServiceImpl](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/application/service/RefreshTokenServiceImpl.java)
- **Config** — [PasswordEncoderConfig](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/config/PasswordEncoderConfig.java) · [TokenProperties](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/config/TokenProperties.java)
- **Entities** — [User](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/infrastructure/persistence/entity/User.java) · [Role](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/infrastructure/persistence/entity/Role.java) · [UserRoles](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/infrastructure/persistence/entity/UserRoles.java) · [RefreshToken](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/infrastructure/persistence/entity/RefreshToken.java)
- **Persistence** — [UserRolesRepositoryImpl](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/infrastructure/persistence/adapter/UserRolesRepositoryImpl.java) · [RefreshTokenRepositoryImpl](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/infrastructure/persistence/adapter/RefreshTokenRepositoryImpl.java)
- **Tests** — [AuthServiceImplTest](API-JDK/module-auth/src/test/java/com/financialplanner/moduleauth/application/service/AuthServiceImplTest.java) · [RefreshTokenServiceImplTest](API-JDK/module-auth/src/test/java/com/financialplanner/moduleauth/application/service/RefreshTokenServiceImplTest.java)

### module-common-bc

- **Sanitizer** — [SanitizerImpl](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/sanitizer/SanitizerImpl.java) · [StrictText](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/sanitizer/annotations/StrictText.java) · [LenientText](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/sanitizer/annotations/LenientText.java) · [NoSanitize](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/sanitizer/NoSanitize.java)
- **Exceptions** — [domain exception hierarchy](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/exception) (`DomainException`, `DomainValidationException`, `ItemNotFoundException`, `DuplicateItemException`, `ForbiddenOperationException`, …)
- **Cross-cutting** — [ErrorLogger](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/logging/ErrorLogger.java) · [BooleanToBitConverter](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/converters/BooleanToBitConverter.java)
- **Tests** — [SanitizerImplTest](API-JDK/module-common-bc/src/test/java/com/financialplanner/modulecommonbc/sanitizer/SanitizerImplTest.java)

---

# **🚀 CI/CD Pipeline**

Both applications ship through **GitHub Actions → Azure Container Registry → Azure Kubernetes Service**. Each application has its own independent workflow, so a change to one side never rebuilds or redeploys the other.

| Workflow | Watches | Image |
| -------- | ------- | ----- |
| [`build-push-api.yml`](./.github/workflows/build-push-api.yml) | `API-JDK/**`, `k8s/API/*.yaml` | `fpflacr.azurecr.io/fpflapijdkimg` |
| [`build-push-ui.yml`](./.github/workflows/build-push-ui.yml) | `UI-TSX/**`, `k8s/UI/*.yaml` | `fpflacr.azurecr.io/fpfluitsximg` |

### **Triggers**

- `push` to `main` — but **only** when files under that application's paths change
- `workflow_dispatch` — manual run from the Actions tab

### **Job 1 — `build-and-push`**

1. Checkout the repository
2. Log in to **ACR** (`fpflacr.azurecr.io`) using `ACR_USERNAME` / `ACR_PASSWORD`
3. Set up **Docker Buildx**
4. Build the application's multi‑stage `Dockerfile` and push **two tags**: `:${{ github.sha }}` for traceability and `:latest` for the cluster to pull
   - The API build uses **GitHub Actions layer caching** (`cache-from`/`cache-to: type=gha`) to keep Maven dependency resolution fast
   - The UI build passes its Vite configuration in as **build args** (`VITE_API_URL`, `VITE_NODE_ENV`, timeout and session settings), since Vite bakes environment values in at build time

### **Job 2 — `deploy`** (runs only after a successful build)

1. **Azure Login** with the `AZURE_CREDENTIALS` service principal
2. **Set AKS context** — resource group `FPFL-React-Java`, cluster `fpfl-cluster`
3. `kubectl apply` the service, deployment, and ingress manifests for that application
4. `kubectl rollout restart` + `rollout status` (120s timeout) to force the new `:latest` image to be pulled and to fail the run if the rollout doesn't become healthy

### **Required GitHub Secrets**

| Secret | Purpose |
| ------ | ------- |
| `ACR_USERNAME` | Azure Container Registry login |
| `ACR_PASSWORD` | Azure Container Registry password |
| `AZURE_CREDENTIALS` | Service principal JSON used by `azure/login` and `aks-set-context` |

### **Cluster Layout**

- Namespace: **`fpfl`**
- Deployments: `api` (container port **8080**) and `ui` (container port **80**), one replica each with CPU/memory requests and limits
- Services: `fpfl-api-service`, `fpfl-ui-service`
- Ingress: **nginx** ingress controller routing `api-jdk.ledger-finance.com` → API and `ui-tsx.ledger-finance.com` → UI
- TLS: **cert-manager** with the `letsencrypt-prod` `ClusterIssuer` ([`k8s/cluster-issuer.yaml`](./k8s/cluster-issuer.yaml)), storing certs in the `fpfl-api-tls` / `fpfl-ui-tls` secrets
- Database credentials are supplied to the API pod from the `api-db-secret` Kubernetes secret (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`), and the JWT signing key from `api-jwt-secret` (`JWT_SECRET`) — never committed to the repo

### **Pipeline Flow**

```
push to main (path-filtered)
        │
        ▼
  build-and-push ──► docker buildx ──► ACR (:sha and :latest)
        │
        ▼
     deploy ──► azure/login ──► aks-set-context
        │
        ▼
  kubectl apply (service, deployment, ingress)
        │
        ▼
  kubectl rollout restart ──► rollout status (fails the run if unhealthy)
```

---

# **🐳 Containerization**

Both applications use **multi‑stage Docker builds**.

### **API-JDK**

- **Build stage**: `maven:3.9.11-eclipse-temurin-25` — module POMs copied first for dependency caching, then `mvn clean package -DskipTests`
- **Runtime stage**: `eclipse-temurin:25-jre` running the `module-api` fat JAR
- Exposes port **8080**

```bash
cd API-JDK
docker compose up --build
```

### **UI-TSX**

- **Build stage**: `node:20-alpine` — writes `.env.production` from build args, then `npm run build`
- **Runtime stage**: `node:20-alpine` serving the built assets via `vite preview` on port **80**
- `docker-compose.yml` targets a local/UAT API; `docker-compose.prod.yml` targets the public API host

```bash
cd UI-TSX
docker compose up --build                          # UAT settings: UI at http://localhost:9000, API at http://localhost:9100
docker compose -f docker-compose.prod.yml up --build  # production settings
```

---

# **⚙️ Running the Full Stack Locally**

## **1. Start the Backend (API‑JDK)**

From the `API-JDK` directory:

```bash
mvn clean install
mvn -pl module-api spring-boot:run
```

The API runs locally on **<http://localhost:8080>** (Swagger UI at `/swagger-ui.html`).

`JWT_SECRET` can be left unset locally: a development‑only key is used and the API logs a warning.

Database connection settings are read from the `.env` / `.env.docker` / `.env.prod` files — copy and populate these before the first run.

## **2. Start the Frontend (UI‑TSX)**

From the `UI-TSX` directory:

```bash
npm install
npm run dev
```

The Vite dev server runs on **<http://localhost:4000>** and talks to the API at the `VITE_API_URL` defined in `.env.development` (default `http://localhost:8080`).

### Useful UI scripts

| Script | Purpose |
| ------ | ------- |
| `npm run dev` | Vite dev server on port 4000 |
| `npm run build` | Type‑check (`tsc -b`) and build |
| `npm run build:prod` | Production‑mode build |
| `npm run preview` | Serve the built output on port 80 |
| `npm run lint` / `lint:fix` | ESLint |
| `npm run format` | Prettier |

---

# **📦 Production Build**

### Backend

`mvn clean package` produces a runnable Spring Boot fat JAR at `module-api/target/module-api-1.0.0.jar`; the Docker build wraps this in a JRE image.

### Frontend

```bash
cd UI-TSX
npm run build:prod
```

Outputs static assets to `dist/`, served by the container in the runtime stage.

In practice, neither build is run by hand for production — pushing to `main` runs the pipeline described above.

---

# **🧰 Development Notes**

- The UI and API are developed independently but run together during local development.
- The API enforces strict domain boundaries using a multi‑module Maven structure; the UI consumes only the public REST contracts.
- PostgreSQL is required for full functionality.
- Environment values for the UI are **build‑time**, not runtime — changing an API URL means rebuilding the image (which is why the workflow passes them as Docker build args).
- Path filters in the workflows mean a change confined to one application only redeploys that application; edit the `paths:` block when adding new top‑level directories.
- Health and status: the API exposes Actuator health, and the admin‑only `/status` page in the UI polls both UI and API health on a 30‑second refresh.

---

# **📄 License**

This project is licensed under the [MIT License](./LICENSE).
