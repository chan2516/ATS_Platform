# Deployment guide (MVP)

This doc is for **shipping a public demo** while **deferring** optional roadmap work (recruiter analytics charts, status timeline, interviews, AI features). Those stay in `DEVELOPMENT_PLAN.md` for later.

**Process order:** read **`DEVELOPMENT_WORKFLOW.md`** first (deploy-first priorities and CI/CD-style workflow), then use this file for concrete hosting steps and environment variables.

---

## What you can skip for first deploy

| Area | Skip for now | Why |
|------|----------------|-----|
| Phase 4 | Dashboard charts, advanced filters, status-change timeline | Nice-to-have; not required for a working demo URL |
| Phase 5–6 | Email, interviews, AI questions | Adds SMTP/keys and scope |
| Redis | — | Compose includes Redis, but the **backend does not use Redis in code yet** — no Redis required for MVP deploy |
| Object storage | S3/Cloudinary | App uses **local disk** under `RESUME_STORAGE_ROOT`. For cloud, use a host with **persistent disk** or plan a storage migration later |

**You should not skip:** PostgreSQL, strong `JWT_SECRET`, `CORS_ORIGINS` matching your frontend URL, `VITE_API_BASE_URL` for production, and Flyway migrations on startup (or run them once against prod DB).

---

## GitHub Student Developer Pack → how it helps you

Verify your pack at [education.github.com/pack](https://education.github.com/pack). Offers change over time; typical **hosting-related** perks include:

| Benefit (common) | How it fits this project |
|------------------|---------------------------|
| **Vercel** (often Pro tier) | Host the **React** static build (`frontend/dist`). Set env `VITE_API_BASE_URL` to your **backend HTTPS URL**. |
| **DigitalOcean** credits | **Droplet**, **App Platform**, or managed DB — good if you want one vendor for VM + Postgres or simple PaaS. |
| **Microsoft Azure** credits | **App Service**, **Container Apps**, **Azure Database for PostgreSQL** — enterprise-style stack. |
| **Namecheap / domains** (if included) | Custom domain for the frontend (and optional API subdomain). |
| **GitHub Actions** | Already used for CI; stays free for public repos with limits. |

You do **not** have to use every perk. The simplest path is often: **managed Postgres + free/cheap backend host + Vercel/Netlify for the UI**.

---

## Recommended MVP architecture (typical free tier)

```
[Browser] → HTTPS → Vercel / Netlify (React)
                ↓ API calls
            HTTPS → Render / Railway / Fly.io (Spring Boot JAR)
                ↓ JDBC
            Neon / Supabase / Railway (PostgreSQL)
```

- **Frontend:** Build with `npm run build`, deploy the `frontend/dist` folder (or connect Git repo with root `frontend/` and build command).
- **Backend:** Fat JAR from `./mvnw -B package -DskipTests` (CI already runs tests). Run `java -jar backend/target/*.jar` or use a Dockerfile.
- **Database:** Create a **PostgreSQL** instance; copy **connection string** → `SPRING_DATASOURCE_URL`, username, password.

**CORS:** Set `CORS_ORIGINS` to your exact frontend origin, e.g. `https://your-app.vercel.app` (no trailing slash unless your app expects it — match what the browser sends).

**JWT:** Set a long random `JWT_SECRET` (32+ bytes) in the backend host’s secrets — never commit it.

---

## Environment variables (checklist)

### Backend (host secrets)

| Variable | Purpose |
|----------|---------|
| `SPRING_DATASOURCE_URL` | JDBC URL to production Postgres |
| `SPRING_DATASOURCE_USERNAME` | DB user |
| `SPRING_DATASOURCE_PASSWORD` | DB password |
| `JWT_SECRET` | HS256 signing secret (strong, random) |
| `CORS_ORIGINS` | Your frontend URL(s), comma-separated |
| `RESUME_STORAGE_ROOT` | Writable path for resumes (ephemeral on some hosts — see below) |

Optional: `SERVER_PORT` if the platform injects `PORT` — on some PaaS you must map `PORT` → Spring (e.g. `SERVER_PORT=${PORT}`).

### Frontend (build-time / Vercel env)

| Variable | Purpose |
|----------|---------|
| `VITE_API_BASE_URL` | Full base URL of API, e.g. `https://api-yourapp.onrender.com` (**no** trailing slash on path — your client uses paths like `/api/...`) |

Rebuild the frontend after changing `VITE_API_BASE_URL` — Vite bakes it in at build time.

---

## Flyway and first boot

- Ensure production DB is empty or migrated: on first deploy, Flyway runs `V1`–`V3` migrations if the app starts with Flyway enabled (default).
- Prefer **one** migration strategy: either let the app migrate on deploy, or run Flyway manually once — don’t mix blindly.

---

## Resume files in the cloud

- **Render/Railway free** tiers often have **ephemeral** filesystem: uploaded resumes can **disappear** on redeploy.
- For a **portfolio demo**, either: accept that limitation, attach a **persistent disk** if the host supports it, or defer resume upload in prod until you add **S3-compatible storage** (Phase 3 optional follow-up).

---

## Minimal smoke test after deploy

1. Open frontend URL → register recruiter → create job.  
2. Register candidate → apply → upload resume (if storage works).  
3. Recruiter sees application and score.  

If step 3 fails, check CORS, `JWT_SECRET` stability (changing it invalidates tokens), and DB connectivity.

---

## Next steps after MVP deploy

- Custom domain (Student Pack domain + DNS CNAME to Vercel).  
- Branch protection + required CI on `main`.  
- Optional: `DEPLOYMENT.md` update with **your** chosen provider and exact click-path screenshots.

---

*Aligned with `DEVELOPMENT_PLAN.md` § Phase 8; product phases 4–6 remain future work.*
