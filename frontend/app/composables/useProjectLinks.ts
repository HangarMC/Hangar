import type { Link, LinkSection } from "#shared/types/backend";

const SUGGESTED_LINK_NAMES = ["Source", "Support"];

export function suggestedLinkSections(): LinkSection[] {
  return [{ id: 0, type: "top", title: "", links: SUGGESTED_LINK_NAMES.map((name, id) => ({ id, name, url: "" })) }];
}

export function isUnfilledSuggestion(link: Link): boolean {
  return !link.url && SUGGESTED_LINK_NAMES.includes(link.name);
}

export function stripUnfilledSuggestions(sections: LinkSection[]): LinkSection[] {
  return sections
    .map((section) => ({ ...section, links: section.links.filter((link) => !isUnfilledSuggestion(link)) }))
    .filter((section) => section.type !== "top" || section.links.length > 0);
}
