import { defineStore } from 'pinia'
import {Ref, ref, unref} from 'vue';

export const useThemeStore = defineStore('theme', () => {
    const darkMode: Ref<boolean> = ref(false);


    function toggleDarkMode() {
        darkMode.value = !unref(darkMode);
    }

    return { darkMode, toggleDarkMode }
})
