# Development workflow — deploy first, then features (CI/CD-style)

This is the **order of work** the repo is using: **get production working first**, then grow the product. It matches `DEPLOYMENT.md` (how to host) and `PROJECT_PROGRESS.md` (what is done).

**Last updated:** 2026-04-11

---

## Guiding idea

| Order | Focus | Outcome |
|-------|--------|---------|
| **1** | **Deploy MVP** | A real URL you can put on a resume; proves the stack end-to-end. |
| **2** | **CI/CD habits** | Every change goes through **CI** before `main`; production updates on a **repeatable** cadence. |
| **3** | **Remaining product** | Phases 4–6 from `DEVELOPMENT_PLAN.md` **after** the demo is live (or in parallel only if deploy is already done). |

---

## Priority 1 — What to do next (deployment first)

Do these **in order**. Details and env vars: **`DEPLOYMENT.md`**.

1. **Confirm CI is green** — GitHub → **Actions** → latest run on `main` (backend tests + frontend lint/build).
2. **Create production PostgreSQL** — e.g. Neon, Supabase, or Railway; save JDBC URL and credentials.
3. **Deploy the backend** — Render, Railway, Fly.io, Azure, or DigitalOcean App Platform; set `SPRING_DATASOURCE_*`, `JWT_SECRET`, `CORS_ORIGINS`, `RESUME_STORAGE_ROOT` (or accept ephemeral disk for a short demo).
4. **Deploy the frontend** — Vercel or Netlify; set **`VITE_API_BASE_URL`** to your API’s **HTTPS** base URL; build `frontend/` (`npm ci && npm run build`).
5. **Smoke test in production** — register → recruiter job → candidate apply → resume upload → recruiter sees score.
6. **(Optional)** Add your public URLs to `README.md` or `PROJECT_JOURNEY.md` for portfolio readers.

Until step 5 passes, treat **Phase 4+ features as secondary** so you are not blocked on “finishing” analytics before you have a URL.

---

## Priority 2 — CI/CD-style development (how to work week to week)

You already have **CI** on every push/PR to `main` (`.github/workflows/ci.yml`). Use this loop:

1. **`main` stays releasable** — do not merge if CI is red.
2. **Branch per change** — e.g. `feature/recruiter-filters`, `fix/cors-prod`.
3. **Open a Pull Request** — let CI run on the PR (enable branch protection + “require status checks” when you are ready).
4. **Merge after green** — squash or merge commit, your preference.
5. **Deploy** — pick one pattern and stick to it:
   - **Simplest “CD”:** In Vercel / Render / Railway, **connect the GitHub repo** and turn on **auto-deploy on push to `main`**. That is real CD without writing extra YAML.
   - **Manual CD:** After merge, click “Deploy” on the host or run your script — still fine if you document it in `DEPLOYMENT.md`.

**Optional later:** a second workflow that only runs deploy (needs host API tokens in **GitHub Secrets**). Add when manual deploy becomes painful.

---

## Priority 3 — Complete the application (after MVP is deployed)

Use `DEVELOPMENT_PLAN.md` and `PROJECT_PROGRESS.md` for detail. Typical order:

| After live URL | Product work |
|----------------|--------------|
| **Phase 4** | Recruiter dashboard metrics, filters, candidate timeline, polish |
| **Phase 5** | Interviews, notifications (needs email provider) |
| **Phase 6** | AI question bank (optional; keys + rate limits) |
| **Phase 7** | Hardening, more tests, security checklist |
| **Phase 8** | Portfolio packaging (already started — keep README + demo links fresh) |

Each slice should still follow: **branch → PR → CI green → merge → auto or manual deploy**.

---

## What counts as “MVP complete” for deployment

**Enough to ship:**

- Login/register, roles, jobs, apply, resume upload, match score visible to recruiter.

**Not required for first deploy:**

- Charts, status history timeline, email, AI, Redis (unused in code today).

---

## Where to look

| Doc | Use for |
|-----|---------|
| `DEPLOYMENT.md` | Hosts, env vars, Student Pack, resume storage caveats |
| `DEVELOPMENT_PLAN.md` | Full phase definitions |
| `PROJECT_PROGRESS.md` | Checkboxes and current focus |
| `PROJECT_JOURNEY.md` | Narrative history you update over time |

---

## GitHub Student Pack

Use perks to reduce cost (Vercel Pro, Azure/DigitalOcean credits, domains). They **replace** which vendor you pick; they do not replace the workflow above. See `DEPLOYMENT.md`.
