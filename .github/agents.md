# Hangar — Repository Guide for AI Agents

## Project Overview

Hangar is a **plugin repository platform** for Paper, Velocity, and Waterfall Minecraft server plugins. It is inspired by [Ore](https://github.com/SpongePowered/Ore), rebuilt from scratch with modern technologies, for PaperMC's needs.

- **Production**: https://hangar.papermc.io/
- **Staging**: https://hangar.papermc.dev/
- **License**: MIT

## Architecture

**Monorepo** with three main components:

| Component | Path | Technology | Purpose |
|-----------|------|-----------|---------|
| Backend | `backend/` | Java 21+ / Spring Boot 3.5 | REST API, business logic, database |
| Frontend | `frontend/` | TypeScript / Vue 3 / Nuxt 4 | SSR web application |
| Infrastructure | `docker/`, `chart/` | Docker Compose, Helm | Local dev and Kubernetes deployment |
| E2E Tests | `e2e/` | CodeceptJS / Playwright | Cross-browser end-to-end tests |

### Data Flow

```
Browser → Nuxt (SSR, port 3333) → Nitro proxy (/api/...) → Spring Boot (port 8080) → PostgreSQL
                                                                                    → MeiliSearch (search)
                                                                                    → RustFS / S3 (file storage)
```

---

## Backend (`backend/`)

### Stack

| Concern | Technology |
|---------|-----------|
| Framework | Spring Boot 3.5.7 |
| Language | Java 21+ (virtual threads enabled) |
| Build | Maven (`mvn`) |
| Database | PostgreSQL 15 via JDBI 3 (not JPA) |
| Migrations | Flyway (versioned + repeatable) |
| Search | MeiliSearch |
| Auth | Spring Security + JWT + TOTP + WebAuthn |
| Caching | Caffeine |
| Rate Limiting | Bucket4j |
| Clustering | JGroups (Kubernetes discovery) |
| Storage | Local filesystem or S3-compatible (RustFS in dev) |
| Error Tracking | Sentry |
| API Docs | SpringDoc OpenAPI |

### Package Structure (`io.papermc.hangar`)

```
├── HangarApplication.java          # Entry point
├── HangarComponent.java            # Base class for components (auth/config helpers)
├── components/                     # Feature modules (auth, jobs, webhooks, scheduler, etc.)
├── config/                         # Spring & Hangar configuration classes
├── controller/
│   ├── api/v1/                     # Public REST API (versioned)
│   └── internal/                   # Internal API used by frontend
├── db/
│   ├── dao/                        # JDBI Data Access Objects (SQL queries)
│   └── mappers/                    # JDBI row mappers (~68 mappers)
├── model/
│   ├── db/                         # Database entities
│   ├── api/                        # API request/response DTOs
│   ├── internal/                   # Internal models
│   └── loggable/                   # Audit logging models
├── service/
│   ├── api/                        # Services for public API
│   └── internal/                   # Core business logic (projects, versions, users, orgs, admin)
├── security/                       # Auth tokens, principals, permission annotations
├── exceptions/                     # Custom exception handling
└── util/                           # Utility classes
```

### Key Files

- **Config**: `backend/src/main/resources/application.yml`
- **Migrations**: `backend/src/main/resources/db/migration/` (V1.0.0 through V1.21.0+)
- **SQL Templates**: `backend/src/main/resources/io/papermc/hangar/db/dao/internal/`
- **Mail Templates**: `backend/src/main/resources/mail-templates/`

### Building & Testing

```bash
cd backend
mvn install                          # Build
mvn spring-boot:run                  # Run locally (needs Docker services)
mvn test                             # Run tests
```

Tests use **JUnit 5**, **Mockito**, and **TestContainers** (PostgreSQL). There are ~18 test classes covering services, controllers, components, and utilities.

### Important Patterns

- **JDBI, not JPA**: Database access uses JDBI with SQL templates and custom row mappers. There are no `@Entity` annotations or Hibernate.
- **Layered architecture**: Controller → Service → DAO
- **HangarComponent base class**: Many components extend `HangarComponent` which provides access to authentication, configuration, and action logging.
- **Custom security annotations**: Permission checks are done via annotations in `security/annotations/`.

---

## Frontend (`frontend/`)

### Stack

| Concern | Technology |
|---------|-----------|
| Framework | Nuxt 4.1 (Vue 3.5) |
| Language | TypeScript 5.9 |
| Package Manager | pnpm 10 |
| Runtime (prod) | Bun |
| Styling | UnoCSS (Tailwind-compatible utility classes) |
| State | Pinia (5 stores: auth, backendData, notification, prism, settings) |
| HTTP | Axios (proxied through Nitro server routes) |
| Forms | Vuelidate |
| i18n | vue-i18n / @nuxtjs/i18n (20+ languages, Crowdin managed) |
| Markdown | Marked + EasyMDE editor |
| Syntax Highlight | PrismJS |
| Charts | Chart.js + vue-chartjs |
| Error Tracking | Sentry |
| CAPTCHA | Cloudflare Turnstile |

### Directory Structure

```
frontend/
├── app/
│   ├── components/          # ~150+ Vue components (auto-imported)
│   │   ├── design/          # Design system (tabs, steps, sortable tables)
│   │   ├── form/            # Form components
│   │   ├── layout/          # Header, footer, navigation
│   │   ├── modals/          # Modals and dialogs
│   │   ├── projects/        # Project-specific components
│   │   └── ui/              # Reusable inputs (InputText, InputSelect, etc.)
│   ├── composables/         # 26 Vue composables (useApi, useAuth, useMarked, etc.)
│   ├── i18n/locales/        # Translation files
│   ├── layouts/             # Nuxt layouts (default.vue)
│   ├── middleware/           # Global middleware (auth, data, settings)
│   ├── pages/               # 59 file-based routes
│   ├── plugins/             # Nuxt plugins (axios, sentry, dompurify, nprogress)
│   ├── store/               # Pinia stores
│   └── types/               # TypeScript types (including auto-generated backend types)
├── modules/                 # Custom Nuxt modules
├── server/routes/           # Nitro server routes (API proxy to backend)
├── shared/types/backend/    # Auto-generated types from backend Swagger
├── .config/                 # Environment files (local, staging, prod)
├── nuxt.config.ts
├── uno.config.ts
└── package.json
```

### Key Routes

| Path | Purpose |
|------|---------|
| `/` | Home / project list |
| `/[user]` | User profile |
| `/[user]/[project]` | Project details |
| `/auth/login`, `/auth/signup` | Authentication |
| `/auth/settings` | Account settings |
| `/admin/*` | Admin panel |
| `/api-docs` | API documentation |
| `/new` | New project creation |

### Building & Linting

```bash
cd frontend
pnpm install                         # Install dependencies
pnpm dev                             # Dev server at localhost:3333 (needs backend)
pnpm devStaging                      # Dev server using staging backend (no local backend needed)
pnpm build                           # Production build
pnpm buildStaging                    # Staging build

# Linting
pnpm lint:eslint                     # ESLint (with auto-fix)
pnpm lint:prettier                   # Prettier formatting
pnpm lint:oxlint                     # OxLint (fast JS linter)
pnpm lint:typecheck                  # TypeScript type checking
```

### Important Patterns

- **Auto-imported components**: Components are auto-imported via `unplugin-vue-components`. No manual import statements needed.
- **API composables**: `useApi<T>()` for public v1 API, `useInternalApi<T>()` for internal endpoints.
- **SSR-first**: The app uses server-side rendering. Always test that changes work with SSR (no hydration warnings).
- **Dark mode**: UnoCSS dark mode is built in. Test both light and dark themes.
- **API types are auto-generated**: Types in `shared/types/backend/` come from the backend's Swagger spec. Don't edit them manually.

---

## Infrastructure

### Docker Dev Environment (`docker/dev.yml`)

```bash
cd docker && docker-compose -f dev.yml up -d
```

| Service | Port | Credentials |
|---------|------|-------------|
| PostgreSQL | 5432 | hangar / hangar / hangar (user/pass/db) |
| MeiliSearch | 7700 | — |
| Mailslurper | 4436 (UI), 1025 (SMTP) | — |
| RustFS (S3) | 9000 (API), 9001 (Console) | hangar_root / hangar_pass |

### Kubernetes Helm Chart (`chart/`)

Defines deployments, services, secrets, ingress, HPAs, and Prometheus ServiceMonitor for both backend and frontend. Uses GHCR container images.

---

## E2E Tests (`e2e/`)

- **Framework**: CodeceptJS + Playwright/WebDriverIO
- **Platform**: BrowserStack (Safari on macOS, Edge on Windows)
- **Test pages**: Index, NewProject, NewOrganization, Staff, Authors, ApiDocs, Linkout, BBCode, NewVersion

```bash
cd e2e
pnpm install
pnpm run browserstack-multiple       # Full cross-browser test suite
```

---

## CI/CD (`.github/workflows/`)

| Workflow | Trigger | Purpose |
|----------|---------|---------|
| `cicd.yml` | Push to `master`/`staging`, manual | Build, lint, Docker push to GHCR |
| `e2e.yml` | After staging deploy, manual | E2E tests on BrowserStack |
| `issues_to_project.yml` | Issue opened | Add issues to project board |

**CI build steps**: Detect changed files → Maven build (backend) → pnpm lint + build (frontend) → Docker build & push to `ghcr.io`.

---

## Development Quick Start

**Prerequisites**: Java 21+, Maven, Node 22+, pnpm, Docker

### Option A — Frontend only (no backend needed)

```bash
cd frontend && pnpm install && pnpm devStaging
# Opens at http://localhost:3333, proxying to staging backend
```

### Option B — Full stack

```bash
cd docker && docker-compose -f dev.yml up -d     # Start PostgreSQL, MeiliSearch, etc.
cd ../backend && mvn spring-boot:run              # Start backend on :8080
cd ../frontend && pnpm install && pnpm dev        # Start frontend on :3333
```

### IntelliJ Run Configurations (`.run/`)

| Config | Purpose |
|--------|---------|
| `docker` | Start Docker Compose dev services |
| `backend` | Spring Boot (IntelliJ Ultimate) |
| `backend-community` | Spring Boot (IntelliJ Community) |
| `frontend` | Full-stack frontend (needs backend) |
| `frontend without backend` | Frontend using staging backend |
| `lint:eslint` | Run ESLint |
| `lint:prettier` | Run Prettier |

---

## Code Style & Conventions

- **Indentation**: 4 spaces everywhere, 2 spaces for YAML
- **Line length**: 160 characters max
- **Java**: Follow Oracle conventions; always use braces for single-line blocks; use `final` for locals and parameters
- **Frontend**: Use UnoCSS utility classes (avoid raw CSS); use composables for shared logic; colors must come from the theme palette (no hex values)
- **Dark mode**: Always test both light and dark themes
- **SSR**: Ensure no hydration warnings

---

## Key External Services

| Service | Dev | Production |
|---------|-----|-----------|
| Database | PostgreSQL (Docker) | PostgreSQL |
| Search | MeiliSearch (Docker) | MeiliSearch |
| Storage | RustFS (Docker) | AWS S3 / B2 |
| Email | Mailslurper (Docker) | Mailgun |
| Auth | Local JWT | JWT + OAuth (GitHub, Microsoft, Google) |
| Monitoring | — | Sentry, Prometheus |
| CAPTCHA | Disabled | Cloudflare Turnstile |
| CDN | — | Cloudflare |
| Translations | — | Crowdin |
