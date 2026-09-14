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
  - `features/catalog-query` — forecasted ledger, date range selection, charting
  - `app/` — layout (Sakai‑style sidebar/top menu), routing and guards, auth, providers, state

### **Backend (API‑JDK)**

- Built with **Java 25**, **Spring Boot 4.0.x**, and **Maven**
- Organized into five Maven modules / bounded contexts:

| Module | Role |
| ------ | ---- |
| `module-api` | REST controllers, DTOs, mappers, security filters, OpenAPI, global exception handling |
| `module-items-bc` | Write‑side domain logic & persistence for items, item types, time periods |
| `module-display-bc` | Read‑side projections — recurrence expansion into the forecasted ledger |
| `module-auth` | Users, roles, authentication, refresh tokens |
| `module-common-bc` | Shared converters, exceptions, sanitization, error logging |

- Connects to **PostgreSQL** (schema `fpfl`)
- Exposes a clean REST API documented with **springdoc‑openapi** (Swagger UI)
- Health exposed via **Spring Boot Actuator**, consumed by the UI's admin `/status` page

For full backend details, see the API README:
👉 [API-JDK/README.md](./API-JDK/README.md)

---

# **🔐 Authentication & Security**

- **Spring Security** with a stateless JWT filter chain (`JwtAuthFilter`, `JwtService`, `SecurityConfig`)
- **JJWT** for token issue/validation; access tokens paired with persisted **refresh tokens** for rotation
- Password hashing via `PasswordEncoderConfig` (BCrypt)
- **Role‑based access** — `User`, `Role`, and `UserRoles` entities drive both API authorization and UI route guards (`AdminRouteGuard`)
- Admin‑only areas of the UI (item type / time period maintenance, `/status`) are filtered out of the menu for non‑admin users

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

---

# 🗂️ Portfolio Samples

A guided tour of the codebase. All links are relative to the repository root.

## Infrastructure

- [.github/workflows/build-push-api.yml](.github/workflows/build-push-api.yml)
- [.github/workflows/build-push-ui.yml](.github/workflows/build-push-ui.yml)
- [k8s/API/api-deployment.yaml](k8s/API/api-deployment.yaml) · [api-service.yaml](k8s/API/api-service.yaml) · [api-ingress.yaml](k8s/API/api-ingress.yaml)
- [k8s/UI/ui-deployment.yaml](k8s/UI/ui-deployment.yaml) · [ui-service.yaml](k8s/UI/ui-service.yaml) · [ui-ingress.yaml](k8s/UI/ui-ingress.yaml)
- [k8s/cluster-issuer.yaml](k8s/cluster-issuer.yaml)

---

## Front End — React 19 / TypeScript

- [UI-TSX](UI-TSX) · [package.json](UI-TSX/package.json) · [vite.config.ts](UI-TSX/vite.config.ts) · [Dockerfile](UI-TSX/Dockerfile)

### App Shell, Routing & Guards

- [App.tsx](UI-TSX/src/app/App.tsx)
- [AppRouter](UI-TSX/src/app/router/AppRouter.tsx)
- [AuthGate](UI-TSX/src/app/router/AuthGate.tsx)
- [AdminRouteGuard](UI-TSX/src/app/router/AdminRouteGuard.tsx)
- [AppLayout](UI-TSX/src/app/layout/AppLayout.tsx)
- [AppSidebar](UI-TSX/src/app/layout/AppSidebar.tsx) · [AppMenu](UI-TSX/src/app/layout/AppMenu.tsx) · [menuModel](UI-TSX/src/app/layout/model/menuModel.ts) · [filterMenu](UI-TSX/src/app/layout/model/filterMenu.ts)

### Authentication & Session

- [authApi](UI-TSX/src/app/auth/api/authApi.ts)
- [useAuth](UI-TSX/src/app/auth/hooks/useAuth.ts) · [useTokenWatcher](UI-TSX/src/app/auth/hooks/useTokenWatcher.ts)
- [sessionStore](UI-TSX/src/app/state/sessionStore.ts)
- [LoginForm](UI-TSX/src/app/auth/components/LoginForm.tsx) · [RegisterForm](UI-TSX/src/app/auth/components/RegisterForm.tsx) · [ChangePasswordForm](UI-TSX/src/app/auth/components/ChangePasswordForm.tsx) · [SessionExpireDialog](UI-TSX/src/app/auth/components/SessionExpireDialog.tsx)

### API Layer

- [client.ts](UI-TSX/src/api/client.ts) — Axios instance, interceptors, refresh handling
- [generated clients](UI-TSX/src/api/generated) — [AuthClient](UI-TSX/src/api/generated/AuthClient.ts) · [ItemClient](UI-TSX/src/api/generated/ItemClient.ts) · [ItemTypeClient](UI-TSX/src/api/generated/ItemTypeClient.ts) · [TimePeriodClient](UI-TSX/src/api/generated/TimePeriodClient.ts) · [InitialAmountClient](UI-TSX/src/api/generated/InitialAmountClient.ts) · [DisplayClient](UI-TSX/src/api/generated/DisplayClient.ts)
- [healthApi](UI-TSX/src/api/status/healthApi.ts)
- [appConfig](UI-TSX/src/config/appConfig.ts)

### Feature — Forecasted Ledger (query side)

- [DisplayPage](UI-TSX/src/features/catalog-query/display/DisplayPage.tsx)
- [LedgerPanel](UI-TSX/src/features/catalog-query/ledger/LedgerPanel.tsx)
- [ChartPanel](UI-TSX/src/features/catalog-query/chart/ChartPanel.tsx)
- [DateRangePanel](UI-TSX/src/features/catalog-query/range/DateRangePanel.tsx)

### Feature — Items & Admin (command side)

- [InitialAmountPage](UI-TSX/src/features/catalog-command/transactions/components/initial-amount/InitialAmountPage.tsx)
- [TimeFrameSelector](UI-TSX/src/features/catalog-command/transactions/components/common/TimeFrameSelector.tsx) · [MonthDayRow](UI-TSX/src/features/catalog-command/transactions/components/common/MonthDayRow.tsx) · [WeekdayRadioGroup](UI-TSX/src/features/catalog-command/transactions/components/common/WeekdayRadioGroup.tsx)
- [ItemTypeTablePage](UI-TSX/src/features/catalog-command/admin/itemType/components/ItemTypeTablePage.tsx)
- [TimePeriodTablePage](UI-TSX/src/features/catalog-command/admin/timePeriod/components/TimePeriodTablePage.tsx)

### Pages & Diagnostics

- [HomePage](UI-TSX/src/app/components/HomePage.tsx)
- [StatusPage](UI-TSX/src/app/components/StatusPage.tsx) — admin‑only UI + API health, 30s refresh
- [AppErrorBoundary](UI-TSX/src/app/providers/error/AppErrorBoundary.tsx) · [ApiErrorBoundary](UI-TSX/src/shared/components/ApiErrorBoundary.tsx)

---

## Back End — Java 25 / Spring Boot 4 (Maven multi‑module)

- [API-JDK/pom.xml](API-JDK/pom.xml) — parent POM, module list, dependency management
- [API-JDK/Dockerfile](API-JDK/Dockerfile) · [docker-compose.yml](API-JDK/docker-compose.yml)

### module-api

- [module-api/pom.xml](API-JDK/module-api/pom.xml)
- [ModuleApiApplication](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/ModuleApiApplication.java)

#### Controllers

- [AuthController](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/controllers/AuthController.java)
- [UserRolesController](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/controllers/UserRolesController.java)
- [ItemController](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/controllers/ItemController.java)
- [ItemTypeController](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/controllers/ItemTypeController.java)
- [TimePeriodController](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/controllers/TimePeriodController.java)
- [InitialAmountController](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/controllers/InitialAmountController.java)
- [DisplayController](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/controllers/DisplayController.java)
- [ClientLogController](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/controllers/ClientLogController.java)

#### Security

- [SecurityConfig](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/security/SecurityConfig.java)
- [JwtAuthFilter](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/security/JwtAuthFilter.java)
- [JwtService](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/security/JwtService.java) · [JwtServiceImpl](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/security/JwtServiceImpl.java)
- [CustomUserDetails](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/security/CustomUserDetails.java)

#### DTOs

- **Auth** — [LoginRequest](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/auth/LoginRequest.java) · [RegisterRequest](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/auth/RegisterRequest.java) · [RefreshTokenRequest](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/auth/RefreshTokenRequest.java) · [ChangePasswordRequest](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/auth/ChangePasswordRequest.java) · [AuthResponse](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/auth/AuthResponse.java)
- **Item** — [ItemRequest](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/item/ItemRequest.java) · [ItemResponse](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/item/ItemResponse.java)
- **ItemType** — [ItemTypeRequest](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/itemtype/ItemTypeRequest.java) · [ItemTypeResponse](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/itemtype/ItemTypeResponse.java) · [UpdateItemTypeNameRequest](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/itemtype/UpdateItemTypeNameRequest.java)
- **TimePeriod** — [TimePeriodRequest](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/timeperiod/TimePeriodRequest.java) · [TimePeriodResponse](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/timeperiod/TimePeriodResponse.java) · [UpdateTimePeriodNameRequest](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/timeperiod/UpdateTimePeriodNameRequest.java)
- **InitialAmount** — [InitialAmountRequest](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/initialamount/InitialAmountRequest.java) · [InitialAmountResponse](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/initialamount/InitialAmountResponse.java)
- **UserRoles** — [UserRoleRequest](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/userroles/UserRoleRequest.java) · [UserRoleResponse](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/userroles/UserRoleResponse.java) · [ReplaceUserRolesRequest](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/userroles/ReplaceUserRolesRequest.java) · [UserRoleListResponse](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/userroles/UserRoleListResponse.java)

#### Mappers

- [ItemMapper](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/mappers/ItemMapper.java)
- [ItemTypeMapper](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/mappers/ItemTypeMapper.java)
- [TimePeriodMapper](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/mappers/TimePeriodMapper.java)
- [InitialAmountMapper](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/mappers/InitialAmountMapper.java)

#### Response Envelope, Config & Exceptions

- [ApiResponse](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/response/ApiResponse.java) · [ApiResponseFactory](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/response/ApiResponseFactory.java)
- [OpenApiConfig](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/config/OpenApiConfig.java) · [WebConfig](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/config/WebConfig.java)
- [GlobalExceptionHandler](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/exception/GlobalExceptionHandler.java)

### module-items-bc

- [module-items-bc/pom.xml](API-JDK/module-items-bc/pom.xml)

#### Domain Contracts

- [ItemService](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/domain/service/ItemService.java) · [ItemTypeService](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/domain/service/ItemTypeService.java) · [TimePeriodService](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/domain/service/TimePeriodService.java)
- [ItemRepository](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/domain/repository/ItemRepository.java) · [ItemTypeRepository](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/domain/repository/ItemTypeRepository.java) · [TimePeriodRepository](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/domain/repository/TimePeriodRepository.java)

#### Application Services

- [ItemServiceImpl](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/application/service/ItemServiceImpl.java)
- [ItemTypeServiceImpl](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/application/service/ItemTypeServiceImpl.java)
- [TimePeriodServiceImpl](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/application/service/TimePeriodServiceImpl.java)

#### Persistence

- **Adapters** — [ItemRepositoryImpl](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/adapter/ItemRepositoryImpl.java) · [ItemTypeRepositoryImpl](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/adapter/ItemTypeRepositoryImpl.java) · [TimePeriodRepositoryImpl](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/adapter/TimePeriodRepositoryImpl.java)
- **JPA repositories** — [JpaItemRepository](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/repository/entity/JpaItemRepository.java) · [JpaItemTypeRepository](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/repository/entity/JpaItemTypeRepository.java) · [JpaTimePeriodRepository](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/repository/entity/JpaTimePeriodRepository.java)
- **Custom queries** — [JpaItemRepositoryCustom](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/repository/custom/JpaItemRepositoryCustom.java) · [JpaItemRepositoryCustomImpl](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/repository/custom/JpaItemRepositoryCustomImpl.java)
- **Entities** — [Item](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/entity/Item.java) · [ItemType](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/entity/ItemType.java) · [TimePeriod](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/entity/TimePeriod.java)
- **Entity mapper** — [ItemEntityMapper](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/mapper/ItemEntityMapper.java)

### module-display-bc

- [module-display-bc/pom.xml](API-JDK/module-display-bc/pom.xml)

#### Ledger Service & Model

- [LedgerReadoutService](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/service/LedgerReadoutService.java) · [LedgerReadoutServiceImpl](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/service/LedgerReadoutServiceImpl.java)
- [Ledger](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/model/Ledger.java) · [LedgerDto](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/model/LedgerDto.java) · [LedgerRequest](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/model/LedgerRequest.java) · [ItemDto](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/model/ItemDto.java)
- [RecurrenceRange](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/utility/RecurrenceRange.java)

#### Recurrence Expanders

- [OneTimeOccurrenceExpander](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/recurrence/OneTimeOccurrenceExpander.java)
- [DailyRecurrenceExpander](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/recurrence/DailyRecurrenceExpander.java)
- [WeeklyRecurrenceExpander](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/recurrence/WeeklyRecurrenceExpander.java)
- [BiWeeklyRecurrenceExpander](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/recurrence/BiWeeklyRecurrenceExpander.java)
- [MonthlyRecurrenceExpander](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/recurrence/MonthlyRecurrenceExpander.java)
- [BiMonthlyRecurrenceExpander](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/recurrence/BiMonthlyRecurrenceExpander.java)
- [QuarterlyRecurrenceExpander](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/recurrence/QuarterlyRecurrenceExpander.java)
- [SemiAnnualRecurrenceExpander](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/recurrence/SemiAnnualRecurrenceExpander.java)
- [AnnualRecurrenceExpander](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/recurrence/AnnualRecurrenceExpander.java)
- [NthWeekdayRecurrenceExpander](API-JDK/module-display-bc/src/main/java/com/financialplanner/moduledisplaybc/recurrence/NthWeekdayRecurrenceExpander.java)

### module-auth

- [module-auth/pom.xml](API-JDK/module-auth/pom.xml)

#### Domain Contracts

- [AuthService](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/domain/service/AuthService.java) · [UserService](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/domain/service/UserService.java) · [RoleService](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/domain/service/RoleService.java) · [UserRolesService](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/domain/service/UserRolesService.java) · [RefreshTokenService](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/domain/service/RefreshTokenService.java)
- [UserRepository](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/domain/repository/UserRepository.java) · [RoleRepository](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/domain/repository/RoleRepository.java) · [UserRolesRepository](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/domain/repository/UserRolesRepository.java) · [RefreshTokenRepository](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/domain/repository/RefreshTokenRepository.java)

#### Application Services

- [AuthServiceImpl](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/application/service/AuthServiceImpl.java)
- [UserServiceImpl](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/application/service/UserServiceImpl.java)
- [RoleServiceImpl](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/application/service/RoleServiceImpl.java)
- [UserRolesServiceImpl](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/application/service/UserRolesServiceImpl.java)
- [RefreshTokenServiceImpl](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/application/service/RefreshTokenServiceImpl.java)

#### Config & Persistence

- [PasswordEncoderConfig](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/config/PasswordEncoderConfig.java) · [TokenProperties](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/config/TokenProperties.java)
- **Entities** — [User](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/infrastructure/persistence/entity/User.java) · [Role](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/infrastructure/persistence/entity/Role.java) · [UserRoles](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/infrastructure/persistence/entity/UserRoles.java) · [UserRolesId](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/infrastructure/persistence/entity/UserRolesId.java) · [RefreshToken](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/infrastructure/persistence/entity/RefreshToken.java)
- **Adapters** — [UserRepositoryImpl](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/infrastructure/persistence/adapter/UserRepositoryImpl.java) · [RoleRepositoryImpl](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/infrastructure/persistence/adapter/RoleRepositoryImpl.java) · [UserRolesRepositoryImpl](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/infrastructure/persistence/adapter/UserRolesRepositoryImpl.java) · [RefreshTokenRepositoryImpl](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/infrastructure/persistence/adapter/RefreshTokenRepositoryImpl.java)
- **JPA repositories** — [JpaUserRepository](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/infrastructure/persistence/repository/JpaUserRepository.java) · [JpaRoleRepository](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/infrastructure/persistence/repository/JpaRoleRepository.java) · [JpaUserRolesRepository](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/infrastructure/persistence/repository/JpaUserRolesRepository.java) · [JpaRefreshTokenRepository](API-JDK/module-auth/src/main/java/com/financialplanner/moduleauth/infrastructure/persistence/repository/JpaRefreshTokenRepository.java)

### module-common-bc

- [module-common-bc/pom.xml](API-JDK/module-common-bc/pom.xml)

#### Converters

- [BooleanToBitConverter](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/converters/BooleanToBitConverter.java)

#### Exceptions

- [DomainException](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/exception/DomainException.java)
- [DomainValidationException](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/exception/DomainValidationException.java)
- [ItemNotFoundException](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/exception/ItemNotFoundException.java)
- [DuplicateItemException](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/exception/DuplicateItemException.java)
- [RepositoryException](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/exception/RepositoryException.java)
- [SanitizationException](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/exception/SanitizationException.java)
- [InvalidCredentialsException](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/exception/InvalidCredentialsException.java)
- [ForbiddenOperationException](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/exception/ForbiddenOperationException.java)

#### Logging

- [ErrorLogger](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/logging/ErrorLogger.java)

#### Sanitizer

- [Sanitizer](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/sanitizer/Sanitizer.java) · [SanitizerImpl](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/sanitizer/SanitizerImpl.java)
- **Annotations** — [StrictText](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/sanitizer/annotations/StrictText.java) · [LenientText](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/sanitizer/annotations/LenientText.java) · [NoSanitize](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/sanitizer/NoSanitize.java)

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
- Database credentials are supplied to the API pod from the `api-db-secret` Kubernetes secret (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) — never committed to the repo

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
docker compose up --build                          # UAT settings
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
