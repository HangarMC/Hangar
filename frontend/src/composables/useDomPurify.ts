import type { default as DOMPurify, Config } from "dompurify";

export const config = {
  FORBID_TAGS: ["style", "base", "head", "link", "meta", "title", "body", "form", "input", "dialog", "embed", "button", "frame", "html", "textarea"],
  FORBID_ATTR: ["style"],
  ADD_TAGS: ["iframe"],
  ADD_ATTR: ["allow", "allowfullscreen", "frameborder", "scrolling"],
} as Config;
export function useDomPurify(text?: string) {
  if (!text) return "";
  const dompurify = useNuxtApp().$dompurify as typeof DOMPurify; // TODO cleanup when DOMPurify exports the type in the next release

  // Manually handle iframe to allow YouTube video embeds
  dompurify.addHook("uponSanitizeElement", (node, data) => {
    if (data.tagName === "iframe") {
      const src = (node as HTMLIFrameElement).getAttribute("src") || "";
      if (!src.startsWith("https://www.youtube.com/embed/")) {
        return node.parentNode && node.parentNode.removeChild(node);
      }
    }
  });

  return dompurify.sanitize(text, config) as string;
}

export const aggressiveConfig = { ALLOWED_TAGS: ["#text"] } as Config;
export function stripAllHtml(text?: string) {
  if (!text) return "";
  const dompurify = useNuxtApp().$dompurify as typeof DOMPurify; // TODO cleanup when DOMPurify exports the type in the next release
  return dompurify.sanitize(text, aggressiveConfig) as string;
}
