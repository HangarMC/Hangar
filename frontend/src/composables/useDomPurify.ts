import { DOMPurifyI } from "dompurify";
import { useNuxtApp } from "#imports";

export function useDomPurify() {
  return useNuxtApp().$dompurify as DOMPurifyI;
}
