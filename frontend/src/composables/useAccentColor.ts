import { useHead } from "@unhead/vue";
import { useLocalStorage } from "@vueuse/core";
import { computed, ref } from "vue";

export function useAccentColor() {
  if (process.server) {
    return ref<string>("blue");
  }

  const accentColor = useLocalStorage("accent-color", "blue");

  const themeClass = computed(() => {
    return "theme-" + accentColor.value;
  });

  useHead({
    htmlAttrs: { class: themeClass },
  });

  return accentColor;
}
