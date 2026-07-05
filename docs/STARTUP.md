# ATS Platform Startup Guide

Use this as the quickest way to run the app locally.

## What Docker runs today

Docker Compose now starts the full stack:

- PostgreSQL on host port `5433`
- Redis on host port `6380`
- Backend API on host port `18080`
- Frontend UI on host port `5173`

## What you need

- JDK 17+
- Node.js LTS
- Docker Desktop

## Start the app

1. Start the full stack:

```bash
docker compose up -d --build
```

2. Open the app in your browser.

4. Open the app:

- Frontend: [http://localhost:5173](http://localhost:5173)
- Backend health: [http://localhost:18080/actuator/health](http://localhost:18080/actuator/health)
- Swagger UI: [http://localhost:18080/swagger-ui.html](http://localhost:18080/swagger-ui.html)

The backend container connects to PostgreSQL inside Docker, and the frontend container is built with `VITE_API_BASE_URL=http://localhost:18080`.

## Rebuild after code changes

Use the rebuild script when you change code:

```powershell
.\rebuild.ps1
```

Rebuild one part at a time if you only changed one side:

```powershell
.\rebuild.ps1 -Part backend
.\rebuild.ps1 -Part frontend
```

The script rebuilds the selected Docker service, or the full stack if you use the default.

## Access points

After everything is running, use these URLs:

- Frontend UI: [http://localhost:5173](http://localhost:5173)
- API health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Backend API base for the frontend proxy: `http://localhost:8080`

If you want to rebuild just one app service after a code change, use `.
ebuild.ps1 -Part backend` or `.
ebuild.ps1 -Part frontend`.

## Main flow

- Register as a recruiter or candidate
- Recruiter creates a job
- Candidate applies to the job
- Candidate uploads a resume
- Recruiter reviews the application and sees the match score

## Useful environment variable

Set a JWT secret before starting the backend when you want a non-default signing key:

```powershell
$env:JWT_SECRET = "your-long-random-secret-at-least-32-chars"
```
