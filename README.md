# **FPFL‑React‑Java — Financial Planner / Forecasted Ledger**

A full‑stack financial planning system built with a **React UI**, a **Java 25 / Spring Boot 4 API**, and a **PostgreSQL** database.  
The project is organized into two top‑level applications:

- **UI‑JSX** → React front‑end
- **API‑JDK** → Modular Java API (multi‑module, DDD‑based)

This repository serves as the **parent project**, containing both applications and their shared development workflow.

---

## **📁 Repository Structure**

```
FPFL-React-Java/
 ├── UI-JSX/        # React front-end application
 ├── API-JDK/       # Java backend (multi-module API)
 ├── LICENSE
 └── README.md      # (this file)
```

### 🔗 Direct Links

- **React UI** → `[Looks like the result wasn't safe to show. Let's switch things up and try something else!]`
- **Java API** → `[Looks like the result wasn't safe to show. Let's switch things up and try something else!]`

Each subproject contains its own dedicated README with deeper details.

---

# **🧭 System Overview**

The FPFL system is designed as a **modular, maintainable, enterprise‑grade full‑stack application**:

### **Frontend (UI‑JSX)**
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
👉 `[Looks like the result wasn't safe to show. Let's switch things up and try something else!]`

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

# **⚙️ Running the Full Stack**

## **1. Start the Backend (API‑JDK)**

From the root:

```bash
mvn -pl API-JDK -am clean install
mvn -pl API-JDK/api spring-boot:run
```

The API runs on **http://localhost:8000**.

---

## **2. Start the Frontend (UI‑JSX)**

From the UI directory:

```bash
cd UI-JSX
npm install
npm start
```

The UI runs on **http://localhost:3000** and communicates with the API.

---

# **🧪 Testing**

### Backend
```bash
mvn -pl API-JDK test
```

### Frontend
```bash
cd UI-JSX
npm test
```

---

# **📦 Production Build**

### Backend
Produces runnable JARs for each bounded context.

### Frontend
```bash
cd UI-JSX
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


