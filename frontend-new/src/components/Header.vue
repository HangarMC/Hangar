<script setup lang="ts">
import type { Announcement as AnnouncementObject } from "hangar-api";
import { onMounted, ref, unref } from 'vue'
import { useInitialState } from "~/composables/useInitialState";
import { useInternalApi } from "~/composables/useApi";

// not sure if they need to be part of the initial state, since we directly render them, would only save a request on page switch at most, but I guess its a good demonstration
/* const announcements = await useInitialState<AnnouncementObject[]>(
    "announcements",
    async () => await useInternalApi<AnnouncementObject[]>("data/announcements", false)
); */ // TODO: This breaks click events

const darkMode = ref(false); // TODO: Make that properly & site-wide

function toggleDarkMode() {
    darkMode.value = !unref(darkMode);
}


const navBarLinks = [
    {link: 'index', label: 'Home'},
    {link: 'staff', label: 'Team'},
];
</script>

<template>
    <template v-if="announcements">
        <Announcement v-for="(announcement, idx) in announcements" :key="idx" :announcement="announcement" />
    </template>
    <header class="bg-white">
        <div class="inner-header flex items-center max-w-1200px mx-auto justify-between">
            <div class="logo-and-nav flex items-center">
                <button class="mr-4">
                    <icon-mdi-menu style="font-size: 1.2em;"/>
                </button>


                <div class="site-logo mr-4 h-60px">
                    <img alt="Hangar Logo" src="/logo.svg" class="h-60px"/>
                </div>
                <nav class="flex gap-3">
                    <router-link
                        v-for='navBarLink in navBarLinks'
                        :key='navBarLink.label'
                        :to="{ name: navBarLink.link }"
                        class="relative after:(absolute content-DEFAULT block w-0 top-30px left-1/10 h-4px rounded-8px)"
                    >
                        {{ navBarLink.label }}
                    </router-link>
                </nav>
            </div>

            <div class="login-buttons flex gap-2">
                <button @click="toggleDarkMode">
                    <icon-mdi-weather-night v-if="darkMode" style="font-size: 1.2em;"></icon-mdi-weather-night>
                    <icon-mdi-white-balance-sunny v-else style="font-size: 1.2em;"></icon-mdi-white-balance-sunny>
                </button>
                <p>login</p>
            </div>
        </div>
    </header>
</template>

<style lang="css" scoped>

nav .router-link-active {
    color: #4080FF;
    font-weight: 700;
}
nav a.router-link-active:after {
    background: linear-gradient(-270deg, #004ee9 0%, #367aff 100%);
    transition: width .2s ease-in;
    width: 80%;
}


nav a:not(.router-link-active):hover:after {
    background: #d3e1f6;
    transition: width .2s ease-in;
    width: 80%;
}


</style>

