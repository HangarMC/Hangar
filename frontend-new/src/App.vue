<script setup lang="ts">
import {useHead} from "@vueuse/head";
import {onMounted} from 'vue'
import {useSeo} from "~/composables/useSeo";
import {useThemeStore} from '~/store/theme'


const title = "Hangar New Test";
const description = "IDK WTF am doing";

useHead(useSeo(title, description, useRoute(), null));

const theme = useThemeStore()

onMounted(() => {
    if (typeof window !== 'undefined') {
        if (localStorage.theme === 'dark' || (!('theme' in localStorage) && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
            theme.enableDarkMode();
        } else {
            theme.disableDarkMode();
        }


        if (theme.darkMode) {
            document.documentElement.classList.add('dark');
            document.documentElement.classList.remove('light');
        } else {
            document.documentElement.classList.add('light');
            document.documentElement.classList.remove('dark');
        }
    }

    theme.$subscribe((mutation, state) => {
        if (typeof window !== 'undefined') {
            if (state.darkMode) {
                localStorage.theme = 'dark';
                document.documentElement.classList.add('dark');
                document.documentElement.classList.remove('light');
            } else {
                localStorage.theme = 'light';
                document.documentElement.classList.add('light');
                document.documentElement.classList.remove('dark');
            }
        }
    })

})


</script>

<template>
    <router-view v-slot="{ Component, route }">
        <transition name="slide">
            <component :is="Component" :key="route"/>
        </transition>
    </router-view>
</template>
