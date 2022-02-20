import { defineStore } from "pinia";
import { Ref, ref, unref } from "vue";

export const useThemeStore = defineStore("theme", () => {
  const darkMode: Ref<boolean> = ref(false);

  function toggleDarkMode() {
    darkMode.value = !unref(darkMode);
  }

  function enableDarkMode() {
    darkMode.value = true;
  }

  function disableDarkMode() {
    darkMode.value = false;
  }

  return { darkMode, toggleDarkMode, enableDarkMode, disableDarkMode };
});
