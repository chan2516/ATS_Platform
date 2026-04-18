# Deploy with GitHub Student Pack + pipeline for ongoing work

This guide ties together **free / student-tier hosting**, how **frontend + backend** talk in production, and a **repeatable workflow** (CI + deploy) for future features.

---

## 1. How the deployed app is wired

```mermaid
flowchart LR
  subgraph browser [Browser]
    UI[React static site]
  end
  subgraph host_ui [e.g. Vercel / Netlify]
    UI
  end
  subgraph host_api [e.g. Render / Railway / Fly.io]
    API[Spring Boot JAR]
  end
  subgraph db [Managed Postgres]
    PG[(Neon / Supabase / Railway DB)]
  end
  UI -->|HTTPS "GET/POST /api/..."| API
  API --> PG
```

- **Frontend** is only **HTML/CSS/JS** after `npm run build`. It does **not** run Node in production. It calls your API using the base URL from **`VITE_API_BASE_URL`** (baked in at **build time**).
- **Backend** is the **Spring Boot** process: same code as locally, with **environment variables** for DB, JWT, CORS, resume path, etc.
- **Database** must be real **PostgreSQL** in the cloud (local Docker is dev-only).

**CORS:** The backend must allow your **exact** frontend origin, e.g. `https://your-app.vercel.app` in `CORS_ORIGINS` (comma-separated if several).

---

## 2. GitHub Student Pack — what to use it for

Benefits change; verify at [education.github.com/pack](https://education.github.com/pack). Typical uses for **this** repo:

| Offer | Use for ATS |
|--------|-------------|
| **Vercel** (often Pro) | Host **frontend** — connect GitHub repo, build `frontend/`, set env vars. |
| **DigitalOcean** credits | **Droplet** or **App Platform** for JAR + optional Postgres. |
| **Azure** credits | **App Service** / **Container Apps** + **Azure Database for PostgreSQL**. |
| **Domain** (if included) | Point `www` → Vercel, optional `api.` → backend host. |

You can still deploy **without** the pack using **free tiers** (Neon + Render + Vercel). The pack mainly **extends limits** or **adds Pro features**.

---

## 3. A concrete “student-friendly” stack (all have free tiers)

| Piece | Service (examples) | Role |
|--------|---------------------|------|
| **Database** | [Neon](https://neon.tech), [Supabase](https://supabase.com), Railway Postgres | JDBC URL → `SPRING_DATASOURCE_*` |
| **Backend** | [Render](https://render.com), [Railway](https://railway.app), [Fly.io](https://fly.io) | Run `java -jar` or Docker; set env secrets |
| **Frontend** | [Vercel](https://vercel.com), [Netlify](https://netlify.com) | Deploy `frontend` folder; `npm run build` |

**Redis** is **not** required by the current Java code — skip for MVP.

---

## 4. Environment variables (production)

### Backend (secrets on the API host)

- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET` — long random string (32+ bytes)
- `CORS_ORIGINS` — `https://your-frontend.vercel.app` (exact origin, no typo)
- `RESUME_STORAGE_ROOT` — writable path, or accept ephemeral disk for demos only

### Frontend (build-time on Vercel/Netlify)

- `VITE_API_BASE_URL` — **`https://your-api.onrender.com`** (your real API base, **no** path suffix; the app calls `/api/...`)

After changing `VITE_API_BASE_URL`, **rebuild** the frontend (new deploy).

---

## 5. Pipeline: what you have vs what to add

### Already in the repo

- **`.github/workflows/ci.yml`** — on every push/PR to `main`: backend `./mvnw` tests, frontend `lint` + `build`. This is your **quality gate**.

### Recommended “CD” without writing YAML

1. In **Vercel** (frontend): **Import Git Repository** → root directory `frontend` → Production Branch `main` → add `VITE_API_BASE_URL` → enable **Auto Deploy** on push to `main`.
2. In **Render** (backend): **New Web Service** → connect repo, build command `./mvnw -B package -DskipTests`, start `java -jar target/*.jar`, set env vars → **Auto-Deploy** `main`.

Then your pipeline is:

```text
feature branch → Pull Request → CI green → merge to main → hosts auto-deploy (or manual trigger)
```

### Optional later: deploy workflow in GitHub Actions

Add a second workflow that runs only when you want (e.g. `workflow_dispatch` or tags) and calls each host’s API with tokens stored in **GitHub → Settings → Secrets**. Only worth it when manual deploy becomes painful.

---

## 6. Day-to-day development (same as CI/CD discipline)

1. Create a branch: `feature/short-description`
2. Open a **Pull Request** into `main`
3. Wait for **CI** (green checks)
4. Merge
5. **Frontend/backend** pick up `main` if auto-deploy is on

Keep **`DEVELOPMENT_WORKFLOW.md`** and **`DEPLOYMENT.md`** updated when URLs or env names change.

---

## 7. Checklist before calling it “live”

- [ ] Production DB created; Flyway migrations applied (app startup or manual)
- [ ] Backend health: `https://api.../actuator/health` shows **UP**
- [ ] Frontend loads; browser **Network** tab shows API calls to the correct host (not `localhost`)
- [ ] Register → login → job → apply → resume → recruiter sees score

---

*See also: root `DEPLOYMENT.md`, `DEVELOPMENT_WORKFLOW.md`, `README.md`.*
