# Project progress & checklists

**Single source of truth for what is done.** Mark items with `[x]` when complete, leave `[ ]` when not. Update the **Last updated** line whenever you change this file.

**Last updated:** 2026-04-11

**Current focus (priority order):**  
1) **Production deploy (MVP)** — follow **`DEVELOPMENT_WORKFLOW.md`** + **`DEPLOYMENT.md`** until smoke tests pass in prod.  
2) **CI/CD-style iteration** — branch → PR → CI green → merge → deploy (auto-deploy from `main` on Vercel/Render is enough).  
3) **Product backlog** — Phase 4+ after the public URL works (see `DEVELOPMENT_PLAN.md`).

**Roadmap phase (product):** Phase 4 — analytics & candidate polish — **after** MVP deploy unless you explicitly parallelize.

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
- [x] **Remote:** GitHub repository — **pushed** (`origin/main`; update this note if the canonical remote changes)
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
- [x] Tests use H2 in PostgreSQL mode (`@ActiveProfiles("test")`) — **`./mvnw test` passes** (Flyway disabled in test; schema from JPA; datasource pinned via `@DynamicPropertySource` so shell `SPRING_DATASOURCE_URL` does not break tests)

### Frontend skeleton

- [x] Vite + React + TS — **`npm run build`** OK
- [x] ESLint — `npm run lint`
- [x] Prettier — `npm run format` (see `frontend/.prettierrc`)
- [x] React Router: `/`, `/health`, `/login`, `/register`, `/account`, `/jobs`, recruiter & candidate flows
- [x] Vite proxy: `/actuator`, `/api` → `http://localhost:8080`

### CI (GitHub Actions)

- [x] `.github/workflows/ci.yml` — Java **17**, Node **22**, backend test + frontend ci/build
- [ ] **Workflow green on GitHub** — *confirm in **Actions** tab on latest `main` (re-run if needed)*

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

**Phase 1 status:** Complete.

---

## Phase 2 — Job postings & applications

**Goal:** Recruiter CRUD jobs; candidate browse/apply; application status + notes; paginated APIs; TanStack Query + forms on the UI.  
**Reference:** `DEVELOPMENT_PLAN.md` § Phase 2.

### Delivered

- [x] Flyway `V2__application_notes.sql` — optional recruiter notes on `applications`
- [x] Public read: `GET /api/jobs`, `GET /api/jobs/{id}` (open jobs only for detail); **no JWT required**
- [x] Candidate: `POST /api/jobs/{jobId}/applications`, `GET /api/me/applications` (paged)
- [x] Recruiter: `GET/POST/PUT/DELETE /api/recruiter/jobs`, `GET .../jobs/{id}/applications`, `PATCH /api/recruiter/applications/{id}`
- [x] DTOs + validation; `Pageable` normalized (max page size 100); `NotFoundException` → 404
- [x] Services: `JobPostingService`, `JobApplicationService`; JPA Specifications for public job search (`q`, `location`)
- [x] Tests: `Phase2JobFlowIntegrationTest` (end-to-end recruiter → candidate → patch)
- [x] Frontend: TanStack Query, React Hook Form + Zod for job form, role-gated routes (`RequireRole`), jobs list/detail, recruiter dashboard, applications table

### Phase 2 — Exit criteria (from plan)

- [x] Recruiter creates job → candidate applies → recruiter changes status (see README **Phase 2 — happy path**)

**Phase 2 status:** Complete.

---

## Phase 3 — Resume upload & ATS scoring

**Goal:** Safe file storage, text extraction, deterministic match score + explanations for recruiters.  
**Reference:** `DEVELOPMENT_PLAN.md` § Phase 3. Narrative: `PROJECT_JOURNEY.md`.

### Delivered

- [x] Flyway `V3__resume_and_match_score.sql` — resume columns + `match_score` / `match_reasons` on `applications`
- [x] Local filesystem storage (`app.storage.local.root-directory` / `RESUME_STORAGE_ROOT`) with a clear swap path for S3-style storage later
- [x] Apache Tika text extraction (PDF/DOCX); size and MIME validation
- [x] `AtsMatchScoringService` — v1 rule-based score (0–100) + bullet reasons
- [x] `POST /api/me/applications/{id}/resume` (multipart); candidate-only, own application
- [x] DTOs: `MyApplicationResponse` / `RecruiterApplicationResponse` extended with score + resume fields
- [x] Spring Data **PagedModel** JSON (`WebConfig` + `VIA_DTO`); frontend `normalizeSpringPage` for both shapes
- [x] Integration test `Phase3ResumeIntegrationTest` (PDFBox **2.x** test scope aligned with Tika)
- [x] UI: upload on **My applications**; score + resume columns for recruiters

### Phase 3 — Exit criteria (from plan)

- [x] Upload → score visible on application row; job text drives scoring (re-upload re-scores with current job description)

**Phase 3 status:** Complete for v1 (local storage + deterministic scorer). Optional later: embeddings, virus scan, Redis-backed re-scoring cache.

---

## Phase 4+ (see plan)

*Source: `DEVELOPMENT_PLAN.md`.*

---

## Quick links

- **What to do next (deploy first, then features):** `DEVELOPMENT_WORKFLOW.md`
- Full roadmap: `DEVELOPMENT_PLAN.md`
- Narrative (living doc): `PROJECT_JOURNEY.md`
- Deploy MVP (hosts, env): `DEPLOYMENT.md`
- Notes: `first_response.md` (if present)
