import { Config, DOMPurifyI } from "dompurify";
import { useNuxtApp } from "#imports";

export const config = {
  FORBID_TAGS: ["style", "base", "head", "link", "meta", "title", "body", "form", "input", "dialog", "embed", "button", "frame", "iframe", "html", "textarea"],
  FORBID_ATTR: ["style"],
} as Config;
export function useDomPurify(text: string) {
  console.log("dum", text);
  return (useNuxtApp().$dompurify as DOMPurifyI).sanitize(text, config) as string;
}
