import type { highlightAll, Languages } from "prismjs";

export const usePrismStore = defineStore("prism", () => {
  const prism = ref<{ highlightAll: typeof highlightAll; languages: Languages } | undefined>(undefined);
  const languages = ref<string[]>([]);

  async function loadPrism() {
    if (prism.value) return;
    prismLog("loading prism...");
    prism.value = (await import("prismjs")) as unknown as typeof prism.value;
    prismLog("done");
  }

  async function loadLanguages() {
    const langs = new Set<string>();
    for (const codeBlock of document.querySelectorAll('code[class*="language-"]') as any as Element[]) {
      langs.add(codeBlock.className.replace("language-", ""));
    }
    prismLog("Load languages", langs);
    for (const lang of langs) {
      try {
        await loadLanguage(lang);
      } catch (err: any) {
        prismLog("failed to load lang %s", lang, err);
      }
    }
    prismLog("done");
  }

  async function loadLanguage(lang: string) {
    const normalLang = lang.trim().toLowerCase();
    if (languages.value.includes(normalLang)) return;
    prism.value?.languages?.extend(normalLang, await import(/* @vite-ignore */ `/prism/prism-${normalLang}.min.js`));
    languages.value.push(normalLang);
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
