# **FPFL‑React‑Java — Financial Planner / Forecasted Ledger**

A full‑stack financial planning system built with a **React UI**, a **Java 25 / Spring Boot 4 API**, and a **PostgreSQL** database.  
The project is organized into two top‑level applications:

- **[UI-TSX](./UI-TSX/README.md)** → React front‑end
- **[API-JDK](./API-JDK/README.md)** → Modular Java API (multi‑module, DDD‑based)

This repository serves as the **parent project**, containing both applications and their shared development workflow.

---

## **📁 Repository Structure**

```
FPFL-React-Java/
 ├── UI-TSX/        # React front-end application
 ├── API-JDK/       # Java backend (multi-module API)
 ├── LICENSE
 └── README.md      # (this file)
```

### 🔗 Direct Links

- **React UI** → [hhttps://github.com/rdonalson/FPFL-React-Java/tree/main/UI-TSX](https://github.com/rdonalson/FPFL-React-Java/tree/main/UI-TSX)
- **Java API** → [https://github.com/rdonalson/FPFL-React-Java/tree/main/API-JDK](https://github.com/rdonalson/FPFL-React-Java/tree/main/API-JDK)

Each subproject contains its own dedicated README with deeper details.

---

# **🧭 System Overview**

The FPFL system is designed as a **modular, maintainable, enterprise‑grade full‑stack application**:

### **Frontend (UI‑TSX)**

- Built with **React 18+**
- Provides the user interface for managing financial items and forecasting ledgers
- Communicates with the API via REST endpoints
- Uses modern React patterns (hooks, components, routing)

### **Backend (API‑JDK)**

- Built with **Java 25**, **Spring Boot 4.x**, and **Maven**
- Organized into four bounded contexts:
  - `api` — REST controllers, DTOs, contracts
  - `items` — write‑side domain logic & persistence
  - `display` — read‑side projections for UI
  - `common` — shared utilities & exceptions
- Connects to **PostgreSQL**
- Exposes a clean, versioned REST API for the UI

For full backend details, see the API README:  
👉 [https://github.com/rdonalson/FPFL-React-Java/blob/main/API-JDK/README.md](https://github.com/rdonalson/FPFL-React-Java/blob/main/API-JDK/README.md)

---

# **🗄️ Database Overview (PostgreSQL)**

The backend uses a PostgreSQL schema centered around **items**, **item types**, and **time periods**.

### **Core Tables**

- `items` — user‑defined financial items with recurrence metadata
- `item_types` — categories/types of items
- `time_periods` — recurrence periods (weekly, monthly, quarterly, etc.)

The API handles all CRUD operations and business rules for these tables.

A full database breakdown is included in the API README.

---
---

# Portfolio Samples

# Front End — React 18+ (In progress)

### Overall

- [UI-TSX](https://github.com/rdonalson/FPFL-React-Java/tree/main/UI-TSX)

---

# Back End — Java Maven API (In progress)

- [pom.xml](API-JDK/pom.xml)

## Module-API

### POM

- [module-api/pom.xml](API-JDK/module-api/pom.xml)

### Controllers

- [ItemController](https://github.com/rdonalson/FPFL-React-Java/blob/main/API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/controllers/ItemController.java)
- [ItemTypeController](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/controllers/ItemTypeController.java)
- [TimePeriodController](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/controllers/TimePeriodController.java)

### DTOs

- [ItemRequest](https://github.com/rdonalson/FPFL-React-Java/blob/main/API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/item/ItemRequest.java)
- [ItemResponse](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/item/ItemResponse.java)
- [ItemTypeRequest](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/itemtype/ItemTypeRequest.java)
- [ItemTypeResponse](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/itemtype/ItemTypeResponse.java)
- [TimePeriodRequest](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/timeperiod/TimePeriodRequest.java)
- [TimePeriodResponse](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/dtos/timeperiod/TimePeriodResponse.java)

### Mappers

- [ItemMapper](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/mappers/ItemMapper.java)
- [ItemTypeMapper](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/mappers/ItemTypeMapper.java)
- [TimePeriodMapper](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/mappers/TimePeriodMapper.java)

### Exceptions

- [GlobalExceptionHandler](API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/exception/GlobalExceptionHandler.java)

## Module-Items

### POM

- [module-items/pom.xml](API-JDK/pom.xml)

### Services

- [ItemServiceImpl](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/application/service/ItemServiceImpl.java)
- [ItemTypeServiceImpl](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/application/service/ItemTypeServiceImpl.java)
- [TimePeriodServiceImpl](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/application/service/TimePeriodServiceImpl.java)

### Repositories

#### Item

- [ItemRepositoryImpl](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/adapter/ItemRepositoryImpl.java)
- [JpaItemRepository](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/repository/entity/JpaItemRepository.java)
- [JpaItemRepositoryCustomImpl](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/repository/custom/JpaItemRepositoryCustomImpl.java)

#### ItemType

- [ItemTypeRepositoryImpl](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/adapter/ItemTypeRepositoryImpl.java)
- [JpaItemTypeRepository](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/repository/entity/JpaItemTypeRepository.java)

#### TimePeriod

- [TimePeriodRepositoryImpl](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/adapter/TimePeriodRepositoryImpl.java)
- [JpaTimePeriodRepository](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/repository/entity/JpaTimePeriodRepository.java)

### Entities

- [Item](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/entity/Item.java)
- [ItemType](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/entity/ItemType.java)
- [TimePeriod](API-JDK/module-items-bc/src/main/java/com/financialplanner/moduleitemsbc/infrastructure/persistence/entity/TimePeriod.java)

## Module-Common

### POM

- [module-common/pom.xml](API-JDK/module-common/pom.xml)

### Converters

- [BooleanToBitConverter](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/converters/BooleanToBitConverter.java)

### Exceptions

- [DomainException](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/exception/DomainException.java)
- [NotFoundException](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/exception/NotFoundException.java)
- [DuplicateItemException](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/exception/DuplicateItemException.java)
- [DomainValidationException](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/exception/DomainValidationException.java)
- [RepositoryException](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/exception/RepositoryException.java)
- [SanitizationException](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/exception/SanitizationException.java)

### Logging

- [ErrorLogger](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/logging/ErrorLogger.java)

### Sanitizer

- [SanitizerImpl](API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/sanitizer/SanitizerImpl.java)

---
---

# **⚙️ Running the Full Stack**

## **1. Start the Backend (API‑JDK)**

From the root:

```bash
mvn -pl API-JDK -am clean install
mvn -pl module-api spring-boot:run
```

The API runs locally on **<http://localhost:8000>**.
Docker on **<http://localhost:9000>**.

---

## **2. Start the Frontend (UI‑TSX)**

From the UI directory:

```bash
cd UI-TSX
npm install
npm start
```

The UI runs on **<http://localhost:4000>** and communicates with the API on Docke.

---

# **🧪 Testing**

### Backend

```bash
mvn -pl API-JDK test
```

### Frontend

```bash
cd UI-TSX
npm test
```

---

# **📦 Production Build**

### Backend

Produces runnable JARs for each bounded context.

### Frontend

```bash
cd UI-TSX
npm run build
```

Outputs static assets for deployment.

---

# **🧰 Development Notes**

- The UI and API are developed independently but run together during local development.
- The API enforces strict domain boundaries using a multi‑module Maven structure.
- The UI consumes only the public REST contracts exposed by the API.
- PostgreSQL is required for full functionality.

---

# **📄 License**

This project is licensed under the [MIT License](./LICENSE).
