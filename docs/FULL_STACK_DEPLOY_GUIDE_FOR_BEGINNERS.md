# Full-stack deploy explained (for beginners)

This document explains **why** the frontend and backend live on different servers, **what** to deploy **first**, **how** they connect over the API only, **free** hosting options, and a **pipeline** you can repeat as you grow into a full-stack developer.

Read this once end-to-end, then keep **`docs/STUDENT_PACK_DEPLOY_AND_PIPELINE.md`** as a shorter checklist.

---

## Part 1 — Mental model: what actually runs where

### On your laptop (development)

| Piece | What it is | Address |
|--------|------------|---------|
| React app | Vite **dev server** (Node) | `http://localhost:5173` |
| Spring Boot | Java process | `http://localhost:8080` |
| Postgres | Docker container | `localhost:5433` |

The browser loads the UI from **5173**. The UI calls `/api/...` — Vite **proxies** those to **8080** (see `frontend/vite.config.ts`). So in dev it *feels* like one app, but there are still **two processes**.

### In production (typical for this repo)

| Piece | What it is | Where it lives |
|--------|------------|----------------|
| React app | **Static files** after `npm run build` (HTML, JS, CSS) — no Node required to *serve* it | e.g. **Vercel / Netlify** |
| Spring Boot | **One long-running Java process** (the API) | e.g. **Render / Railway / Fly.io** |
| Postgres | **Managed database** | e.g. **Neon / Supabase** |

The user’s **browser** downloads the UI from `https://your-frontend.vercel.app`.  
When the UI calls the API, it uses the **full URL** `https://your-api.onrender.com/api/...` (from `VITE_API_BASE_URL`).  
There is **no** automatic proxy in production — you must configure the base URL and **CORS** correctly.

**Important:** The frontend does **not** “contain” the backend. They only talk through **HTTP** (REST). That is the normal “decoupled” full-stack setup companies use.

---

## Part 2 — Two common layouts (and which is better for learning)

### Layout A — “Split” (three services) — **recommended for this project**

```
Browser → Vercel (static React)
       → HTTPS JSON → Render (Spring Boot) → JDBC → Neon (Postgres)
```

**Why it’s good to learn**

- Matches **real jobs**: separate UI team / API team / DBA concerns.
- Each piece has a **clear free tier** and dashboard.
- You learn **CORS**, **env vars**, and **API base URLs** — core full-stack skills.

**Downside:** Three places to configure (more moving parts at first).

---

### Layout B — “Same platform, two services” (e.g. Railway)

Some platforms let you run **Postgres + backend + static site** in one project with multiple services.

**Why people use it:** One bill, one dashboard, slightly fewer tabs.

**Trade-off:** You still have **two URLs** (or paths) for UI vs API — the mental model is the same; only the vendor changes.

---

### Layout C — “Backend serves the frontend” (monolith)

You could configure Spring Boot to serve the **built** `frontend/dist` as static files from the **same** server and port.

**Why teams sometimes do it:** One deployment unit, no CORS between UI and API if same origin.

**Why this repo doesn’t assume it:** The React app is built for **Vite env vars** and a separate deploy story; changing to true monolith is a **different** setup (not covered here).

**For learning modern hiring loops, Layout A is enough.**

---

## Part 3 — What to deploy **first** (order matters)

Deploy in this order **on purpose**:

### Step 1 — Source code on GitHub

**Why first:** Hosts connect to **GitHub** to pull code and run builds. CI (`.github/workflows/ci.yml`) also runs on push.

---

### Step 2 — Database (Neon or similar)

**Why before the API:** Spring Boot needs a **JDBC URL** on startup. You can’t point the API at “nothing.”

**What you get:** `SPRING_DATASOURCE_URL`, username, password.

**Tip:** Create **one** database for production; don’t reuse your laptop Docker data.

---

### Step 3 — Backend (Render / Railway / …)

**Why before the frontend:** The UI needs a **real API base URL** for `VITE_API_BASE_URL`. The API does **not** need the final frontend URL to *start* (but see CORS below).

**What you configure:**

- Database credentials  
- `JWT_SECRET` (long random)  
- `CORS_ORIGINS` — see Step 4 (you may update this twice)

**Smoke test:** Open `https://your-api.../actuator/health` — should show **UP** (and DB healthy if wired).

---

### Step 4 — Frontend (Vercel / Netlify)

**Why last:** You paste **`VITE_API_BASE_URL=https://your-api-host...`** (no trailing slash; your code calls `/api/...`).

**CORS catch:** After the first successful frontend deploy, you know the **exact** origin, e.g. `https://ats-platform.vercel.app`. Put that string into **`CORS_ORIGINS`** on the backend and **redeploy** the API if the first deploy used a wrong or missing origin.

**Why two passes are normal:** You can’t know the final Vercel URL before the first deploy; fixing CORS once is standard.

---

### Step 5 — Verify the full flow

Register → login → create job → apply → upload resume. If anything fails, check **browser DevTools → Network** (are requests going to **Render**, not `localhost`?) and **CORS** errors in the console.

---

## Part 4 — “Free server for both” — honest answer

There is usually **no single magic box** that runs Postgres + Java + global CDN for **everything** forever free without limits. What people mean by “free full stack” is:

| Layer | Typical free approach |
|--------|------------------------|
| DB | Neon / Supabase free tier |
| API | Render free tier (may sleep; cold start) |
| UI | Vercel / Netlify free tier |

**Student Pack** often upgrades **Vercel** or gives **Azure/DigitalOcean credits** — use those to reduce limits or sleep time, not to replace the architecture.

---

## Part 5 — Pipeline: how development and deploy fit together

### What “pipeline” means here

1. **CI (Continuous Integration)** — automatic checks on every push/PR.  
   **You already have this:** backend tests + frontend lint/build (`ci.yml`).

2. **CD (Continuous Delivery/Deployment)** — after merge, **something** updates production.

**Simplest CD (no extra YAML):**

- Connect **Vercel** and **Render** to the same GitHub repo / `main` branch.  
- Turn on **Auto Deploy**.  
- Flow: `feature/x` → **Pull Request** → CI green → **merge** → hosts rebuild.

That **is** a pipeline: quality gate + automated release.

**Advanced CD:** GitHub Action that calls Render/Vercel APIs — only add when you outgrow button deploys.

---

## Part 6 — Diagram: request flow in production

```text
User clicks “Sign in”
  → Browser runs React JS (loaded from Vercel)
  → fetch("https://YOUR-API.onrender.com/api/auth/login", ...)
  → Render runs Spring Boot
  → Spring talks to Neon Postgres
  → JSON response back to browser
  → React updates the screen
```

Nothing in that chain requires the UI and API to share one physical machine — only **HTTPS** and correct **CORS**.

---

## Part 7 — How to practice becoming a full-stack developer

1. **Run locally** until comfortable (Docker DB + backend + frontend).  
2. **Deploy once** using this guide (accept that first deploy takes time).  
3. **Change a small feature** on a branch → PR → CI → merge → watch auto-deploy.  
4. **Break and fix:** wrong `VITE_API_BASE_URL`, wrong CORS — read the error, fix one variable at a time.  
5. **Read Network tab** until it feels natural — that skill transfers to every stack.

---

## Part 8 — Quick reference (this repo)

| Variable | Where | Purpose |
|----------|--------|---------|
| `VITE_API_BASE_URL` | Vercel build env | Tells React where the API lives |
| `CORS_ORIGINS` | Render env | Tells Spring which **browser origins** may call the API |
| `SPRING_DATASOURCE_*` | Render env | Production Postgres |
| `JWT_SECRET` | Render env | Signs tokens |

---

## See also

- **`docs/STUDENT_PACK_DEPLOY_AND_PIPELINE.md`** — shorter checklist + Student Pack notes  
- **`DEPLOYMENT.md`** — env details and resume storage caveats  
- **`DEVELOPMENT_WORKFLOW.md`** — deploy-first product priorities  

When your URLs are stable, add them to **`README.md`** or **`PROJECT_JOURNEY.md`** so future you remembers what you shipped.
