/*
Hangar changelogs, newest first. `id` is the anchor on /changelog and the entry's guid in the Atom feed.
*/

export type ChangelogKind = "feature" | "improvement" | "fix" | "api";

export interface ChangelogEntry {
  id: string;
  date: string;
  title: string;
  kind: ChangelogKind;
  body: string;
}

export const changelog: ChangelogEntry[] = [
  {
    id: "granular-permissions",
    date: "2026-08-10",
    title: "Granular project and organization permissions",
    kind: "feature",
    body: `Members of a project or organization are no longer limited to a handful of pre-defined roles. You can now grant
individual permissions per member, and give a role your own display name.`,
  },
  {
    id: "scoped-api-keys",
    date: "2026-08-10",
    title: "API keys can be scoped to individual projects",
    kind: "api",
    body: `A new API key can be restricted to specific projects instead of everything your account can reach.`,
  },
  {
    id: "daily-discovery",
    date: "2026-08-10",
    title: "Daily discovery on the homepage",
    kind: "feature",
    body: `The homepage now contains a strip of projects that changes once a day. Picks are spread
across download tiers rather than ranked by popularity, so smaller projects appear next to more popular ones.`,
  },
];

export function unseenChangelog(since?: string | null): ChangelogEntry[] {
  if (!since) return changelog;
  const seenAt = new Date(since).getTime();
  // start of day, not end: an entry dated today would otherwise stay unseen until the day is over
  return changelog.filter((entry) => new Date(entry.date + "T00:00:00Z").getTime() > seenAt);
}
