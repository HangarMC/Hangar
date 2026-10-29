---
name: hangar-changelog
description: How to write entries for Hangar's user-facing changelog in frontend/shared/changelog.ts. Use when adding a changelog entry or turning commits and closed issues into one.
---

# Writing changelog entries

Entries live in `frontend/shared/changelog.ts`, newest first. `id` is permanent. English-only.

## What earns an entry

Only things a **user of the site** would act on or notice.

- A feature they can now use that is actually interesting enough and benefits from being highlighted on a changelog page.
- A visible change to something they already used — moved, renamed, removed, or behaving differently.
- Something they must do: a breaking API change, a deadline, a setting they need to revisit.

## What does not

- Simple bugfixes or cosmetic visual changes.
- Changes that do not concern normal users of the site or API (e.g. internal refactors).

When in doubt, leave it out. A short changelog gets read.

## How to write one

- **Title**: what changed, in the user's words. "API keys can be scoped to individual projects", not "Implement scoped API keys".
- **Body**: one or two sentences. What it is, and why you'd use it.
- Name things as the UI names them, and point at the place: **Settings → Account**.
- Group related commits into one entry.
- `kind` is `feature`, `improvement`, `fix`, or `api`. Use `api` for anything an API consumer must react to — that entry is also their notice, since the feed is how they hear about it.
