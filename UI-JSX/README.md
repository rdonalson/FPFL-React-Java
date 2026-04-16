# UI-JSX — React Frontend for FPFL-React-Java

This is the React + TypeScript frontend for the FPFL full‑stack system.  
It communicates with the Java 25 / Spring Boot 4 API and provides a clean, modular UI.

---

## 🚀 Tech Stack

- React 18
- TypeScript
- Vite
- React Query (TanStack Query)
- React Router
- Axios (with interceptors)
- ESLint + Prettier (strict mode)
- PrimeReact / PrimeIcons / PrimeFlex

---

## 📁 Project Structure

![API_Welcome_Page.png](./docs/images/UI-JSX%20Overview.png)


### 🟦 public/

![API_Welcome_Page.png](./docs/images/Public.png)

### 🟩 src/

![API_Welcome_Page.png](./docs/images/Src-Root.png)

### 🔌 API Layer (`src/api`)

![API_Welcome_Page.png](./docs/images/Src-Api.png)

### 🧩 Application Layer (`src/app`)

![API_Welcome_Page.png](./docs/images/Src-App.png)

### 🧱 Shared Components (`src/components` + `src/shared`)

![API_Welcome_Page.png](./docs/images/Src-Shared.png)

### 🟨 Feature Modules (`src/features`)

![API_Welcome_Page.png](./docs/images/Src-Features.png)

The structure mirrors the backend’s bounded contexts for clarity and onboarding.

---

## 🔌 API Client

All HTTP requests go through `src/api/client.ts`, which provides:

- Base URL handling
- JSON headers
- Correlation IDs
- Normalized error responses

Domain modules never talk to Axios directly.

---

## 📦 Features

Each domain lives in:
src/features/<domain>/ api/ hooks/ types/ components/

This keeps the UI modular, scalable, and easy to onboard.

---

## 🧪 Running the App

```bash
npm install
npm run dev
```

