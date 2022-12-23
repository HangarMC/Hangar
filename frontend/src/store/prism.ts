import { defineStore } from "pinia";
import type { highlightAll, Languages } from "prismjs";
import { ref } from "#imports";
import { prismLog } from "~/lib/composables/useLog";
import "prismjs/themes/prism-tomorrow.min.css";

export const usePrismStore = defineStore("prism", () => {
  const prism = ref<{ highlightAll: typeof highlightAll; languages: Languages } | null>(null);
  const languages = ref<string[]>([]);

  async function loadPrism() {
    if (prism.value) return;
    prismLog("loading prism...");
    prism.value = (await import("prismjs")) as unknown as typeof prism.value;
    prismLog("done");
  }

  async function loadLanguages() {
    const langs = new Set<string>();
    for (const codeBlock of document.querySelectorAll('code[class*="language-"]')) {
      langs.add(codeBlock.className.replace("language-", ""));
    }
    prismLog("Load languages", langs);
    for (const lang of langs) {
      await loadLanguage(lang);
    }
    prismLog("done");
  }

  async function loadLanguage(lang: string) {
    if (languages.value.includes(lang)) return;
    prism.value?.languages?.extend(lang, await import(`../../node_modules/prismjs/components/prism-${lang}.js`));
    languages.value.push(lang);
  }

  async function handlePrism() {
    prismLog("handle prism...");
    await loadPrism();
    await loadLanguages();
    prism.value?.highlightAll();
    prismLog("DONE!");
  }

  return { prism, languages, handlePrism };
});
