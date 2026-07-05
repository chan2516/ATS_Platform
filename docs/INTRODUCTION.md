# ATS Platform Introduction

This document gives a practical overview of the current application so a new contributor can understand what exists, how the pieces fit together, and where the project is headed.

## What this application is

ATS Platform is a full-stack applicant tracking system built as a portfolio-ready monorepo:

- Backend: Spring Boot 3.5, Java 17, PostgreSQL, Flyway, Spring Security, JWT, OpenAPI
- Frontend: React 19 + TypeScript + Vite + TanStack Query + React Router
- Local infrastructure: Docker Compose for PostgreSQL and Redis

The product is designed around two primary roles:

- Candidate: register, log in, browse open jobs, apply, view applications, and upload a resume
- Recruiter: register with a company, create and manage job postings, review applications, update status, and inspect resume match scores

## Current product scope

The codebase currently implements the following end-to-end flow:

1. Authentication and role-based access
2. Public job search and job detail pages
3. Candidate application flow
4. Recruiter job management and application review
5. Resume upload, text extraction, and ATS-style match scoring

The app is already organized as a layered system, with API controllers, services, repositories, domain entities, and a separate frontend that consumes the API over HTTP.

## System architecture

### Backend

The backend is a stateless REST API with Spring Security and JWT authentication. It exposes:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/me`
- Public job APIs under `/api/jobs`
- Recruiter job and application APIs under `/api/recruiter`
- Candidate application and resume upload APIs under `/api/jobs/{jobId}/applications` and `/api/me/applications`

Core backend concerns:

- User registration and login
- Company ownership for recruiters
- Job posting lifecycle
- Candidate applications with one application per candidate per job
- Resume storage on the local filesystem
- Resume parsing with Apache Tika
- Deterministic match scoring from job text and resume text

### Frontend

The frontend is a routed React application that provides separate experiences for candidates and recruiters.

Main pages currently include:

- Home
- Jobs list
- Job detail and apply flow
- Health check page
- Login and register
- Account summary
- My applications for candidates
- Recruiter dashboard, job creation/editing, and job application review

The UI uses TanStack Query for server state, React Hook Form and Zod for form handling, and route guards for role-based access.

### Data storage

The backend uses PostgreSQL with Flyway migrations. The main domain objects are:

- User
- Company
- JobPosting
- JobApplication

Resume metadata and scoring data are stored on the application record. The actual resume file is stored outside the database in a local directory by default.

## Domain model summary

### Users and roles

Users authenticate with email and password. The role model currently includes:

- `CANDIDATE`
- `RECRUITER`
- `ADMIN` exists in the domain, but self-registration is limited to candidate and recruiter accounts

Recruiters belong to a company. Candidates are linked to applications and resume uploads.

### Job postings

Job postings belong to a company and support the fields needed for a real hiring flow:

- Title
- Description
- Location
- Employment type
- Salary range
- Status such as open or closed

Public users can search open jobs without a token. Recruiters can create, update, delete, and list only the jobs that belong to their company.

### Applications

Applications connect one candidate to one job posting. The application record tracks:

- Status
- Recruiter notes
- Resume storage metadata
- Parsed resume text
- Match score
- Match reasons

This gives the recruiter a single place to review both workflow state and resume fit.

## Resume and match scoring workflow

The resume pipeline currently works like this:

1. A candidate applies to a job
2. The candidate uploads a PDF or DOCX resume
3. The backend validates file type and size
4. Apache Tika extracts text from the resume
5. The scoring service compares job text to resume text
6. The application is updated with a score from 0 to 100 and short explanation strings
7. Recruiters can see the score and resume metadata in their application review screens

The scorer is deterministic and rule-based in the current implementation. That keeps results explainable and easy to test.

## Frontend user journeys

The current UI is built around these flows:

- Browse open jobs without logging in
- Open a job and apply as a candidate
- Sign in or register from the UI
- See your own applications and upload a resume
- Sign in as a recruiter, manage your company jobs, and review applications
- Check backend health from the browser

The application header changes based on auth state and role, so the main actions stay visible without exposing routes that do not apply to the current user.

## Development phases implemented

The repository’s current implementation corresponds to the early phases of the larger roadmap:

- Phase 0: project foundation, local infrastructure, frontend and backend skeleton
- Phase 1: auth, JWT, roles, and core domain model
- Phase 2: job postings and applications
- Phase 3: resume upload and ATS scoring

Later roadmap items exist in the planning docs, but they are not the current core implementation. The next larger product areas are analytics, candidate polish, interview workflow, notifications, and optional AI-assisted features.

## Local development setup

Typical local startup order:

1. Start PostgreSQL and Redis with Docker Compose
2. Run the Spring Boot backend from `backend/`
3. Run the Vite frontend from `frontend/`

By default, the frontend talks to the backend through the Vite dev proxy, so local browser requests can use `/api` and `/actuator` without hardcoding a production host.

Important local paths and defaults:

- Resume files are stored under `./data/resumes` by default
- Backend health is exposed through Actuator
- Swagger UI is enabled for API exploration

## Deployment model

The project is designed to deploy as a split full-stack app:

- Frontend: static React build on a frontend host
- Backend: Spring Boot service on an API host
- Database: managed PostgreSQL

That means production needs two configuration values to line up correctly:

- `VITE_API_BASE_URL` on the frontend build
- `CORS_ORIGINS` on the backend

Resume storage is the main deployment caveat. The current implementation uses local filesystem storage, which is fine for local development and demos but needs a writable persistent location for production.

## What is intentionally not in scope yet

The codebase does not yet implement the later roadmap items such as:

- Interview scheduling
- Notifications and email workflows
- Recruiter analytics dashboards
- AI-generated question banks
- Advanced match scoring using embeddings or LLMs

Those belong in the roadmap, not the current shipped baseline.

## Recommended next reading

- `README.md` for quick start and current phase summary
- `DEVELOPMENT_PLAN.md` for the full roadmap
- `PROJECT_PROGRESS.md` for the current completion status
- `DEPLOYMENT.md` for deployment variables and hosting guidance
- `docs/FULL_STACK_DEPLOY_GUIDE_FOR_BEGINNERS.md` for the deployment mental model
