# Hangar — repository guide for AI agents

Plugin repository for Paper, Velocity and Waterfall server plugins. Inspired by [Ore](https://github.com/SpongePowered/Ore), rebuilt for PaperMC. MIT.
Production: https://hangar.papermc.io · Staging: https://hangar.papermc.dev

```
Browser → Nuxt SSR (:3333) → Nitro proxy (/api/…) → Spring Boot (:8080) → PostgreSQL
                                                                        → MeiliSearch
                                                                        → S3 / RustFS
```

| Path | Stack |
| --- | --- |
| `backend/` | Java 25, Spring Boot 4.1, JDBI 3 (**not JPA**), Flyway, Maven |
| `frontend/` | Nuxt 4.5 / Vue 3.5, TypeScript, UnoCSS, Pinia, pnpm 11 (Bun in prod) |
| `docker/`, `chart/` | Compose dev services, Helm |
| `e2e/` | CodeceptJS + Playwright on BrowserStack |

Also: Spring Security + JWT/TOTP/WebAuthn, Caffeine cache, Bucket4j rate limiting, JGroups clustering, Sentry, SpringDoc OpenAPI. Frontend adds Vuelidate, vue-i18n (Crowdin), Marked + EasyMDE, PrismJS, Chart.js, Cloudflare Turnstile.

## Rules that apply to every change

- **Authorization is method-level only.** `SecurityConfig` ends in `anyRequest().permitAll()`; an endpoint with no annotation is reachable anonymously. Guard with the annotations in `security/annotations/` — `@Anyone`/`@LoggedIn` on the class, then `@PermissionRequired`, `@Unlocked` on anything mutating, `@RateLimit`, `@RequireAal` when sensitive.
- **`@Size` and `@NotBlank` accept `null`** (Bean Validation spec). Required request-body fields need `@NotNull` as well, or the service NPEs into a 500. Validation `message =` values are i18n keys, not English.
- **Never edit an applied `V*` migration** — Flyway checksums it and the app won't start. Add a new one. `R__*` are repeatable and must be `CREATE OR REPLACE`.
- **Frontend: use the existing component** (`Button`, `Card`, `Modal`, `Alert`, `Chip`, `Tabs`, `SegmentedControl`, `ui/Input*`). A raw `<button>` or hand-rolled panel is the usual consistency defect.
- **No hex colors** — the accent is user-selectable across 18 themes via `--primary-*`; use the UnoCSS shortcuts. Check light *and* dark.
- **No ES2023 array methods** (`toSorted`, `toReversed`, `Object.groupBy`). esbuild polyfills syntax, not methods — they hard-crash older browsers.
- Never hand-edit generated files: `frontend/shared/types/backend/` (from the Swagger spec), `app/i18n/locales/processed/`, non-English locales.
- 4 spaces (2 for YAML), 160 columns. Java: `final` params/locals, braces always, explicit `this.`. Comments only where the *why* isn't obvious.

## Backend — `io.papermc.hangar`

```
components/    feature modules (auth, discovery, index, jobs, stats, webhook, scheduler, …)
config/        Spring + @ConfigurationProperties records (backed by application.yml)
controller/    api/v1 (public, = the OpenAPI contract) · internal (frontend only)
db/dao/        JDBI repositories; long SQL under resources/io/papermc/hangar/db/dao/
db/mappers/    JDBI row mappers
model/         db (entities) · api (DTOs) · internal · loggable
service/       api · internal (core business logic)
security/      principals, tokens, authorization annotations
```

Controller → service → DAO. Controllers hold no business logic, DAOs no authorization. Most services extend `HangarComponent` for `this.config`, the principal, permissions and `actionLogger`. Caching is `@Cacheable`/`@CacheEvict` with `CacheConfig` constants — every write that invalidates a cached read must evict explicitly.

Migrations: `backend/src/main/resources/db/migration/` (currently through V1.24.x). Permissions are `bit(64)` masks; adding a bit means updating roles seeded from `Permission.All` in a migration, or `PopulationService` fails on startup.

Tests: JUnit 5, Mockito, TestContainers.

## Frontend — `frontend/app`

```
components/   design/ (Button, Card, Modal, Alert, Chip, Tabs, Steps, Table, …)
              ui/ (Input*, all wrapping InputWrapper) · form/ · layout/ · modals/ · projects/
composables/  useApi / useInternalApi, useValidation, useMarked, …
middleware/   auth.global · data.global (loads user/project/version/page) · settings.global
store/        auth · backendData · notification · prism · settings
pages/        file-based routes · plugins/ · layouts/ · i18n/locales/
```

Components and composables are **auto-imported** — no import statements. `useApi<T>()` hits the public v1 API, `useInternalApi<T>()` the internal one.

SSR-first: register lifecycle hooks before any top-level `await`, guard `window`/`document`, no hydration warnings. Every user-facing string goes through `t()`, with new keys added to `app/i18n/locales/en.json` only.

## Commands

```bash
cd docker && docker-compose -f dev.yml up -d   # Postgres :5432, MeiliSearch :7700, Mailslurper :4436, RustFS :9000
cd backend  && mvn spring-boot:run             # :8080          (mvn test for tests)
cd frontend && pnpm dev                        # :3333, hot reload -- pnpm devStaging needs no local backend

cd frontend && pnpm lint:eslint && pnpm lint:prettier && pnpm lint:typecheck   # before finishing
```

IntelliJ run configurations for all of the above live in `.run/`.

CI (`.github/workflows/cicd.yml`) on push to `master`/`staging`: detect changed files → Maven build → pnpm lint + build → Docker push to GHCR. `e2e.yml` runs BrowserStack tests after a staging deploy.

Claude Code users: the `hangar-backend`, `hangar-security`, `hangar-frontend` and `hangar-changelog` skills in `.claude/` carry these conventions in more depth.
