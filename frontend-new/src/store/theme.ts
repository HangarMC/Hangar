import { defineStore } from 'pinia'
import {ref, unref} from 'vue';

export const useThemeStore = defineStore('theme', () => {
    const darkMode = ref(false);


    function toggleDarkMode() {
        darkMode.value = !unref(darkMode);
    }

    return { darkMode, toggleDarkMode }
})
