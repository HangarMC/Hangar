<script setup lang="ts">
import { useHead } from "@vueuse/head";
import {ref} from 'vue';
import { useSeo } from "~/composables/useSeo";
import { useThemeStore } from '~/store/theme'

const title = "Hangar New Test";
const description = "IDK WTF am doing";

useHead(useSeo(title, description, useRoute(), null));

const theme = useThemeStore()

if (typeof window !== 'undefined') {
    theme.darkMode = (localStorage.theme === 'dark' || (!('theme' in localStorage) && window.matchMedia('(prefers-color-scheme: dark)').matches));


    if (unref(theme.darkMode)){
        document.documentElement.classList.add('dark');
    }else{
        document.documentElement.classList.add('light');
    }
}


theme.$subscribe((mutation, state) => {
    if (typeof window !== 'undefined') {
        if(state.darkMode){
            localStorage.theme = 'dark';
            document.documentElement.classList.add('dark');
            document.documentElement.classList.remove('light');
        }else{
            localStorage.theme = 'light';
            document.documentElement.classList.add('light');
            document.documentElement.classList.remove('dark');
        }
    }


})
</script>

<template>
  <router-view v-slot="{ Component, route }">
    <transition name="slide">
      <component :is="Component" :key="route" />
    </transition>
  </router-view>
</template>
