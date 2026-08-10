import { marked } from "marked";
import { changelog } from "#shared/changelog";

function escapeXml(text: string): string {
  return text.replaceAll(/[<>&'"]/g, (c) => `&${{ "<": "lt", ">": "gt", "&": "amp", "'": "apos", '"': "quot" }[c]};`);
}

// entries carry a day, the feed needs an instant
function instant(date: string): string {
  return `${date}T23:59:59Z`;
}

export default defineEventHandler(async (event) => {
  const host = useRuntimeConfig().public.host || "https://hangar.papermc.io";
  const self = `${host}/changelog.atom`;
  const updated = changelog[0] ? instant(changelog[0].date) : new Date(0).toISOString();

  const entries = await Promise.all(
    changelog.map(async (entry) => {
      const html = await marked.parse(entry.body);
      return `  <entry>
    <id>tag:hangar.papermc.io,2026:changelog/${escapeXml(entry.id)}</id>
    <title>${escapeXml(entry.title)}</title>
    <link href="${escapeXml(`${host}/changelog#${entry.id}`)}" />
    <updated>${instant(entry.date)}</updated>
    <category term="${escapeXml(entry.kind)}" />
    <content type="html">${escapeXml(html)}</content>
  </entry>`;
    })
  );

  setResponseHeader(event, "content-type", "application/atom+xml; charset=utf-8");
  setResponseHeader(event, "cache-control", "public, max-age=3600");
  return `<?xml version="1.0" encoding="utf-8"?>
<feed xmlns="http://www.w3.org/2005/Atom">
  <id>${escapeXml(self)}</id>
  <title>Hangar changelog</title>
  <subtitle>New features, changes and API notices for Hangar.</subtitle>
  <link rel="self" href="${escapeXml(self)}" />
  <link href="${escapeXml(`${host}/changelog`)}" />
  <updated>${updated}</updated>
${entries.join("\n")}
</feed>
`;
});
