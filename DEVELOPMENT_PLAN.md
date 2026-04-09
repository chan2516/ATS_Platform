# End-to-End Development Plan  
## AI-Assisted Interview & ATS Platform

This document is the single reference for **what to build**, **in what order**, **what must be ready before each phase**, and **how to ship and showcase** the app. Use it when planning sprints or when you return to the project after a break.

---

## 1. Product vision (one sentence)

A multi-role SaaS where **recruiters** manage job postings and candidate pipelines, **candidates** apply with resumes and go through scoring and (optionally) AI-assisted interviews, with a **clean API + modern UI** suitable for portfolio demos.

---

## 2. Locked stack (adjust only if you have a strong reason)

| Layer | Choice |
|--------|--------|
| Backend | Java 17+ (repo targets 17 in `pom.xml`; 21 OK if you bump `java.version`), Spring Boot 3.x |
| API | REST, OpenAPI 3 (Swagger UI) |
| Auth | Spring Security, JWT, role-based access (CANDIDATE, RECRUITER, ADMIN) |
| DB | PostgreSQL, Flyway migrations |
| Cache / rate limits | Redis (Upstash in cloud) |
| Frontend | React 18+, TypeScript, Vite |
| UI | Tailwind CSS + shadcn/ui (or similar) |
| HTTP client / server state | TanStack Query |
| Forms | React Hook Form + Zod |
| Local dev | Docker Compose (Postgres + Redis + optional Mailhog) |
| CI | GitHub Actions (backend test, frontend lint/build) |

**Optional later:** message queue (Kafka/RabbitMQ), WebSockets for live updates, separate admin service.

---

## 3. Phase 0 — Before you write feature code

**Goal:** Repo structure, environments, and conventions so later phases do not need rewrites.

### 3.1 Prerequisites (do these first)

- [ ] JDK 21 and a current Node LTS installed locally.
- [ ] GitHub repo created (public is fine for portfolio).
- [ ] Accounts ready (free tier): GitHub, one DB host (e.g. Neon), one app host (e.g. Render), optional Vercel for frontend.
- [ ] Decide product name and repo name (consistent URLs in README).

### 3.2 Deliverables

- [ ] Monorepo or two repos: `backend/` (Spring Boot) and `frontend/` (Vite React), documented in root README.
- [ ] Root `docker-compose.yml`: PostgreSQL + Redis (+ optional Mailhog for email dev).
- [ ] `.env.example` for backend and frontend (never commit secrets).
- [ ] Backend: Spring Boot skeleton with Actuator health, structured logging pattern, global exception handler placeholder.
- [ ] Frontend: Vite + TS + ESLint + Prettier; one routed page (e.g. `/` and `/health` or API ping).
- [ ] GitHub Actions: backend `./mvnw test` (or Gradle), frontend `npm ci && npm run build`.
- [ ] Branch strategy: `main` protected; work in `feature/*` branches.

### 3.3 Exit criteria

- `docker compose up` brings DB/Redis; backend connects; frontend builds; CI green on sample push.

---

## 4. Phase 1 — Identity, security baseline, and core domain model

**Goal:** Users can register/login with roles; database reflects core entities without full business workflows yet.

### 4.1 Prerequisites (from Phase 0)

- Compose and CI working.
- PostgreSQL reachable from backend.

### 4.2 Main features

- User registration and login (email + password; password hashed with BCrypt or Argon2 via Spring Security defaults).
- JWT issuance and refresh strategy (either short-lived access + refresh cookie or documented single-token MVP—pick one and document it).
- Roles: `CANDIDATE`, `RECRUITER`, `ADMIN`.
- Entities (minimum): `User`, `Company` (optional but good for recruiter context), `JobPosting` (stub fields), `Application` (stub linking user ↔ job).
- API: auth endpoints, `GET /api/me`, health already via Actuator.

### 4.3 Before starting Phase 2

- [ ] Flyway migration V1 with tables and indexes for the above.
- [ ] Integration tests for auth (Testcontainers optional but valuable).
- [ ] CORS configured for local frontend origin; production origins via env.

### 4.4 Exit criteria

- Swagger lists secured routes; wrong role gets 403; unauthenticated gets 401.
- You can demo login from Postman or a minimal React login form.

---

## 5. Phase 2 — Job postings and applications (CRUD + state machine lite)

**Goal:** Recruiters create jobs; candidates browse and apply; applications have a simple status.

### 5.1 Prerequisites

- Phase 1 complete; roles enforced on controllers.

### 5.2 Main features

- **Recruiter:** CRUD job postings (title, description, location, employment type, salary range optional, open/closed flag).
- **Candidate:** List/search open jobs (pagination, basic filters).
- **Candidate:** Submit application to a job (one application per candidate per job).
- **Application status:** e.g. `SUBMITTED`, `SCREENING`, `INTERVIEW`, `OFFER`, `REJECTED`, `WITHDRAWN` (keep enum in DB).
- **Recruiter:** List applications per job; update status; notes field optional.

### 5.3 Backend practices

- DTOs + validation (`@Valid`); no entity leakage in API responses.
- Pagination on list endpoints (`page`, `size`, `sort`).

### 5.4 Frontend

- Recruiter dashboard shell: jobs list + create/edit job forms.
- Candidate: job list + job detail + apply CTA.
- Shared layout, auth guard by role, TanStack Query for server state.

### 5.5 Exit criteria

- End-to-end demo: recruiter creates job → candidate applies → recruiter changes status.
- README updated with “happy path” screenshots or short bullet flow.

---

## 6. Phase 3 — Resume upload, parsing pipeline v1, ATS-style match score

**Goal:** Files stored safely; text extracted; a deterministic score vs job description (recruiters see why).

### 6.1 Prerequisites

- Object storage decision: Cloudinary, Supabase Storage, or S3-compatible bucket (free tier).
- Max file size and allowed MIME types defined (PDF/DOCX).

### 6.2 Main features

- Upload resume per candidate (metadata in DB: `fileUrl`, `mimeType`, `uploadedAt`, `parsedText` nullable).
- Async or sync pipeline: extract text (library or external API—document choice).
- **Match score v1:** rule-based or embedding-based:
  - **Simple:** keyword overlap + section detection (Skills, Experience) with weighted score 0–100.
  - **Better:** call an embeddings API to compare resume text vs job description (rate limit + cache in Redis).
- Recruiter view: score + short explanation (bullet reasons).

### 6.3 Non-functional

- Virus scanning is optional on free tier; at minimum validate type/size and store outside DB.
- Idempotent re-parse if user re-uploads.

### 6.4 Exit criteria

- Demo: upload → score appears on application row; changing job text changes score predictably.

---

## 7. Phase 4 — Recruiter analytics and candidate experience polish

**Goal:** Dashboard feels “product-grade”: charts, filters, and a clear candidate timeline.

### 7.1 Prerequisites

- Phases 2–3 data available.

### 7.2 Main features

- Recruiter dashboard: counts by status, time-to-fill placeholder, funnel chart (Recharts/ECharts).
- Search/filter applications (by score range, status, date).
- Candidate: “My applications” page with timeline of status changes.
- Optional: export applications CSV for a job.

### 7.3 Exit criteria

- UI responsive; loading/error states; empty states for new accounts.

---

## 8. Phase 5 — Interview workflow and notifications

**Goal:** Structured next steps after apply; email or in-app notifications.

### 8.1 Prerequisites

- SMTP or transactional email (SendGrid/Mailgun/Resend free tier) or Mailhog locally only.

### 8.2 Main features

- Interview “rounds” or simple `InterviewSession` entity: type (e.g. `PHONE`, `TECH`, `HR`), scheduled time, link/location, outcome.
- Status transitions tied to events (e.g. move to `INTERVIEW` when session scheduled).
- Email templates: application received, interview scheduled, status changed (start with one or two).

### 8.3 Optional async

- Spring `@Async` or simple queue later; MVP can send email synchronously with try/catch and logging.

### 8.4 Exit criteria

- Full flow: apply → schedule interview → candidate sees it → status updates.

---

## 9. Phase 6 — AI-assisted questions (responsible, bounded)

**Goal:** Show modern AI integration without making the app depend on flaky free quotas.

### 9.1 Prerequisites

- API key management via env (`OPENAI_API_KEY` or compatible endpoint); never commit keys.
- Redis cache for prompts+responses per (jobId, round type) to reduce cost.

### 9.2 Main features

- Generate question bank from job description + seniority (stored in DB, editable by recruiter).
- Optional: candidate Q&A practice mode (not proctored; clear disclaimer).

### 9.3 Guardrails

- Rate limits per user; timeout and fallback message if AI fails.
- Log prompt IDs or hashes only if needed for debugging—not full PII in logs.

### 9.4 Exit criteria

- Recruiter can generate and save questions; candidate can view assigned set (role-gated).

---

## 10. Phase 7 — Hardening: tests, security, performance, observability

**Goal:** Recruiter-ready quality bar for a portfolio project.

### 10.1 Testing

- Backend: unit tests for services; integration tests for main flows; contract tests for critical APIs if time.
- Frontend: component tests for forms; e2e smoke (Playwright optional) for login + apply path.

### 10.2 Security checklist

- [ ] HTTPS only in production; secure cookies if used.
- [ ] Input validation on all write endpoints.
- [ ] OWASP basics: SQL injection avoided via JPA; XSS considered in any rich text later.
- [ ] File upload limits; content-type checks.
- [ ] Secrets only in env / secret manager.

### 10.3 Performance

- DB indexes on foreign keys and frequent filters.
- Pagination everywhere lists can grow.
- Optional: Spring Cache on read-heavy config endpoints.

### 10.4 Observability

- Actuator: `/health`, `/info` (non-sensitive); metrics endpoint if host allows.
- Correlation ID in logs (filter once in backend).

### 10.5 Exit criteria

- CI runs tests on PR; README states how to run tests locally; no known critical security TODOs.

---

## 11. Phase 8 — Deployment and portfolio packaging

**Goal:** Public demo URL + reproducible deploy story.

### 11.1 Prerequisites

- Production builds work locally (`./mvnw -Pprod package` or equivalent; `npm run build`).
- All URLs and keys externalized to environment variables.

### 11.2 Recommended free-tier layout

| Piece | Typical free option |
|--------|----------------------|
| Frontend | Vercel or Netlify |
| Backend | Render, Railway, or Fly.io |
| PostgreSQL | Neon, Supabase, or Railway |
| Redis | Upstash |
| Files | Cloudinary or Supabase Storage |

### 11.3 Deployment steps (high level)

1. Create production Postgres and run Flyway (or let app run migrations on startup if you accept that trade-off—document it).
2. Deploy backend: set `SPRING_DATASOURCE_*`, `JWT_SECRET`, `CORS_ORIGINS`, Redis URL, storage keys, AI key if used.
3. Deploy frontend: set `VITE_API_BASE_URL` (or your convention) to backend public URL.
4. Smoke test: register → login → create job → apply → upload resume → score visible.
5. Seed script or admin endpoint for demo data (optional read-only demo user).

### 11.4 DNS and HTTPS

- Use platform-provided HTTPS; custom domain optional.
- If backend and frontend on different domains, CORS must list the exact frontend origin.

### 11.5 Portfolio artifacts

- [ ] README: architecture diagram (draw.io or Mermaid), setup, env vars, scripts.
- [ ] Live demo link + test accounts (`recruiter@demo.com` / `candidate@demo.com`) with **password rotation** if repo is public—prefer env-based seed passwords.
- [ ] 2–5 minute screen recording (Loom/YouTube unlisted): problem, stack, demo, trade-offs.
- [ ] GitHub profile pinned; optional blog post “what I learned.”

### 11.6 Exit criteria

- Someone unfamiliar can follow README and run locally within reasonable time.
- Live URL works; you can share it on a resume or LinkedIn.

---

## 12. Risk register (plan for these early)

| Risk | Mitigation |
|------|-----------|
| Free AI API limits / downtime | Cache, fallbacks, feature-flag AI phase |
| Large file uploads on small instances | Strict size limits; direct-to-storage uploads later |
| Cold starts on free backend | Health check + keep-alive note in README; upgrade tier if demo fails often |
| Scope creep | Ship Phases 1–4 before heavy AI; treat Phase 6 as optional for first “done” milestone |

---

## 13. Definition of “MVP done” vs “portfolio gold”

- **MVP done (hireable demo):** Phases 0–4 + Phase 7 (hardening subset) + Phase 8 deploy.
- **Portfolio gold:** MVP + Phases 5–6 + full tests + video walkthrough.

---

## 14. How to use this doc during development

- At the start of each week: pick the matching phase; copy “Deliverables / Exit criteria” into your task board.
- When blocked: check “Prerequisites” of the current phase—often something in the prior phase was skipped.
- Before any deploy: re-run Phase 11 checklist and Phase 10 security list.

---

*Last aligned with stack: Spring Boot 3 + React/Vite + PostgreSQL + Redis. Update section 2 if you change stack.*
