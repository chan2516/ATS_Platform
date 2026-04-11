# ATS Platform (portfolio)

Monorepo: **Spring Boot** API (`backend/`) + **Vite React** UI (`frontend/`).

- **`DEVELOPMENT_WORKFLOW.md`** — **start here:** deploy MVP first, then iterate with CI/CD-style habits; then Phase 4+ backlog.
- `DEVELOPMENT_PLAN.md` — full roadmap · `PROJECT_PROGRESS.md` — checklists · `PROJECT_JOURNEY.md` — narrative history · `DEPLOYMENT.md` — hosting and env vars.

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
./mvnw spring-boot:run
```

On Windows use `mvnw.cmd spring-boot:run`. Health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

Set `JWT_SECRET` (32+ bytes for HS256) in the environment for non-default signing; example PowerShell before starting:

`$env:JWT_SECRET = "your-long-random-secret-at-least-32-chars"`

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Open [http://localhost:5173](http://localhost:5173). The **API health** page calls the backend via the Vite dev proxy (`/actuator` and `/api` → `http://localhost:8080`).

### Phase 1 — Auth & API docs

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) (API running).
- **Auth:** Register and sign in from the UI (`/register`, `/login`), or call `POST /api/auth/register` and `POST /api/auth/login`. Responses include a **Bearer JWT** (`accessToken`). Use `Authorization: Bearer <token>` for `GET /api/me` and other secured routes.
- **JWT:** Set a long random `JWT_SECRET` (at least 32 bytes for HS256) in the environment when not using the dev default — see `backend/.env.example`.

Optional: copy `frontend/.env.example` to `frontend/.env` and set `VITE_API_BASE_URL` when not using the proxy (e.g. production build against a remote API).

## Phase 2 — Jobs & applications (happy path)

1. Start Postgres (Compose) and the API (`mvnw.cmd spring-boot:run` in `backend/`).
2. Register a **recruiter** with a **company name**, then open **Recruiter →** in the UI and create a job (or use `POST /api/recruiter/jobs`).
3. Register a **candidate**, open **Jobs**, open the posting, and **Apply** (or `POST /api/jobs/{id}/applications`).
4. As the recruiter, open **Applications** for that job and update status / notes (`PATCH /api/recruiter/applications/{id}`).

Public **GET `/api/jobs`** and **GET `/api/jobs/{id}`** do not require a JWT; applying and recruiter routes do.

## Phase 3 — Resumes & match score

1. Complete the Phase 2 apply flow so the candidate has an **application id** (visible in **My applications** or from `GET /api/me/applications`).
2. **Upload** a PDF or DOCX: `POST /api/me/applications/{applicationId}/resume` with multipart field `file`, or use **Upload resume** on **My applications**.
3. The API stores the file under `./data/resumes` by default (`RESUME_STORAGE_ROOT` / `app.storage.local.root-directory`), extracts text with **Apache Tika**, and stores a **0–100 match score** plus short reasons derived from the job title/description vs resume text.
4. Recruiters see **Match** and **Resume** on the job’s **Applications** screen.

Paginated JSON uses Spring Data **PagedModel** (`content` + `page.{number,size,totalElements,totalPages}`). The frontend normalizes both that shape and the older flat page JSON.

## Deployment (production demo)

See **`DEPLOYMENT.md`** for what to skip vs ship first, how **GitHub Student Pack** perks map to hosting (Vercel, DigitalOcean, Azure, etc.), env vars (`JWT_SECRET`, `CORS_ORIGINS`, `VITE_API_BASE_URL`), and caveats (resume storage on ephemeral disks).

## Git workflow

Use `feature/*` branches; keep `main` stable. Enable CI on push/PR to `main` (see `.github/workflows/ci.yml`).

## Product name

Working title: **ATS Platform** — update this README and UI copy when you finalize branding.
