---
name: hangar-frontend
description: Hangar's Nuxt/Vue design system and frontend conventions — which component to reach for, UnoCSS tokens, theming, SSR and i18n rules, browser support. Use when writing or reviewing anything under frontend/.
---

# Hangar frontend

Nuxt 4 / Vue 3.5 / TypeScript / UnoCSS / Pinia, SSR-first. Components and composables are **auto-imported** — no import statements for anything in `app/components/` or `app/composables/`.

Dev server, if not already running: `pnpm dev` (needs a local backend) or `pnpm devStaging` (proxies staging) → http://localhost:3333, hot reloads.

## Use the existing component

Reaching for a raw `<div>`/`<button>` where one of these exists is the most common consistency defect.

| Need | Use |
| --- | --- |
| Any button or link-styled-as-button | `Button` (renders `NuxtLink`/`a`/`button` from `to`/`href`) |
| Text link | `Link` |
| Panel / section container | `Card` (`accent`, `alternateBackground`, `flat`, `padding`; `header` + `footer` slots) |
| Page wrapper, page heading | `Container`, `PageTitle` |
| Dialog | `Modal` (native `<dialog>`; `activator` and `footer` slots) |
| Banner / callout | `Alert` (`success` / `info` / `warning` / `danger` / `neutral`) |
| Status pill, count badge | `Chip` (`neutral`/`amber`/`green`/`red`/`primary`); `Tag` for role & channel colors |
| Tab bar | `Tabs` (`vertical`, `router`, `compact`, `divided`) |
| Segmented pill row | `SegmentedControl` — not raw `<button>`s |
| Labelled form row | `FormSection`; `ProjectSettingsSection` inside project settings |
| Label/value line, metric tile | `InfoRow`, `StatTile` |
| Wizard | `Steps` · Table → `Table` · Paging → `Pagination` / `PaginationButtons` |
| Any input | `ui/Input*` — all wrap `InputWrapper` (floating label, counter, `ErrorTooltip`) |
| Async search-and-pick | `SearchSelect` (debounce, stale-response guard, keyboard nav) |
| Loading | `Skeleton` for content shape, `Spinner` for actions, `Delayed` for Suspense fallbacks |

Validation is vuelidate via `useValidation` and the rule helpers (`required()`, `maxLength()(n)`, `pattern()`, `validUrl()`).

### Button API

`variant` (`solid` | `outline` | `ghost`) × `tone` (`primary` | `neutral` | `danger`) × `size` (`sm` 32px | `md` 36px default | `lg` 44px, aligned to input height). `iconOnly` requires an `aria-label`. The gap between icon and label is on the button — icons take no `mr-*`/`ml-*`.

- Create/Save → `solid`/`primary`, `md`.
- Destructive **trigger** (opens a confirm) → `ghost`/`danger`; the **confirm** inside the modal → `solid`/`danger`. Never `danger` for Back or Cancel.
- Modal footer is the standard right-aligned `[Cancel ghost/neutral] [Confirm solid]` row via the `footer` slot — no ad-hoc margins.
- `lg` is for genuine hero CTAs only.

## Styling

UnoCSS utilities, not raw CSS. Use the shortcuts rather than re-deriving the pairs:

`background-body` · `background-default` · `background-card` · `shadow-default` · `color-primary` · `border-top-primary` · `accent-fill` · `text-gray` · `text-gray-secondary`

- **No hex colors.** The accent is user-selectable across 18 themes and resolves through `--primary-*`; hardcoding defeats it. Surface tokens live in `app/assets/css/color.css` (`--input-surface`, `--surface-sunken`, `--placeholder`).
- Classes composed at runtime must be added to `safelist` in `uno.config.ts` or they get tree-shaken out of the build.
- **Verify both themes.** Light mode is where low-contrast text and invisible gradients show up.
- Respect `prefers-reduced-motion` for anything animated.

## SSR

Renders on the server first. Register lifecycle hooks *before* any top-level `await` in `<script setup>`, guard `window`/`document` access, and check for hydration warnings. Global middleware re-runs on the new navigation after a redirect, so the response may already be sent — guard `setResponseHeader` with `headersSent`.

## Data

`useApi<T>(url, method?, data?)` for the public v1 API, `useInternalApi<T>(…)` for internal endpoints. Route-level data (user/project/version/page) is already loaded by `middleware/data.global.ts`.

`shared/types/backend/` is generated from the backend's Swagger spec — never edit by hand.

## i18n

Every user-facing string goes through `t("…")`. Add keys to `app/i18n/locales/en.json` only; other locales are Crowdin-managed and `locales/processed/` is generated.

## Before finishing

```bash
pnpm lint:eslint && pnpm lint:prettier && pnpm lint:typecheck
```

No unnecessary or overly verbose code comments.
