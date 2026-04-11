# Project progress & checklists

**Single source of truth for what is done.** Mark items with `[x]` when complete, leave `[ ]` when not. Update the **Last updated** line whenever you change this file.

**Last updated:** 2026-04-11

**Current phase:** Phase 2 — Job postings & applications (next)

---

## How to use

1. Work top to bottom within the active phase.
2. Change `- [ ]` to `- [x]` when an item is truly done (build runs, docs accurate).
3. Add short notes under a phase header if something is blocked (e.g. “waiting on GitHub org”).
4. When Phase 0 exit criteria are all satisfied, change **Current phase** to Phase 1 and copy tasks from `DEVELOPMENT_PLAN.md` into the Phase 1 section below.
5. After each coding session, update this file so future you (and collaborators) know the real state.

---

## Phase 0 — Foundations (before feature work)

**Goal:** Repo layout, Docker, env templates, backend/frontend skeletons, CI.  
**Reference:** `DEVELOPMENT_PLAN.md` § Phase 0.

### Prerequisites (your machine & accounts)

- [x] JDK 17+ installed — **repo uses `java.version` 17 in `backend/pom.xml`**
- [x] Node.js installed (`node -v`) — v24+ verified
- [x] Git installed — verified; **local `git init` done** in project root
- [ ] **Remote:** GitHub repository created and this project pushed — *you create the repo and `git remote add` + push*
- [ ] Free-tier accounts for deploy (Neon, Render, Vercel, etc.) — *Phase 8; optional now*
- [x] Product / repo name — **working title: ATS Platform**

### Repository structure & documentation

- [x] `backend/` — Spring Boot 3.5.x, Java 17
- [x] `frontend/` — Vite + React + TypeScript
- [x] Root `README.md` — layout, ports **5433** / **6380**, run instructions
- [x] Root `.gitignore`

### Docker (local dependencies)

- [x] Root `docker-compose.yml` — PostgreSQL (**host `5433`**) + Redis (**host `6380`**) to avoid clashes with local services on `5432`/`6379`
- [ ] (Optional) Mailhog — *Phase 5*
- [x] README documents dev ports and credentials

### Environment templates (no secrets in git)

- [x] `backend/.env.example`
- [x] `frontend/.env.example`

### Backend skeleton

- [x] Web + Actuator + JPA + Postgres driver
- [x] `/actuator/health` (includes `db` when Postgres is up)
- [x] `GlobalExceptionHandler` + `ApiError`
- [x] Logging pattern in `application.yml`
- [x] Default JDBC URL matches Compose: `localhost:5433/ats`
- [x] Tests use H2 in PostgreSQL mode (`@ActiveProfiles("test")`) — **`./mvnw test` passes** (Flyway disabled in test; schema from JPA)

### Frontend skeleton

- [x] Vite + React + TS — **`npm run build`** OK
- [x] ESLint — `npm run lint`
- [x] Prettier — `npm run format` (see `frontend/.prettierrc`)
- [x] React Router: `/`, `/health`, `/login`, `/register`, `/account`
- [x] Vite proxy: `/actuator`, `/api` → `http://localhost:8080`

### CI (GitHub Actions)

- [x] `.github/workflows/ci.yml` — Java **17**, Node **22**, backend test + frontend ci/build
- [ ] **Workflow green on GitHub** — *after you push and enable Actions*

### Git workflow

- [x] README: `feature/*` branches, stable `main`
- [ ] (Optional) Branch protection on `main`

### Phase 0 — Exit criteria

- [x] `docker compose up -d` — verified (Postgres + Redis healthy with remapped ports)
- [x] Backend runs against Compose Postgres — smoke-tested (`/actuator/health` shows `db` **UP**)
- [x] Frontend `npm run build` succeeds
- [ ] CI passes on GitHub — *pending your push*

**Phase 0 status:** Done for local development; remote CI still pending.

---

## Phase 1 — Identity & core domain

**Goal:** Register/login with JWT; roles; Flyway schema for `User`, `Company`, `JobPosting`, `JobApplication` stubs; `GET /api/me`; OpenAPI; tests.  
**Reference:** `DEVELOPMENT_PLAN.md` § Phase 1.

### Delivered

- [x] Spring Security (stateless), BCrypt passwords, JWT access token (configurable expiry; **MVP: no refresh token** — document in `application.yml` / README)
- [x] Roles: `CANDIDATE`, `RECRUITER`, `ADMIN` (self-registration only CANDIDATE/RECRUITER)
- [x] Flyway `V1__baseline.sql` on PostgreSQL; JPA entities + auditing
- [x] API: `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/me`
- [x] OpenAPI 3 + Swagger UI (`/swagger-ui.html`), Bearer scheme
- [x] CORS via `CORS_ORIGINS` / `app.cors.allowed-origins`
- [x] Integration-style tests (`AuthIntegrationTest`) + `BackendApplicationTests`
- [x] Frontend: sign in, register, account summary from `/api/me`
- [x] Probe endpoints: `GET /api/phase1/recruiter-only`, `GET /api/phase1/candidate-only` (403/200 for RBAC checks)

### Phase 1 — Exit criteria (from plan)

- [x] Secured routes require JWT; wrong role → **403**; unauthenticated → **401**
- [x] Demo: Postman or UI login/register flow works locally against Postgres

**Phase 1 status:** Complete. **Next:** Phase 2 (job CRUD, applications, dashboards).

---

## Phase 2+ (checklist TBD)

*Source: `DEVELOPMENT_PLAN.md`.*

---

## Quick links

- Full roadmap: `DEVELOPMENT_PLAN.md`
- Notes: `first_response.md` (if present)
