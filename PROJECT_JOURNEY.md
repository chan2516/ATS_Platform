# ATS Platform — project journey

**Purpose:** A single narrative document for **what has been built**, **how the stack fits together**, and **where we are going**. Update this file when you ship meaningful increments (new phases, major API/UI changes, deployment). For granular checkboxes, use `PROJECT_PROGRESS.md`; for the full roadmap, use `DEVELOPMENT_PLAN.md`.

**Last updated:** 2026-04-11

---

## Vision (one line)

A multi-role applicant tracking system where recruiters manage jobs and pipelines, candidates apply and upload resumes, and the platform surfaces **deterministic match scores** and explanations—designed for a clear portfolio story (REST API + modern React).

---

## Stack (locked)

| Area | Choice |
|------|--------|
| API | Spring Boot 3.x, Java 17, REST, OpenAPI (Swagger) |
| Security | JWT (Bearer), roles `CANDIDATE` / `RECRUITER` / `ADMIN` |
| Data | PostgreSQL, Flyway migrations, Spring Data JPA |
| UI | React 18, TypeScript, Vite, TanStack Query, React Hook Form + Zod |
| Local infra | Docker Compose (Postgres, Redis on non-default host ports) |
| Resume parsing | Apache Tika (PDF/DOCX text extraction) |
| File storage (dev) | Local filesystem under configurable root (`./data/resumes` by default) |

---

## Timeline of delivery

### Phase 0–1 — Foundations and identity

- Monorepo layout, Compose, CI workflow, backend skeleton (Actuator, exception handling), frontend skeleton with routing.
- Auth: register/login, JWT, `GET /api/me`, Flyway baseline schema (`users`, `companies`, `job_postings`, `applications`), role-gated endpoints, integration tests.

### Phase 2 — Jobs and applications

- Recruiter CRUD for job postings; public read for open jobs (no JWT).
- Candidate apply (one application per job per candidate); paginated lists; application status and recruiter notes.
- Frontend: job search/detail, recruiter dashboard, TanStack Query, role-based routes.
- Pagination JSON stabilized using Spring Data **`PagedModel`** (`@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)`) so clients do not rely on raw `PageImpl` serialization.

### Phase 3 — Resumes and ATS-style scoring (current)

- **Storage:** Resume files attached to an **application** (candidate-owned). Metadata and parsed text persisted on `applications` (Flyway `V3__resume_and_match_score.sql`).
- **Parsing:** Apache Tika extracts plain text from PDF/DOCX (max size and MIME types enforced).
- **Scoring v1:** `AtsMatchScoringService` computes a **0–100** score plus short **reason strings** (token overlap / Jaccard-style blend with recall against job keywords). Designed so a future **embeddings** implementation can replace the internals behind the same application service.
- **API:** `POST /api/me/applications/{applicationId}/resume` (multipart `file`). Recruiter/candidate list DTOs include `matchScore`, `matchReasons`, resume metadata.
- **Frontend:** Candidates upload/replace resumes on **My applications**; recruiters see score and file info on the job applications table.
- **Tests:** `Phase3ResumeIntegrationTest` (PDF generated with PDFBox **2.x** aligned with Tika’s PDFBox—avoid mixing PDFBox 3 on the classpath).

---

## Operational notes

- **JWT:** Set `JWT_SECRET` (32+ bytes) in non-dev environments (`backend/.env.example`).
- **Resume storage:** `RESUME_STORAGE_ROOT` (maps to `app.storage.local.root-directory`) defaults to `./data/resumes`; add to `.gitignore` / backups as needed.
- **Upload limits:** `spring.servlet.multipart` and `app.resume.max-size-bytes` (default 5 MiB); PDF/DOCX only.

---

## Next milestones (from `DEVELOPMENT_PLAN.md`)

**Operating sequence:** see **`DEVELOPMENT_WORKFLOW.md`** — deploy MVP first, then feature phases with the same CI → merge → deploy loop.

1. **Production MVP** — **`DEPLOYMENT.md`** (hosts, env, Student Pack). Do this **before** treating Phase 4 as mandatory.
2. **Phase 4** — Recruiter analytics, richer filters, candidate timeline polish.
3. **Phase 5** — Interviews and notifications.
4. **Phase 6** — Optional AI-assisted question generation (feature-flagged, cached).
5. **Phase 7** — Hardening, observability.

---

## How to maintain this document

- After each **phase** or **major feature**, add a short subsection under **Timeline of delivery** and bump **Last updated**.
- Link to `PROJECT_PROGRESS.md` for checkbox status, not for long prose.
- Keep **Vision** and **Stack** accurate when technology choices change.
