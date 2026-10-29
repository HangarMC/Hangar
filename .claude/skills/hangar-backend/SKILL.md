---
name: hangar-backend
description: Conventions for Hangar's Spring Boot backend — controller/service/DAO layering, validation annotations, Flyway migrations, config, caching, and Java style. Use when writing or reviewing anything under backend/.
---

# Hangar backend

Spring Boot on Java 25, **JDBI 3 — not JPA**. There are no `@Entity` classes or a Hibernate session; every query is SQL in a DAO.

## Layering

`controller/` → `service/` → `db/dao/`. Controllers do no business logic; DAOs do no authorization.

- `controller/api/v1/` — public, versioned API. Its models are the OpenAPI contract; changing one changes `frontend/shared/types/backend` (auto-generated — never hand-edit).
- `controller/internal/` — endpoints for the Nuxt frontend only.
- Most services extend `HangarComponent`, which supplies `this.config`, `getHangarPrincipal()`, `getGlobalPermissions()`, `this.actionLogger`.
- DAOs are `@JdbiRepository` interfaces using `@SqlQuery` / `@SqlUpdate` / `@RegisterConstructorMapper`. Long SQL lives in `resources/io/papermc/hangar/db/dao/`.

## Validation

Request bodies are validated by `@Valid` on the controller parameter. Per the Bean Validation spec **`@Size`, `@NotBlank` and friends all treat `null` as valid.** A field carrying only `@Size(min = 1)` is not required, and dereferencing it in the service is an NPE (a 500) instead of a 400.

- Required field → `@NotNull` **in addition to** `@Size`/`@NotBlank`.
- Genuinely optional field → normalize it in the `@JsonCreator` constructor if needed.
- `message = ` values are **i18n keys resolved by the frontend**, not English text. Reuse an existing key from `frontend/app/i18n/locales/en.json` or add one there.
- Config-driven limits use the custom `@Validate(SpEL = "@validate.regex(#root, @'hangar-…HangarConfig'.projects.versionNameRegex)", message = "…")`.

## Migrations (Flyway)

`backend/src/main/resources/db/migration/`

- **Never edit a `V*` migration that has been applied** — Flyway stores its checksum and the app refuses to start. Add a new `V<major>.<minor>.<patch>__desc.sql`.
- `R__*.sql` are repeatable (views, functions, triggers); they re-run whenever their checksum changes and must be `CREATE OR REPLACE`.
- Write DDL idempotently (`ADD COLUMN IF NOT EXISTS`, backfill, then `SET NOT NULL`) — dev databases are frequently in a half-migrated state.
- Permissions are `bit(64)` masks; roles seeded from `Permission.All` must be updated when a bit is added, or `PopulationService` re-seeds and hits `roles_pkey`.

## Config

`@ConfigurationProperties` records under `config/hangar/`, backed by `application.yml`. Nested records need `@NestedConfigurationProperty`. Note that `spring.jackson2.*` feeds `Jackson2ObjectMapperBuilder` while `spring.jackson.*` feeds Boot's Jackson 3 mapper — they are different objects, so renaming a key silently changes date serialization across the whole API.

## Caching

`@Cacheable` / `@CacheEvict` with the constants in `CacheConfig`. Every write path that invalidates a cached read must evict it explicitly.

## Style

4 spaces, 160 columns, `final` on parameters and locals, braces on every block, explicit `this.` for fields. Errors are `HangarApiException` with an i18n key or an `HttpStatus`.

### Comments

Unnecessary comments are a defect, not a style preference. The exception is a non-obvious *why* a reader would otherwise get wrong or would have hard time parsing (planner quirk, spec constraint, long runs of code): Do not comment just to describe code, SQL, or a change you just made.

## Building

```bash
cd backend && mvn compile
```

Full build if not already running:
```bash
cd docker && docker-compose -f dev.yml up -d  # Start PostgreSQL, MeiliSearch, etc.
cd ../backend && mvn spring-boot:run          # Start backend on :8080
```
