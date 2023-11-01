import type { Config, DOMPurifyI } from "dompurify";
import { useNuxtApp } from "#imports";

export const config = {
  FORBID_TAGS: ["style", "base", "head", "link", "meta", "title", "body", "form", "input", "dialog", "embed", "button", "frame", "html", "textarea"],
  FORBID_ATTR: ["style"],
  ADD_TAGS: ["iframe"],
  ADD_ATTR: ["allow", "allowfullscreen", "frameborder", "scrolling"],
} as Config;
export function useDomPurify(text: string) {
  const dompurify = useNuxtApp().$dompurify as DOMPurifyI;

  // Manually handle iframe to allow YouTube video embeds
  dompurify.addHook("uponSanitizeElement", (node: any, data) => {
    if (data.tagName === "iframe") {
      const src = node.getAttribute("src") || "";
      if (!src.startsWith("https://www.youtube.com/embed/")) {
        return node.parentNode && node.parentNode.removeChild(node);
      }
    }
  });

  return dompurify.sanitize(text, config) as string;
}
