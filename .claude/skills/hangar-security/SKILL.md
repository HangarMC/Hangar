---
name: hangar-security
description: Hangar's authorization model — which annotation guards which endpoint, permission types, and the input/output rules that must hold. Use when adding or reviewing any endpoint, permission check, or user-supplied content path.
---

# Hangar security

## An unannotated endpoint is public

`SecurityConfig` ends in `anyRequest().permitAll()`. **All authorization is method-level**, applied by `MethodSecurityConfig`'s interceptor over the annotations in `security/annotations/`. A controller method with no annotation, on a class with no annotation, is reachable by anonymous users. This is the single most important thing to check on any new endpoint.

Every controller class carries `@Anyone` (anonymous allowed) or `@LoggedIn`. Methods then narrow it.

| Annotation | Guards |
| --- | --- |
| `@Anyone` / `@LoggedIn` | class-level baseline: anonymous vs authenticated |
| `@PermissionRequired(type = …, perms = …, args = "{#project}")` | the actual permission check; `type` is `GLOBAL` (default), `PROJECT` or `ORGANIZATION`, `args` is SpEL naming the subject parameter |
| `@Unlocked` | rejects locked accounts — required on **every** mutating endpoint |
| `@RequireAal(n)` | step-up auth for sensitive actions (uploads, key/password/2FA changes) |
| `@CurrentUser("#name")` | "the user themselves, or staff" |
| `@VisibilityRequired(type = …, args = …)` | hides soft-deleted/needs-approval projects and versions from users who may not see them |
| `@Privileged` | staff-only |
| `@RateLimit(overdraft, refillTokens, refillSeconds)` | abuse control; put it on anything that writes, sends mail, or does expensive work |

## Rules

- **The frontend's `hasPerms()` is UX only.** It hides buttons; it authorizes nothing. Every gated action needs the backend annotation too.
- Never derive ownership from a client-supplied id or name. Resolve the subject through its service and let `@PermissionRequired` evaluate against the resolved entity.
- Validate at the model with jakarta constraints, and remember `@Size`/`@NotBlank` accept `null` — pair with `@NotNull` (see the `hangar-backend` skill). An unvalidated field reaching a service is a 500, and 500s on user input are a DoS surface.
- User-authored markdown and HTML go through the DOMPurify plugin. Never `v-html` a raw API string.
- Do not log tokens, API keys, session cookies, or password material; exceptions reach Sentry with their context attached.
- Audit-worthy mutations get an `actionLogger` entry (`LogAction.*`).
