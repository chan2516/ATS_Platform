# ATS Platform (portfolio)

Monorepo: **Spring Boot** API (`backend/`) + **Vite React** UI (`frontend/`). See `DEVELOPMENT_PLAN.md` for the full roadmap and `PROJECT_PROGRESS.md` for phase checklists.

## Prerequisites

- JDK 17+ (project targets **17** for broad LTS support; JDK 21 also works if you set `java.version` in `backend/pom.xml` to `21`).
- Node.js LTS
- Docker (for PostgreSQL + Redis via Compose)

## Local development

### 1. Start infrastructure

```bash
docker compose up -d
```

Defaults: PostgreSQL **`localhost:5433`** → container `5432` (database `ats`, user/password `ats`), Redis **`localhost:6380`** → container `6379`. These host ports avoid common clashes with local Postgres/Redis on `5432`/`6379`.

### 2. Backend

```bash
cd backend
./mvnw spring-boot-run
```

On Windows use `mvnw.cmd spring-boot-run`. Health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Open [http://localhost:5173](http://localhost:5173). The **API health** page calls the backend via the Vite dev proxy (`/actuator` → `http://localhost:8080`).

Optional: copy `frontend/.env.example` to `frontend/.env` and set `VITE_API_BASE_URL` when not using the proxy (e.g. production build against a remote API).

## Git workflow

Use `feature/*` branches; keep `main` stable. Enable CI on push/PR to `main` (see `.github/workflows/ci.yml`).

## Product name

Working title: **ATS Platform** — update this README and UI copy when you finalize branding.
