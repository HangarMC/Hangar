import { defineStore } from "pinia";
import { Ref, ref, unref } from "vue";

export const useThemeStore = defineStore("theme", () => {
  const darkMode: Ref<boolean> = ref(false);

  const mobile: Ref<boolean> = ref(true); //True cause mobile first!!

  const mobileBreakPoint: number = 700;


  function toggleDarkMode() {
    darkMode.value = !unref(darkMode);
  }

  function enableDarkMode() {
    darkMode.value = true;
  }

  function disableDarkMode() {
    darkMode.value = false;
  }

function toggleMobile() {
    mobile.value = !unref(mobile);
}

function enableMobile() {
    mobile.value = true;
}

function disableMobile() {
    mobile.value = false;
}

  return { darkMode, toggleDarkMode, enableDarkMode, disableDarkMode, mobile, toggleMobile, enableMobile, disableMobile, mobileBreakPoint };
});
